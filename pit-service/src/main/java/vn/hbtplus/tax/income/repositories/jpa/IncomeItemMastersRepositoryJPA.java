/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.tax.income.repositories.entity.IncomeItemMastersEntity;

/**
 * Lop repository JPA ung voi bang pit_income_item_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface IncomeItemMastersRepositoryJPA extends JpaRepository<IncomeItemMastersEntity, Long> {

}
