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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.gilligan.repository.ManifestRepository;
import org.springframework.cloud.skipper.rpc.domain.Release;
import org.springframework.stereotype.Component;

/**
 * @author Mark Pollack
 */
@Component
public class SimpleUpdateStrategy implements UpdateStrategy {

	private final ReleaseDeployer releaseDeployer;

	private final ManifestRepository manifestRepository;

	@Autowired
	public SimpleUpdateStrategy(ReleaseDeployer releaseDeployer, ManifestRepository manifestRepository) {
		this.releaseDeployer = releaseDeployer;
		this.manifestRepository = manifestRepository;
	}

	@Override
	public Release update(Release currentRelease, Release updatedRelease) {

		releaseDeployer.deploy(updatedRelease);

		// Do something fancy in terms of health detection of new release.

		manifestRepository.store(updatedRelease);

		releaseDeployer.undeploy(currentRelease);

		releaseDeployer.calculateStatus(updatedRelease);

		return updatedRelease;

	}
}
