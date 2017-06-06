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
package org.springframework.cloud.skipper.client.old;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppInstanceStatus;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.skipper.client.Skipper;
import org.springframework.cloud.skipper.client.domain.Deployment;
import org.springframework.core.io.Resource;

/**
 * @author Mark Pollack
 */
public class AppDeployerSkipper implements Skipper {

	private final ReleasePocV1Repository releaseRepository;

	private final AppDeployer appDeployer;

	private final DelegatingResourceLoader delegatingResourceLoader;

	@Autowired
	public AppDeployerSkipper(ReleasePocV1Repository releaseRepository, AppDeployer appDeployer,
			DelegatingResourceLoader delegatingResourceLoader) {
		this.releaseRepository = releaseRepository;
		this.appDeployer = appDeployer;
		this.delegatingResourceLoader = delegatingResourceLoader;
	}

	@Override
	public void install(String releaseName, String deploymentResource) {

		Deployment deployment = DeploymentUtils.load(deploymentResource);

		int version = 1;
		ReleasePocV1 release = newRelease(releaseName, deployment, version);

		deploy(deployment, release);
	}

	private void deploy(Deployment deployment, ReleasePocV1 release) {
		String deploymentId = appDeployer.deploy(
				createAppDeploymentRequest(deployment, release.getName(), String.valueOf(release.getVersion())));
		release.setDeploymentId(deploymentId);
		release.setStatus("Applications deploying.  DeploymentId = [" + deploymentId + "]");
		releaseRepository.save(release);
	}

	private void undeploy(ReleasePocV1 release) {
		appDeployer.undeploy(release.getDeploymentId());
		release.setStatus("Applications undeploying.  DeploymentId = [" + release.getDeploymentId() + "]");
		releaseRepository.save(release);
	}

	private ReleasePocV1 newRelease(String releaseName, Deployment deployment, int version) {
		ReleasePocV1 release = new ReleasePocV1(deployment, version);
		release.setName(releaseName);
		release.setFirstDeployed(ISO8601Utils.format(new Date(), true));
		release.setStatus("Installing...");
		releaseRepository.save(release);

		ReleasePocV1 test = releaseRepository.findOne(release.getId());
		return release;
	}

	private void calculateStatus(ReleasePocV1 release) {
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
			release.setStatus("All Applications deployed successfully");
			releaseRepository.save(release);
		}
		else {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Not all applications deployed successfully. ");
			for (AppInstanceStatus appInstanceStatus : instances.values()) {
				stringBuffer.append(appInstanceStatus.getId()).append("=").append(appInstanceStatus.getState())
						.append(", ");
			}
			release.setStatus(stringBuffer.toString());
			releaseRepository.save(release);
		}

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

	@Override
	public String status(String releaseName) {
		ReleasePocV1 latestRelease = getLatestRelease(releaseName);
		if (latestRelease != null) {
			calculateStatus(latestRelease);
			return latestRelease.getStatus();
		}
		else {
			throw new IllegalArgumentException("No release name " + releaseName);
		}

	}

	private ReleasePocV1 getLatestRelease(String releaseName) {
		Iterable<ReleasePocV1> releases = releaseRepository.findAll();
		int lastVersion = 0;
		ReleasePocV1 latestRelease = null;
		for (ReleasePocV1 release : releases) {
			// Find the latest release
			if (release.getName().equals(releaseName)) {
				if (release.getVersion() > lastVersion) {
					lastVersion = release.getVersion();
					latestRelease = release;
				}
			}
		}
		return latestRelease;
	}

	@Override
	public ReleasePocV1 describe(String releaseName) {
		ReleasePocV1 latestRelease = getLatestRelease(releaseName);
		if (latestRelease != null) {
			calculateStatus(latestRelease);
			return latestRelease;
		}
		else {
			throw new IllegalArgumentException("No release name " + releaseName);
		}
	}

	@Override
	public List<ReleasePocV1> history(String releaseName) {
		List<ReleasePocV1> releaseList = new ArrayList<>();
		Iterable<ReleasePocV1> releases = releaseRepository.findAll();
		for (ReleasePocV1 release : releases) {
			if (release.getName().equals(releaseName)) {
				releaseList.add(release);
			}
		}
		// TODO order by date.
		return releaseList;
	}

	@Override
	public String upgrade(String releaseName, String deploymentResource) {

		Deployment deployment = DeploymentUtils.load(deploymentResource);

		// Get latestRelease, which becomes the previous release
		ReleasePocV1 previousRelease = getLatestRelease(releaseName);
		if (previousRelease == null) {
			throw new IllegalArgumentException("Could not find latest release for [" + releaseName + "]");
		}

		// Create new release with updated version
		ReleasePocV1 release = newRelease(releaseName, deployment, previousRelease.getVersion() + 1);
		releaseRepository.save(release);

		// Deploy new release
		deploy(deployment, release);

		// Ideally wait until the apps are live and healthy.

		// Undeploy previous release
		undeploy(previousRelease);

		return null;
	}

	@Override
	public String rollback(String releaseName, String version) {
		return null;
	}

	@Override
	public void delete(String releaseName) {

	}
}
