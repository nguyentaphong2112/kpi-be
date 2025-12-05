/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.ResearchProjectLifecyclesEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang med_research_project_lifecycles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface ResearchProjectLifecyclesRepositoryJPA extends JpaRepository<ResearchProjectLifecyclesEntity, Long> {

    List<ResearchProjectLifecyclesEntity> findByResearchProjectId(Long researchProjectId);
}
