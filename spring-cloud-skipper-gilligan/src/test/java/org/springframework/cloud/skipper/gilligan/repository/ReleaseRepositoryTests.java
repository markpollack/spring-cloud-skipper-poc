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
package org.springframework.cloud.skipper.gilligan.repository;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.skipper.gilligan.GilliganApplication;
import org.springframework.cloud.skipper.gilligan.util.YmlUtils;
import org.springframework.cloud.skipper.rpc.domain.*;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author Mark Pollack
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GilliganApplication.class)
public class ReleaseRepositoryTests<K, V> {

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
		String text = loadYml("/deserialization/log.yml");
		List<Deployment> deployments = YmlUtils.unmarshallDeployments(text);
		Deployment deployment = deployments.get(0);
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
	public void testMultipleDocuments() {
		String text = loadYml("/deserialization/multipleDeployments.yml");
		List<Deployment> deployments = YmlUtils.unmarshallDeployments(text);
		assertThat(deployments).hasSize(2);
	}

	private String loadYml(String file) {
		return new Scanner(ReleaseRepositoryTests.class.getResourceAsStream(file), "UTF-8")
				.useDelimiter("\\A").next();
	}

	@Test
	public void testReleaseStorage() {

		String text = loadYml("/deserialization/log.yml");

		Release release = new Release();
		release.setName("log");
		// release.setChart(installReleaseRequest.getChart());
		// release.setConfig(installReleaseRequest.getConfigValues());
		release.setVersion(1);

		Info info = new Info();
		info.setFirstDeployed(new Date());
		info.setLastDeployed(new Date());
		Status status = new Status();
		status.setStatusCode(StatusCode.UNKNOWN);
		info.setStatus(status);
		info.setDescription("Initial install underway"); // Will be overwritten
		release.setInfo(info);

		release.setManifest(text);
		releaseRepository.save(release);

		Release retrievedRelease = releaseRepository.findOne(release.getId());
		assertThat(retrievedRelease.getName()).isNotBlank();
	}

}
