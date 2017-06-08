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

package org.springframework.cloud.skipper.gilligan.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used from
 * https://github.com/cobbzilla/merge-yml/blob/master/src/main/java/org/cobbzilla/util/yml/YmlMerger.java
 * @author Mark Pollack
 */
public class YmlMergeUtils {

	private static final Logger log = LoggerFactory.getLogger(YmlMergeUtils.class);

	public YmlMergeUtils() {
	}

	@SuppressWarnings("unchecked")
	public void merge(Map<String, Object> mergedResult, Map<String, Object> yamlContents) {

		if (yamlContents == null) {
			return;
		}

		for (String key : yamlContents.keySet()) {

			Object yamlValue = yamlContents.get(key);
			if (yamlValue == null) {
				addToMergedResult(mergedResult, key, yamlValue);
				continue;
			}

			Object existingValue = mergedResult.get(key);
			if (existingValue != null) {
				if (yamlValue instanceof Map) {
					if (existingValue instanceof Map) {
						merge((Map<String, Object>) existingValue, (Map<String, Object>) yamlValue);
					}
					else if (existingValue instanceof String) {
						throw new IllegalArgumentException(
								"Cannot merge complex element into a simple element: " + key);
					}
					else {
						throw unknownValueType(key, yamlValue);
					}
				}
				else if (yamlValue instanceof List) {
					mergeLists(mergedResult, key, yamlValue);

				}
				else if (yamlValue instanceof String || yamlValue instanceof Boolean || yamlValue instanceof Double
						|| yamlValue instanceof Integer) {
					log.info("overriding value of " + key + " with value " + yamlValue);
					addToMergedResult(mergedResult, key, yamlValue);

				}
				else {
					throw unknownValueType(key, yamlValue);
				}

			}
			else {
				if (yamlValue instanceof Map || yamlValue instanceof List || yamlValue instanceof String
						|| yamlValue instanceof Boolean || yamlValue instanceof Integer
						|| yamlValue instanceof Double) {
					log.info("adding new key->value: " + key + "->" + yamlValue);
					addToMergedResult(mergedResult, key, yamlValue);
				}
				else {
					throw unknownValueType(key, yamlValue);
				}
			}
		}
	}

	private IllegalArgumentException unknownValueType(String key, Object yamlValue) {
		final String msg = "Cannot merge element of unknown type: " + key + ": " + yamlValue.getClass().getName();
		log.error(msg);
		return new IllegalArgumentException(msg);
	}

	private Object addToMergedResult(Map<String, Object> mergedResult, String key, Object yamlValue) {
		return mergedResult.put(key, yamlValue);
	}

	@SuppressWarnings("unchecked")
	private void mergeLists(Map<String, Object> mergedResult, String key, Object yamlValue) {
		if (!(yamlValue instanceof List && mergedResult.get(key) instanceof List)) {
			throw new IllegalArgumentException("Cannot merge a list with a non-list: " + key);
		}

		List<Object> originalList = (List<Object>) mergedResult.get(key);
		originalList.addAll((List<Object>) yamlValue);
	}

}
