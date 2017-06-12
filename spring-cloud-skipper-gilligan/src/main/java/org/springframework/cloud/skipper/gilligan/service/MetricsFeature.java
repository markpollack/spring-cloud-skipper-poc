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

import org.springframework.cloud.skipper.rpc.domain.Deployment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Mark Pollack
 */
@Component
public class MetricsFeature implements Feature {

	@Override
	public String getName() {
		return "metrics";
	}

	@Override
	public Deployment addFeature(Deployment deployment) {
		Map<String, String> applicationProperties = deployment.getApplicationProperties();

		Map<String, String> labels = deployment.getLabels();
		String streamName = null;
		String appName = null;
		if (labels != null) {
			if (labels.containsKey("streamName")) {
				streamName = labels.get("streamName");
				applicationProperties.put("spring.cloud.dataflow.stream.name", streamName);

			}
			if (labels.containsKey("name")) {
				appName = labels.get("name");
				applicationProperties.put("spring.cloud.dataflow.stream.app.label", appName);
			}
			if (labels.containsKey("streamType")) {
				applicationProperties.put("spring.cloud.dataflow.stream.app.type", labels.get("streamType"));
			}
			if (StringUtils.hasText(streamName) && StringUtils.hasText(appName)) {
				StringBuilder sb = new StringBuilder().append(streamName).append(".").append(appName).append(".")
						.append("${spring.cloud.application.guid}");
				applicationProperties.put("spring.cloud.stream.metrics.key", sb.toString());
			}
		}
		return deployment;

	}
}
