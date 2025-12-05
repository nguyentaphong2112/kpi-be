/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.PositionSalaryProcessEntity;

import java.util.Date;
import java.util.List;

/**
 * Lop repository JPA ung voi bang hr_position_salary_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface PositionSalaryProcessRepositoryJPA extends JpaRepository<PositionSalaryProcessEntity, Long> {

    List<PositionSalaryProcessEntity> findByEmployeeIdAndStartDate(Long employeeId, Date startDate);

    List<PositionSalaryProcessEntity> findByEmployeeIdAndStartDateAndIsDeleted(Long employeeId, Date oldStartDate, String n);
}
