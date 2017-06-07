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
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.gilligan.service.ReleaseService;
import org.springframework.cloud.skipper.rpc.InstallReleaseRequest;
import org.springframework.cloud.skipper.rpc.InstallReleaseResponse;
import org.springframework.cloud.skipper.rpc.ReleaseStatusRequest;
import org.springframework.cloud.skipper.rpc.ReleaseStatusResponse;
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
		return performRelease(release);
	}

	@GetMapping
	@RequestMapping("/status")
	public ReleaseStatusResponse status(@RequestBody ReleaseStatusRequest releaseStatusRequest) {
		return releaseService.status(releaseStatusRequest.getName(), releaseStatusRequest.getVersion());
	}

	private InstallReleaseResponse performRelease(Release release) {
		InstallReleaseResponse response = new InstallReleaseResponse(release);
		return response;
	}

	private Release prepareRelease(InstallReleaseRequest installReleaseRequest) {

		Release release = createInitialReleaseObject(installReleaseRequest);

		// Resolve model values to render from the template file and command line values.
		Properties model = releaseService.mergeConfigValues(installReleaseRequest.getConfigValues(),
				installReleaseRequest.getChart().getConfigValues());

		Template[] templates = installReleaseRequest.getChart().getTemplates();
		String manifest = releaseService.createManifest(templates, model);
		release.setManifest(manifest);

		// Store in DB
		releaseRepository.save(release);

		// Store manifest in git?

		// Deploy the application
		releaseService.deploy(release);

		return release;
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
