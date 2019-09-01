package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "PlanResponse")
@Table(name = "plan_response")
public class PlanResponse implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name = "plan_id")
	private Plan plan;

	@Column(name = "response_class")
	private String responseClass;

	@Column(name = "response_id")
	private Long responseId;

	@Column(name = "position")
	private Long position;

	@OneToOne
	@JoinColumn(name = "block_id")
	private Block block;

	public PlanResponse(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public Plan getPlan(){
		return plan;
	}

	public void setPlan(Plan plan){
		this.plan = plan;
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