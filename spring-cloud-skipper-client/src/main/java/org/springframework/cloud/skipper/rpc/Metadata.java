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
 * Metadata for a Chart file. This models the structure of a Chart.yaml file.
 *
 * Spec: https://k8s.io/helm/blob/master/docs/design/chart_format.md#the-chart-file
 *
 * @author Mark Pollack
 */
public class Metadata {

	// Minimal number of fields for now...

	// The name of the chart
	private String name;

	// A SemVer 2 conformant version string of the chart
	private String version;

	// A one-sentence description of the chart
	private String description;

	// The version of the application enclosed inside of this chart.
	private String appVersion;

	// A list of string keywords
	private String[] keywords;

	// The URL to a relevant project page, git repo, or contact person
	private String home;

	public Metadata() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}
}
