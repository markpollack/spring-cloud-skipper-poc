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

import org.springframework.util.StringUtils;

/**
 * Order of resolution: current working directory,
 *
 * TODP: if path is absolute or begins with * '.', error out here ; chart repos in
 * $HELM_HOME ; URL
 * @author Mark Pollack
 */
public class ChartResolver {

	public String resolve(String chartName) {
		String name = StringUtils.trimAllWhitespace(chartName);
		File file = new File(name);
		if (file.exists()) {
			if (file.isDirectory()) {
				return name;
			}
		}
		throw new IllegalArgumentException("Can not locate chart directory " + name);
	}
}
