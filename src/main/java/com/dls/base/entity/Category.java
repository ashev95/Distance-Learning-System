package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Category")
@Table(name = "category")
public class Category implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	public Category(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}