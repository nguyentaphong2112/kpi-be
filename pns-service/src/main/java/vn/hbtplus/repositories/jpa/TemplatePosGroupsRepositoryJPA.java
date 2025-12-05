/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.TemplatePosGroupsEntity;

/**
 * Lop repository JPA ung voi bang PNS_TEMPLATE_POS_GROUPS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface TemplatePosGroupsRepositoryJPA extends JpaRepository<TemplatePosGroupsEntity, Long> {

}
