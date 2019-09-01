package com.dls.base.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "TemplateTest")
@Table(name = "template_test")
public class TemplateTest implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@OneToOne
	@JoinColumn(name = "status_id")
	private Status status;

	@OneToOne
	@JoinColumn(name = "parent_id")
	private TemplateTest parent;

	@Column(name = "version")
	private Long version;

	@Column(name = "date_create")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreate;

	@OneToOne
	@JoinColumn(name="author_id")
	private Person author;

	@Column(name = "by_order")
	private Boolean byOrder;

	@Column(name = "deprecate_change_answer_count")
	private Long deprecateChangeAnswerCount;

	@Column(name = "time_limit")
	private Long timeLimit;

	@OneToOne
	@JoinColumn(name="category_id")
	private Category category;

	public TemplateTest(){}

    public Long getId(){
    	return id;
	}

	public void setId(Long id){
    	this.id = id;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public Status getStatus(){
		return status;
	}

	public void setStatus(Status status){
		this.status = status;
	}

	public TemplateTest getParent(){
		return parent;
	}

	public void setParent(TemplateTest parent){
		this.parent = parent;
	}

	public Long getVersion(){
		return version;
	}

	public void setVersion(Long version){
		this.version = version;
	}

	public Date getDateCreate(){
		return dateCreate;
	}

	public void setDateCreate(Date dateCreate){
		this.dateCreate = dateCreate;
	}

	public Person getAuthor(){
		return author;
	}

	public void setAuthor(Person author){
		this.author = author;
	}

	public Boolean getByOrder() {
		return byOrder;
	}

	public void setByOrder(Boolean byOrder) {
		this.byOrder = byOrder;
	}

	public Long getDeprecateChangeAnswerCount() {
		return deprecateChangeAnswerCount;
	}

	public void setDeprecateChangeAnswerCount(Long deprecateChangeAnswerCount) {
		this.deprecateChangeAnswerCount = deprecateChangeAnswerCount;
	}

	public Long getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(Long timeLimit) {
		this.timeLimit = timeLimit;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

}