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

package org.springframework.cloud.skipper.cli;

import org.springframework.boot.cli.command.CommandRunner;
import org.springframework.boot.cli.command.core.HelpCommand;
import org.springframework.boot.cli.command.core.HintCommand;
import org.springframework.cloud.skipper.cli.command.CreateCommand;
import org.springframework.cloud.skipper.cli.command.VersionCommand;
import org.springframework.cloud.skipper.client.ChartCreator;
import org.springframework.cloud.skipper.client.SkipperClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Mark Pollack
 */
@Configuration
@Import(SkipperClientConfiguration.class)
public class SkipperCliConfiguration {

	@Bean
	public CommandRunner commandRunner(ChartCreator chartCreator) {
		CommandRunner runner = new CommandRunner("skipper");
		runner.addCommand(new HelpCommand(runner));
		runner.addCommand(new HintCommand(runner));
		runner.addCommand(new CreateCommand(chartCreator));
		runner.addCommand(new VersionCommand());
		runner.setOptionCommands(HelpCommand.class);
		runner.setHiddenCommands(HintCommand.class);
		return runner;
	}
}
