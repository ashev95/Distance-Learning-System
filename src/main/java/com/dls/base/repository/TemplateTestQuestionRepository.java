package com.dls.base.repository;

import com.dls.base.entity.TemplateTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplateTestQuestionRepository extends JpaRepository<TemplateTestQuestion, Long> {

	@Query("select tt from TemplateTestQuestion tt where tt.id = :id")
	TemplateTestQuestion findByTemplateTestQuestionId(@Param("id") long id);

	@Query(value = "select * from template_test_question " +
			"where template_test_variant_id = ?1 " +
			"order by template_test_question.number"
			, nativeQuery = true)
	Set<TemplateTestQuestion> findByTemplateTestVariantId(long templateTestVariantId);

	@Query(value = "select * from template_test_question " +
			"where template_test_variant_id = ?1 and number = ?2"
			, nativeQuery = true)
	Set<TemplateTestQuestion> findByTemplateTestVariantIdAndNumber(long templateTestVariantId, long number);

	@Query(value = "select (coalesce(max(template_test_question.number),0)) " +
			"from template_test_variant " +
			"join template_test_question on template_test_variant.id = template_test_question.template_test_variant_id " +
			"and template_test_variant.id = ?1"
			, nativeQuery = true)
	Long findLastPositionByTemplateTestVariantId(Long templateTestVariantId);

}
