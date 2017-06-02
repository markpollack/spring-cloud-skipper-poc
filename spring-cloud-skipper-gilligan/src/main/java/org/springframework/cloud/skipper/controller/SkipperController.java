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

package org.springframework.cloud.skipper.controller;

import org.springframework.cloud.skipper.rpc.InstallReleaseRequest;
import org.springframework.cloud.skipper.rpc.InstallReleaseResponse;
import org.springframework.cloud.skipper.rpc.Release;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mark Pollack
 */
@RestController
@RequestMapping("/skipper")
public class SkipperController {

	@PostMapping
	@RequestMapping("/install")
	@ResponseStatus(HttpStatus.CREATED)
	public InstallReleaseResponse install(InstallReleaseRequest installReleaseRequest) {
		Release release = new Release("test");
		release.setVersion("1.0.0");
		InstallReleaseResponse response = new InstallReleaseResponse(release);
		return response;
	}

}
