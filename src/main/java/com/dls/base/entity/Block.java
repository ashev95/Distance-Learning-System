package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Block")
@Table(name = "block")
public class Block implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "position")
	private Long position;

	@Column(name = "type")
	private Byte type;

	@Column(name = "parent_class")
	private String parentClass;

	@Column(name = "parent_id")
	private Long parentId;

	public Block(){}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}

	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}

	public String getParentClass() {
		return parentClass;
	}

	public void setParentClass(String parentClass) {
		this.parentClass = parentClass;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
}