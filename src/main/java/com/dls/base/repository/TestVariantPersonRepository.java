package com.dls.base.repository;

import com.dls.base.entity.TestVariantPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Set;

@Transactional(readOnly = true)
public interface TestVariantPersonRepository extends JpaRepository<TestVariantPerson, Long> {

	@Query("select tl from TestVariantPerson tl where tl.id = :id")
	TestVariantPerson findByTestVariantPersonId(@Param("id") long id);

	@Query(value = "select tc from TestVariantPerson tc " +
			"where tc.test.id = :testId ")
	Set<TestVariantPerson> findByTestId(@Param("testId") long testId);

	/*
	@Query(value = "select tc from TestVariantPerson tc " +
			"where tc.test.id = :testId and tc.status.code = :statusCode")
	Set<TestVariantPerson> findByTestIdAndStatusCode(@Param("testId") long testId, @Param("statusCode") String statusCode);
	 */

	@Query(value = "select tc from TestVariantPerson tc " +
			"where tc.person.id = :personId ")
	Set<TestVariantPerson> findByPersonId(@Param("personId") long personId);

	@Query(value = "select tc from TestVariantPerson tc " +
			"where tc.person.id = :personId and tc.status.code in ('assigned', 'in_progress')")
	Set<TestVariantPerson> findByPersonIdInWorkStatus(@Param("personId") long personId);

	@Query(value = "select tc from TestVariantPerson tc " +
			"where tc.person.id = :personId and tc.status.code = :statusCode")
	Set<TestVariantPerson> findByPersonIdAndStatusCode(@Param("personId") long personId, @Param("statusCode") String statusCode);

	@Query(value = "select tc from TestVariantPerson tc " +
			"where tc.test.id = :testId and tc.status.code = :testVariantPersonStatusCode")
	Set<TestVariantPerson> findByTestIdAndTestVariantPersonStatusCode(@Param("testId") long testId, @Param("testVariantPersonStatusCode") String testVariantPersonStatusCode);

	@Query(value = "select " +
			"test_variant_person.id, " +
			"test_variant_person.test_id, " +
			"test_variant_person.template_test_variant_id, " +
			"test_variant_person.person_id, " +
			"test_variant_person.status_id, " +
			"test_variant_person.date_start, " +
			"test_variant_person.date_end, " +
			"test_variant_person.current_template_test_question_id, " +
			"test_variant_person.correct_answer_count, " +
			"test_variant_person.incorrect_answer_count, " +
			"test_variant_person.total_score, " +
			"test_variant_person.available_score " +
			" from test_variant_person  " +
			"join template_test_variant on test_variant_person.template_test_variant_id = template_test_variant.id " +
			"join template_test on  template_test_variant.template_test_id = template_test.id " +
			"where test_variant_person.status_id = (select id from status where code = 'in_progress') " +
			"and template_test.time_limit > 0 " +
			"and extract(epoch from (now() - test_variant_person.date_start)) > (template_test.time_limit * 60)"
			, nativeQuery = true)
	Set<TestVariantPerson> findAllExpiredInProgress();

}
