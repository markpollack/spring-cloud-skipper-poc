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

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * @author Dave Syer
 */
public class TemplateRenderer {

	public static final String prefix = "classpath:/templates/";

	private static final Logger log = LoggerFactory.getLogger(TemplateRenderer.class);

	private final Compiler mustache;

	private final ConcurrentMap<String, Template> templateCaches = new ConcurrentReferenceHashMap<>();

	private boolean cache = true;

	public TemplateRenderer(final Compiler mustache) {
		this.mustache = mustache;
	}

	public TemplateRenderer() {
		this(mustacheCompiler());
	}

	private static Compiler mustacheCompiler() {
		return Mustache.compiler().withLoader(mustacheTemplateLoader());
	}

	private static TemplateLoader mustacheTemplateLoader() {
		final ResourceLoader resourceLoader = new DefaultResourceLoader();
		final Charset charset = Charset.forName("UTF-8");
		return (String name) -> {
			return new InputStreamReader(resourceLoader.getResource(prefix + name).getInputStream(), charset);
		};
	}

	public boolean isCache() {
		return cache;
	}

	public void setCache(final boolean cache) {
		this.cache = cache;
	}

	public String process(final String name, final Map<String, ?> model) {
		try {
			final Template template = getTemplate(name);
			return template.execute(model);
		}
		catch (final Exception e) {
			log.error("Cannot render: " + name, e);
			throw new IllegalStateException("Cannot render template", e);
		}
	}

	public Template getTemplate(final String name) {
		if (cache) {
			return this.templateCaches.computeIfAbsent(name, this::loadTemplate);
		}
		return loadTemplate(name);
	}

	protected Template loadTemplate(final String name) {
		try {
			final Reader template;
			template = mustache.loader.getTemplate(name);
			return mustache.compile(template);
		}
		catch (final Exception e) {
			throw new IllegalStateException("Cannot load template " + name, e);
		}
	}

}
