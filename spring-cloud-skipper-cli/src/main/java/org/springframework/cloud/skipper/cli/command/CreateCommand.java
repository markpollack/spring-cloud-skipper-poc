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

package org.springframework.cloud.skipper.cli.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.cli.command.HelpExample;
import org.springframework.boot.cli.command.OptionParsingCommand;
import org.springframework.boot.cli.command.options.OptionHandler;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.cloud.skipper.client.ChartCreator;
import org.springframework.util.Assert;

/**
 * @author Mark Pollack
 */
public class CreateCommand extends OptionParsingCommand {

	private static final Logger log = LoggerFactory.getLogger(CreateCommand.class);

	@Autowired
	public CreateCommand(ChartCreator chartCreator) {
		this(new CreateOptionHandler(chartCreator));
	}

	public CreateCommand(CreateOptionHandler handler) {
		super("create", "Create a new chart with the given name", handler);
	}

	@Override
	public Collection<HelpExample> getExamples() {
		List<HelpExample> examples = new ArrayList<HelpExample>();
		examples.add(new HelpExample("Create a new chart named 'mychart'", "skipper chart mychart"));
		return examples;
	}

	/**
	 * {@link OptionHandler} for {@link CreateCommand}.
	 */
	static class CreateOptionHandler extends OptionHandler {

		private OptionSpec<String> starter;

		private final ChartCreator chartCreator;

		CreateOptionHandler(ChartCreator chartCreator) {
			this.chartCreator = chartCreator;
		}

		@Override
		protected void options() {

			// Note, not yet implemented.
			this.starter = option(Arrays.asList("starter", "p"), "the named Skipper starter scaffold")
					.withRequiredArg();
		}

		@Override
		protected ExitStatus run(OptionSet options) throws Exception {
			try {
				List<?> nonOptionArguments = new ArrayList<Object>(options.nonOptionArguments());
				Assert.isTrue(nonOptionArguments.size() <= 1, "Only the chart name may be specified");

				String name = (String) nonOptionArguments.get(0);
				log.info("Creating " + name);
				chartCreator.createChart(name);

				return ExitStatus.OK;
			}
			catch (Exception ex) {
				log.error("Error excuting create command", ex);
				return ExitStatus.ERROR;
			}
		}
	}
}
