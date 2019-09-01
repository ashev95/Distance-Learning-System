package com.dls.base.repository;

import com.dls.base.entity.TemplateCourse;
import com.dls.base.entity.TemplateCourseTemplateResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplateCourseTemplateResponseRepository extends JpaRepository<TemplateCourseTemplateResponse, Long> {

	@Query("select tc from TemplateCourseTemplateResponse tc where tc.id = :id")
    TemplateCourseTemplateResponse findByTemplateCourseTemplateResponseId(@Param("id") long id);

	@Query(value = "select coalesce(max(template_course_template_response.position),0) from template_course " +
			"join block on block.parent_class = 'templatecourse' and block.parent_id = template_course.id " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where template_course.id = ?1"
			, nativeQuery = true)
	Long findLastPositionByTemplateCourseId(Long templateCourseId);

	@Query(value = "select * from template_course_template_response " +
			"where template_course_template_response.block_id = ?1 " +
			"order by template_course_template_response.position"
			, nativeQuery = true)
	Set<TemplateCourseTemplateResponse> findTemplateCourseTemplateResponseByBlockId(Long blockId);

	@Query(value = "select template_course_template_response.id, " +
			"template_course_template_response.template_course_id, " +
			"template_course_template_response.template_response_class, " +
			"template_course_template_response.template_response_id, " +
			"template_course_template_response.position, " +
			"template_course_template_response.block_id" +
			" from block " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where block.parent_class = 'templatecourse' and block.parent_id = ?1 and " +
			"block.position = ?2"
			, nativeQuery = true)
	Set<TemplateCourseTemplateResponse> findTemplateCourseTemplateResponseByTemplateCourseIdAndBlockPosition(Long templateCourseId, Long blockPosition);

	@Query(value = "select template_course_template_response.id, " +
			"template_course_template_response.template_course_id, " +
			"template_course_template_response.template_response_class, " +
			"template_course_template_response.template_response_id, " +
			"template_course_template_response.position, " +
			"template_course_template_response.block_id" +
			" from template_course " +
			"join block on block.parent_class = 'templatecourse' and block.parent_id = template_course.id " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where template_course.id = ?1 " +
			"order by block.position, template_course_template_response.position"
			, nativeQuery = true)
	Set<TemplateCourseTemplateResponse> findTemplateCourseTemplateResponseByTemplateCourseId(Long templateCourseId);

	@Query(value = "select template_course_template_response.id, " +
			"template_course_template_response.template_course_id, " +
			"template_course_template_response.template_response_class, " +
			"template_course_template_response.template_response_id, " +
			"template_course_template_response.position, " +
			"template_course_template_response.block_id" +
			" from template_course " +
			"join block on block.parent_class = 'templatecourse' and block.parent_id = template_course.id " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where template_course.id = ?1 and " +
			"template_course_template_response.template_response_class = ?2 and " +
			"template_course_template_response.template_response_id = ?3 " +
			"order by template_course_template_response.position"
			, nativeQuery = true)
	TemplateCourseTemplateResponse findTemplateCourseTemplateResponseByTemplateCourseIdAndTemplateResponseClassAndTemplateResponseId(Long templateCourseId, String templateResponseClass, Long templateResponseId);

}
