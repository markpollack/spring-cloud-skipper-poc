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

/**
 * @author Mark Pollack
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LocalSkipperTests<K, V> {

	@Autowired
	private Skipper skipper;

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
	public void simpleTest() throws InterruptedException {
		assertThat(skipper).isNotNull();
		String releaseName = "myLogRelease";
		skipper.install(releaseName, "classpath:/log/deployments/log.yml");
		System.out.println(skipper.describe(releaseName));
		for (int i = 0; i < 30; i++) {
			System.out.println(skipper.status(releaseName));
			System.out.println(skipper.describe(releaseName));
			Thread.sleep(1000);
		}
		System.out.println("Going to upgrade....");
		skipper.upgrade(releaseName, "classpath:/log2/deployments/log.yml");
		for (int i = 0; i < 30; i++) {
			System.out.println(skipper.status(releaseName));
			System.out.println(skipper.describe(releaseName));
			Thread.sleep(1000);
		}
		System.out.println(skipper.history(releaseName));
		System.out.println("Exiting in 5 seconds...");
		Thread.sleep(5000);
	}

}
