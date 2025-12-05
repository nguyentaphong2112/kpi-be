package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.ParameterEntity;

import java.util.Date;
import java.util.List;

@Repository
public interface ParameterRepositoryJPA extends JpaRepository<ParameterEntity, Long> {

    @Query("from ParameterEntity a where a.configGroup = :configGroup and a.startDate = :startDate and a.isDeleted = 'N'")
    List<ParameterEntity> getByStartDate(String configGroup, Date startDate);

    @Query(value = "UPDATE sys_parameters " +
            " SET end_date = DATE_SUB(:startDate, INTERVAL 1 DAY) " +
            " WHERE config_group = :configGroup " +
            " AND (end_date IS NULL OR end_date >= :startDate) " +
            " AND start_date < :startDate" +
            " AND is_deleted = 'N'",
            nativeQuery = true)
    @Modifying
    void updatePreConfigs(String configGroup, Date startDate);

    @Modifying
    @Query("update ParameterEntity a " +
            "   set a.isDeleted = 'Y', a.modifiedTime = CURRENT_TIMESTAMP, a.modifiedBy = :userName" +
            "   where a.configGroup = :configGroup" +
            "   and a.startDate = :startDate" +
            "   and a.isDeleted = 'N'")
    void deleteParameter(String configGroup, Date startDate, String userName);
}