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

package org.springframework.cloud.skipper.gilligan.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.samskivert.mustache.Mustache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.client.YamlUtils;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.gilligan.util.YmlMergeUtils;
import org.springframework.cloud.skipper.rpc.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Performs release operations based on method signatures independent of skipper.rpc data
 * types
 *
 * @author Mark Pollack
 */
@Service
public class ReleaseService {

	private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);

	private final ReleaseRepository releaseRepository;

	private final ReleaseDeployer releaseDeployer;

	private final UpdateStrategy updateStrategy;

	@Autowired
	public ReleaseService(ReleaseRepository releaseRepository, ReleaseDeployer releaseDeployer,
			UpdateStrategy updateStrategy) {
		this.releaseRepository = releaseRepository;
		this.releaseDeployer = releaseDeployer;
		this.updateStrategy = updateStrategy;
	}

	public Release install(Release release, Template[] templates, Config configValues) {

		// Resolve model values to render from the chart values file and command line
		// values.
		Properties model = mergeConfigValues(release.getChart().getConfigValues(), configValues);

		// Render yaml resources
		String manifest = createManifest(templates, model);
		release.setManifest(manifest);

		// Store in DB
		releaseRepository.save(release);

		// Store manifest in git?

		// Deploy the application
		releaseDeployer.deploy(release);

		return release;

	}

	/**
	 * Return the status of the specified release
	 * @param releaseName name of the release
	 * @param version version of the release, find the latest release if 0
	 * @return the status of the specified release
	 */
	public Release status(String releaseName, int version) {
		Release requestedRelease = null;
		if (version <= 0) {
			requestedRelease = releaseRepository.findLatestRelease(releaseName);
		}
		else {
			requestedRelease = releaseRepository.findByNameAndVersion(releaseName, version);
		}
		if (requestedRelease == null) {
			throw new IllegalArgumentException("Could not find release with name " + releaseName);
		}

		Assert.notNull(requestedRelease.getInfo(),
				"Release Info is missing for release name = " + releaseName + " version = " + version);
		Assert.notNull(requestedRelease.getChart(),
				"Release Chart is missing for release name = " + releaseName + " version = " + version);
		releaseDeployer.calculateStatus(requestedRelease);

		return requestedRelease;

	}

	public Release update(String name, Chart chart, Config configValues, boolean resetValues, boolean reuseValues) {
		// No locking...
		Release currentRelease = releaseRepository.findLatestRelease(name);

		// Detemine if configValues should be updated to the current release's values
		updateValues(chart, configValues, resetValues, reuseValues, currentRelease);

		int revision = currentRelease.getVersion() + 1;

		Properties model = mergeConfigValues(chart.getConfigValues(), configValues);

		// Render yaml resources
		String manifest = createManifest(chart.getTemplates(), model);

		Release updatedRelease = new Release();
		updatedRelease.setName(name);
		updatedRelease.setChart(chart);
		updatedRelease.setConfig(configValues);
		Info info = new Info();
		info.setFirstDeployed(currentRelease.getInfo().getFirstDeployed());
		info.setLastDeployed(new Date());
		info.getStatus().setStatusCode(StatusCode.UNKNOWN);
		info.setDescription("Preparing upgrade");
		updatedRelease.setInfo(info);
		updatedRelease.setVersion(revision);
		updatedRelease.setManifest(manifest);

		// Store in DB
		releaseRepository.save(updatedRelease);

		updateStrategy.update(currentRelease, updatedRelease);

		return null;
	}

	/**
	 * Somewhat convoluted implementation to set the passed Config for update to be either
	 * 1) those of the original chart if resetValues = true, 2) copy over previous values
	 * if resuseValuses = true, and otherwise set to the currently deployed release values
	 * if the Config for update is empty.
	 * @param chart
	 * @param configValues
	 * @param resetValues
	 * @param reuseValues
	 * @param currentRelease
	 */
	private void updateValues(Chart chart, Config configValues, boolean resetValues, boolean reuseValues,
			Release currentRelease) {
		if (resetValues) {
			log.info("Reset values to the chart's orginal version.");
			return;
		}
		// If the ReuseValues flag is set, we always copy the old values over the new
		// config's values.
		if (reuseValues) {
			log.info("Reusing the old release's values");

			// second yml overwrites first yml argument
			String oldMergedYml = mergeYml(currentRelease.getChart().getConfigValues().getRaw(),
					currentRelease.getConfig().getRaw());
			Config oldConfig = new Config();
			oldConfig.setRaw(oldMergedYml);
			chart.setConfigValues(oldConfig);
		}

		// If request config Values is empty, but current.Config is not, copy current into
		// the request.
		if ((configValues.isConfigEmpty()) && (!currentRelease.getConfig().isConfigEmpty())) {
			log.info("Copying values from " + currentRelease.getName() + " + v(" + currentRelease.getVersion() + ")");
			configValues = currentRelease.getConfig();
		}
	}

	/**
	 * Iterate overall the template files, replacing placeholders with model values. One
	 * string is returned that contain all the YAML of multiple files using YAML file
	 * delimiter.
	 * @param templates YAML with placeholders to replace
	 * @param model The placeholder values.
	 * @return A YAML string containing all the templates with replaced values.
	 */
	private String createManifest(Template[] templates, Properties model) {
		// Aggregate all valid manifests into one big doc.
		StringBuilder sb = new StringBuilder();
		for (Template template : templates) {
			String templateAsString = new String(template.getData());
			com.samskivert.mustache.Template mustacheTemplate = Mustache.compiler().compile(templateAsString);
			sb.append("\n---\n# Source: " + template.getName() + "\n");
			sb.append(mustacheTemplate.execute(model));
		}
		return sb.toString();
	}

	/**
	 * Will merge the properties, derived from YAML formst, contained in
	 * commandLineConfigValues and templateConfigValue, giving preference to
	 * commandLineConfigValues. Assumes that the YAML is stored as "raw" data in the
	 * Config object. If the "raw" data is empty or null, an empty property object is
	 * returned.
	 *
	 * @param templateConfigValue YAML data defined in the template.yaml file
	 * @param commandLineConfigValues YAML data passed at the application runtime
	 * @return A Properties object that is the merger of both Config objects,
	 * commandLineConfig values override values in templateConfig.
	 */
	private Properties mergeConfigValues(Config templateConfigValue, Config commandLineConfigValues) {
		Assert.notNull(commandLineConfigValues, "Config object for commandLine Config Values can't be null");
		Assert.notNull(templateConfigValue, "Config object for template Config Values can't be null");

		Properties commandLineOverrideProperties = YamlUtils.getProperties(commandLineConfigValues.getRaw());
		Properties templateVariables = YamlUtils.getProperties(templateConfigValue.getRaw());

		Properties model = new Properties();
		model.putAll(templateVariables);
		model.putAll(commandLineOverrideProperties);
		return model;
	}

	/**
	 * Values in the second, ovrerride those in the first.
	 * @param firstYml
	 * @param secondYml
	 * @return merged yml
	 */
	private String mergeYml(String firstYml, String secondYml) {
		final DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(options);

		Map<String, Object> mergedResult = new LinkedHashMap<String, Object>();
		Map<String, Object> firstYmlMap = (Map<String, Object>) yaml.load(firstYml);
		YmlMergeUtils mergeUtils = new YmlMergeUtils();

		mergeUtils.merge(mergedResult, firstYmlMap);

		Map<String, Object> secondYmlMap = (Map<String, Object>) yaml.load(secondYml);
		mergeUtils.merge(mergedResult, secondYmlMap);

		return yaml.dump(mergedResult);
	}

}
