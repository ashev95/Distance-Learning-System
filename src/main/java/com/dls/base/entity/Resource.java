package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Resource")
@Table(name = "resource")
public class Resource implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "entity_class")
	private String entityClass;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(name = "position")
	private Long position;

	@Column(name = "type")
	private String type;

	@Column(name = "name")
	private String name;

	@Column(name = "extension")
	private String extension;

	@Column(name = "value")
	private String value;

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public String getEntityClass(){
		return entityClass;
	}

	public void setEntityClass(String entityClass){
		this.entityClass = entityClass;
	}

	public Long getEntityId(){
		return entityId;
	}

	public void setEntityId(Long entityId){
		this.entityId = entityId;
	}

	public Long getPosition(){
		return position;
	}

	public void setPosition(Long position){
		this.position = position;
	}

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getExtension(){
		return extension;
	}

	public void setExtension(String extension){
		this.extension = extension;
	}

	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value = value;
	}

}