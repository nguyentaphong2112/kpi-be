/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.repositories.entity.CourseTraineesEntity;

import java.util.Collection;
import java.util.List;

/**
 * Lop repository JPA ung voi bang crm_course_trainees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface CourseTraineesRepositoryJPA extends JpaRepository<CourseTraineesEntity, Long> {
    @Transactional
    void deleteByCourseId(Long courseId);

    CourseTraineesEntity getByCourseIdAndTraineeId(Long courseId, Long traineeId);

    void deleteByCourseIdAndCourseTraineeIdNotIn(Long courseId, Collection<Long> courseTraineeIds);

    List<CourseTraineesEntity> findByCourseId(Long courseId);
}
