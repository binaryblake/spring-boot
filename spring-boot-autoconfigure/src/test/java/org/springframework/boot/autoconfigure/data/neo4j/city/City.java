package org.springframework.boot.autoconfigure.data.neo4j.city;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class City implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@GraphId
	private Long id;
	
	@Indexed
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
