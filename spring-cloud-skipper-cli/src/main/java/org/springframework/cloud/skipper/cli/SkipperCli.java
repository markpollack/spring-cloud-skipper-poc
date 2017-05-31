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

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.cli.command.CommandRunner;
import org.springframework.boot.loader.tools.LogbackInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SkipperCli {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", Boolean.toString(true));
		LogbackInitializer.initialize();

		/*
		 * public static void main(String[] args) {
		 * SpringApplication.run(SkipperApplication.class, args); }
		 */

		SpringApplicationBuilder builder = new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF)
				.sources(SkipperCli.class);
		SpringApplication springApplication = builder.build();
		ConfigurableApplicationContext ctx = springApplication.run();
		CommandRunner runner = ctx.getBean(CommandRunner.class);

		// CommandRunner runner = new CommandRunner("skipper");
		// runner.addCommand(new HelpCommand(runner));
		// runner.addCommand(new HintCommand(runner));
		// runner.addCommand(new CreateCommand(runner));
		// runner.addCommand(new VersionCommand());
		// runner.setOptionCommands(HelpCommand.class);
		// runner.setHiddenCommands(HintCommand.class);

		int exitCode = runner.runAndHandleErrors(args);
		System.exit(exitCode);
		// if (exitCode != 0) {
		// // If successful, leave it to run in case it's a server app
		// System.exit(exitCode);
		// }
	}
}
