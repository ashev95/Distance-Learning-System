package com.dls.base.repository;

import com.dls.base.entity.TemplateLifeCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TemplateLifeCycleRepository extends JpaRepository<TemplateLifeCycle, Long> {

	@Query("select tl from TemplateLifeCycle tl where tl.id = :id")
	TemplateLifeCycle findByTemplateLifeCycleId(@Param("id") long id);

	@Query(value = "select * from template_life_cycle " +
			"where template_life_cycle.template_class = ?1 "
			, nativeQuery = true)
	TemplateLifeCycle findByTemplateClass(String templateClass);

}
