package com.dls.base.repository;

import com.dls.base.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface ResourceRepository extends JpaRepository<Resource, Long> {

	@Query("select r from Resource r where r.id = :id")
	Resource findByFileId(@Param("id") long id);

	@Query("select r from Resource r where r.entityClass = :entityClass and r.entityId = :entityId")
	Set<Resource> findByEntityClassAndEntityId(@Param("entityClass") String entityClass, @Param("entityId") Long entityId);

}
