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

package org.springframework.cloud.skipper.rpc;

/**
 * @author Mark Pollack
 */
public class InstallReleaseRequest {

	private Chart chart;

	private Config configValues;

	private String name;

	public InstallReleaseRequest() {
	}

	public InstallReleaseRequest(String name, Chart chart, Config configValues) {
		this.chart = chart;
		this.configValues = configValues;
		this.name = name;
	}

	public Chart getChart() {
		return chart;
	}

	public Config getConfigValues() {
		return configValues;
	}

	public String getName() {
		return name;
	}
}
