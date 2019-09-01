package com.dls.base.repository;

import com.dls.base.entity.TemplateTestVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
public interface TemplateTestVariantRepository extends JpaRepository<TemplateTestVariant, Long> {

	@Query("select tt from TemplateTestVariant tt where tt.id = :id")
	TemplateTestVariant findByTemplateTestVariantId(@Param("id") long id);

	@Query(value = "select * from template_test_variant " +
			"where template_test_id = ?1 " +
			"order by template_test_variant.number"
			, nativeQuery = true)
	Set<TemplateTestVariant> findByTemplateTestId(long templateTestId);

	@Query(value = "select * from template_test_variant " +
			"where template_test_id = ?1 and number = ?2"
			, nativeQuery = true)
	Set<TemplateTestVariant> findByTemplateTestIdAndNumber(long templateTestId, long number);

	@Query(value = "select (coalesce(max(template_test_variant.number),0)) " +
			"from template_test_variant " +
			"where template_test_variant.template_test_id = ?1"
			, nativeQuery = true)
	Long findLastPositionByTemplateTestId(Long templateTestId);

}
