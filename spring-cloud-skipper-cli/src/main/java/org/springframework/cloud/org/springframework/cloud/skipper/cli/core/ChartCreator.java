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

package org.springframework.cloud.org.springframework.cloud.skipper.cli.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import jline.internal.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

/**
 * @author Mark Pollack
 */
public class ChartCreator {

	final ResourceLoader resourceLoader = new DefaultResourceLoader();

	private final TemplateRenderer templateRenderer;

	private final Home home;

	@Autowired
	public ChartCreator(final Home home, final TemplateRenderer templateRenderer) {
		this.home = home;
		this.templateRenderer = templateRenderer;
	}

	public void createChart(final String name) {
		Log.info("Creating " + name);
		final File rootDir;
		rootDir = new File(home.getHomeDirectory());
		rootDir.mkdirs();
		final File chartDir = new File(rootDir, name);
		chartDir.mkdirs();

		// Save the chart file
		File chartFile = new File(chartDir, ChartKeys.ChartFileName);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", name);
		writeText(chartFile, this.templateRenderer.process(ChartKeys.ChartFileName, model));

		// Save the values file
		File valuesFile = new File(chartDir, ChartKeys.ValuesFileName);
		model = new HashMap<String, Object>();
		model.put("name", name);
		writeText(valuesFile, this.templateRenderer.process(ChartKeys.ValuesFileName, model));

		// Save the deployment file in the templates directory
		File templatesDir = new File(chartDir, ChartKeys.TemplatesDir);
		templatesDir.mkdirs();
		File deploymentFile = new File(templatesDir, ChartKeys.DeploymentFileName);
		model = new HashMap<String, Object>();
		// model.put("name", name);
		// model.put("values", values);
		writeText(deploymentFile, this.templateRenderer.process(ChartKeys.DeploymentFileName, model));

		// Create the charts directory
		File chartsDir = new File(chartDir, ChartKeys.ChartsDir);
		chartsDir.mkdirs();
	}

	private void writeText(final File target, final String body) {
		try (OutputStream stream = new FileOutputStream(target)) {
			StreamUtils.copy(body, Charset.forName("UTF-8"), stream);
		}
		catch (final Exception e) {
			throw new IllegalStateException("Cannot write file " + target, e);
		}
	}
}
