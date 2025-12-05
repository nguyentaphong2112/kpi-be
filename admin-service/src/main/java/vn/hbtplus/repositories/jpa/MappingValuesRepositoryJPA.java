/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.MappingValuesEntity;

/**
 * Lop repository JPA ung voi bang sys_mapping_values
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface MappingValuesRepositoryJPA extends JpaRepository<MappingValuesEntity, Long> {

}
