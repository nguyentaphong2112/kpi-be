/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.PositionGroupConfigsEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang hr_position_group_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface PositionGroupConfigsRepositoryJPA extends JpaRepository<PositionGroupConfigsEntity, Long> {

    @Modifying
    @Query("update PositionGroupConfigsEntity a set a.isDeleted = 'Y', modifiedBy=:userName," +
            "   modifiedTime=sysdate() " +
            " where a.positionGroupId = :positionGroupId " +
            " and a.positionGroupConfigId not in (:configIds)" +
            " and a.isDeleted = 'N'")
    void inactiveConfigNotIn(Long positionGroupId, List<Long> configIds, String userName);
}
