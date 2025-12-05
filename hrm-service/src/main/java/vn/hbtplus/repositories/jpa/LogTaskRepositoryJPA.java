package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.LogTaskEntity;

@Repository
public interface LogTaskRepositoryJPA extends JpaRepository<LogTaskEntity, Long> {
}
