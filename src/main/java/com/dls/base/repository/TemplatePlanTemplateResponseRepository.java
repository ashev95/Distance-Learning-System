package com.dls.base.repository;

import com.dls.base.entity.TemplatePlanTemplateResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplatePlanTemplateResponseRepository extends JpaRepository<TemplatePlanTemplateResponse, Long> {

	@Query("select tc from TemplatePlanTemplateResponse tc where tc.id = :id")
	TemplatePlanTemplateResponse findByTemplatePlanTemplateResponseId(@Param("id") long id);

	@Query(value = "select coalesce(max(template_plan_template_response.position),0) from template_plan " +
			"join block on block.parent_class = 'templateplan' and block.parent_id = template_plan.id " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where template_plan.id = ?1"
			, nativeQuery = true)
	Long findLastPositionByTemplatePlanId(Long templatePlanId);

	@Query(value = "select * from template_plan_template_response " +
			"where template_plan_template_response.block_id = ?1 " +
			"order by template_plan_template_response.position"
			, nativeQuery = true)
	Set<TemplatePlanTemplateResponse> findTemplatePlanTemplateResponseByBlockId(Long blockId);

	@Query(value = "select template_plan_template_response.id, " +
			"template_plan_template_response.template_plan_id, " +
			"template_plan_template_response.template_response_class, " +
			"template_plan_template_response.template_response_id, " +
			"template_plan_template_response.position, " +
			"template_plan_template_response.block_id" +
			" from block " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where block.parent_class = 'templateplan' and block.parent_id = ?1 and " +
			"block.position = ?2"
			, nativeQuery = true)
	Set<TemplatePlanTemplateResponse> findTemplatePlanTemplateResponseByTemplatePlanIdAndBlockPosition(Long templatePlanId, Long blockPosition);

	@Query(value = "select template_plan_template_response.id, " +
			"template_plan_template_response.template_plan_id, " +
			"template_plan_template_response.template_response_class, " +
			"template_plan_template_response.template_response_id, " +
			"template_plan_template_response.position, " +
			"template_plan_template_response.block_id" +
			" from template_plan " +
			"join block on block.parent_class = 'templateplan' and block.parent_id = template_plan.id " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where template_plan.id = ?1 " +
			"order by block.position, template_plan_template_response.position"
			, nativeQuery = true)
	Set<TemplatePlanTemplateResponse> findTemplatePlanTemplateResponseByTemplatePlanId(Long templatePlanId);

	@Query(value = "select template_plan_template_response.id, " +
			"template_plan_template_response.template_plan_id, " +
			"template_plan_template_response.template_response_class, " +
			"template_plan_template_response.template_response_id, " +
			"template_plan_template_response.position, " +
			"template_plan_template_response.block_id" +
			" from template_plan " +
			"join block on block.parent_class = 'templateplan' and block.parent_id = template_plan.id " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where template_plan.id = ?1 and " +
			"template_plan_template_response.template_response_class = ?2 and " +
			"template_plan_template_response.template_response_id = ?3 " +
			"order by template_plan_template_response.position"
			, nativeQuery = true)
	TemplatePlanTemplateResponse findTemplatePlanTemplateResponseByTemplatePlanIdAndTemplateResponseClassAndTemplateResponseId(Long templatePlanId, String templateResponseClass, Long templateResponseId);

}
