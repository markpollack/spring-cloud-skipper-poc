/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.cloud.skipper.core;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * @author Mark Pollack
 */
@RedisHash("releases")
public class Release {

	@Id
	private String id;

	private String name;

	private Deployment deployment;

	private int version;

	private String status;

	private String deploymentId;

	private String firstDeployed;

	private String description;

	public Release() {

	}

	public Release(Deployment deployment, int version) {
		this.deployment = deployment;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Deployment getDeployment() {
		return deployment;
	}

	public void setDeployment(Deployment deployment) {
		this.deployment = deployment;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getFirstDeployed() {
		return firstDeployed;
	}

	public void setFirstDeployed(String firstDeployed) {
		this.firstDeployed = firstDeployed;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Release{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", deployment=" + deployment + ", version="
				+ version + ", status='" + status + '\'' + ", deploymentId='" + deploymentId + '\''
				+ ", firstDeployed='" + firstDeployed + '\'' + ", description='" + description + '\'' + '}';
	}
}
