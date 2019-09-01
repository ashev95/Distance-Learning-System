package com.dls.base.repository;

import com.dls.base.reports.container.TrainingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface TrainingInfoRepository extends JpaRepository<TrainingInfo, Long> {

	@Query(value = "" +
			"select test_variant_person.id as id, " +
			"'Тест' as type, test.name as name, test.date_start as trainingstart, test.date_end as trainingend, test_variant_person.date_start as variantstart, test_variant_person.date_end as variantend, (CAST(test_variant_person.total_score AS text) || '/' || CAST(test_variant_person.available_score AS text)) as score from status " +
			"join test on status.id = test.status_id " +
			"join test_variant_person on test_variant_person.test_id = test.id " +
			" where test_variant_person.test_id in ( " +
			"select test.id from test  " +
			"where test.id not in ( " +
			"select test.id from test  " +
			"join course_response on test.id = course_response.response_id and course_response.response_class = 'test')  " +
			"and test.id not in ( " +
			"select test.id from test  " +
			"join plan_response on test.id = plan_response.response_id and plan_response.response_class = 'test' " +
			") " +
			")  " +
			"and status.code = 'completed' " +
			"and test_variant_person.date_start is not null " +
			"and test_variant_person.person_id = ?1 " +
			"UNION " +
			"select test_variant_person.id as id, " +
			"'Курс' as type, course.name as name, course.date_start as trainingstart, course.date_end as trainingend, test_variant_person.date_start as variantstart, test_variant_person.date_end as variantend, (CAST(test_variant_person.total_score AS text) || '/' || CAST(test_variant_person.available_score AS text)) as score from status " +
			"join course on status.id = course.status_id " +
			"join course_response on course.id = course_response.course_id " +
			"join test on course_response.response_id = test.id and course_response.response_class = 'test'  " +
			"join test_variant_person on test.id = test_variant_person.test_id " +
			"and test_variant_person.date_start is not null " +
			"where course_response.course_id in ( " +
			"select course.id from course  " +
			"where course.id not in ( " +
			"select course.id from course  " +
			"join plan_response on course.id = plan_response.response_id and plan_response.response_class = 'course' " +
			") " +
			") and status.code = 'completed' " +
			"and test_variant_person.person_id = ?1 " +
			"UNION " +
			"select test_variant_person.id as id, " +
			"'План' as type, plan.name as name, plan.date_start as trainingstart, plan.date_end as trainingend, test_variant_person.date_start as variantstart, test_variant_person.date_end as variantend, (CAST(test_variant_person.total_score AS text) || '/' || CAST(test_variant_person.available_score AS text)) as score from status " +
			"join plan on status.id = plan.status_id " +
			"join plan_response on plan.id = plan_response.plan_id and plan_response.response_class = 'test' " +
			"join test on plan_response.response_id = test.id " +
			"join test_variant_person on test.id = test_variant_person.test_id and test_variant_person.person_id = ?1 " +
			"and status.code = 'completed' " +
			"and test_variant_person.date_start is not null " +
			"and test_variant_person.person_id = ?1 " +
			"UNION " +
			"select test_variant_person.id as id, " +
			"'План' as type, plan.name as name, plan.date_start as trainingstart, plan.date_end as trainingend, test_variant_person.date_start as variantstart, test_variant_person.date_end as variantend, (CAST(test_variant_person.total_score AS text) || '/' || CAST(test_variant_person.available_score AS text)) as score from status " +
			"join plan on status.id = plan.status_id " +
			"join plan_response on plan.id = plan_response.plan_id and plan_response.response_class = 'course' " +
			"join course on plan_response.response_id = course.id " +
			"join course_response on course.id = course_response.course_id and course_response.response_class = 'test' " +
			"join test on course_response.response_id = test.id " +
			"join test_variant_person on test.id = test_variant_person.test_id and test_variant_person.person_id = ?1 " +
			"and status.code = 'completed' " +
			"and test_variant_person.date_start is not null " +
			"and test_variant_person.person_id = ?1 " +
			"order by 1"
			, nativeQuery = true)
	List<TrainingInfo> collect(long personId);

}
