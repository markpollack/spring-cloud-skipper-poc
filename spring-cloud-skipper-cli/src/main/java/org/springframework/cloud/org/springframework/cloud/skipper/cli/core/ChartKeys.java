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

/**
 * @author Mark Pollack
 */
public abstract class ChartKeys {

	// ChartfileName is the default Chart file name.
	public static final String ChartFileName = "Chart.yaml";

	// ValuesfileName is the default values file name.
	public static final String ValuesFileName = "values.yaml";

	// TemplatesDir is the relative directory name for templates.
	public static final String TemplatesDir = "templates";

	// ChartsDir is the relative directory name for charts dependencies.
	public static final String ChartsDir = "charts";

	// // IgnorefileName is the name of the Skipper ignore file.
	// public static final String IgnorefileName = ".skipperignore";
	// // IngressFileName is the name of the example ingress file.
	// public static final String IngressFileName = "ingress.yaml";
	// DeploymentName is the name of the example deployment file.
	public static final String DeploymentFileName = "deployment.yaml";
	// // ServiceName is the name of the example service file.
	// public static final String ServiceName = "service.yaml";
	// // NotesName is the name of the example NOTES.txt file.
	// public static final String NotesName = "NOTES.txt";
	// // HelpersName is the name of the example NOTES.txt file.
	// public static final String HelpersName = "_helpers.tpl";
}
