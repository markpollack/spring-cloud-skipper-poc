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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * @author Mark Pollack
 */
public abstract class YamlUtils {

	public static Properties getProperties(File file) {
		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
		Resource resource = new FileSystemResource(file);
		yaml.setResources(resource);
		yaml.afterPropertiesSet();
		Properties values = yaml.getObject();
		return values;
	}

	public static Properties getProperties(String templateString) {
		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();

		Properties values;
		if (StringUtils.hasText(templateString)) {
			try (InputStream is = new ByteArrayInputStream(templateString.getBytes())) {
				yaml.setResources(new InputStreamResource(is));
				yaml.afterPropertiesSet();
				values = yaml.getObject();
			}
			catch (Exception e) {
				throw new IllegalArgumentException(
						"Could not convert YAML to properties object from string " + templateString, e);
			}
		}
		else {
			values = new Properties();
		}
		return values;

	}
}
