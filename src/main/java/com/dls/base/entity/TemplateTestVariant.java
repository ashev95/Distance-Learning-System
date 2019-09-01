package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "TemplateTestVariant")
@Table(name = "template_test_variant")
public class TemplateTestVariant implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "number")
	private Long number;

	@OneToOne
	@JoinColumn(name = "template_test_id")
	private TemplateTest templateTest;

	@OneToOne
	@JoinColumn(name="author_id")
	private Person author;

	public TemplateTestVariant(){}

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

	public TemplateTest getTemplateTest(){
		return templateTest;
	}

	public void setTemplateTest(TemplateTest templateTest){
		this.templateTest = templateTest;
	}

	public Person getAuthor(){
		return author;
	}

	public void setAuthor(Person author){
		this.author = author;
	}

}