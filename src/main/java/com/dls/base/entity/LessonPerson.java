package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "LessonPerson")
@Table(name = "lesson_person")
public class LessonPerson implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name="lesson_id")
	private Lesson lesson;

	@OneToOne
	@JoinColumn(name="person_id")
	private Person person;

	@OneToOne
	@JoinColumn(name="status_id")
	private Status status;

	@Column(name = "date_end")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateEnd;

	public LessonPerson(){}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public Lesson getLesson(){
		return lesson;
	}

	public void setLesson(Lesson lesson){
		this.lesson = lesson;
	}

	public Person getPerson(){
		return person;
	}

	public void setPerson(Person person){
		this.person = person;
	}

	public Status getStatus(){
		return status;
	}

	public void setStatus(Status status){
		this.status = status;
	}

	public Date getDateEnd(){
		return dateEnd;
	}

	public void setDateEnd(Date dateEnd){
		this.dateEnd = dateEnd;
	}

}