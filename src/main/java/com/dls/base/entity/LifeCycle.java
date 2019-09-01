package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "LifeCycle")
@Table(name = "life_cycle")
public class LifeCycle implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@OneToOne
	@JoinColumn(name="init_status_id")
	private Status initStatus;

	public LifeCycle(){}

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

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public Status getInitStatus(){
		return initStatus;
	}

	public void setInitStatus(Status initStatus){
		this.initStatus = initStatus;
	}

}