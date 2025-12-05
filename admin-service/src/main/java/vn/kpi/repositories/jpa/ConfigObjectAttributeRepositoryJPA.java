package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.ConfigObjectAttributeEntity;

@Repository
public interface ConfigObjectAttributeRepositoryJPA extends JpaRepository<ConfigObjectAttributeEntity, Long> {
}
