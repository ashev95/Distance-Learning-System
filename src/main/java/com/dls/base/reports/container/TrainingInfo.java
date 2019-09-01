package com.dls.base.reports.container;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "TrainingInfo")
@Table(name = "training_info")
public class TrainingInfo implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "name")
	private String name;

	@Column(name = "trainingstart")
	@Temporal(TemporalType.TIMESTAMP)
	private Date trainingstart;

	@Column(name = "trainingend")
	@Temporal(TemporalType.TIMESTAMP)
	private Date trainingend;

	@Column(name = "variantstart")
	@Temporal(TemporalType.TIMESTAMP)
	private Date variantstart;

	@Column(name = "variantend")
	@Temporal(TemporalType.TIMESTAMP)
	private Date variantend;

	@Column(name = "score")
	private String score;

	public TrainingInfo(){}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getTrainingstart() {
		return trainingstart;
	}

	public void setTrainingstart(Date trainingstart) {
		this.trainingstart = trainingstart;
	}

	public Date getTrainingend() {
		return trainingend;
	}

	public void setTrainingend(Date trainingend) {
		this.trainingend = trainingend;
	}

	public Date getVariantstart() {
		return variantstart;
	}

	public void setVariantstart(Date variantstart) {
		this.variantstart = variantstart;
	}

	public Date getVariantend() {
		return variantend;
	}

	public void setVariantend(Date variantend) {
		this.variantend = variantend;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

}