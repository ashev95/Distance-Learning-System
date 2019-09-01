package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "TemplateLifeCycle")
@Table(name = "template_life_cycle")
public class TemplateLifeCycle implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name="template_class")
	private String templateClass;

	@OneToOne
	@JoinColumn(name="life_cycle_id")
	private LifeCycle lifeCycle;

	public TemplateLifeCycle(){}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public String getTemplateClass(){
		return templateClass;
	}

	public void setTemplateClass(String templateClass){
		this.templateClass = templateClass;
	}

	public LifeCycle getLifeCycle(){
		return lifeCycle;
	}

	public void setLifeCycle(LifeCycle lifeCycle){
		this.lifeCycle = lifeCycle;
	}

}