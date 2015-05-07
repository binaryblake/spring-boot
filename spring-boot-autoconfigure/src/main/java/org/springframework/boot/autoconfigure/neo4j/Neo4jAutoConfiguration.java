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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Neo4j.
 *
 * @author Blake Janelle
 */
@Configuration
@ConditionalOnClass(GraphDatabaseService.class)
@EnableConfigurationProperties(Neo4jProperties.class)
@ConditionalOnMissingBean(type = "org.neo4j.graphdb.factory.GraphDatabaseFactory")
public class Neo4jAutoConfiguration {

	@Autowired
	private Neo4jProperties properties;
	
	@Autowired
	private ApplicationContext ctx;
	
	@Bean
	@ConditionalOnMissingBean
	public GraphDatabaseService graphDatabaseService() {
		GraphDatabaseFactory factory = new GraphDatabaseFactory();
		GraphDatabaseService service;

		if (properties.getEmbedded()) {
			service = factory.newEmbeddedDatabase(properties.getPath());
		} else {
			if (properties.credentialsDefined()) {
				service = new SpringCypherRestGraphDatabase(
						properties.getUri(), properties.getUsername(),
						properties.getPassword().toString());
			} else {
				service = new SpringCypherRestGraphDatabase(properties.getUri());
			}
		}

		return service;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public GraphDatabase graphDatabase(GraphDatabaseService service) {
		Neo4jTemplate template = new Neo4jTemplate(service);
		return template.getGraphDatabase();
	}
}
