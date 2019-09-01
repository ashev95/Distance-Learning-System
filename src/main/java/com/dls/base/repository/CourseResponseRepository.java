package com.dls.base.repository;

import com.dls.base.entity.CourseResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface CourseResponseRepository extends JpaRepository<CourseResponse, Long> {

	@Query("select tc from CourseResponse tc where tc.id = :id")
    CourseResponse findByCourseResponseId(@Param("id") long id);

	@Query(value = "select course_response.id, " +
			"course_response.course_id, " +
			"course_response.response_class, " +
			"course_response.response_id, " +
			"course_response.position, " +
			"course_response.block_id" +
			" from course " +
			"join block on block.parent_class = 'course' and block.parent_id = course.id " +
			"join course_response on block.id = course_response.block_id " +
			"where course.id = ?1 " +
			"order by block.position, course_response.position"
			, nativeQuery = true)
	Set<CourseResponse> findByCourseId(Long courseId);

	@Query(value = "select * from course_response " +
			"where course_response.response_class = ?1 and " +
            "course_response.response_id = ?2"
			, nativeQuery = true)
	CourseResponse findByResponseClassAndResponseId(String responseClass, Long responseId);

	@Query(value = "select coalesce(max(course_response.position),0) " +
			"from course_response " +
			"where course_response.course_id = ?1"
			, nativeQuery = true)
	Long findLastPositionByCourseId(Long courseId);

	@Query(value = "select * from course_response " +
			"where course_response.course_id = ?1 and " +
            "course_response.response_class = ?2 and " +
            "course_response.response_id = ?3"
			, nativeQuery = true)
    CourseResponse findByCourseAndResponseClassAndResponseId(Long courseId, String responseClass, Long responseId);

	@Query(value = "select * from course_response " +
			"where course_response.block_id = ?1 " +
			"order by course_response.position"
			, nativeQuery = true)
	Set<CourseResponse> findCourseResponseByBlockId(Long blockId);

}
