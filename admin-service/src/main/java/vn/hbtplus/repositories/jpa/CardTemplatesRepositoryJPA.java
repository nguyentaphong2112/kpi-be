/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.CardTemplatesEntity;

/**
 * Lop repository JPA ung voi bang sys_card_templates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface CardTemplatesRepositoryJPA extends JpaRepository<CardTemplatesEntity, Long> {

}
