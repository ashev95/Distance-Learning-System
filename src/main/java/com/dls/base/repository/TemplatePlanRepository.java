package com.dls.base.repository;

import com.dls.base.entity.TemplatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplatePlanRepository extends JpaRepository<TemplatePlan, Long> {

	@Query("select tl from TemplatePlan tl where tl.id = :id")
	TemplatePlan findByTemplatePlanId(@Param("id") long id);

	@Query("select tl from TemplatePlan tl where tl.status.code = :statusCode")
	Set<TemplatePlan> findByStatusCode(@Param("statusCode") String statusCode);

	@Query("select tl from TemplatePlan tl where tl.version = :version")
	Set<TemplatePlan> findByVersion(@Param("version") long version);

	@Query(value = "with recursive r as ( " +
			"select id, " +
			"description, " +
			"name, " +
			"version, " +
			"author_id, " +
			"parent_id, " +
			"status_id, " +
			"date_create " +
			"from template_plan where id = ?1 " +
			"union all " +
			"select template_plan.id, " +
			"template_plan.description, " +
			"template_plan.name, " +
			"template_plan.version, " +
			"template_plan.author_id, " +
			"template_plan.parent_id, " +
			"template_plan.status_id, " +
			"template_plan.date_create " +
			"from template_plan " +
			"join r on template_plan.parent_id = r.id " +
			") select * from r order by r.version", nativeQuery = true)
	Set<TemplatePlan> findRecursiveByParentId(Long id);

	@Query("select tl from TemplatePlan tl where tl.parent.id = :parentId")
	Set<TemplatePlan> findByParentId(@Param("parentId") long parentId);

	@Query(value = "select * from template_plan " +
			"where template_plan.author_id = ?1"
			, nativeQuery = true)
	Set<TemplatePlan> findByAuthorId(long authorId);

	@Query(value = "select template_plan.id, " +
			"template_plan.name, " +
			"template_plan.description, " +
			"template_plan.status_id, " +
			"template_plan.parent_id, " +
			"template_plan.version, " +
			"template_plan.date_create, " +
			"template_plan.author_id" +
			" from template_plan " +
			"join block on block.parent_class = 'templateplan' and block.parent_id = template_plan.id " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where template_plan_template_response.id = ?1 "
			, nativeQuery = true)
	TemplatePlan findTemplatePlanByTemplatePlanTemplateResponseId(Long templatePlanTemplateResponseId);

}