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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.gilligan.service.ReleaseService;
import org.springframework.cloud.skipper.rpc.*;
import org.springframework.cloud.skipper.rpc.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mark Pollack
 */
@RestController
@RequestMapping("/skipper")
public class GilliganController {

	private final ReleaseRepository releaseRepository;

	private final ReleaseService releaseService;

	@Autowired
	public GilliganController(ReleaseService releaseService, ReleaseRepository releaseRepository) {
		this.releaseService = releaseService;
		this.releaseRepository = releaseRepository;
	}

	@PostMapping
	@RequestMapping("/install")
	@ResponseStatus(HttpStatus.CREATED)
	public InstallReleaseResponse install(@RequestBody InstallReleaseRequest installReleaseRequest) {
		Release release = prepareRelease(installReleaseRequest);
		InstallReleaseResponse response = new InstallReleaseResponse(release);
		return response;
	}

	@GetMapping
	@RequestMapping("/status")
	public ReleaseStatusResponse status(@RequestBody ReleaseStatusRequest releaseStatusRequest) {

		Release release = releaseService.status(releaseStatusRequest.getName(), releaseStatusRequest.getVersion());
		ReleaseStatusResponse releaseStatusResponse = new ReleaseStatusResponse();
		releaseStatusResponse.setName(release.getName());
		releaseStatusResponse.setInfo(release.getInfo());
		return releaseStatusResponse;
	}

	@PostMapping
	@RequestMapping("/update")
	@ResponseStatus(HttpStatus.CREATED)
	public UpdateReleaseResponse update(@RequestBody UpdateReleaseRequest updateReleaseRequest) {

		Release release = releaseService.update(updateReleaseRequest.getName(), updateReleaseRequest.getChart(),
				updateReleaseRequest.getConfigValues(), updateReleaseRequest.isResetValues(),
				updateReleaseRequest.isReuseValues());

		UpdateReleaseResponse updateReleaseResponse = new UpdateReleaseResponse();
		updateReleaseResponse.setRelease(release);
		return updateReleaseResponse;

	}

	private Release prepareRelease(InstallReleaseRequest installReleaseRequest) {
		Release release = createInitialReleaseObject(installReleaseRequest);
		Config configValues = installReleaseRequest.getConfigValues();
		Template[] templates = installReleaseRequest.getChart().getTemplates();
		return releaseService.install(release, templates, configValues);
	}

	private Release createInitialReleaseObject(InstallReleaseRequest installReleaseRequest) {
		Release release = new Release();
		release.setName(installReleaseRequest.getName());
		release.setChart(installReleaseRequest.getChart());
		release.setConfig(installReleaseRequest.getConfigValues());
		release.setVersion(1);

		Info info = new Info();
		info.setFirstDeployed(new Date());
		info.setLastDeployed(new Date());
		Status status = new Status();
		status.setStatusCode(StatusCode.UNKNOWN);
		info.setStatus(status);
		info.setDescription("Initial install underway"); // Will be overwritten
		release.setInfo(info);
		return release;
	}

}
