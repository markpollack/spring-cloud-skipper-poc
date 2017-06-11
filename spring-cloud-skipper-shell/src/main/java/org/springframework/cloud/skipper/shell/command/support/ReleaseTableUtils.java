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

package org.springframework.cloud.skipper.shell.command.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.cloud.skipper.rpc.domain.Release;
import org.springframework.shell.table.*;

/**
 * @author Mark Pollack
 */
public abstract class ReleaseTableUtils {

	public static Table format(Release[] releases) {
		List<ReleaseSummary> releaseSummaries = convertToSummary(releases);
		LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
		headers.put("version", "Version");
		headers.put("lastUpdated", "Updated");
		headers.put("statusCode", "Status");
		headers.put("chartName", "Chart");
		headers.put("description", "Description");
		BeanListTableModel<ReleaseSummary> model = new BeanListTableModel<>(releaseSummaries, headers);
		return ReleaseTableUtils.applyStyle(new TableBuilder(model)).build();
	}

	private static TableBuilder applyStyle(TableBuilder builder) {
		builder.addOutlineBorder(BorderStyle.fancy_double)
				.paintBorder(BorderStyle.air, BorderSpecification.INNER_VERTICAL).fromTopLeft().toBottomRight()
				.paintBorder(BorderStyle.fancy_light, BorderSpecification.INNER_VERTICAL).fromTopLeft().toBottomRight()
				.addHeaderBorder(BorderStyle.fancy_double).on(CellMatchers.row(0))
				.addAligner(SimpleVerticalAligner.middle).addAligner(SimpleHorizontalAligner.center);
		return Tables.configureKeyValueRendering(builder, " = ");
	}

	private static List<ReleaseSummary> convertToSummary(Release[] releases) {
		List<ReleaseSummary> releaseSummaryList = new ArrayList<>();
		for (int i = 0; i < releases.length; i++) {
			Release release = releases[i];
			ReleaseSummary releaseSummary = new ReleaseSummary();
			releaseSummary.setVersion(release.getVersion());
			releaseSummary.setLastUpdated(release.getInfo().getLastDeployed());
			releaseSummary.setStatusCode(release.getInfo().getStatus().getStatusCode());
			releaseSummary.setChartName(release.getName());
			releaseSummary.setDescription(release.getInfo().getDescription());
			releaseSummaryList.add(releaseSummary);
		}
		return releaseSummaryList;
	}
}
