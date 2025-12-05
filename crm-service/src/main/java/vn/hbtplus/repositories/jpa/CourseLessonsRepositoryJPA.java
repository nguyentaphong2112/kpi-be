/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.repositories.entity.CourseLessonsEntity;

/**
 * Lop repository JPA ung voi bang crm_course_lessons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface CourseLessonsRepositoryJPA extends JpaRepository<CourseLessonsEntity, Long> {

    @Transactional
    void deleteByCourseId(Long courseId);
}
