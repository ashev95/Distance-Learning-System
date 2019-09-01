package com.dls.base.repository;

import com.dls.base.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RoleRepository extends JpaRepository<Role, String> {

	@Query("select r from Role r where r.code = :code")
	Role findByRoleCode(@Param("code") String code);

}
