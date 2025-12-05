/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.FeedbackCommentsEntity;

/**
 * Lop repository JPA ung voi bang sys_categories
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
public interface FeedbackCommentRepositoryJPA extends JpaRepository<FeedbackCommentsEntity, Long> {

   
}
