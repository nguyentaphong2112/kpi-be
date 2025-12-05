package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.UserLoginHistoryEntity;

@Repository
public interface UserLoginHistoryRepositoryJPA extends JpaRepository<UserLoginHistoryEntity, Long> {
}
