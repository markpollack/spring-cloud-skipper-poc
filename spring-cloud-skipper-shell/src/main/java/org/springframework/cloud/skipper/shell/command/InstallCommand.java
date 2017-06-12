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

package org.springframework.cloud.skipper.shell.command;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.client.GilliganService;
import org.springframework.cloud.skipper.rpc.domain.Release;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * @author Mark Pollack
 */
@Component
public class InstallCommand implements CommandMarker {

	@Autowired
	private GilliganService gilliganService;

	@CliCommand("skipper install")
	public String install(@CliOption(mandatory = true, key = { "", "chartPath" }, help = "Chart path") String chartPath,
			@CliOption(key = "releaseName", help = "Release name") String releaseName,
			@CliOption(key = "set", help = "Application Properties to set") String commandLineProperties) {

		Release release = gilliganService.install(chartPath, releaseName, commandLineProperties);
		StringBuilder sb = new StringBuilder();
		sb.append("Release Name: " + release.getName() + "\n");
		sb.append("Release Version: " + release.getVersion() + "\n");
		if (release.getInfo().getLastDeployed() != null) {
			sb.append("Last Deployed: " + ISO8601Utils.format(release.getInfo().getLastDeployed()) + "\n");
		}
		sb.append("Status: " + release.getInfo().getStatus().getStatusCode() + "\n");
		sb.append("Platform Status: " + release.getInfo().getStatus().getPlatformStatus() + "\n");

		return sb.toString();

	}
}
