/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.skipper.gilligan.service;

import java.util.Map;
import java.util.Properties;

import com.samskivert.mustache.Mustache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppInstanceStatus;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.skipper.client.YamlUtils;
import org.springframework.cloud.skipper.client.domain.Deployment;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.gilligan.util.YmlUtils;
import org.springframework.cloud.skipper.rpc.ReleaseStatusResponse;
import org.springframework.cloud.skipper.rpc.domain.*;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Mark Pollack
 */
@Service
public class ReleaseService {

	private final AppDeployer appDeployer;

	private final DelegatingResourceLoader delegatingResourceLoader;

	private final ReleaseRepository releaseRepository;

	@Autowired
	public ReleaseService(ReleaseRepository releaseRepository, AppDeployer appDeployer,
			DelegatingResourceLoader delegatingResourceLoader) {
		this.releaseRepository = releaseRepository;
		this.appDeployer = appDeployer;
		this.delegatingResourceLoader = delegatingResourceLoader;
	}

	/**
	 * Will merge the properties, derived from YAML formst, contained in
	 * commandLineConfigValues and templateConfigValue, giving preference to
	 * commandLineConfigValues. Assumes that the YAML is stored as "raw" data in the
	 * Config object. If the "raw" data is empty or null, an empty property object is
	 * returned.
	 *
	 * @param commandLineConfigValues YAML data passed at the application runtime
	 * @param templateConfigValue YAML data defined in the template.yaml file
	 * @return A Properties object that is the merger of both Config objects,
	 * commandLineConfig values override values in templateConfig.
	 */
	public Properties mergeConfigValues(Config commandLineConfigValues, Config templateConfigValue) {
		Assert.notNull(commandLineConfigValues, "Config object for commandLine Config Values can't be null");
		Assert.notNull(templateConfigValue, "Config object for template Config Values can't be null");

		Properties commandLineOverrideProperties = YamlUtils.getProperties(commandLineConfigValues.getRaw());
		Properties templateVariables = YamlUtils.getProperties(templateConfigValue.getRaw());

		Properties model = new Properties();
		model.putAll(templateVariables);
		model.putAll(commandLineOverrideProperties);
		return model;
	}

	/**
	 * Iterate overall the template files, replacing placeholders with model values. One
	 * string is returned that contain all the YAML of multiple files using YAML file
	 * delimiter.
	 * @param templates YAML with placeholders to replace
	 * @param model The placeholder values.
	 * @return A YAML string containing all the templates with replaced values.
	 */
	public String createManifest(Template[] templates, Properties model) {
		// Aggregate all valid manifests into one big doc.
		StringBuilder sb = new StringBuilder();
		for (Template template : templates) {
			String templateAsString = new String(template.getData());
			com.samskivert.mustache.Template mustacheTemplate = Mustache.compiler().compile(templateAsString);
			sb.append("\n---\n# Source: " + template.getName() + "\n");
			sb.append(mustacheTemplate.execute(model));
		}
		return sb.toString();
	}

	public void deploy(Release release) {
		// Deploy the application
		Deployment appDeployment = YmlUtils.unmarshallDeployment(release.getManifest());
		deploy(appDeployment, release);
		// Store updated state in in DB
		releaseRepository.save(release);
	}

	private void deploy(Deployment appDeployment, Release release) {
		String deploymentId = appDeployer.deploy(
				createAppDeploymentRequest(appDeployment, release.getName(), String.valueOf(release.getVersion())));
		release.setDeploymentId(deploymentId);

		// Store in DB
		Status status = new Status();
		status.setStatusCode(StatusCode.DEPLOYED);
		release.getInfo().setStatus(status);
		release.getInfo().setDescription("Install complete");
	}

	private AppDeploymentRequest createAppDeploymentRequest(Deployment deployment, String releaseName, String version) {
		AppDefinition appDefinition = new AppDefinition(deployment.getName(), deployment.getApplicationProperties());
		Resource resource = delegatingResourceLoader.getResource(deployment.getResource());

		Map<String, String> deploymentProperties = deployment.getDeploymentProperties();
		deploymentProperties.put(AppDeployer.COUNT_PROPERTY_KEY, String.valueOf(deployment.getCount()));
		deploymentProperties.put(AppDeployer.GROUP_PROPERTY_KEY, releaseName + "-v" + version);

		AppDeploymentRequest appDeploymentRequest = new AppDeploymentRequest(appDefinition, resource,
				deploymentProperties);
		return appDeploymentRequest;

	}

	public ReleaseStatusResponse status(String releaseName, int version) {

		Release requestedRelease = null;
		if (version <= 0) {
			requestedRelease = releaseRepository.findLatestRelease(releaseName);
		}
		else {
			requestedRelease = releaseRepository.findByNameAndVersion(releaseName, version);
		}
		if (requestedRelease == null) {
			throw new IllegalArgumentException("Could not find release with name " + releaseName);
		}

		Assert.notNull(requestedRelease.getInfo(),
				"Release Info is missing for release name = " + releaseName + " version = " + version);
		Assert.notNull(requestedRelease.getChart(),
				"Release Chart is missing for release name = " + releaseName + " version = " + version);
		calculateStatus(requestedRelease);

		ReleaseStatusResponse releaseStatusResponse = new ReleaseStatusResponse();
		releaseStatusResponse.setName(requestedRelease.getName());
		releaseStatusResponse.setInfo(requestedRelease.getInfo());

		return releaseStatusResponse;

	}

	// private Release getLatestRelease(String releaseName) {
	// Iterable<Release> releases = releaseRepository.findAll();
	// int lastVersion = 0;
	// Release latestRelease = null;
	// for (Release release : releases) {
	// // Find the latest release
	// if (release.getName().equals(releaseName)) {
	// if (release.getVersion() > lastVersion) {
	// lastVersion = release.getVersion();
	// latestRelease = release;
	// }
	// }
	// }
	// return latestRelease;
	// }

	private void calculateStatus(Release release) {
		// TODO put in background thread.
		boolean allClear = true;

		AppStatus status = appDeployer.status(release.getDeploymentId());
		Map<String, AppInstanceStatus> instances = status.getInstances();
		for (AppInstanceStatus appInstanceStatus : instances.values()) {
			if (appInstanceStatus.getState() != DeploymentState.deployed) {
				allClear = false;
			}
		}
		if (allClear) {
			release.getInfo().getStatus().setPlatformStatus("All Applications deployed successfully");
			releaseRepository.save(release);
		}
		else {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Not all applications deployed successfully. ");
			for (AppInstanceStatus appInstanceStatus : instances.values()) {
				stringBuffer.append(appInstanceStatus.getId()).append("=").append(appInstanceStatus.getState())
						.append(", ");
			}
			release.getInfo().getStatus().setPlatformStatus(stringBuffer.toString());
			releaseRepository.save(release);
		}

	}
}
