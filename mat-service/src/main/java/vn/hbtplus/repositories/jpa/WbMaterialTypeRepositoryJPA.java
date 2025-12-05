package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hbtplus.repositories.entity.WbMaterialTypeEntity;

@Repository
public interface WbMaterialTypeRepositoryJPA  extends JpaRepository<WbMaterialTypeEntity, Long> {

}
