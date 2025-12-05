package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.UserLogActivityEntity;

@Repository
public interface UserLogActivityRepositoryJPA extends JpaRepository<UserLogActivityEntity, Long> {
}
