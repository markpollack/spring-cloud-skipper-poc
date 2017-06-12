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

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import com.samskivert.mustache.Mustache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.client.YamlUtils;
import org.springframework.cloud.skipper.gilligan.repository.ManifestRepository;
import org.springframework.cloud.skipper.gilligan.repository.ReleaseRepository;
import org.springframework.cloud.skipper.gilligan.util.YmlMergeUtils;
import org.springframework.cloud.skipper.rpc.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

	private final ManifestRepository manifestRepository;

	@Autowired
	public ReleaseService(ReleaseRepository releaseRepository, ReleaseDeployer releaseDeployer,
			UpdateStrategy updateStrategy, ManifestRepository manifestRepository) {
		this.releaseRepository = releaseRepository;
		this.releaseDeployer = releaseDeployer;
		this.updateStrategy = updateStrategy;
		this.manifestRepository = manifestRepository;
	}

	public Release install(Release release, Chart chart, Config configValues) {

		// Resolve model values to render from the chart values file and command line
		// values.
		// ATM only take top level config values
		Properties model = mergeConfigValues(release.getChart().getConfigValues(), configValues);

		// Render yaml resources
		String manifest = createManifest(chart, model);
		release.setManifest(manifest);

		// Store in DB
		releaseRepository.save(release);

		// Store manifest in git?

		manifestRepository.store(release);

		// Deploy the application
		releaseDeployer.deploy(release);

		releaseDeployer.calculateStatus(release);

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
			throw new IllegalArgumentException(
					"Could not find release with name =" + releaseName + " version = " + version);
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
		String manifest = createManifest(chart, model);

		Release updatedRelease = new Release();
		updatedRelease.setName(name);
		updatedRelease.setChart(chart);
		updatedRelease.setConfig(configValues);
		Info info = new Info();
		info.setFirstDeployed(currentRelease.getInfo().getFirstDeployed());
		info.setLastDeployed(new Date());
		Status status = new Status();
		status.setStatusCode(StatusCode.UNKNOWN);
		info.setStatus(status);
		info.setDescription("Preparing upgrade");
		updatedRelease.setInfo(info);
		updatedRelease.setVersion(revision);
		updatedRelease.setManifest(manifest);

		// Store in DB
		releaseRepository.save(updatedRelease);

		return updateStrategy.update(currentRelease, updatedRelease);

	}

	public Release[] history(String name, int max) {
		Iterable<Release> releases = releaseRepository.findAll();

		List<Release> releaseList = new ArrayList<Release>();
		for (Release release : releases) {
			releaseList.add(release);
		}
		releaseList.sort(Comparator.comparing(Release::getVersion));
		return releaseList.toArray(new Release[releaseList.size()]);
	}

	public Release rollback(String name, int version) {
		// finds the previous release and prepares a new release object with the previous
		// release's configuration

		Release currentRelease = releaseRepository.findLatestRelease(name);
		int rollbackVersion = version;
		// default is to go back by one if no version specified.
		if (version == 0) {
			rollbackVersion = currentRelease.getVersion() - 1;
		}
		log.info("Rolling back " + name + " (current: v" + currentRelease.getVersion()
				+ ", target: v" + rollbackVersion + ")");

		Release previousRelease = releaseRepository.findByNameAndVersion(name, version);

		Release release = new Release();
		release.setName(name);
		release.setChart(previousRelease.getChart());
		release.setConfig(previousRelease.getConfig());
		Info info = new Info();
		info.setFirstDeployed(currentRelease.getInfo().getFirstDeployed());
		info.setLastDeployed(new Date());
		Status status = new Status();
		status.setStatusCode(StatusCode.UNKNOWN);
		info.setStatus(status);
		info.setDescription("Rollback to " + rollbackVersion);
		release.setInfo(info);
		release.setVersion(currentRelease.getVersion() + 1);
		release.setManifest(previousRelease.getManifest());

		// Store in DB
		releaseRepository.save(release);

		return updateStrategy.update(currentRelease, release);

	}

	public Deployment[] select(String selectorExpression) {
		Properties selectorProperties = new Properties();
		String[] selectorLines = StringUtils.commaDelimitedListToStringArray(selectorExpression);
		StringBuilder sb = new StringBuilder();
		for (String selectorLine : selectorLines) {
			sb.append(selectorLine + "\n");
		}
		try {
			selectorProperties.load(new StringReader(sb.toString()));
			return releaseRepository.select(propertiesToMap(selectorProperties));
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Could not parse selectorExpression", e);
		}
	}

	private static Map<String, String> propertiesToMap(Properties props) {
		HashMap<String, String> hm = new HashMap<String, String>();
		Enumeration<Object> e = props.keys();
		while (e.hasMoreElements()) {
			String s = (String) e.nextElement();
			hm.put(s, props.getProperty(s));
		}
		return hm;
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
		boolean configValuesEmpty = configValues != null && configValues.isConfigEmpty();
		boolean currentConfigEmpty = currentRelease.getConfig().isConfigEmpty();
		if (configValuesEmpty && (!currentConfigEmpty)) {
			// if ((configValues.isConfigEmpty()) &&
			// (!currentRelease.getConfig().isConfigEmpty())) {
			log.info("Copying values from " + currentRelease.getName() + " + v(" + currentRelease.getVersion() + ")");
			configValues = currentRelease.getConfig();
		}
	}

	/**
	 * Iterate overall the template files, replacing placeholders with model values. One
	 * string is returned that contain all the YAML of multiple files using YAML file
	 * delimiter.
	 * @param chart The top level chart that contains all templates where placeholders are
	 * to be replaced
	 * @param model The placeholder values.
	 * @return A YAML string containing all the templates with replaced values.
	 */
	private String createManifest(Chart chart, Properties model) {

		// Aggregate all valid manifests into one big doc.
		StringBuilder sb = new StringBuilder();
		// Top level templates.
		Template[] templates = chart.getTemplates();
		if (templates != null) {
			for (Template template : templates) {
				String templateAsString = new String(template.getData());
				com.samskivert.mustache.Template mustacheTemplate = Mustache.compiler().compile(templateAsString);
				sb.append("\n---\n# Source: " + template.getName() + "\n");
				sb.append(mustacheTemplate.execute(model));
			}
		}

		if (chart.getDependencies() != null) {
			Chart[] charts = chart.getDependencies();
			for (Chart subChart : charts) {
				sb.append(createManifest(subChart, model));
			}
		}
		// sb.append(createManifest())

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
		// TODO investigate
		Properties commandLineOverrideProperties;
		if (commandLineConfigValues == null) {
			commandLineOverrideProperties = new Properties();
		}
		else {
			commandLineOverrideProperties = YamlUtils.getProperties(commandLineConfigValues.getRaw());
		}
		Properties templateVariables;
		if (templateConfigValue == null) {
			templateVariables = new Properties();
		}
		else {
			templateVariables = YamlUtils.getProperties(templateConfigValue.getRaw());
		}

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
