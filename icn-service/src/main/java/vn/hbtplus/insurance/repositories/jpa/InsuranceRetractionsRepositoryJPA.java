/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.insurance.repositories.entity.InsuranceRetractionsEntity;

/**
 * Lop repository JPA ung voi bang icn_insurance_retractions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface InsuranceRetractionsRepositoryJPA extends JpaRepository<InsuranceRetractionsEntity, Long> {

    @Modifying
    @Query("update InsuranceRetractionsEntity a set a.insuranceContributionId = null, a.retroPeriodDate = null " +
            " where a.insuranceContributionId = :id")
    void resetRetraction(Long id);
}
