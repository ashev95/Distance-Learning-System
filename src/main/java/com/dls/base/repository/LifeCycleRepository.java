package com.dls.base.repository;

import com.dls.base.entity.LifeCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface LifeCycleRepository extends JpaRepository<LifeCycle, Long> {

	@Query("select tl from LifeCycle tl where tl.id = :id")
	LifeCycle findByLifeCycleId(@Param("id") long id);

}
