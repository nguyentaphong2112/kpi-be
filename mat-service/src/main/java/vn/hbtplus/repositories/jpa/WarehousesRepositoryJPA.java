/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.WarehousesEntity;

import java.util.Optional;

/**
 * Lop repository JPA ung voi bang stk_warehouses
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface WarehousesRepositoryJPA extends JpaRepository<WarehousesEntity, Long> {

    Optional<WarehousesEntity> findByCode(String code);
}
