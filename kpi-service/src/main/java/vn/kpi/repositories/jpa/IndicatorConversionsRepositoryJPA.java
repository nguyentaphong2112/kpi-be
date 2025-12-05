/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.IndicatorConversionsEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang kpi_indicator_conversions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface IndicatorConversionsRepositoryJPA extends JpaRepository<IndicatorConversionsEntity, Long> {
    @Query("select a from IndicatorConversionsEntity a where a.isDeleted = 'N' and a.indicatorMasterId = :indicatorMasterId")
    List<IndicatorConversionsEntity> findByIndicatorMasterId(Long indicatorMasterId);
}
