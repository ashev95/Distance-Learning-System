package com.dls.base.repository;

import com.dls.base.reports.container.StudentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {

	@Query(value = "select person.id as id, " +
			"person.surname, " +
			"person.name, " +
			"person.middlename, " +
			"(CASE WHEN person.gender = 0 THEN 'М' ELSE 'Ж' END) as gender, " +
			"person.email, " +
			"person.additionally " +
			"from person " +
			"join group1_person on person.id = group1_person.person_id " +
			"where group1_person.group1_id = ?1 and person.id in ( " +
			"select person.id from person " +
			"join person_role on person.id = person_role.person_id and person_role.role_code = 'STUDENT' " +
			") order by person.surname"
			, nativeQuery = true)
	List<StudentInfo> collect(long groupId);

}
