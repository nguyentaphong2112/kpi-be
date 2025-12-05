/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.WorkCalendarDetailsEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang abs_work_calendar_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface WorkCalendarDetailsRepositoryJPA extends JpaRepository<WorkCalendarDetailsEntity, Long> {
    public List<WorkCalendarDetailsEntity> findByWorkCalendarId(Long workCalendarId);

    @Query(" SELECT wcd "
            + " FROM WorkCalendarDetailsEntity wcd "
            + " WHERE wcd.workCalendarDetailId in (:workCalendarIds)")
    public List<WorkCalendarDetailsEntity> findByWorkCalendarDetailIdIs(List<Long> workCalendarIds);
}
