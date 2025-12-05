/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.WorkdayTypesEntity;

/**
 * Lop repository JPA ung voi bang abs_workday_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface WorkdayTypesRepositoryJPA extends JpaRepository<WorkdayTypesEntity, Long> {

    @Query(" SELECT wcd.workdayTypeId "
            + " FROM WorkdayTypesEntity wcd "
            + " WHERE wcd.code = :code" +
            "   and wcd.isDeleted = 'N'")
    public Long getWorkdayTypeIdByCode(String code);
}
