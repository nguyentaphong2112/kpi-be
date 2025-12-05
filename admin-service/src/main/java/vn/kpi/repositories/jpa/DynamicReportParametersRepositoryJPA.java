/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.DynamicReportParametersEntity;

/**
 * Lop repository JPA ung voi bang sys_dynamic_report_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface DynamicReportParametersRepositoryJPA extends JpaRepository<DynamicReportParametersEntity, Long> {

}
