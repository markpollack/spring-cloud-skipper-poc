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

package org.springframework.cloud.skipper.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.bind.YamlConfigurationFactory;
import org.springframework.cloud.skipper.rpc.domain.Chart;
import org.springframework.cloud.skipper.rpc.domain.Config;
import org.springframework.cloud.skipper.rpc.domain.Metadata;
import org.springframework.cloud.skipper.rpc.domain.Template;
import org.springframework.core.io.FileSystemResource;

/**
 * @author Mark Pollack
 */
public class ChartLoader {

	public Chart load(String path) {

		List<File> files;
		try (Stream<Path> paths = Files.walk(Paths.get(path))) {
			files = paths.map(i -> i.toAbsolutePath().toFile()).collect(Collectors.toList());
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Could not process files in path " + path, e);
		}

		Chart chart = new Chart();
		List<Template> templates = new ArrayList<>();
		for (File file : files) {
			if (file.getName().equalsIgnoreCase("Chart.yaml") || file.getName().equalsIgnoreCase("chart.yml")) {
				chart.setMetadata(loadChartMetadata(file));
			}
			if (file.getName().equalsIgnoreCase("values.yaml") || file.getName().equalsIgnoreCase("values.yml")) {
				chart.setConfigValues(loadConfigValues(file));
			}
			if (file.getAbsoluteFile().toString().contains("templates/")) {
				templates.add(loadTemplate(file));

			}
		}

		chart.setTemplates(templates.toArray(new Template[templates.size()]));

		return chart;
	}

	private Config loadConfigValues(File file) {
		Config config = new Config();
		try {
			config.setRaw(new String(Files.readAllBytes(file.toPath()), "UTF-8"));
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Could read values file " + file.getAbsoluteFile(), e);
		}

		return config;
	}

	private Template loadTemplate(File file) {
		Template template = new Template();
		template.setName(file.getName());
		try {
			template.setData(new String(Files.readAllBytes(file.toPath()), "UTF-8"));
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Could read template file " + file.getAbsoluteFile(), e);
		}
		return template;
	}

	private Metadata loadChartMetadata(File file) {
		YamlConfigurationFactory<Metadata> factory = new YamlConfigurationFactory<Metadata>(Metadata.class);
		factory.setResource(new FileSystemResource(file));
		try {
			return factory.getObject();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Exception processing yaml file " + file.getName(), e);
		}

	}

}
