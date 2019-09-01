package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Move")
@Table(name = "move")
public class Move implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@OneToOne
	@JoinColumn(name="life_cycle_id")
	private LifeCycle lifeCycle;

	@OneToOne
	@JoinColumn(name="from_status_id")
	private Status fromStatus;

	@OneToOne
	@JoinColumn(name="to_status_id")
	private Status toStatus;

	public Move(){}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public LifeCycle getLifeCycle(){
		return lifeCycle;
	}

	public void setLifeCycle(LifeCycle lifeCycle){
		this.lifeCycle = lifeCycle;
	}

	public Status getFromStatus(){
		return fromStatus;
	}

	public void setFromStatus(Status fromStatus){
		this.fromStatus = fromStatus;
	}

	public Status getToStatus(){
		return toStatus;
	}

	public void setToStatus(Status toStatus){
		this.toStatus = toStatus;
	}

}