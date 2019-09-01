package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "TestAnswer")
@Table(name = "test_answer")
public class TestAnswer implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name="test_question_id")
	private TestQuestion testQuestion;

	@Column(name="number")
	private Long number;

	public TestAnswer(){}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public TestQuestion getTestQuestion() {
		return testQuestion;
	}

	public void setTestQuestion(TestQuestion testQuestion) {
		this.testQuestion = testQuestion;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

}