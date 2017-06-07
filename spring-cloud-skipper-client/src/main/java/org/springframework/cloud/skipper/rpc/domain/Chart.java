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

package org.springframework.cloud.skipper.rpc.domain;

/**
 * Chart is a skipper package that contains metadata, a default config, zero or more
 * optionally parameterizable templates, and zero or more charts (dependencies).
 *
 * @author Mark Pollack
 *
 */
public class Chart {

	// Contents of the Chartfile.
	private Metadata metadata;

	// Templates for this chart.
	private Template[] templates;

	// Charts that this chart depends on.
	private Chart[] dependencies;

	// Default config for this template.
	private Config configValues;

	// Miscellaneous files in a chart archive,
	// e.g. README, LICENSE, etc.
	private FileHolder[] fileHolders;

	public Chart() {
	}

	public Metadata getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public Template[] getTemplates() {
		return templates;
	}

	public void setTemplates(Template[] templates) {
		this.templates = templates;
	}

	public Chart[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(Chart[] dependencies) {
		this.dependencies = dependencies;
	}

	public Config getConfigValues() {
		return configValues;
	}

	public void setConfigValues(Config configValues) {
		this.configValues = configValues;
	}

	public FileHolder[] getFileHolders() {
		return fileHolders;
	}

	public void setFileHolders(FileHolder[] fileHolders) {
		this.fileHolders = fileHolders;
	}
}
