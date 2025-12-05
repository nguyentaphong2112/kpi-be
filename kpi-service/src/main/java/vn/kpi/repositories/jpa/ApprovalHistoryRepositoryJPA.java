/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.repositories.entity.ApprovalHistoryEntity;

/**
 * Lop repository JPA ung voi bang kpi_employee_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
public interface ApprovalHistoryRepositoryJPA extends JpaRepository<ApprovalHistoryEntity, Long> {

    @Query("select a from ApprovalHistoryEntity a " +
           " where a.tableName = :tableName" +
           "    and a.objectId = :objectId" +
           "    and a.isDeleted = 'N'" +
           "    and a.status = '" + ApprovalHistoryEntity.STATUS.WAITING + "'")
    ApprovalHistoryEntity getWaitingApproval(String tableName, Long objectId);

    @Transactional
    @Query("update ApprovalHistoryEntity a " +
           "    set a.isDeleted = 'Y'" +
           " where a.tableName = :tableName" +
           "    and a.objectId = :objectId" +
           "    and a.isDeleted = 'N'")
    @Modifying
    void inactiveOldData(String tableName, Long objectId);
}
