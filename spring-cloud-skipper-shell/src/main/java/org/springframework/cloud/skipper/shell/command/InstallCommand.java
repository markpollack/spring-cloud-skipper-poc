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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.client.InstallService;
import org.springframework.cloud.skipper.rpc.Release;
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
	private InstallService installService;

	@CliCommand("skipper install")
	public String install(@CliOption(mandatory = true, key = { "", "chartName" }, help = "Chart name") String chartName,
			@CliOption(key = "releaseName", help = "Release name") String releaseName) {

		Release release = installService.install(chartName, releaseName);
		return release.getName() + ":" + release.getVersion();

	}
}