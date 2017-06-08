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

package org.springframework.cloud.skipper.rpc.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.util.StringUtils;

/**
 * Config supplies values to the parametrizable templates of a chart.
 *
 * On the client side, it is a string of unparsed YAMl values.
 * @author Mark Pollack
 */
public class Config {

	private String raw;

	private Map<String, String> values;

	public Config() {
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	@JsonIgnore
	public boolean isConfigEmpty() {
		if (values == null || StringUtils.isEmpty(raw)) {
			return true;
		}
		return false;
	}
}
