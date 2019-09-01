package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "TemplateTestAnswer")
@Table(name = "template_test_answer")
public class TemplateTestAnswer implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "number")
	private Long number;

	@Column(name = "content")
	private String content;

	@OneToOne
	@JoinColumn(name = "template_test_question_id")
	private TemplateTestQuestion templateTestQuestion;

	@Column(name = "correct", columnDefinition = "boolean default false", nullable = false)
	private Boolean correct;

	@OneToOne
	@JoinColumn(name="author_id")
	private Person author;

	public TemplateTestAnswer(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public Long getNumber(){
		return number;
	}

	public void setNumber(Long number){
		this.number = number;
	}

	public String getContent(){
		return content;
	}

	public void setContent(String content){
		this.content = content;
	}

	public TemplateTestQuestion getTemplateTestQuestion(){
		return templateTestQuestion;
	}

	public void setTemplateTestQuestion(TemplateTestQuestion templateTestQuestion){
		this.templateTestQuestion = templateTestQuestion;
	}

	public Boolean getCorrect(){
		return correct;
	}

	public void setCorrect(Boolean correct){
		this.correct = correct;
	}

	public Person getAuthor(){
		return author;
	}

	public void setAuthor(Person author){
		this.author = author;
	}

}