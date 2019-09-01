package com.dls.base.repository;

import com.dls.base.entity.Person;
import com.dls.base.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TestRepository extends JpaRepository<Test, Long> {

	@Query("select tl from Test tl where tl.id = :id")
	Test findByTestId(@Param("id") long id);

	@Query(value = "select tl from Test tl where tl.curator = :id")
	Set<Test> findByCurator(@Param("id") Person person);

	@Query(value = "select * from test " +
			"where test.id not in (" +
			"select test.id from test " +
			"join course_response on test.id = course_response.response_id and course_response.response_class = 'test') " +
			"and test.id not in (" +
			"select test.id from test " +
			"join plan_response on test.id = plan_response.response_id and plan_response.response_class = 'test'" +
			")"
			, nativeQuery = true)
	Set<Test> findAllParent();

	@Query(value = "select * from test " +
			"where test.id not in (" +
			"select test.id from test " +
			"join course_response on test.id = course_response.response_id and course_response.response_class = 'test') " +
			"and test.id not in (" +
			"select test.id from test " +
			"join plan_response on test.id = plan_response.response_id and plan_response.response_class = 'test'" +
			") and test.curator_id = ?1"
			, nativeQuery = true)
	Set<Test> findParentByCuratorId(long curatorId);

	@Query(value = "select * from test " +
			"where test.id not in (" +
			"select test.id from test " +
			"join course_response on test.id = course_response.response_id and course_response.response_class = 'test') " +
			"and test.id not in (" +
			"select test.id from test " +
			"join plan_response on test.id = plan_response.response_id and plan_response.response_class = 'test'" +
			") and test.author_id = ?1"
			, nativeQuery = true)
	Set<Test> findParentByAuthorId(long authorId);

	@Query(value = "select * from test " +
			"where test.id not in (" +
			"select test.id from test " +
			"join course_response on test.id = course_response.response_id and course_response.response_class = 'test') " +
			"and test.id not in (" +
			"select test.id from test " +
			"join plan_response on test.id = plan_response.response_id and plan_response.response_class = 'test'" +
			") and test.status_id = ?1"
			, nativeQuery = true)
	Set<Test> findParentByStatusId(long statusId);

}
