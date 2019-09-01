package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "TestVariantPerson")
@Table(name = "test_variant_person")
public class TestVariantPerson implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name="test_id")
	private Test test;

	@OneToOne
	@JoinColumn(name="template_test_variant_id")
	private TemplateTestVariant templateTestVariant;

	@OneToOne
	@JoinColumn(name="person_id")
	private Person person;

	@OneToOne
	@JoinColumn(name="status_id")
	private Status status;

	@Column(name = "date_start")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateStart;

	@Column(name = "date_end")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateEnd;

	@OneToOne
	@JoinColumn(name="current_template_test_question_id")
	private TemplateTestQuestion currentTemplateTestQuestion;

	@Column(name="correct_answer_count")
	private Long correctAnswerCount;

	@Column(name="incorrect_answer_count")
	private Long incorrectAnswerCount;

	@Column(name="total_score")
	private Long totalScore;

	@Column(name="available_score")
	private Long availableScore;

	public TestVariantPerson(){}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public Test getTest(){
		return test;
	}

	public void setTest(Test test){
		this.test = test;
	}

	public TemplateTestVariant getTemplateTestVariant(){
		return templateTestVariant;
	}

	public void setTemplateTestVariant(TemplateTestVariant templateTestVariant){
		this.templateTestVariant = templateTestVariant;
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

	public TemplateTestQuestion getCurrentTemplateTestQuestion() {
		return currentTemplateTestQuestion;
	}

	public void setCurrentTemplateTestQuestion(TemplateTestQuestion currentTemplateTestQuestion) {
		this.currentTemplateTestQuestion = currentTemplateTestQuestion;
	}

	public Long getCorrectAnswerCount() {
		return correctAnswerCount;
	}

	public void setCorrectAnswerCount(Long correctAnswerCount) {
		this.correctAnswerCount = correctAnswerCount;
	}

	public Long getIncorrectAnswerCount() {
		return incorrectAnswerCount;
	}

	public void setIncorrectAnswerCount(Long incorrectAnswerCount) {
		this.incorrectAnswerCount = incorrectAnswerCount;
	}

	public Long getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Long totalScore) {
		this.totalScore = totalScore;
	}

	public Long getAvailableScore() {
		return availableScore;
	}

	public void setAvailableScore(Long availableScore) {
		this.availableScore = availableScore;
	}

}