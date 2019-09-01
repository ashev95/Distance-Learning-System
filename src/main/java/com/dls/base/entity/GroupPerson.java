package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "GroupPerson")
@Table(name = "group1_person")
public class GroupPerson implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name = "group1_id")
	private Group group;

	@OneToOne
	@JoinColumn(name = "person_id")
	private Person person;

	public GroupPerson(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public Group getGroup(){
		return group;
	}

	public void setGroup(Group group){
		this.group = group;
	}

	public Person getPerson(){
		return person;
	}

	public void setPerson(Person person){
		this.person = person;
	}

}