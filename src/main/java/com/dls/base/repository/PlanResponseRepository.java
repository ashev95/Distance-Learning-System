package com.dls.base.repository;

import com.dls.base.entity.PlanResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface PlanResponseRepository extends JpaRepository<PlanResponse, Long> {

	@Query("select tc from PlanResponse tc where tc.id = :id")
	PlanResponse findByPlanResponseId(@Param("id") long id);

	@Query(value = "select plan_response.id, " +
			"plan_response.plan_id, " +
			"plan_response.response_class, " +
			"plan_response.response_id, " +
			"plan_response.position, " +
			"plan_response.block_id" +
			" from plan " +
			"join block on block.parent_class = 'plan' and block.parent_id = plan.id " +
			"join plan_response on block.id = plan_response.block_id " +
			"where plan.id = ?1 " +
			"order by block.position, plan_response.position"
			, nativeQuery = true)
	Set<PlanResponse> findByPlanId(Long planId);

	@Query(value = "select * from plan_response " +
			"where plan_response.response_class = ?1 and " +
            "plan_response.response_id = ?2"
			, nativeQuery = true)
	PlanResponse findByResponseClassAndResponseId(String responseClass, Long responseId);

	@Query(value = "select coalesce(max(plan_response.position),0) " +
			"from plan_response " +
			"where plan_response.plan_id = ?1"
			, nativeQuery = true)
	Long findLastPositionByPlanId(Long planId);

	@Query(value = "select * from plan_response " +
			"where plan_response.plan_id = ?1 and " +
            "plan_response.response_class = ?2 and " +
            "plan_response.response_id = ?3"
			, nativeQuery = true)
	PlanResponse findByPlanAndResponseClassAndResponseId(Long planId, String responseClass, Long responseId);

	@Query(value = "select plan_response.id, " +
			"plan_response.plan_id, " +
			"plan_response.response_class, " +
			"plan_response.response_id, " +
			"plan_response.position, " +
			"plan_response.block_id" +
			" from plan " +
			"join block on block.parent_class = 'plan' and block.parent_id = plan.id " +
			"join plan_response on block.id = plan_response.block_id " +
			"where plan.id = ?1 " +
			"order by block.position, plan_response.position"
			, nativeQuery = true)
	Set<PlanResponse> findPlanResponseByPlanId(Long planId);

	@Query(value = "select * from plan_response " +
			"where plan_response.block_id = ?1 " +
			"order by plan_response.position"
			, nativeQuery = true)
	Set<PlanResponse> findPlanResponseByBlockId(Long blockId);

}
