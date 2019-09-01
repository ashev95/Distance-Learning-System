package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Role")
@Table(name = "role")
public class Role implements Serializable{

	@Id
	@Column(name = "code")
	private String code;

    @Column(name = "name")
    private String name;

    public Role(){}

	public String getCode(){
		return code;
	}

	public void setCode(String code){
		this.code = code;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

}