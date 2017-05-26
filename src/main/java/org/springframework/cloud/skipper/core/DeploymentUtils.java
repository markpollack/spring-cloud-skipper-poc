/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.cloud.skipper.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * @author Mark Pollack
 */
public abstract class DeploymentUtils {

	public static Deployment load(String resourceLocation, String... propertyFiles) {
		Map<String, Object> properties = new HashMap<>();
		//properties.put("spring.config.location","classpath:/log/deployments/log.yml,classpath:/log/values.properties");
		if (propertyFiles == null) {
			properties.put("spring.config.location", resourceLocation);
		} else {
			String listOfPropertyFiles = StringUtils.arrayToCommaDelimitedString(propertyFiles);
			properties.put("spring.config.location", resourceLocation + "," + listOfPropertyFiles);
		}

		SpringApplicationBuilder builder = new SpringApplicationBuilder().properties(properties)
				.bannerMode(Banner.Mode.OFF).sources(SimpleApp.class);
		SpringApplication springApplication = builder.build();
		ConfigurableApplicationContext ctx = springApplication.run();
		Deployment deployment = ctx.getBean(Deployment.class);
		if (deployment == null) {
			throw new RuntimeException("Could not find resource: " + resourceLocation);
		}
		return deployment;
	}
}
