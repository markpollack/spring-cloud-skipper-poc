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

package org.springframework.cloud.skipper.gilligan.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.gilligan.util.YmlUtils;
import org.springframework.cloud.skipper.rpc.domain.Deployment;
import org.springframework.cloud.skipper.rpc.domain.Release;

/**
 * @author Mark Pollack
 */
public class ReleaseRepositoryImpl implements CustomReleaseRepository {

	@Autowired
	private ReleaseRepository releaseRepository;

	@Override
	public Release findLatestRelease(String releaseName) {
		Iterable<Release> releases = releaseRepository.findAll();
		int lastVersion = 0;
		Release latestRelease = null;
		for (Release release : releases) {
			// Find the latest release
			if (release.getName().equals(releaseName)) {
				if (release.getVersion() > lastVersion) {
					lastVersion = release.getVersion();
					latestRelease = release;
				}
			}
		}
		return latestRelease;
	}

	@Override
	public Release findByNameAndVersion(String releaseName, int version) {
		Iterable<Release> releases = releaseRepository.findAll();

		Release matchingRelease = null;
		for (Release release : releases) {
			if (release.getName().equals(releaseName) && release.getVersion() == version) {
				matchingRelease = release;
				break;
			}
		}
		return matchingRelease;
	}

	@Override
	public Deployment[] select(Map<String, String> selectorMap) {
		List<Deployment> matchingDeployments = new ArrayList<>();

		Iterable<Release> releases = releaseRepository.findAll();
		for (Release release : releases) {
			List<Deployment> unmarshalledDeployments = YmlUtils.unmarshallDeployments(release.getManifest());
			for (Deployment unmarshalledDeployment : unmarshalledDeployments) {
				boolean match = false;
				Map<String, String> labels = unmarshalledDeployment.getLabels();
				if (labels != null) {
					for (Map.Entry<String, String> selectorEntry : selectorMap.entrySet()) {
						if (labels.containsKey(selectorEntry.getKey())) {
							if (labels.get(selectorEntry.getKey()).equals(selectorEntry.getValue())) {
								match = true;
							}
							else {
								match = false;
								break;
							}
						}
					}
				}
				if (match) {
					matchingDeployments.add(unmarshalledDeployment);
				}
			}
		}
		return matchingDeployments.toArray(new Deployment[matchingDeployments.size()]);
	}
}
