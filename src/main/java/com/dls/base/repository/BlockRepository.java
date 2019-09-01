package com.dls.base.repository;

import com.dls.base.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface BlockRepository extends JpaRepository<Block, Long> {

	@Query("select tl from Block tl where tl.id = :id")
	Block findByBlockId(@Param("id") long id);

	@Query(value = "select distinct * from block " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where template_course_template_response.id = ?1"
			, nativeQuery = true)
	Block findBlockByTemplateBlockTemplateCourseTemplateResponseId(Long templateBlockTemplateResponseId);

	@Query(value = "select distinct * from block " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where template_plan_template_response.id = ?1"
			, nativeQuery = true)
	Block findBlockByTemplateBlockTemplatePlanTemplateResponseId(Long templateBlockTemplateResponseId);

	@Query(value = "select * from block " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where block.parent_class = 'templatecourse' and block.parent_id = ?1 and " +
			"block.position = ?2"
			, nativeQuery = true)
	Block findBlockByTemplateCourseIdAndBlockPosition(long templateCourseId, long templateCourseTemplateResponsePosition);

	@Query(value = "select * from block " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where block.parent_class = 'templateplan' and block.parent_id = ?1 and " +
			"block.position = ?2"
			, nativeQuery = true)
	Block findBlockByTemplatePlanIdAndBlockPosition(long templatePlanId, long templatePlanTemplateResponsePosition);

	@Query(value = "select distinct block.id, " +
			"block.parent_class, " +
			"block.parent_id, " +
			"block.position, " +
			"block.type " +
			"from block " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where block.parent_class = 'templatecourse' and block.parent_id = ?1 " +
			"order by block.position"
			, nativeQuery = true)
	Set<Block> findBlockByTemplateCourseId(long templateCourseId);

	@Query(value = "select distinct block.id, " +
			"block.parent_class, " +
			"block.parent_id, " +
			"block.position, " +
			"block.type " +
			"from block " +
			"join template_plan_template_response on block.id = template_plan_template_response.block_id " +
			"where block.parent_class = 'templateplan' and block.parent_id = ?1 " +
			"order by block.position"
			, nativeQuery = true)
	Set<Block> findBlockByTemplatePlanId(long templatePlanId);

	@Query(value = "select distinct block.id, " +
			"block.parent_class, " +
			"block.parent_id, " +
			"block.position, " +
			"block.type " +
			"from block " +
			"join course_response on block.id = course_response.block_id " +
			"where block.parent_class = 'course' and block.parent_id = ?1 " +
			"order by block.position"
			, nativeQuery = true)
	Set<Block> findBlockByCourseId(long courseId);

	@Query(value = "select distinct block.id, " +
			"block.parent_class, " +
			"block.parent_id, " +
			"block.position, " +
			"block.type " +
			"from block " +
			"join plan_response on block.id = plan_response.block_id " +
			"where block.parent_class = 'plan' and block.parent_id = ?1 " +
			"order by block.position"
			, nativeQuery = true)
	Set<Block> findBlockByPlanId(long templatePlanId);

}
