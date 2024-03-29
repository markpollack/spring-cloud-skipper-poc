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

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mark Pollack
 */
@ConfigurationProperties(prefix = "deployment")
public class Deployment {

	private String name;

	private int count;

	private Map<String, String> labels;

	private Map<String, String> applicationProperties;

	private String resource;

	private String resourceMetadata;

	private Map<String, String> deploymentProperties;

	private String features;

	public Deployment() {
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Map<String, String> getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(Map<String, String> applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getResourceMetadata() {
		return resourceMetadata;
	}

	public void setResourceMetadata(String resourceMetadata) {
		this.resourceMetadata = resourceMetadata;
	}

	public Map<String, String> getDeploymentProperties() {
		return deploymentProperties;
	}

	public void setDeploymentProperties(Map<String, String> deploymentProperties) {
		this.deploymentProperties = deploymentProperties;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	@Override
	public String toString() {
		return "Deployment{" +
				"name='" + name + '\'' +
				", count=" + count +
				", labels=" + labels +
				", applicationProperties=" + applicationProperties +
				", resource='" + resource + '\'' +
				", resourceMetadata='" + resourceMetadata + '\'' +
				", deploymentProperties=" + deploymentProperties +
				'}';
	}
}
