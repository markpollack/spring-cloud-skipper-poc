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
package org.springframework.cloud.skipper.client;

import java.util.List;

import org.springframework.cloud.skipper.client.old.ReleasePocV1;

/**
 * @author Mark Pollack
 */
public interface Skipper {

	void install(String releaseName, String deploymentResource);

	String status(String releaseName);

	ReleasePocV1 describe(String releaseName);

	List<ReleasePocV1> history(String releaseName);

	String upgrade(String releaseName, String deploymentResource);

	String rollback(String releaseName, String version);

	void delete(String releaseName);
}
