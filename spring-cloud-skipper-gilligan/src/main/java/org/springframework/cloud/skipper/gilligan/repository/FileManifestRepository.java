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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.cloud.skipper.rpc.domain.Release;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * @author Mark Pollack
 */
@Component
public class FileManifestRepository implements ManifestRepository {

	private final String homeDir = System.getProperty("user.home");

	@Override
	public void store(Release release) {
		final File releaseDir = new File(homeDir + File.separator + "manifests" + File.separator
				+ release.getName() + "-v"
				+ release.getVersion());
		releaseDir.mkdirs();
		File manifestFile = new File(releaseDir, "manifest.yml");
		writeText(manifestFile, release.getManifest());

	}

	private void writeText(final File target, final String body) {
		try (OutputStream stream = new FileOutputStream(target, false)) {
			StreamUtils.copy(body, Charset.forName("UTF-8"), stream);
		}
		catch (final Exception e) {
			throw new IllegalStateException("Cannot write file " + target, e);
		}
	}
}
