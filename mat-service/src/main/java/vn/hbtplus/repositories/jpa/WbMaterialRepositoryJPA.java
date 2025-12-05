package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hbtplus.repositories.entity.WbMaterialEntity;

@Repository
public interface WbMaterialRepositoryJPA extends JpaRepository<WbMaterialEntity, Long>{
	WbMaterialEntity findFirstByOrderByWbMaterialIdDesc();
}
