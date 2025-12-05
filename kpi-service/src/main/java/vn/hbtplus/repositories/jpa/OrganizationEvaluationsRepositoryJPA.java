/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.OrganizationEvaluationsEntity;

/**
 * Lop repository JPA ung voi bang kpi_organization_evaluations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface OrganizationEvaluationsRepositoryJPA extends JpaRepository<OrganizationEvaluationsEntity, Long> {

}
