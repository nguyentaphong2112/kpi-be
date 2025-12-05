/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.InventoryAdjustmentsEntity;

import java.util.Collection;
import java.util.List;

/**
 * Lop repository JPA ung voi bang stk_inventory_adjustments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface InventoryAdjustmentsRepositoryJPA extends JpaRepository<InventoryAdjustmentsEntity, Long> {

    List<InventoryAdjustmentsEntity> findByInventoryAdjustmentIdIn(Collection<Long> inventoryAdjustmentIds);
}
