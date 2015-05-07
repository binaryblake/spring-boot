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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Validator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.convert.DefaultTypeMapper;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.context.MappingContextIsNewStrategyFactory;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.config.JtaTransactionManagerFactoryBean;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.core.TypeRepresentationStrategy;
import org.springframework.data.neo4j.fieldaccess.FieldAccessorFactoryFactory;
import org.springframework.data.neo4j.fieldaccess.Neo4jConversionServiceFactoryBean;
import org.springframework.data.neo4j.fieldaccess.NodeDelegatingFieldAccessorFactory;
import org.springframework.data.neo4j.fieldaccess.RelationshipDelegatingFieldAccessorFactory;
import org.springframework.data.neo4j.mapping.EntityInstantiator;
import org.springframework.data.neo4j.support.MappingInfrastructureFactoryBean;
import org.springframework.data.neo4j.support.Neo4jExceptionTranslator;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.mapping.EntityAlias;
import org.springframework.data.neo4j.support.mapping.EntityIndexCreator;
import org.springframework.data.neo4j.support.mapping.EntityStateHandler;
import org.springframework.data.neo4j.support.mapping.Neo4jEntityFetchHandler;
import org.springframework.data.neo4j.support.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.support.mapping.NoEntityIndexCreator;
import org.springframework.data.neo4j.support.mapping.SourceStateTransmitter;
import org.springframework.data.neo4j.support.mapping.TRSTypeAliasAccessor;
import org.springframework.data.neo4j.support.node.NodeEntityInstantiator;
import org.springframework.data.neo4j.support.node.NodeEntityStateFactory;
import org.springframework.data.neo4j.support.relationship.RelationshipEntityInstantiator;
import org.springframework.data.neo4j.support.relationship.RelationshipEntityStateFactory;
import org.springframework.data.neo4j.support.schema.SchemaIndexProvider;
import org.springframework.data.neo4j.support.typerepresentation.ClassValueTypeInformationMapper;
import org.springframework.data.neo4j.support.typerepresentation.TypeRepresentationStrategyFactory;
import org.springframework.data.neo4j.support.typesafety.TypeSafetyOption;
import org.springframework.data.neo4j.support.typesafety.TypeSafetyPolicy;
import org.springframework.data.support.IsNewStrategyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's neo4j
 * support.
 * <p>
 * Registers a {@link Neo4jTemplate} if no other beans of the same type are
 * configured.
 * <P>
 * Honors the {@literal spring.data.neo4j.database} property if set, otherwise
 * connects to the {@literal test} database.
 *
 * @author Blake Janelle
 */
@Configuration
@ConditionalOnClass({ GraphDatabaseService.class, Neo4jTemplate.class })
@EnableConfigurationProperties(Neo4jProperties.class)
@AutoConfigureAfter(Neo4jAutoConfiguration.class)
public class Neo4jDataAutoConfiguration {

	@Autowired
	private GraphDatabaseService service;

	@Autowired
	private GraphDatabase db;

	@Autowired
	private Neo4jProperties properties;

	@Autowired
	private Environment environment;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private ResourceLoader resourceLoader;

	@Qualifier("conversionService")
	@Autowired(required = false)
	private ConversionService conversionService;

	@Bean
	@ConditionalOnMissingBean
	public GraphDatabase graphDatabase(Neo4jTemplate template) {
		return template.getGraphDatabase();
	}

	@Bean
	@ConditionalOnMissingBean
	public Neo4jMappingContext neo4jMappingContext(BeanFactory beanFactory)
			throws ClassNotFoundException {
		Neo4jMappingContext context = new Neo4jMappingContext();
		context.setInitialEntitySet(getInitialEntitySet());
		return context;
	}

	@Autowired(required = false)
	private Validator validator;

	@Bean
	public MappingInfrastructureFactoryBean mappingInfrastructure(
			Neo4jMappingContext mapping) throws Exception {
		MappingInfrastructureFactoryBean factoryBean = new MappingInfrastructureFactoryBean();
		factoryBean.setGraphDatabaseService(service);
		factoryBean
				.setTypeRepresentationStrategyFactory(typeRepresentationStrategyFactory());
		factoryBean.setConversionService(neo4jConversionService());
		factoryBean.setMappingContext(mapping);
		factoryBean.setEntityStateHandler(entityStateHandler(mapping));
		factoryBean.setNodeEntityStateFactory(nodeEntityStateFactory(service,
				mapping));
		factoryBean
				.setNodeTypeRepresentationStrategy(nodeTypeRepresentationStrategy());
		factoryBean.setNodeEntityInstantiator(graphEntityInstantiator(mapping));

		factoryBean
				.setRelationshipEntityStateFactory(relationshipEntityStateFactory(mapping));
		factoryBean
				.setRelationshipTypeRepresentationStrategy(relationshipTypeRepresentationStrategy());
		factoryBean
				.setRelationshipEntityInstantiator(graphRelationshipInstantiator(mapping));

		factoryBean.setTransactionManager(neo4jTransactionManager());
		factoryBean.setGraphDatabase(db);
		factoryBean.setIsNewStrategyFactory(isNewStrategyFactory(mapping));
		factoryBean.setTypeSafetyPolicy(typeSafetyPolicy());

		if (validator != null) {
			factoryBean.setValidator(validator);
		}
		factoryBean.afterPropertiesSet();
		return factoryBean;
	}

	@Bean
	public IsNewStrategyFactory isNewStrategyFactory(Neo4jMappingContext mapping)
			throws Exception {
		ArrayList<Neo4jMappingContext> sets = new ArrayList<Neo4jMappingContext>();
		sets.add(mapping);
		return new MappingContextIsNewStrategyFactory(new PersistentEntities(
				sets));
	}

	@Bean
	public Neo4jTemplate neo4jTemplate(Neo4jMappingContext mapping)
			throws Exception {

		return new Neo4jTemplate(mappingInfrastructure(mapping).getObject());
	}

	@Bean
	public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy()
			throws Exception {
		return typeRepresentationStrategyFactory()
				.getRelationshipTypeRepresentationStrategy();
	}

	@Bean
	public TypeRepresentationStrategy<Node> nodeTypeRepresentationStrategy()
			throws Exception {
		return typeRepresentationStrategyFactory()
				.getNodeTypeRepresentationStrategy();
	}

	@Bean
	public TypeRepresentationStrategyFactory typeRepresentationStrategyFactory()
			throws Exception {
		return new TypeRepresentationStrategyFactory(db);
	}

	@Bean
	public EntityStateHandler entityStateHandler(Neo4jMappingContext mapping)
			throws Exception {
		return new EntityStateHandler(mapping, db, neo4jConversionService());
	}

	@Bean
	public TypeMapper<Node> nodeTypeMapper() throws Exception {
		return new DefaultTypeMapper<Node>(new TRSTypeAliasAccessor<Node>(
				nodeTypeRepresentationStrategy()),
				asList(new ClassValueTypeInformationMapper()));
	}

	@Bean
	public TypeMapper<Relationship> relationshipTypeMapper() throws Exception {
		return new DefaultTypeMapper<Relationship>(
				new TRSTypeAliasAccessor<Relationship>(
						relationshipTypeRepresentationStrategy()),
				asList(new ClassValueTypeInformationMapper()));
	}

	@Bean
	public Neo4jEntityFetchHandler entityFetchHandler(
			Neo4jMappingContext mapping) throws Exception {
		final SourceStateTransmitter<Node> nodeSourceStateTransmitter = nodeStateTransmitter(mapping);
		final SourceStateTransmitter<Relationship> relationshipSourceStateTransmitter = new SourceStateTransmitter<Relationship>(
				relationshipEntityStateFactory(mapping));
		return new Neo4jEntityFetchHandler(entityStateHandler(mapping),
				neo4jConversionService(), nodeSourceStateTransmitter,
				relationshipSourceStateTransmitter);
	}

	@Bean
	public SourceStateTransmitter<Node> nodeStateTransmitter(
			Neo4jMappingContext mapping) throws Exception {
		return new SourceStateTransmitter<Node>(nodeEntityStateFactory(service,
				mapping));
	}

	@Bean
	protected ConversionService neo4jConversionService() throws Exception {
		final Neo4jConversionServiceFactoryBean neo4jConversionServiceFactoryBean = new Neo4jConversionServiceFactoryBean();
		if (conversionService != null) {
			neo4jConversionServiceFactoryBean.addConverters(conversionService);
			return conversionService;
		}
		return neo4jConversionServiceFactoryBean.getObject();
	}

	@Bean
	protected RelationshipEntityInstantiator graphRelationshipInstantiator(
			Neo4jMappingContext mapping) throws Exception {
		return new RelationshipEntityInstantiator(entityStateHandler(mapping));
	}

	@Bean
	protected EntityInstantiator<Node> graphEntityInstantiator(
			Neo4jMappingContext mapping) throws Exception {
		return new NodeEntityInstantiator(entityStateHandler(mapping));
	}

	@Bean
	protected EntityAlias entityAlias() {
		return new EntityAlias();
	}

	@Bean
	public RelationshipEntityStateFactory relationshipEntityStateFactory(
			Neo4jMappingContext mapping) throws Exception {
		return new RelationshipEntityStateFactory(mapping,
				relationshipFactory());
	}

	@Bean
	public NodeEntityStateFactory nodeEntityStateFactory(
			GraphDatabaseService gds, Neo4jMappingContext mapping)
			throws Exception {
		return new NodeEntityStateFactory(mapping, nodeFactory());
	}

	@Bean
	public FieldAccessorFactoryFactory nodeFactory() throws Exception {
		return new NodeDelegatingFieldAccessorFactory.Factory();
	}

	@Bean
	public FieldAccessorFactoryFactory relationshipFactory() throws Exception {
		return new RelationshipDelegatingFieldAccessorFactory.Factory();
	}

	@Bean(name = { "neo4jTransactionManager", "transactionManager" })
	public PlatformTransactionManager neo4jTransactionManager() {
		JtaTransactionManagerFactoryBean jtaTransactionManagerFactoryBean = new JtaTransactionManagerFactoryBean(
				service);
		return jtaTransactionManagerFactoryBean.getObject();
	}

	@Bean
	public EntityIndexCreator entityIndexCreator() throws Exception {
		return new NoEntityIndexCreator();
	}

	@Bean
	public PersistenceExceptionTranslator persistenceExceptionTranslator() {
		return new Neo4jExceptionTranslator();
	}

	@Bean
	public SchemaIndexProvider schemaIndexProvider() throws Exception {
		return new SchemaIndexProvider(db);
	}

	@Bean
	public TypeSafetyPolicy typeSafetyPolicy() throws Exception {
		return new TypeSafetyPolicy(TypeSafetyOption.THROWS_EXCEPTION);
	}

	private Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
		Set<Class<?>> entitySet = new HashSet<Class<?>>();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);
		scanner.setEnvironment(this.environment);
		scanner.setResourceLoader(this.resourceLoader);
		scanner.addIncludeFilter(new AnnotationTypeFilter(NodeEntity.class));
		scanner.addIncludeFilter(new AnnotationTypeFilter(
				RelationshipEntity.class));
		Collection<String> packages = getMappingBasePackages(beanFactory);

		for (String basePackage : packages) {
			if (StringUtils.hasText(basePackage)) {
				for (BeanDefinition candidate : scanner
						.findCandidateComponents(basePackage)) {
					entitySet.add(ClassUtils.forName(
							candidate.getBeanClassName(),
							Neo4jDataAutoConfiguration.class.getClassLoader()));
				}
			}
		}

		return entitySet;
	}

	private static Collection<String> getMappingBasePackages(
			BeanFactory beanFactory) {
		try {
			return AutoConfigurationPackages.get(beanFactory);
		} catch (IllegalStateException ex) {
			// no auto-configuration package registered yet
			return Collections.emptyList();
		}
	}
}
