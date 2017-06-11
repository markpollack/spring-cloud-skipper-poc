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

package org.springframework.cloud.skipper.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.skipper.rpc.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * Client API that uses RestTemplate to communicate with the Gilligan Server
 *
 * Based off ReleaseServiceClient in tiller.pb.go
 *
 * @author Mark Pollack
 */
public class GilliganClient {

	protected final RestTemplate restTemplate;

	private final String baseURI;

	private static final Logger log = LoggerFactory.getLogger(GilliganClient.class);

	public GilliganClient(String baseURI) {
		Assert.notNull(baseURI, "The provided baseURI must not be null.");
		this.baseURI = baseURI;
		this.restTemplate = getDefaultRestTemplate();
	}

	public InstallReleaseResponse install(InstallReleaseRequest installReleaseRequest) {

		log.debug("Posting to " + baseURI + "/install");
		InstallReleaseResponse response = restTemplate.postForObject(baseURI + "/install", installReleaseRequest,
				InstallReleaseResponse.class);
		return response;
	}

	public ReleaseStatusResponse status(ReleaseStatusRequest releaseStatusRequest) {
		// TODO change to get
		log.debug("Posting to " + baseURI + "/status");
		ReleaseStatusResponse response = restTemplate.postForObject(baseURI + "/status", releaseStatusRequest,
				ReleaseStatusResponse.class);
		return response;
	}

	public UpdateReleaseResponse update(UpdateReleaseRequest updateReleaseRequest) {
		log.debug("Posting to " + baseURI + "/update");
		UpdateReleaseResponse response = restTemplate.postForObject(baseURI + "/update", updateReleaseRequest,
				UpdateReleaseResponse.class);
		return response;
	}

	public HistoryResponse history(HistoryRequest historyRequest) {
		log.debug("Posting to " + baseURI + "/history");
		return restTemplate.postForObject(baseURI + "/history", historyRequest, HistoryResponse.class);
	}

	public RollbackResponse rollback(RollbackRequest rollbackRequest) {
		log.debug("Posting to " + baseURI + "/rollback");
		return restTemplate.postForObject(baseURI + "/rollback", rollbackRequest, RollbackResponse.class);
	}

	public static RestTemplate getDefaultRestTemplate() {

		RestTemplate restTemplate = new RestTemplate();

		boolean containsMappingJackson2HttpMessageConverter = false;

		for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				containsMappingJackson2HttpMessageConverter = true;
			}
		}

		if (!containsMappingJackson2HttpMessageConverter) {
			throw new IllegalArgumentException(
					"The RestTemplate does not contain a required " + "MappingJackson2HttpMessageConverter.");
		}
		return restTemplate;
	}

}
