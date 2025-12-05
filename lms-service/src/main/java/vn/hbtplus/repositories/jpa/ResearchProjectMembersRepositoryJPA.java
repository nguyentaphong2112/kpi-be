/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.ResearchProjectMembersEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang lms_research_project_members
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ResearchProjectMembersRepositoryJPA extends JpaRepository<ResearchProjectMembersEntity, Long> {

    List<ResearchProjectMembersEntity> findByResearchProjectIdAndType(Long researchProjectId, String type);
}
