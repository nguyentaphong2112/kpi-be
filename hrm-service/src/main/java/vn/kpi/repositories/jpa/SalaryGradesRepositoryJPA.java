/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.SalaryGradesEntity;

/**
 * Lop repository JPA ung voi bang hr_salary_grades
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface SalaryGradesRepositoryJPA extends JpaRepository<SalaryGradesEntity, Long> {

}
