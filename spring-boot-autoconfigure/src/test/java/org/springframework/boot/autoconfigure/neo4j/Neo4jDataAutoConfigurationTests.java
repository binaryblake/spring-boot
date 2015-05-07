/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.autoconfigure.neo4j;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.city.City;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.mapping.Neo4jMappingContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for {@link Neo4jDataAutoConfiguration}.
 *
 * @author Blake Janelle
 */
public class Neo4jDataAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void templateExists() {
		this.context = new AnnotationConfigApplicationContext(
				PropertyPlaceholderAutoConfiguration.class, Neo4jAutoConfiguration.class,
				Neo4jDataAutoConfiguration.class);
		assertEquals(1, this.context.getBeanNamesForType(Neo4jTemplate.class).length);
	}
	
	@Test
	public void templateExists() {
		this.context = new AnnotationConfigApplicationContext(
				PropertyPlaceholderAutoConfiguration.class, Neo4jAutoConfiguration.class,
				Neo4jDataAutoConfiguration.class);
		assertEquals(1, this.context.getBeanNamesForType(Neo4jTemplate.class).length);
	}

	@Test
	public void usesAutoConfigurationPackageToPickUpDocumentTypes() {
		this.context = new AnnotationConfigApplicationContext();
		String cityPackage = City.class.getPackage().getName();
		AutoConfigurationPackages.register(this.context, cityPackage);
		this.context.register(Neo4jAutoConfiguration.class,
				Neo4jDataAutoConfiguration.class);
		this.context.refresh();
		assertDomainTypesDiscovered(this.context.getBean(Neo4jMappingContext.class),
				City.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void assertDomainTypesDiscovered(Neo4jMappingContext mappingContext,
			Class<?>... types) {
		Set<Class> initialEntitySet = (Set<Class>) ReflectionTestUtils.getField(
				mappingContext, "initialEntitySet");
		assertThat(initialEntitySet, hasSize(types.length));
		assertThat(initialEntitySet, Matchers.<Class> hasItems(types));
	}

	@Configuration
	static class CustomConversionsConfig {

		@Bean
		public CustomConversions customConversions() {
			return new CustomConversions(Arrays.asList(new MyConverter()));
		}
	}
	
	private static class MyConverter implements Converter<GraphDatabaseService, Boolean> {

		@Override
		public Boolean convert(GraphDatabaseService source) {
			return null;
		}

	}

}
