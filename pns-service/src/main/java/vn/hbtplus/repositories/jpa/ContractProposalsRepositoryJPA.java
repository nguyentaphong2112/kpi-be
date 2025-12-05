/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.ContractProposalsEntity;

/**
 * Lop repository JPA ung voi bang PNS_CONTRACT_PROPOSALS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ContractProposalsRepositoryJPA extends JpaRepository<ContractProposalsEntity, Long> {

}
