package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "CourseResponse")
@Table(name = "course_response")
public class CourseResponse implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name = "course_id")
	private Course course;

	@Column(name = "response_class")
	private String responseClass;

	@Column(name = "response_id")
	private Long responseId;

	@Column(name = "position")
	private Long position;

	@OneToOne
	@JoinColumn(name = "block_id")
	private Block block;

	public CourseResponse(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public Course getCourse(){
		return course;
	}

	public void setCourse(Course course){
		this.course = course;
	}

	public String getResponseClass(){
		return responseClass;
	}

	public void setResponseClass(String responseClass){
		this.responseClass = responseClass;
	}

	public Long getResponseId(){
		return responseId;
	}

	public void setResponseId(Long responseId){
		this.responseId = responseId;
	}

	public Long getPosition(){
		return position;
	}

	public void setPosition(Long position){
		this.position = position;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

}