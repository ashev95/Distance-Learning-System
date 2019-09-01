package com.dls.base.repository;

import com.dls.base.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("select cl from Category cl where cl.id = :id")
	Category findByCategoryId(@Param("id") long id);

	@Query("select p from Category p where p.name = :name")
	Category findByName(@Param("name") String name);

}
