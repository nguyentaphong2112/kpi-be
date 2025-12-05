/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.InternshipSessionDetailsEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang lms_internship_session_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface InternshipSessionDetailsRepositoryJPA extends JpaRepository<InternshipSessionDetailsEntity, Long> {

    List<InternshipSessionDetailsEntity> findByInternshipSessionId(Long internshipSessionId);
}
