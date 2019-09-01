package com.dls.base.repository;

import com.dls.base.entity.Lesson;
import com.dls.base.entity.TemplateLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplateLessonRepository extends JpaRepository<TemplateLesson, Long> {

	@Query("select tl from TemplateLesson tl where tl.id = :id")
	TemplateLesson findByTemplateLessonId(@Param("id") long id);

	@Query("select tl from TemplateLesson tl where tl.status.code = :statusCode")
	Set<TemplateLesson> findByStatusCode(@Param("statusCode") String statusCode);

	@Query("select tl from TemplateLesson tl where tl.version = :version")
	Set<TemplateLesson> findByVersion(@Param("version") long version);

	@Query(value = "with recursive r as ( " +
			"select id, " +
			"description, " +
			"name, " +
			"version, " +
			"author_id, " +
			"parent_id, " +
			"status_id, " +
			"date_create, " +
			"category_id " +
			"from template_lesson where id = ?1 " +
			"union all " +
			"select template_lesson.id, " +
			"template_lesson.description, " +
			"template_lesson.name, " +
			"template_lesson.version, " +
			"template_lesson.author_id, " +
			"template_lesson.parent_id, " +
			"template_lesson.status_id, " +
			"template_lesson.date_create, " +
			"template_lesson.category_id " +
			"from template_lesson " +
			"join r on template_lesson.parent_id = r.id " +
			") select * from r order by r.version", nativeQuery = true)
	Set<TemplateLesson> findRecursiveByParentId(Long id);

	@Query("select tl from TemplateLesson tl where tl.parent.id = :parentId")
	Set<TemplateLesson> findByParentId(@Param("parentId") long parentId);

	@Query(value = "select * from template_lesson " +
			"where template_lesson.author_id = ?1"
			, nativeQuery = true)
	Set<TemplateLesson> findByAuthorId(long authorId);

	@Query("select tl from TemplateLesson tl where tl.category.id = :categoryId")
	Set<TemplateLesson> findByCategoryId(@Param("categoryId") Long categoryId);

	@Query("select tl from TemplateLesson tl where tl.status.code = :statusCode and tl.category.id = :categoryId")
	Set<TemplateLesson> findByStatusByCategoryId(@Param("statusCode") String statusCode, @Param("categoryId") Long categoryId);

	@Query("select tl from TemplateLesson tl where tl.status.code = :statusCode and tl.category.id = null")
	Set<TemplateLesson> findByStatusWithoutCategory(@Param("statusCode") String statusCode);

}
