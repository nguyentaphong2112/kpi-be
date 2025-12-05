/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.EmployeesEntity;

/**
 * Lop repository JPA ung voi bang hr_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface EmployeesRepositoryJPA extends JpaRepository<EmployeesEntity, Long> {

    @Query("select employeeId from EmployeesEntity where employeeCode = :employeeCode and isDeleted = 'N'")
    Long getIdByEmployeeCode(String employeeCode);
}
