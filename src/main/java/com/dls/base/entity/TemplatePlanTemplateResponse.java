package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "TemplatePlanTemplateResponse")
@Table(name = "template_plan_template_response")
public class TemplatePlanTemplateResponse implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name = "template_plan_id")
	private TemplatePlan templatePlan;

	@Column(name = "template_response_class")
	private String templateResponseClass;

	@Column(name = "template_response_id")
	private Long templateResponseId;

	@Column(name = "position")
	private Long position;

	@OneToOne
	@JoinColumn(name = "block_id")
	private Block block;

	public TemplatePlanTemplateResponse(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public TemplatePlan getTemplatePlan(){
		return templatePlan;
	}

	public void setTemplatePlan(TemplatePlan templatePlan){
		this.templatePlan = templatePlan;
	}

	public String getTemplateResponseClass(){
		return templateResponseClass;
	}

	public void setTemplateResponseClass(String templateResponseClass){
		this.templateResponseClass = templateResponseClass;
	}

	public Long getTemplateResponseId(){
		return templateResponseId;
	}

	public void setTemplateResponseId(Long templateResponseId){
		this.templateResponseId = templateResponseId;
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