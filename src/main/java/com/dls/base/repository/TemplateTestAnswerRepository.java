package com.dls.base.repository;

import com.dls.base.entity.TemplateTestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplateTestAnswerRepository extends JpaRepository<TemplateTestAnswer, Long> {

	@Query("select tt from TemplateTestAnswer tt where tt.id = :id")
	TemplateTestAnswer findByTemplateTestAnswerId(@Param("id") long id);

	@Query(value = "select * from template_test_answer " +
			"where template_test_question_id = ?1 " +
			"order by template_test_answer.number"
			, nativeQuery = true)
	Set<TemplateTestAnswer> findByTemplateTestQuestionId(long templateTestQuestionId);

	@Query(value = "select * from template_test_answer " +
			"where template_test_question_id = ?1 and number = ?2"
			, nativeQuery = true)
	Set<TemplateTestAnswer> findByTemplateTestQuestionIdAndNumber(long templateTestQuestionId, long number);

	@Query(value = "select (coalesce(max(template_test_answer.number),0)) from template_test_question " +
			"join template_test_answer on template_test_question.id = template_test_answer.template_test_question_id " +
			"where template_test_question.id = ?1"
			, nativeQuery = true)
	Long findLastPositionByTemplateTestQuestionId(Long templateTestQuestionId);

}