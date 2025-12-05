/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.QuestionsEntity;

/**
 * Lop repository JPA ung voi bang exm_questions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface QuestionsRepositoryJPA extends JpaRepository<QuestionsEntity, Long> {

}
