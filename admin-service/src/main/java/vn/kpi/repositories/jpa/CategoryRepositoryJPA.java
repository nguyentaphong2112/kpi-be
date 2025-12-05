/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.CategoryEntity;

import java.util.Optional;

/**
 * Lop repository JPA ung voi bang sys_categories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface CategoryRepositoryJPA extends JpaRepository<CategoryEntity, Long> {

    @Modifying
    @Query("delete from CategoryEntity a where a.categoryType = :categoryType and a.value = :value and a.isDeleted = 'Y'")
    void deleteOldValue(String categoryType, String value);

    Optional<CategoryEntity> findByCategoryTypeAndValue(String categoryType, String value);
}
