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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase;

/**
 * Tests for {@link MongoAutoConfiguration}.
 *
 * @author Blake Janelle
 */
public class Neo4jAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void clientExists() {
		this.context = new AnnotationConfigApplicationContext(PropertyPlaceholderAutoConfiguration.class, Neo4jAutoConfiguration.class);
		assertEquals(1, this.context.getBeanNamesForType(GraphDatabaseService.class).length);
	}

	@Test
	public void testEmbedded() {
		this.context = new AnnotationConfigApplicationContext(PropertyPlaceholderAutoConfiguration.class, Neo4jAutoConfiguration.class);
		assertEquals(EmbeddedGraphDatabase.class, this.context.getBean(GraphDatabaseService.class).getClass());
	}
	
	@Test
	public void testRemote() {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.data.neo4j.embedded:false");
		this.context.register(PropertyPlaceholderAutoConfiguration.class, Neo4jAutoConfiguration.class);
		this.context.refresh();
		assertEquals(SpringCypherRestGraphDatabase.class, this.context.getBean(GraphDatabaseService.class).getClass());
	}
}
