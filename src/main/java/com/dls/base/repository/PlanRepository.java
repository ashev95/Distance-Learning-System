package com.dls.base.repository;

import com.dls.base.entity.Person;
import com.dls.base.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface PlanRepository extends JpaRepository<Plan, Long> {

	@Query("select tl from Plan tl where tl.id = :id")
	Plan findByPlanId(@Param("id") long id);

	@Query(value = "select tl from Plan tl where tl.curator = :id")
	Set<Plan> findByCurator(@Param("id") Person person);

	@Query(value = "select tl from Plan tl where tl.author = :id")
	Set<Plan> findByAuthor(@Param("id") Person person);

	@Query(value = "select * from plan " +
			"where plan.id not in (" +
			"select plan.id from plan " +
			"join plan_response on plan.id = plan_response.response_id and plan_response.response_class = 'plan'" +
			") and plan.curator_id = ?1"
			, nativeQuery = true)
	Set<Plan> findParentByCuratorId(long curatorId);

	@Query(value = "select * from plan " +
			"where plan.id not in (" +
			"select plan.id from plan " +
			"join plan_response on plan.id = plan_response.response_id and plan_response.response_class = 'plan'" +
			") and plan.author_id = ?1"
			, nativeQuery = true)
	Set<Plan> findParentByAuthorId(long authorId);

	@Query(value = "select * from plan " +
			"where plan.id not in (" +
			"select plan.id from plan " +
			"join plan_response on plan.id = plan_response.response_id and plan_response.response_class = 'plan'" +
			") and plan.status_id = ?1"
			, nativeQuery = true)
	Set<Plan> findParentByStatusId(long statusId);

}
