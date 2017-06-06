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

package org.springframework.cloud.skipper.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.rpc.Chart;
import org.springframework.cloud.skipper.rpc.InstallReleaseRequest;
import org.springframework.cloud.skipper.rpc.InstallReleaseResponse;
import org.springframework.cloud.skipper.rpc.Release;

/**
 * @author Mark Pollack
 */
public class InstallService {

	private final GilliganClient gilliganClient;

	private final ChartResolver chartResolver;

	private final ChartLoader chartLoader;

	private static final Logger log = LoggerFactory.getLogger(InstallService.class);

	@Autowired
	public InstallService(GilliganClient gilliganClient, ChartResolver chartResolver, ChartLoader chartLoader) {
		this.gilliganClient = gilliganClient;
		this.chartResolver = chartResolver;
		this.chartLoader = chartLoader;
	}

	public Release install(String chartName, String releaseName) {

		String releaseNameToUse;
		if (releaseName == null) {
			releaseNameToUse = generateReleaseName();
		}
		else {
			releaseNameToUse = releaseName;
		}

		String chartPath = chartResolver.resolve(chartName);
		Chart chart = chartLoader.load(chartPath);

		InstallReleaseRequest request = new InstallReleaseRequest(releaseNameToUse, chart, null);
		InstallReleaseResponse response = gilliganClient.install(request);
		return response.getRelease();
	}

	private String generateReleaseName() {
		return "happy-panda";
	}

}
