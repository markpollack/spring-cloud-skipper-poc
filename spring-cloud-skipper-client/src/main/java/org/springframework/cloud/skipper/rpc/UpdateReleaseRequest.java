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

import org.springframework.cloud.skipper.rpc.domain.Chart;
import org.springframework.cloud.skipper.rpc.domain.Config;

/**
 * @author Mark Pollack
 */
public class UpdateReleaseRequest {

	// The name of the release
	private String name;

	private Chart chart;

	// A string containing (unparsed) YAML values.
	private Config configValues;

	// reuseValues will cause Gilligan to reuse the values from the last release.
	// This is ignored if resetValues is set.
	private boolean reuseValues;

	// resetValues will cause Gilligan to ignore stored values, resetting to default
	// values.
	private boolean resetValues;

	public UpdateReleaseRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Chart getChart() {
		return chart;
	}

	public void setChart(Chart chart) {
		this.chart = chart;
	}

	public Config getConfigValues() {
		return configValues;
	}

	public void setConfigValues(Config configValues) {
		this.configValues = configValues;
	}

	public boolean isReuseValues() {
		return reuseValues;
	}

	public void setReuseValues(boolean reuseValues) {
		this.reuseValues = reuseValues;
	}

	public boolean isResetValues() {
		return resetValues;
	}

	public void setResetValues(boolean resetValues) {
		this.resetValues = resetValues;
	}
}
