package com.dls.base.repository;

import com.dls.base.entity.Lesson;
import com.dls.base.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface LessonRepository extends JpaRepository<Lesson, Long> {

	@Query("select tl from Lesson tl where tl.id = :id")
	Lesson findByLessonId(@Param("id") long id);

	@Query(value = "select tl from Lesson tl where tl.curator = :id")
	Set<Lesson> findByCurator(@Param("id") Person person);

	@Query(value = "select * from lesson " +
			"where lesson.id not in (" +
				"select lesson.id from lesson " +
					"join course_response on lesson.id = course_response.response_id and course_response.response_class = 'lesson') " +
				"and lesson.id not in (" +
				"select lesson.id from lesson " +
					"join plan_response on lesson.id = plan_response.response_id and plan_response.response_class = 'lesson'" +
			")"
			, nativeQuery = true)
	Set<Lesson> findAllParent();

	@Query(value = "select * from lesson " +
			"where lesson.id not in (" +
			"select lesson.id from lesson " +
			"join course_response on lesson.id = course_response.response_id and course_response.response_class = 'lesson') " +
			"and lesson.id not in (" +
			"select lesson.id from lesson " +
			"join plan_response on lesson.id = plan_response.response_id and plan_response.response_class = 'lesson'" +
			") and lesson.curator_id = ?1"
			, nativeQuery = true)
	Set<Lesson> findParentByCuratorId(long curatorId);

	@Query(value = "select * from lesson " +
			"where lesson.id not in (" +
			"select lesson.id from lesson " +
			"join course_response on lesson.id = course_response.response_id and course_response.response_class = 'lesson') " +
			"and lesson.id not in (" +
			"select lesson.id from lesson " +
			"join plan_response on lesson.id = plan_response.response_id and plan_response.response_class = 'lesson'" +
			") and lesson.author_id = ?1"
			, nativeQuery = true)
	Set<Lesson> findParentByAuthorId(long authorId);

	@Query(value = "select * from lesson " +
			"where lesson.id not in (" +
			"select lesson.id from lesson " +
			"join course_response on lesson.id = course_response.response_id and course_response.response_class = 'lesson') " +
			"and lesson.id not in (" +
			"select lesson.id from lesson " +
			"join plan_response on lesson.id = plan_response.response_id and plan_response.response_class = 'lesson'" +
			") and lesson.status_id = ?1"
			, nativeQuery = true)
	Set<Lesson> findParentByStatusId(long statusId);

}
