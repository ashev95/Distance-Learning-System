package com.dls.base.reports.container;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "StudentInfo")
@Table(name = "student_info")
public class StudentInfo implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "email")
	private String email;

	@Column(name = "surname")
	private String surname;

	@Column(name = "name")
	private String name;

	@Column(name = "middlename")
	private String middlename;

	@Column(name = "gender")
	private String gender;

	@Column(name = "additionally")
	private String additionally;

	public StudentInfo(){}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail(){
		return email;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getSurname(){
		return surname;
	}

	public void setSurname(String surname){
		this.surname = surname;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getMiddlename(){
		return middlename;
	}

	public void setMiddlename(String middlename){
		this.middlename = middlename;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAdditionally() {
		return additionally;
	}

	public void setAdditionally(String additionally) {
		this.additionally = additionally;
	}

}