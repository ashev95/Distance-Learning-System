package com.dls.base.repository;

import com.dls.base.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface PersonRepository extends JpaRepository<Person, Long> {

	@Query("select p from Person p where p.id = :id")
	Person findByPersonId(@Param("id") long id);

	@Query("select p from Person p where p.username = :username")
	Person findByUsername(@Param("username") String username);

	@Query("select p from Person p where p.email = :email")
	Person findByEmail(@Param("email") String email);

	@Query(value = "select * from person " +
			"join person_role on person.id = person_role.person_id " +
			"where person_role.role_code = ?1"
			, nativeQuery = true)
	Set<Person> findByRoleCode(String role_code);

	@Query(value = "select * from person where person.id not in (select person_role.person_id from person_role)"
			, nativeQuery = true)
	Set<Person> findAllWithoutRole();

	@Query(value = "select * from person where person.id in (select person_role.person_id from person_role)"
			, nativeQuery = true)
	Set<Person> findActive();

	@Query(value = "select person.id, " +
			"person.username, " +
			"person.password, " +
			"person.email, " +
			"person.surname, " +
			"person.name, " +
			"person.middlename, " +
			"person.gender, " +
			"person.additionally " +
			" from person " +
			"join group1_person on person.id = group1_person.person_id " +
			"where group1_person.group1_id = ?1 and person.id in ( " +
			"select person.id from person " +
			"join person_role on person.id = person_role.person_id and person_role.role_code = 'STUDENT' " +
			")"
			, nativeQuery = true)
	Set<Person> findActiveStudentsByGroupId(long groupId);

}
