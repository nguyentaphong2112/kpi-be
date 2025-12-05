/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.IndicatorsEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang kpi_indicators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface IndicatorsRepositoryJPA extends JpaRepository<IndicatorsEntity, Long> {

    List<IndicatorsEntity> findByOrganizationId(Long organizationId);
}
