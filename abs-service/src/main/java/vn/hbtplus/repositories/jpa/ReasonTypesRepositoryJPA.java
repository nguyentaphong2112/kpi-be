/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.ReasonTypesEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang abs_reason_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ReasonTypesRepositoryJPA extends JpaRepository<ReasonTypesEntity, Long> {


    @Query("select a from ReasonTypesEntity a where a.isDeleted = :activeStatus order by a.name")
    List<ReasonTypesEntity> getListReasonLeavesEntitys(String activeStatus);
}
