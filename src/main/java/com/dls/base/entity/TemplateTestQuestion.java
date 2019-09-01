package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "TemplateTestQuestion")
@Table(name = "template_test_question")
public class TemplateTestQuestion implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "number")
	private Long number;

	@Column(name = "content")
	private String content;

	@Column(name = "score_positive")
	private Long scorePositive;

	@Column(name = "score_negative")
	private Long scoreNegative;

	@OneToOne
	@JoinColumn(name = "template_test_variant_id")
	private TemplateTestVariant templateTestVariant;

	@OneToOne
	@JoinColumn(name="author_id")
	private Person author;

	public TemplateTestQuestion(){}

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

	public Long getScorePositive() {
		return scorePositive;
	}

	public void setScorePositive(Long scorePositive) {
		this.scorePositive = scorePositive;
	}

	public Long getScoreNegative() {
		return scoreNegative;
	}

	public void setScoreNegative(Long scoreNegative) {
		this.scoreNegative = scoreNegative;
	}

	public TemplateTestVariant getTemplateTestVariant(){
		return templateTestVariant;
	}

	public void setTemplateTestVariant(TemplateTestVariant templateTestVariant){
		this.templateTestVariant = templateTestVariant;
	}

	public Person getAuthor(){
		return author;
	}

	public void setAuthor(Person author){
		this.author = author;
	}

}