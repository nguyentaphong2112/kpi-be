/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.PartnersEntity;

/**
 * Lop repository JPA ung voi bang crm_partners
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface PartnersRepositoryJPA extends JpaRepository<PartnersEntity, Long> {

}
