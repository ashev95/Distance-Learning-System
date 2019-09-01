package com.dls.base.repository;

import com.dls.base.entity.GroupPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface GroupPersonRepository extends JpaRepository<GroupPerson, Long> {

	@Query("select tc from GroupPerson tc where tc.id = :id")
	GroupPerson findByGroupPersonId(@Param("id") long id);

	@Query(value = "select * from group1_person " +
			"where group1_person.group1_id = ?1"
			, nativeQuery = true)
	Set<GroupPerson> findByGroupId(long groupId);

	@Query(value = "select * from group1_person " +
			"where group1_person.person_id = ?1"
			, nativeQuery = true)
	Set<GroupPerson> findByPersonId(Long personId);

	@Query(value = "select tc from GroupPerson tc " +
			"where tc.group.id = :groupId and " +
			"tc.person.id = :personId")
	GroupPerson findByGroupAndPersonId(@Param("groupId") long groupId, @Param("personId") long personId);

}
