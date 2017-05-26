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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author Mark Pollack
 */
public class StorageTests<K, V> {

	@Autowired
	private RedisOperations<K, V> operations;

	@Before
	@After
	public void setUp() {

		operations.execute((RedisConnection connection) -> {
			connection.flushDb();
			return "OK";
		});
	}

	@Test
	public void testDeserialization() {

		Deployment deployment = DeploymentUtils.load("classpath:/log/deployments/log.yml");
		assertThat(deployment.getCount()).isEqualTo(2);
		assertThat(deployment.getName()).isEqualTo("log");
		assertThat(deployment.getResource())
				.isEqualTo("maven://org.springframework.cloud.stream.app:log-sink-rabbit:1.2.0.RELEASE");
		assertThat(deployment.getResourceMetadata())
				.isEqualTo("maven://org.springframework.cloud.stream.app:log-sink-rabbit:jar:metadata:1.2.0.RELEASE");
		assertThat(deployment.getDeploymentProperties()).hasSize(1).contains(entry("memory", "2048m"));
		assertThat(deployment.getApplicationProperties()).hasSize(1).contains(entry("log.level", "WARN"));
		System.out.println(deployment);
	}

	@Test
	void testReleaseStorage() {

	}

}
