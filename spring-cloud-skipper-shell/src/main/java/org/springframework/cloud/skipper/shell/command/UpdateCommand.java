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
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * @author Mark Pollack
 */
@Component
public class UpdateCommand implements CommandMarker {

	private static final String reuseValuesHelp = "Reuse the values from the last release, "
			+ "ignored if resetValues is set";

	private static final String resetValuesHelp = "Ignore stored values, resetting to default values.";

	@Autowired
	private GilliganService gilliganService;

	@CliCommand("skipper update")
	public String install(@CliOption(mandatory = true, key = { "", "chartPath" }, help = "Chart path") String chartPath,
			@CliOption(mandatory = true, key = "releaseName", help = "Release name") String releaseName,
			@CliOption(key = "version", help = "Release version", unspecifiedDefaultValue = "0", mandatory = false) Integer releaseVersion,
			@CliOption(key = "reuseValues", help = reuseValuesHelp, unspecifiedDefaultValue = "false", mandatory = false) Boolean reuseValues,
			@CliOption(key = "resetValues", help = resetValuesHelp, unspecifiedDefaultValue = "false", mandatory = false) Boolean resetValues) {

		gilliganService.upgrade(chartPath, releaseName, releaseVersion, reuseValues, resetValues);
		return "Release.getStatus";
	}
}
