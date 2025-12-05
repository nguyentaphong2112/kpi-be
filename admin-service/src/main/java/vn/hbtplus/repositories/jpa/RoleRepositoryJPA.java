/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.RoleEntity;

/**
 * Lop repository JPA ung voi bang sys_roles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface RoleRepositoryJPA extends JpaRepository<RoleEntity, Long> {

    @Query("select roleId from RoleEntity where isDeleted = 'N' and upper(code) like :code")
    Long getRoleId(String code);
}
