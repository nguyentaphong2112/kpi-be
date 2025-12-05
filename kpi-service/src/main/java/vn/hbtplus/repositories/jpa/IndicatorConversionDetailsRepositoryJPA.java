/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.repositories.entity.IndicatorConversionDetailEntity;
import vn.hbtplus.repositories.entity.IndicatorConversionsEntity;

/**
 * Lop repository JPA ung voi bang kpi_indicator_conversions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface IndicatorConversionDetailsRepositoryJPA extends JpaRepository<IndicatorConversionDetailEntity, Long> {
    @Transactional
    void deleteByIndicatorConversionId(Long indicatorConversionId);
}
