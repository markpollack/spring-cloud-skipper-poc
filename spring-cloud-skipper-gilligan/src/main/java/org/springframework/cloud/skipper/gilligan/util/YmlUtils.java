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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.cloud.skipper.rpc.domain.Deployment;
import org.springframework.cloud.skipper.rpc.domain.DeploymentKind;

/**
 * @author Mark Pollack
 */
public class YmlUtils {

	public static List<Deployment> unmarshallDeployments(String manifests) {

		List<DeploymentKind> deploymentKindList = new ArrayList<>();
		YAMLMapper mapper = new YAMLMapper();
		// TypeReference<DeploymentKind> typeReference = new
		// TypeReference<DeploymentKind>();
		try {
			MappingIterator<DeploymentKind> it = mapper.readerFor(DeploymentKind.class).readValues(manifests);
			while (it.hasNextValue()) {
				DeploymentKind deploymentKind = it.next();
				deploymentKindList.add(deploymentKind);
			}

		}
		catch (IOException e) {
			throw new IllegalArgumentException("Can't parse Release manifest YAML", e);
		}

		List<Deployment> deploymentList = deploymentKindList.stream().map(DeploymentKind::getDeployment)
				.collect(Collectors.toList());
		return deploymentList;
	}
}
