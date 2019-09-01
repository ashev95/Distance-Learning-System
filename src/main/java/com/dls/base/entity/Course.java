package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "Course")
@Table(name = "course")
public class Course implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@OneToOne
	@JoinColumn(name="template_course_id")
	private TemplateCourse templateCourse;

	@OneToOne
	@JoinColumn(name = "status_id")
	private Status status;

	@OneToOne
	@JoinColumn(name="group1_id")
	private Group group;

	@OneToOne
	@JoinColumn(name="curator_id")
	private Person curator;

	@Column(name = "date_start")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateStart;

	@Column(name = "date_end")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateEnd;

	@Column(name = "date_create")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreate;

	@OneToOne
	@JoinColumn(name="author_id")
	private Person author;

	public Course(){}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public TemplateCourse getTemplateCourse(){
		return templateCourse;
	}

	public void setTemplateCourse(TemplateCourse templateCourse){
		this.templateCourse = templateCourse;
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

	public Status getStatus(){
		return status;
	}

	public void setStatus(Status status){
		this.status = status;
	}

	public Group getGroup(){
		return group;
	}

	public void setGroup(Group group){
		this.group = group;
	}

	public Person getCurator(){
		return curator;
	}

	public void setCurator(Person curator){
		this.curator = curator;
	}

	public Date getDateStart(){
		return dateStart;
	}

	public void setDateStart(Date dateStart){
		this.dateStart = dateStart;
	}

	public Date getDateEnd(){
		return dateEnd;
	}

	public void setDateEnd(Date dateEnd){
		this.dateEnd = dateEnd;
	}

	public Date getDateCreate(){
		return dateCreate;
	}

	public void setDateCreate(Date dateCreate){
		this.dateCreate = dateCreate;
	}

	public Person getAuthor(){
		return author;
	}

	public void setAuthor(Person author){
		this.author = author;
	}

}