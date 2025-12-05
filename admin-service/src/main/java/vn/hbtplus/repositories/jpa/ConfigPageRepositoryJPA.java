/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.ConfigPageEntity;

/**
 * Lop repository JPA ung voi bang sys_categories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ConfigPageRepositoryJPA extends JpaRepository<ConfigPageEntity, Long> {


}
