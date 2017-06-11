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
import org.springframework.cloud.skipper.client.GilliganService;
import org.springframework.cloud.skipper.rpc.HistoryResponse;
import org.springframework.cloud.skipper.rpc.domain.Release;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * @author Mark Pollack
 */
@Component
public class HistoryCommand implements CommandMarker {

	private final String maxHelp = "maximum length of the revision list returned";

	@Autowired
	private GilliganService gilliganService;

	@CliCommand("skipper history")
	public String history(
			@CliOption(key = { "", "releaseName" }, help = "Release name", mandatory = true) String releaseName,
			@CliOption(key = "max", mandatory = false, help = maxHelp, unspecifiedDefaultValue = "0") int max) {
		HistoryResponse historyRepsonse = gilliganService.history(releaseName, max);
		return format(historyRepsonse.getReleases());
	}

	private String format(Release[] releases) {
		return releases.toString();
	}
}
