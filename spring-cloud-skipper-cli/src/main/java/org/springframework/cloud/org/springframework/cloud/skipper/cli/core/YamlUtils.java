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
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Mark Pollack
 */
public abstract class YamlUtils {

	static Properties getProperties(File file) {
		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
		Resource resource = new FileSystemResource(file);
		yaml.setResources(resource);
		yaml.afterPropertiesSet();
		Properties values = yaml.getObject();
		return values;
	}
}