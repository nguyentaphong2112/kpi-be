/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.PositionsEntity;

/**
 * Lop repository JPA ung voi bang hr_positions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface PositionsRepositoryJPA extends JpaRepository<PositionsEntity, Long> {

    PositionsEntity findByOrganizationIdAndJobId(Long organizationId, Long jobId);
}
