package com.dls.base.repository;

import com.dls.base.entity.TemplateCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplateCourseRepository extends JpaRepository<TemplateCourse, Long> {

	@Query("select tl from TemplateCourse tl where tl.id = :id")
	TemplateCourse findByTemplateCourseId(@Param("id") long id);

	@Query("select tl from TemplateCourse tl where tl.status.code = :statusCode")
	Set<TemplateCourse> findByStatusCode(@Param("statusCode") String statusCode);

	@Query("select tl from TemplateCourse tl where tl.version = :version")
	Set<TemplateCourse> findByVersion(@Param("version") long version);

	@Query(value = "with recursive r as ( " +
			"select id, " +
			"description, " +
			"name, " +
			"version, " +
			"author_id, " +
			"parent_id, " +
			"status_id, " +
			"date_create " +
			"from template_course where id = ?1 " +
			"union all " +
			"select template_course.id, " +
			"template_course.description, " +
			"template_course.name, " +
			"template_course.version, " +
			"template_course.author_id, " +
			"template_course.parent_id, " +
			"template_course.status_id, " +
			"template_course.date_create " +
			"from template_course " +
			"join r on template_course.parent_id = r.id " +
			") select * from r order by r.version", nativeQuery = true)
	Set<TemplateCourse> findRecursiveByParentId(Long id);

	@Query("select tl from TemplateCourse tl where tl.parent.id = :parentId")
	Set<TemplateCourse> findByParentId(@Param("parentId") long parentId);

	@Query(value = "select * from template_course " +
			"where template_course.author_id = ?1"
			, nativeQuery = true)
	Set<TemplateCourse> findByAuthorId(long authorId);

	@Query(value = "select template_course.id, " +
			"template_course.name, " +
			"template_course.description, " +
			"template_course.status_id, " +
			"template_course.parent_id, " +
			"template_course.version, " +
			"template_course.date_create, " +
			"template_course.author_id" +
			" from template_course " +
			"join block on block.parent_class = 'templatecourse' and block.parent_id = template_course.id " +
			"join template_course_template_response on block.id = template_course_template_response.block_id " +
			"where template_course_template_response.id = ?1 "
			, nativeQuery = true)
	TemplateCourse findTemplateCourseByTemplateCourseTemplateResponseId(Long templateCourseTemplateResponseId);

}