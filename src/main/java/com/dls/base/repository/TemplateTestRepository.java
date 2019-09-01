package com.dls.base.repository;

import com.dls.base.entity.TemplateTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplateTestRepository extends JpaRepository<TemplateTest, Long> {

	@Query("select tl from TemplateTest tl where tl.id = :id")
	TemplateTest findByTemplateTestId(@Param("id") long id);

	@Query("select tl from TemplateTest tl where tl.status.code = :statusCode")
	Set<TemplateTest> findByStatusCode(@Param("statusCode") String statusCode);

	@Query("select tl from TemplateTest tl where tl.version = :version")
	Set<TemplateTest> findByVersion(@Param("version") long version);

	@Query(value = "with recursive r as ( " +
			"select id, " +
			"description, " +
			"name, " +
			"version, " +
			"author_id, " +
			"parent_id, " +
			"status_id, " +
			"date_create, " +
			"by_order, " +
			"deprecate_change_answer_count, " +
			"time_limit, " +
			"category_id " +
			"from template_test where id = ?1 " +
			"union all " +
			"select template_test.id, " +
			"template_test.description, " +
			"template_test.name, " +
			"template_test.version, " +
			"template_test.author_id, " +
			"template_test.parent_id, " +
			"template_test.status_id, " +
			"template_test.date_create, " +
			"template_test.by_order, " +
			"template_test.deprecate_change_answer_count, " +
			"template_test.time_limit, " +
			"template_test.category_id " +
			"from template_test " +
			"join r on template_test.parent_id = r.id " +
			") select * from r order by r.version", nativeQuery = true)
	Set<TemplateTest> findRecursiveByParentId(Long id);

	@Query("select tl from TemplateTest tl where tl.parent.id = :parentId")
	Set<TemplateTest> findByParentId(@Param("parentId") long parentId);

	@Query(value = "select * from template_test " +
			"where template_test.author_id = ?1"
			, nativeQuery = true)
	Set<TemplateTest> findByAuthorId(long authorId);

	//Вернуть все шаблоны
	@Query(value = "select template_test.id, " +
			"template_test.description, " +
			"template_test.name, " +
			"template_test.version, " +
			"template_test.author_id, " +
			"template_test.parent_id, " +
			"template_test.status_id, " +
			"template_test.date_create, " +
			"template_test.by_order, " +
			"template_test.deprecate_change_answer_count, " +
			"template_test.time_limit" +
			" from test " +
			"join template_test on test.template_test_id = template_test.id " +
			"where test.template_test_id in ( " +
			"select test.template_test_id from plan " +
			"join plan_response on plan.id = plan_response.plan_id and plan_response.response_class = 'test' " +
			"join test on plan_response.response_id = test.id " +
			"where plan.id = ?1 " +
			"UNION " +
			"select test.template_test_id  from plan " +
			"join plan_response on plan.id = plan_response.plan_id and plan_response.response_class = 'course' " +
			"join course on plan_response.response_id = course.id " +
			"join course_response on course_response.course_id = course.id and course_response.response_class = 'test' " +
			"join test on course_response.response_id = test.id " +
			"where plan.id = ?1 " +
			"UNION " +
			"select test.template_test_id from course " +
			"join course_response on course_response.course_id = course.id and course_response.response_class = 'test' and course.id not in (select plan_response.response_id from plan_response where plan_response.response_class = 'course') " +
			"join test on course_response.response_id = test.id " +
			"where course.id = ?2 " +
			"UNION " +
			"select test.template_test_id from test where test.id not in ( " +
			"select test.id from course " +
			"join course_response on course_response.course_id = course.id and course_response.response_class = 'test' " +
			"join test on course_response.response_id = test.id " +
			") " +
			"and test.id not in ( " +
			"select test.id from plan " +
			"join plan_response on plan.id = plan_response.plan_id and plan_response.response_class = 'test' " +
			"join test on plan_response.response_id = test.id " +
			") " +
			"and test.id = ?3 " +
			") "
			, nativeQuery = true)
	Set<TemplateTest> findByParentIds(long planId, long courseId, long testId);

	@Query("select tl from TemplateTest tl where tl.category.id = :categoryId")
	Set<TemplateTest> findByCategoryId(@Param("categoryId") Long categoryId);

	@Query("select tl from TemplateTest tl where tl.status.code = :statusCode and tl.category.id = :categoryId")
	Set<TemplateTest> findByStatusByCategoryId(@Param("statusCode") String statusCode, @Param("categoryId") Long categoryId);

	@Query("select tl from TemplateTest tl where tl.status.code = :statusCode and tl.category.id = null")
	Set<TemplateTest> findByStatusWithoutCategory(@Param("statusCode") String statusCode);

}