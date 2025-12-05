package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.CategoryTypesEntity;

@Repository
public interface CategoryTypesRepositoryJPA extends JpaRepository<CategoryTypesEntity, Long> {
}
