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

package org.springframework.boot.autoconfigure.data.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jRepositoryConfigurationExtension;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.GraphRepositoryFactoryBean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's Neo4j
 * Repositories.
 * <p>
 * Activates when there is no bean of type
 * {@link org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean}
 * configured in the context, the Spring Data Mongo
 * {@link org.springframework.data.mongodb.repository.MongoRepository} type is on the
 * classpath, the Mongo client driver API is on the classpath, and there is no other
 * configured {@link org.springframework.data.mongodb.repository.MongoRepository}.
 * <p>
 * Once in effect, the auto-configuration is the equivalent of enabling Mongo repositories
 * using the
 * {@link org.springframework.data.mongodb.repository.config.EnableMongoRepositories}
 * annotation.
 *
 * @author Blake Janelle
 * @see EnableNeo4jRepositories
 */
@Configuration
@ConditionalOnClass({ GraphDatabaseService.class, GraphRepository.class })
@ConditionalOnMissingBean({ GraphRepositoryFactoryBean.class,
	Neo4jRepositoryConfigurationExtension.class })
@ConditionalOnProperty(prefix = "spring.data.neo4j.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(Neo4jRepositoriesAutoConfigureRegistrar.class)
@AutoConfigureAfter(Neo4jAutoConfiguration.class)
public class Neo4jRepositoriesAutoConfiguration {

}
