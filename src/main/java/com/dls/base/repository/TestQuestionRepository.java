package com.dls.base.repository;

import com.dls.base.entity.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

	@Query("select tl from TestQuestion tl where tl.id = :id")
	TestQuestion findByTestQuestionId(@Param("id") long id);

	@Query(value = "select " +
			"test_question.id, " +
			"test_question.number, " +
			"test_question.test_variant_person_id, " +
			"test_question.current_deprecate_change_answer_counter, " +
			"test_question.visited " +
			" from test_variant_person " +
			"join test_question on test_variant_person.id = test_question.test_variant_person_id " +
			"where test_variant_person.id = ?1 and test_question.number = ?2 "
			, nativeQuery = true)
	TestQuestion findByTestVariantPersonIdAndTestQuestionNumber(Long testVariantPersonId, Long testQuestionNumber);

	@Query("select tl from TestQuestion tl where tl.testVariantPerson.id = :testVariantPersonId")
	Set <TestQuestion> findByTestVariantPersonId(@Param("testVariantPersonId") long testVariantPersonId);

}