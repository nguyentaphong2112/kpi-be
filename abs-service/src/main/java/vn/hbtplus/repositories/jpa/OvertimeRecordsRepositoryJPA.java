/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.OvertimeRecordsEntity;

/**
 * Lop repository JPA ung voi bang abs_overtime_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface OvertimeRecordsRepositoryJPA extends JpaRepository<OvertimeRecordsEntity, Long> {

}
