/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.tax.personal.repositories.entity.DeclarationRegistersEntity;

/**
 * Lop repository JPA ung voi bang PTX_DECLARATION_REGISTERS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface DeclarationRegistersRepositoryJPA extends JpaRepository<DeclarationRegistersEntity, Long> {

}
