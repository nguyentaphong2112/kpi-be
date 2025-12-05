/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.WarehouseManagersEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang stk_warehouse_managers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface WarehouseManagersRepositoryJPA extends JpaRepository<WarehouseManagersEntity, Long> {
    List<WarehouseManagersEntity> findByWarehouseId(Long warehouseId);
}
