package vn.hbtplus.insurance.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.insurance.repositories.entity.ContributionRateEntity;

import java.util.Date;
import java.util.List;

@Repository
public interface ContributionRateRepositoryJPA extends JpaRepository<ContributionRateEntity, Long> {
    @Query(value = "UPDATE icn_contribution_rates " +
            " SET end_date = DATE_SUB(:startDate, INTERVAL 1 DAY) " +
            " WHERE emp_type_code = :empTypeCode " +
            " AND (end_date IS NULL OR end_date >= :startDate) " +
            " AND start_date < :startDate" +
            " AND is_deleted = 'N'",
            nativeQuery = true)
    @Modifying
    void updatePreConfig(String empTypeCode, Date startDate);

    @Query("select a from ContributionRateEntity a where a.isDeleted = 'N'" +
            " and a.startDate <= :periodDate" +
            " and (a.endDate is null or a.endDate >= :periodDate)")
    List<ContributionRateEntity> getConfigActive(Date periodDate);

    @Query("select a.empTypeCode from ContributionRateEntity a where a.isDeleted = 'N'" +
            " and a.startDate <= :periodDate" +
            " and (a.endDate is null or a.endDate >= :periodDate)")
    List<String> getListEmTypeCodes(Date periodDate);
}
