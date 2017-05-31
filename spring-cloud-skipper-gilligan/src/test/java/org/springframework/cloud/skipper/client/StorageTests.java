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
package org.springframework.cloud.skipper.client;

import java.util.Date;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author Mark Pollack
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageTests<K, V> {

	@Autowired
	private RedisOperations<K, V> operations;

	@Autowired
	private ReleaseRepository releaseRepository;

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

		Deployment deployment = DeploymentUtils.load("classpath:/log/deployments/log.yml",
				"classpath:/log/values.properties");
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
	public void testReleaseStorage() {
		Deployment deployment = DeploymentUtils.load("classpath:/log/deployments/log.yml");
		int version = 1;
		Release release = new Release(deployment, version);
		release.setName("myLogRelease");
		release.setFirstDeployed(ISO8601Utils.format(new Date(), true));
		release.setStatus("Installing...");
		assertThat(release.getDeployment()).isNotNull();
		releaseRepository.save(release);
		assertThat(release.getDeployment()).isNotNull();
		Release retrievedRelease = releaseRepository.findOne(release.getId());
		assertThat(retrievedRelease.getDeployment()).isNotNull();

	}

}
