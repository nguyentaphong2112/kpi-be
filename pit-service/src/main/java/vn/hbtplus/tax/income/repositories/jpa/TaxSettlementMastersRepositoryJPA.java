/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.tax.income.repositories.entity.TaxSettlementMastersEntity;

/**
 * Lop repository JPA ung voi bang pit_tax_settlement_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface TaxSettlementMastersRepositoryJPA extends JpaRepository<TaxSettlementMastersEntity, Long> {

}
