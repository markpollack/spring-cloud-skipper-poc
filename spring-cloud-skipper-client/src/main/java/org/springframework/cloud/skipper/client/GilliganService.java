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
import org.springframework.cloud.skipper.rpc.*;
import org.springframework.cloud.skipper.rpc.domain.Chart;
import org.springframework.cloud.skipper.rpc.domain.Config;
import org.springframework.cloud.skipper.rpc.domain.Release;

/**
 * The high level client API that communicates with the Gilligan Server.
 *
 * Uses the GilliganClient.
 *
 * @author Mark Pollack
 */
public class GilliganService {

	private final GilliganClient gilliganClient;

	private final ChartResolver chartResolver;

	private final ChartLoader chartLoader;

	private static final Logger log = LoggerFactory.getLogger(GilliganService.class);

	@Autowired
	public GilliganService(GilliganClient gilliganClient, ChartResolver chartResolver, ChartLoader chartLoader) {
		this.gilliganClient = gilliganClient;
		this.chartResolver = chartResolver;
		this.chartLoader = chartLoader;
	}

	public Release install(String chartPath, String releaseName) {

		String releaseNameToUse;
		if (releaseName == null) {
			releaseNameToUse = generateReleaseName();
		}
		else {
			releaseNameToUse = releaseName;
		}

		String resolvedChartPath = chartResolver.resolve(chartPath);

		Chart chart = chartLoader.load(resolvedChartPath);

		InstallReleaseRequest request = new InstallReleaseRequest(releaseNameToUse, chart, new Config());
		InstallReleaseResponse response = gilliganClient.install(request);
		return response.getRelease();
	}

	public ReleaseStatusResponse status(String releaseName, int version) {
		ReleaseStatusRequest releaseStatusRequest = new ReleaseStatusRequest();
		releaseStatusRequest.setName(releaseName);
		releaseStatusRequest.setVersion(version);
		return gilliganClient.status(releaseStatusRequest);
	}

	public Release upgrade(String chartPath, String releaseName, int releaseVersion, boolean reuseValues,
			boolean resetValues) {

		// Note, releaseVersion is not used now as we only support local paths. Is used
		// when getting chart from the chart repository.
		String resolvedChartPath = chartResolver.resolve(chartPath);
		Chart chart = chartLoader.load(resolvedChartPath);

		UpdateReleaseRequest updateReleaseRequest = new UpdateReleaseRequest();
		updateReleaseRequest.setChart(chart);
		updateReleaseRequest.setName(releaseName);
		updateReleaseRequest.setReuseValues(reuseValues);
		updateReleaseRequest.setResetValues(resetValues);
		return gilliganClient.update(updateReleaseRequest).getRelease();

	}

	public HistoryResponse history(String releaseName, int max) {
		HistoryRequest historyRequest = new HistoryRequest();
		historyRequest.setName(releaseName);
		historyRequest.setMax(max);
		return gilliganClient.history(historyRequest);

	}

	private String generateReleaseName() {
		return "happy-panda";
	}

	public RollbackResponse rollback(String releaseName, Integer releaseVersion) {
		RollbackRequest rollbackRequest = new RollbackRequest();
		rollbackRequest.setName(releaseName);
		rollbackRequest.setVersion(releaseVersion);
		return gilliganClient.rollback(rollbackRequest);
	}

	public SelectorResponse select(String selectorExpression) {
		SelectorRequest selectorRequest = new SelectorRequest();
		selectorRequest.setSelectorExpression(selectorExpression);
		return gilliganClient.select(selectorRequest);
	}
}
