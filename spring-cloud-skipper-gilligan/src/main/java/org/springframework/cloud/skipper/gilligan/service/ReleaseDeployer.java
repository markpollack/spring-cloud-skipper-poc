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

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppInstanceStatus;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.gilligan.util.YmlUtils;
import org.springframework.cloud.skipper.rpc.domain.Deployment;
import org.springframework.cloud.skipper.rpc.domain.Release;
import org.springframework.cloud.skipper.rpc.domain.Status;
import org.springframework.cloud.skipper.rpc.domain.StatusCode;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Mark Pollack
 */
@Component
public class ReleaseDeployer {

	private final AppDeployer appDeployer;

	private final DelegatingResourceLoader delegatingResourceLoader;

	private final ReleaseRepository releaseRepository;

	private final List<Feature> featureList;

	@Autowired
	public ReleaseDeployer(AppDeployer appDeployer, DelegatingResourceLoader delegatingResourceLoader,
			ReleaseRepository releaseRepository, List<Feature> featureList) {
		this.appDeployer = appDeployer;
		this.delegatingResourceLoader = delegatingResourceLoader;
		this.releaseRepository = releaseRepository;
		this.featureList = featureList;
	}

	/**
	 * Deploy the specified release
	 *
	 * @param release the release to deploy
	 */
	public void deploy(Release release) {
		// Deploy the application
		List<Deployment> appDeployments = YmlUtils.unmarshallDeployments(release.getManifest());

		List<String> deploymentIds = new ArrayList<>();
		for (Deployment appDeployment : appDeployments) {
			deploymentIds.add(appDeployer.deploy(
					createAppDeploymentRequest(appDeployment, release.getName(),
							String.valueOf(release.getVersion()))));
		}
		release.setDeploymentId(StringUtils.collectionToCommaDelimitedString(deploymentIds));

		// Store in DB
		Status status = new Status();
		status.setStatusCode(StatusCode.DEPLOYED);
		release.getInfo().setStatus(status);
		release.getInfo().setDescription("Install complete");

		// Store updated state in in DB
		releaseRepository.save(release);

	}

	public void undeploy(Release release) {
		List<String> deploymentIds = Arrays
				.asList(StringUtils.commaDelimitedListToStringArray(release.getDeploymentId()));

		for (String deploymentId : deploymentIds) {
			appDeployer.undeploy(deploymentId);
		}
		Status status = new Status();
		status.setStatusCode(StatusCode.SUPERSEDED);
		release.getInfo().setStatus(status);

		releaseRepository.save(release);
	}

	public void calculateStatus(Release release) {
		// TODO put in background thread.
		boolean allClear = true;
		Map<String, AppInstanceStatus> instances = new HashMap<String, AppInstanceStatus>();
		List<String> deploymentIds = Arrays
				.asList(StringUtils.commaDelimitedListToStringArray(release.getDeploymentId()));

		for (String deploymentId : deploymentIds) {
			AppStatus status = appDeployer.status(deploymentId);
			for (AppInstanceStatus appInstanceStatus : status.getInstances().values()) {
				if (appInstanceStatus.getState() != DeploymentState.deployed) {
					allClear = false;
				}
			}
		}
		if (allClear) {
			release.getInfo().getStatus().setPlatformStatus("All Applications deployed successfully");
			releaseRepository.save(release);
		}
		else {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Not all applications deployed successfully. ");
			for (String deploymentId : deploymentIds) {
				AppStatus status = appDeployer.status(deploymentId);
				for (AppInstanceStatus appInstanceStatus : status.getInstances().values()) {
					stringBuffer.append(appInstanceStatus.getId()).append("=").append(appInstanceStatus.getState())
							.append(", ");
				}
			}
			String platformStatus = stringBuffer.toString();
			platformStatus = platformStatus.replaceAll(", $", "");
			release.getInfo().getStatus().setPlatformStatus(platformStatus);
			releaseRepository.save(release);
		}

	}

	private AppDeploymentRequest createAppDeploymentRequest(Deployment deployment, String releaseName,
			String version) {

		/*
		 * String[] featureNames =
		 * StringUtils.commaDelimitedListToStringArray(rawDeployment.getFeatures());
		 * Deployment deployment = rawDeployment; for (String featureName : featureNames)
		 * { for (Feature feature : featureList) { if
		 * (feature.getName().equals(featureName)) { deployment =
		 * feature.addFeature(deployment); } } }
		 */

		AppDefinition appDefinition = new AppDefinition(deployment.getName(), deployment.getApplicationProperties());
		Resource resource = delegatingResourceLoader.getResource(deployment.getResource());

		Map<String, String> deploymentProperties = deployment.getDeploymentProperties();
		deploymentProperties.put(AppDeployer.COUNT_PROPERTY_KEY, String.valueOf(deployment.getCount()));
		deploymentProperties.put(AppDeployer.GROUP_PROPERTY_KEY, releaseName + "-v" + version);

		AppDeploymentRequest appDeploymentRequest = new AppDeploymentRequest(appDefinition, resource,
				deploymentProperties);
		return appDeploymentRequest;
	}

}
