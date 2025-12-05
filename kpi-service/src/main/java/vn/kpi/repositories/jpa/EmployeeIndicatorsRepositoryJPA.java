/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.EmployeeIndicatorsEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang kpi_employee_indicators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface EmployeeIndicatorsRepositoryJPA extends JpaRepository<EmployeeIndicatorsEntity, Long> {

    @Query("SELECT e.indicatorId FROM EmployeeIndicatorsEntity e WHERE e.employeeEvaluationId = :employeeEvaluationId AND e.isDeleted = 'N'")
    List<Long> findIndicatorIdsByEmployeeEvaluationId(@Param("employeeEvaluationId") Long employeeEvaluationId);
}
