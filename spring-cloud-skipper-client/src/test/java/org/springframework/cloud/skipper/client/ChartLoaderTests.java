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
import java.net.URL;

import org.junit.Test;

import org.springframework.cloud.skipper.rpc.domain.Chart;
import org.springframework.cloud.skipper.rpc.domain.Metadata;
import org.springframework.cloud.skipper.rpc.domain.Template;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 */
public class ChartLoaderTests {

	@Test
	public void chartLoading() {
		ChartLoader chartLoader = new ChartLoader();
		URL url = this.getClass().getResource("/log");
		assertThat(url).isNotNull();
		File file = new File(url.getFile());
		assertThat(file).exists();
		Chart chart = chartLoader.load(file.getAbsolutePath());
		Metadata metadata = chart.getMetadata();
		assertThat(metadata.getName()).isEqualTo("log");
		assertThat(metadata.getVersion()).isEqualTo("1.2.0");
		assertThat(metadata.getDescription()).isEqualTo("Logs payload to the console");
		assertThat(metadata.getKeywords()).contains("logging");
		assertThat(metadata.getHome()).isEqualTo("https://github.com/spring-cloud-stream-app-starters/log");
		assertThat(chart.getConfigValues().getRaw()).contains("1024m");
		Template template = chart.getTemplates()[0];
		assertThat(template.getName()).isEqualTo("log.yml");
		assertThat(template.getData()).isNotEmpty();
	}
}
