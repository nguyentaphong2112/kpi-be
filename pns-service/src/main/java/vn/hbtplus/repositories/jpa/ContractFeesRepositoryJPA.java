/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.ContractFeesEntity;

/**
 * Lop repository JPA ung voi bang PNS_CONTRACT_FEES
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ContractFeesRepositoryJPA extends JpaRepository<ContractFeesEntity, Long> {

}
