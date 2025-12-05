/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.ConfigApprovalsEntity;

/**
 * Lop repository JPA ung voi bang PNS_CONFIG_APPROVALS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ConfigApprovalsRepositoryJPA extends JpaRepository<ConfigApprovalsEntity, Long> {

}
