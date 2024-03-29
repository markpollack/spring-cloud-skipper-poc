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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.skipper.SkipperConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Mark Pollack
 */
@Configuration
@EnableConfigurationProperties(SkipperConfigurationProperties.class)
public class SkipperClientConfiguration {

	@Bean
	public Home home(SkipperConfigurationProperties skipperConfigurationProperties) {
		return new Home(skipperConfigurationProperties.getHome());
	}

	@Bean
	@ConditionalOnMissingBean
	public TemplateRenderer templateRenderer(Environment environment) {
		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment, "spring.mustache.");
		boolean cache = resolver.getProperty("cache", Boolean.class, true);
		TemplateRenderer templateRenderer = new TemplateRenderer();
		templateRenderer.setCache(cache);
		return templateRenderer;
	}

	@Bean
	public ChartCreator chartCreator(Home home, TemplateRenderer templateRenderer) {
		return new ChartCreator(home, templateRenderer);
	}

	@Bean
	public GilliganClient gilliganClient() {
		return new GilliganClient("http://localhost:8080/skipper");
	}

	@Bean
	public PackageManager packageManager() {
		return new PackageManager();
	}

	@Bean
	public GilliganService installService(GilliganClient gilliganClient, ChartResolver chartResolver,
			ChartLoader chartLoader) {
		return new GilliganService(gilliganClient, chartResolver, chartLoader);
	}

	@Bean
	public ChartResolver chartResolver() {
		return new ChartResolver();
	}

	@Bean
	public ChartLoader chartLoader() {
		return new ChartLoader();
	}

}
