package com.dls.base.repository;

import com.dls.base.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface StatusRepository extends JpaRepository<Status, Long> {

	@Query("select tl from Status tl where tl.id = :id")
	Status findByStatusId(@Param("id") long id);

	@Query("select tl from Status tl where tl.code = :code")
	Status findByStatusCode(@Param("code") String code);

	/*
	@Query(value = "select " +
			"status.id, " +
			"status.code, " +
			"status.description, " +
			"status.name" +
			" from status where status.id in ( " +
			"(select move.from_status_id from template_life_cycle  " +
			"join life_cycle on template_life_cycle.life_cycle_id = life_cycle.id " +
			"join move on life_cycle.id = move.life_cycle_id " +
			"where template_life_cycle.template_class = ?1) " +
			"UNION " +
			"(select  move.to_status_id from template_life_cycle  " +
			"join life_cycle on template_life_cycle.life_cycle_id = life_cycle.id " +
			"join move on life_cycle.id = move.life_cycle_id " +
			"where template_life_cycle.template_class = ?1) " +
			")"
			, nativeQuery = true)
	Set<Status> findStatusByTemplateLifeCycleClass(String templateLifeCycleClass);
	 */

}
