package com.dls.base.repository;

import com.dls.base.entity.Group;
import com.dls.base.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface GroupRepository extends JpaRepository<Group, Long> {

	@Query("select g from Group g where g.id = :id")
	Group findByGroupId(@Param("id") long id);

	@Query(value = "select g from Group g where g.curator = :id")
	Set<Group> findByCurator(@Param("id") Person person);

	@Query(value = "select group1.id, " +
			"group1.name, " +
			"group1.curator_id " +
			"from group1 " +
			"where group1.name = ?1 and group1.curator_id = ?2"
			, nativeQuery = true)
	Group findByNameAndCuratorId(String name, Long curatorId);

	@Query(value = "select group1.id, " +
			"group1.name, " +
			"group1.curator_id " +
			"from group1_person " +
			"join group1 on group1_person.group1_id = group1.id " +
			"where group1_person.person_id = ?1 "
			, nativeQuery = true)
	Set<Group> findByPersonId(Long personId);

}