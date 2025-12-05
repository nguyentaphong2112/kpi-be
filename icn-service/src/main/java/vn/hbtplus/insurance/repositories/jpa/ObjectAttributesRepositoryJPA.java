/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.insurance.repositories.entity.ObjectAttributesEntity;

/**
 * Lop repository JPA ung voi bang hr_object_attributes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ObjectAttributesRepositoryJPA extends JpaRepository<ObjectAttributesEntity, Long> {

}
