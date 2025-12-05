package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.UserLoginHistoryEntity;

@Repository
public interface UserLoginHistoryRepositoryJPA extends JpaRepository<UserLoginHistoryEntity, Long> {
}
