package com.dls.base.repository;

import com.dls.base.entity.Course;
import com.dls.base.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface CourseRepository extends JpaRepository<Course, Long> {

	@Query("select tl from Course tl where tl.id = :id")
	Course findByCourseId(@Param("id") long id);

	@Query(value = "select tl from Course tl where tl.curator = :id")
	Set<Course> findByCurator(@Param("id") Person person);

	@Query(value = "select * from course " +
			"where course.id not in (" +
			"select course.id from course " +
			"join plan_response on course.id = plan_response.response_id and plan_response.response_class = 'course'" +
			")"
			, nativeQuery = true)
	Set<Course> findAllParent();

	@Query(value = "select * from course " +
			"where course.id not in (" +
			"select course.id from course " +
			"join plan_response on course.id = plan_response.response_id and plan_response.response_class = 'course'" +
			") and course.curator_id = ?1"
			, nativeQuery = true)
	Set<Course> findParentByCuratorId(long curatorId);

	@Query(value = "select * from course " +
			"where course.id not in (" +
			"select course.id from course " +
			"join plan_response on course.id = plan_response.response_id and plan_response.response_class = 'course'" +
			") and course.author_id = ?1"
			, nativeQuery = true)
	Set<Course> findParentByAuthorId(long authorId);

	@Query(value = "select * from course " +
			"where course.id not in (" +
			"select course.id from course " +
			"join plan_response on course.id = plan_response.response_id and plan_response.response_class = 'course'" +
			") and course.status_id = ?1"
			, nativeQuery = true)
	Set<Course> findParentByStatusId(long statusId);
	
}
