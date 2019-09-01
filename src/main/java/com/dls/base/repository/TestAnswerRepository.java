package com.dls.base.repository;

import com.dls.base.entity.TestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TestAnswerRepository extends JpaRepository<TestAnswer, Long> {

	@Query("select tl from TestAnswer tl where tl.id = :id")
	TestAnswer findByTestAnswerId(@Param("id") long id);

	@Query(value = "select " +
			"test_answer.id, " +
			"test_answer.test_question_id, " +
			"test_answer.number " +
			" from test_variant_person " +
			"join test_question on test_variant_person.id = test_question.test_variant_person_id " +
			"join test_answer on test_question.id = test_answer.test_question_id " +
			"where test_variant_person.id = ?1 and test_question.number = ?2 and test_answer.number = ?3 "
			, nativeQuery = true)
	TestAnswer findByTestVariantPersonIdAndTestQuestionNumberAndTestAnswerNumber(Long testVariantPersonId, Long testQuestionNumber, Long testAnswerNumber);

	@Query(value = "select " +
			"test_answer.id, " +
			"test_answer.test_question_id, " +
			"test_answer.number " +
			" from test_variant_person " +
			"join test_question on test_variant_person.id = test_question.test_variant_person_id " +
			"join test_answer on test_question.id = test_answer.test_question_id " +
			"where test_variant_person.id = ?1 and test_question.number = ?2 "
			, nativeQuery = true)
	Set<TestAnswer> findByTestVariantPersonIdAndTestQuestionNumber(Long testVariantPersonId, Long testQuestionNumber);

}
