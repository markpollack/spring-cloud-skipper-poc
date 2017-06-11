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

package org.springframework.cloud.skipper.gilligan.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.skipper.client.ChartLoader;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.rpc.*;
import org.springframework.cloud.skipper.rpc.domain.Chart;
import org.springframework.cloud.skipper.rpc.domain.Config;
import org.springframework.cloud.skipper.rpc.domain.Release;
import org.springframework.cloud.skipper.rpc.domain.StatusCode;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mark Pollack
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GilliganControllerTests<K, V> {

	private static String lastTestMethod;

	private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private final String releaseName = "mylog";

	@Autowired
	private RedisOperations<K, V> operations;

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private ReleaseRepository releaseRepository;

	private MockMvc mockMvc;

	public static String convertObjectToJson(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		String json = mapper.writeValueAsString(object);
		return json;
	}

	@Before
	public void setupMockMvc() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
				.defaultRequest(get("/").accept(MediaType.APPLICATION_JSON)).build();
	}

	@After
	public void setUpAfter() {
		if (lastTestMethod.equals("t3update")) {
			operations.execute((RedisConnection connection) -> {
				connection.flushDb();
				return "OK";
			});
		}
	}

	@Before
	public void setUpBefore() {
		if (lastTestMethod == null) {
			operations.execute((RedisConnection connection) -> {
				connection.flushDb();
				return "OK";
			});
		}
	}

	@Test
	public void t1Install() throws Exception {
		lastTestMethod = "t1Install";
		InstallReleaseRequest installReleaseRequest = createInstallReleaseRequest();
		MvcResult result = mockMvc
				.perform(post("/skipper/install").contentType(contentType)
						.content(convertObjectToJson(installReleaseRequest)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		String releaseResponseString = result.getResponse().getContentAsString();
		// wait for deployment
		ObjectMapper mapper = new ObjectMapper();
		InstallReleaseResponse installReleaseResponse = mapper.readValue(releaseResponseString,
				InstallReleaseResponse.class);

		// Test release object is as expected in terms of basic state.
		Release release = installReleaseResponse.getRelease();
		assertRelease(release, 1);

		// Test that Release object was stored.
		Release retrievedRelease = releaseRepository.findOne(release.getId());
		assertThat(retrievedRelease.getName()).isNotBlank();

		// Test that template values were replaced.
		assertThat(installReleaseResponse.getRelease().getManifest()).contains("count: 1");
		assertThat(installReleaseResponse.getRelease().getManifest())
				.contains("resource: maven://org.springframework.cloud.stream.app:log-sink-rabbit:1.2.0.RELEASE");
		assertThat(installReleaseResponse.getRelease().getManifest()).contains("resourceMetadata: "
				+ "maven://org.springframework.cloud.stream.app:log-sink-rabbit:jar:metadata:1.2.0.RELEASE");

	}

	private void assertRelease(Release release, int expectedVersion) {
		assertThat(release.getName()).isNotBlank();
		assertThat(release.getVersion()).isEqualTo(expectedVersion);
		assertThat(release.getInfo().getStatus().getStatusCode()).isEqualByComparingTo(StatusCode.DEPLOYED);
		assertThat(release.getInfo().getDescription()).isEqualTo("Install complete");
	}

	@Test
	public void t2status() throws Exception {

		lastTestMethod = "t2status";
		ReleaseStatusResponse releaseStatusResponse = null;
		for (int i = 0; i < 10; i++) {

			ReleaseStatusRequest releaseStatusRequest = new ReleaseStatusRequest();
			releaseStatusRequest.setName(releaseName);
			releaseStatusRequest.setVersion(1);

			InstallReleaseRequest installReleaseRequest = createInstallReleaseRequest();
			MvcResult result = mockMvc
					.perform(post("/skipper/status").contentType(contentType)
							.content(convertObjectToJson(releaseStatusRequest)))
					.andDo(print()).andExpect(status().isOk()).andReturn();
			String releaseStatusResponseString = result.getResponse().getContentAsString();
			// wait for deployment
			ObjectMapper mapper = new ObjectMapper();
			releaseStatusResponse = mapper.readValue(releaseStatusResponseString, ReleaseStatusResponse.class);

			System.out.println("i = " + i + "status response = " + releaseStatusResponse);
			System.out.println("----------------------------");
			Thread.sleep(2000);
		}
		assertThat(releaseStatusResponse.getInfo().getStatus().getPlatformStatus())
				.isEqualTo("All Applications deployed successfully");

	}

	@Test
	public void t3update() throws Exception {
		lastTestMethod = "t3update";
		UpdateReleaseRequest updateReleaseRequest = createUpdateReleaseRequest();

		MvcResult result = mockMvc
				.perform(post("/skipper/update").contentType(contentType)
						.content(convertObjectToJson(updateReleaseRequest)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();

		String updateResponseString = result.getResponse().getContentAsString();
		// wait for deployment
		ObjectMapper mapper = new ObjectMapper();
		UpdateReleaseResponse updateReleaseResponse = mapper.readValue(updateResponseString,
				UpdateReleaseResponse.class);

		Release release = updateReleaseResponse.getRelease();
		assertRelease(updateReleaseResponse.getRelease(), 2);

		// Test that Release object was stored.
		Release retrievedRelease = releaseRepository.findOne(release.getId());
		assertThat(retrievedRelease.getName()).isNotBlank();

		// Test that template values were replaced.
		assertThat(release.getManifest()).contains("count: 1");
		assertThat(release.getManifest())
				.contains("resource: maven://org.springframework.cloud.stream.app:log-sink-rabbit:1.2.1.RELEASE");
		assertThat(release.getManifest()).contains("resourceMetadata: "
				+ "maven://org.springframework.cloud.stream.app:log-sink-rabbit:jar:metadata:1.2.1.RELEASE");

		t2status();
		lastTestMethod = "t3update";
	}

	private UpdateReleaseRequest createUpdateReleaseRequest() {
		ChartLoader chartLoader = new ChartLoader();
		URL url = this.getClass().getResource("/log2");
		File file = new File(url.getFile());
		Chart chart = chartLoader.load(file.getAbsolutePath());
		UpdateReleaseRequest updateReleaseRequest = new UpdateReleaseRequest();
		updateReleaseRequest.setName(releaseName);
		updateReleaseRequest.setChart(chart);
		updateReleaseRequest.setConfigValues(new Config());
		return updateReleaseRequest;
	}

	private InstallReleaseRequest createInstallReleaseRequest() {
		ChartLoader chartLoader = new ChartLoader();
		URL url = this.getClass().getResource("/log");
		assertThat(url).isNotNull();
		File file = new File(url.getFile());
		assertThat(file).exists();
		Chart chart = chartLoader.load(file.getAbsolutePath());
		InstallReleaseRequest request = new InstallReleaseRequest(releaseName, chart, new Config());
		return request;
	}

}
