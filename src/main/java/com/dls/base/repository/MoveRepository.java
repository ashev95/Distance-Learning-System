package com.dls.base.repository;

import com.dls.base.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface MoveRepository extends JpaRepository<Move, Long> {

	@Query("select tl from Move tl where tl.id = :id")
	Move findByMoveId(@Param("id") long id);

	@Query(value = "select * from template_life_cycle " +
			"join move on template_life_cycle.life_cycle_id = move.life_cycle_id " +
			"where template_life_cycle.template_class = ?1 and move.from_status_id = ?2 and move.to_status_id = ?3"
			, nativeQuery = true)
	Move findByTemplateClassAndFromStatusIdAndToStatusId(String templateClass, Long fromStatusId, Long toStatusId);

}
