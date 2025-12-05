/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.PytagoValuesEntity;

/**
 * Lop repository JPA ung voi bang crm_pytago_values
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface PytagoValuesRepositoryJPA extends JpaRepository<PytagoValuesEntity, Long> {

}
