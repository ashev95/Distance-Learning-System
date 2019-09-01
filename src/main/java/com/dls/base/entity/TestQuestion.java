package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "TestQuestion")
@Table(name = "test_question")
public class TestQuestion implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name="test_variant_person_id")
	private TestVariantPerson testVariantPerson;

	@Column(name = "number")
	private Long number;

	@Column(name = "current_deprecate_change_answer_counter")
	private Long currentDeprecateChangeAnswerCounter;

	@Column(name = "visited")
	private Boolean visited;

	public TestQuestion(){}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public TestVariantPerson getTestVariantPerson(){
		return testVariantPerson;
	}

	public void setTestVariantPerson(TestVariantPerson testVariantPerson){
		this.testVariantPerson = testVariantPerson;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

	public Long getCurrentDeprecateChangeAnswerCounter() {
		return currentDeprecateChangeAnswerCounter;
	}

	public void setCurrentDeprecateChangeAnswerCounter(Long currentDeprecateChangeAnswerCounter) {
		this.currentDeprecateChangeAnswerCounter = currentDeprecateChangeAnswerCounter;
	}

	public Boolean getVisited() {
		return visited;
	}

	public void setVisited(Boolean visited) {
		this.visited = visited;
	}

}