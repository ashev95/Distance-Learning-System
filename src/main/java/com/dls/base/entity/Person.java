package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Person")
@Table(name = "person")
public class Person implements Serializable{
 
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Transient
	private String confirmPassword;

	@Column(name = "email")
	private String email;

    @Column(name = "surname")
    private String surname;

	@Column(name = "name")
	private String name;

	@Column(name = "middlename")
	private String middlename;

	@Column(name = "gender")
	private Byte gender;

	@Column(name = "additionally")
	private String additionally;

	public Person(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
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

	public Byte getGender() {
		return gender;
	}

	public void setGender(Byte gender) {
		this.gender = gender;
	}

	public String getAdditionally() {
		return additionally;
	}

	public void setAdditionally(String additionally) {
		this.additionally = additionally;
	}

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
	@JoinTable(
			name = "person_role",
			joinColumns = { @JoinColumn(name = "person_id") },
			inverseJoinColumns = { @JoinColumn(name = "role_code") }
	)
	private Set<Role> roles = new HashSet<Role>();

	public Set<Role> getRoles(){
		return roles;
	}

	public void setRoles(Set<Role> roles){
		this.roles = roles;
	}

}