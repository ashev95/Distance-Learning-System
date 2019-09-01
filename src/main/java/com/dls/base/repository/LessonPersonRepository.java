package com.dls.base.repository;

import com.dls.base.entity.LessonPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface LessonPersonRepository extends JpaRepository<LessonPerson, Long> {

	@Query("select tl from LessonPerson tl where tl.id = :id")
	LessonPerson findByLessonPersonId(@Param("id") long id);

	@Query(value = "select tc from LessonPerson tc " +
			"where tc.lesson.id = :lessonId ")
	Set<LessonPerson> findByLessonId(@Param("lessonId") long lessonId);

	@Query(value = "select tc from LessonPerson tc " +
			"where tc.person.id = :personId ")
	Set<LessonPerson> findByPersonId(@Param("personId") long personId);

	@Query(value = "select tc from LessonPerson tc " +
			"where tc.person.id = :personId and tc.status.code in ('assigned', 'in_progress')")
	Set<LessonPerson> findByPersonIdInWorkStatus(@Param("personId") long personId);

	@Query(value = "select tc from LessonPerson tc " +
			"where tc.lesson.id = :lessonId and tc.status.code = :lessonPersonStatusCode")
	Set<LessonPerson> findByLessonIdAndLessonPersonStatusCode(@Param("lessonId") long lessonId, @Param("lessonPersonStatusCode") String lessonPersonStatusCode);

}
