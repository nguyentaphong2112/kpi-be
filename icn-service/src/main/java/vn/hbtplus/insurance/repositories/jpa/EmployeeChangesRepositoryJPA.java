/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.insurance.repositories.entity.EmployeeChangesEntity;

/**
 * Lop repository JPA ung voi bang icn_employee_changes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface EmployeeChangesRepositoryJPA extends JpaRepository<EmployeeChangesEntity, Long> {

}
