/*
 * Copyright 2012-2015 the original author or authors.
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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Neo4j.
 *
 * @author Blake Janelle
 */
@ConfigurationProperties(prefix = "spring.data.neo4j")
public class Neo4jProperties {
	
	/**
	 * Neo4j embedded database name. Only applies the embedded DB, which is the default.
	 */
	private Boolean embedded = true;
	
	/**
	 * Neo4j embedded database name. Only applies the embedded DB, which is the default.
	 */
	private String path = "./test.db";

	/**
	 * Neo4j database URI. When set, host and port are ignored.
	 */
	private String uri = "localhost:7474/data/db";

	/**
	 * Login user of the Neo4j server.
	 */
	private String username;

	/**
	 * Login password of the Neo4j server.
	 */
	private char[] password;

	
	public Boolean credentialsDefined(){
		return this.username != null;
	}
	
	public Boolean getEmbedded() {
		return embedded;
	}

	public void setEmbedded(Boolean embedded) {
		this.embedded = embedded;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public char[] getPassword() {
		return this.password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public void clearPassword() {
		if (this.password == null) {
			return;
		}
		for (int i = 0; i < this.password.length; i++) {
			this.password[i] = 0;
		}
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
