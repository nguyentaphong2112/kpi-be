package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.ConfigParameterEntity;

@Repository
public interface ConfigParameterRepositoryJPA extends JpaRepository<ConfigParameterEntity, Long> {
    @Query("select a from ConfigParameterEntity a where a.configGroup = :configGroup and a.isDeleted = 'N'")
    ConfigParameterEntity findByConfigGroup(String configGroup);


}