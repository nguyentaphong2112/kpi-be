/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.EmployeesEntity;

/**
 * Lop repository JPA ung voi bang crm_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface EmployeesRepositoryJPA extends JpaRepository<EmployeesEntity, Long> {
    @Query("from EmployeesEntity a where a.isDeleted = 'N' and upper(a.loginName) like upper(:loginName)")
    EmployeesEntity getEmployeeByLoginName(String loginName);
}
