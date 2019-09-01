package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Group")
@Table(name = "group1")
public class Group implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "name")
	private String name;

	@OneToOne
	@JoinColumn(name="curator_id")
	private Person curator;

	public Group(){}

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

	public Person getCurator(){
		return curator;
	}

	public void setCurator(Person curator){
		this.curator = curator;
	}

}