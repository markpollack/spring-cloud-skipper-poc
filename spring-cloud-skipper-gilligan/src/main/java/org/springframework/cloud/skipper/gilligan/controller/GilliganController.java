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

package org.springframework.cloud.skipper.gilligan.controller;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.samskivert.mustache.Mustache;

import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.skipper.client.YamlUtils;
import org.springframework.cloud.skipper.client.domain.Deployment;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.rpc.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mark Pollack
 */
@RestController
@RequestMapping("/skipper")
public class GilliganController {

	private final AppDeployer appDeployer;

	private final ReleaseRepository releaseRepository;

	private final DelegatingResourceLoader delegatingResourceLoader;

	public GilliganController(ReleaseRepository releaseRepository, AppDeployer appDeployer,
			DelegatingResourceLoader delegatingResourceLoader) {
		this.appDeployer = appDeployer;
		this.releaseRepository = releaseRepository;
		this.delegatingResourceLoader = delegatingResourceLoader;
	}

	@PostMapping
	@RequestMapping("/install")
	@ResponseStatus(HttpStatus.CREATED)
	public InstallReleaseResponse install(@RequestBody InstallReleaseRequest installReleaseRequest) {
		Release release = prepareRelease(installReleaseRequest);
		return performRelease(release);
	}

	private InstallReleaseResponse performRelease(Release release) {
		InstallReleaseResponse response = new InstallReleaseResponse(release);
		return response;
	}

	private Release prepareRelease(InstallReleaseRequest installReleaseRequest) {

		// Merge chart metadata, values, and install values into a map
		// Render any template files based on map values and store back in chart object.

		// Enrich app properties based on "policies", e.g. metrics

		Release release = new Release();
		release.setName(installReleaseRequest.getName());
		release.setChart(installReleaseRequest.getChart());
		release.setConfig(installReleaseRequest.getConfigValues());
		release.setVersion(1);

		Info info = new Info();
		info.setFirstDeployed(new Date());
		info.setLastDeployed(new Date());
		info.setStatus(Status.UNKNOWN);
		info.setDescription("Initial install underway"); // Will be overwritten
		release.setInfo(info);

		Template[] templates = installReleaseRequest.getChart().getTemplates();

		// Resolve values in the template files.
		Properties model = getModel(installReleaseRequest.getConfigValues().getRaw());

		// Aggregate all valid manifests into one big doc.
		StringBuilder sb = new StringBuilder();
		for (Template template : templates) {
			String templateAsString = new String(template.getData());
			com.samskivert.mustache.Template mustacheTemplate = Mustache.compiler().compile(templateAsString);
			sb.append("\n---\n# Source: " + template.getName() + "\n");
			sb.append(mustacheTemplate.execute(model));
		}
		String manifests = sb.toString();
		release.setManifest(manifests);

		// Store in DB
		releaseRepository.save(release);

		// Store in git?

		Deployment appDeployment = YmlUtils.unmarshallDeployment(manifests);

		deploy(appDeployment, release);

		return release;
	}

	private void deploy(Deployment appDeployment, Release release) {
		String deploymentId = appDeployer.deploy(
				createAppDeploymentRequest(appDeployment, release.getName(), String.valueOf(release.getVersion())));
		release.setDeploymentId(deploymentId);

		// Store in DB
		release.getInfo().setStatus(Status.DEPLOYED);
		release.getInfo().setDescription("Install complete");
		releaseRepository.save(release);
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

	// private Deployment unmarshallDeployment(String manifests) {
	// ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	// try {
	// Deployment deployment = mapper.readValue(manifests, Deployment.class);
	// return deployment;
	// }
	// catch (IOException e) {
	// throw new IllegalArgumentException("Could not map YAML to deployment. YAML = " +
	// manifests, e);
	// }
	// }

	private Properties getModel(String raw) {
		return YamlUtils.getProperties(raw);
	}

}
