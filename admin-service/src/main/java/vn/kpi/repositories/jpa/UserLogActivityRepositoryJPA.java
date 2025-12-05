package vn.kpi.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kpi.repositories.entity.UserLogActivityEntity;

@Repository
public interface UserLogActivityRepositoryJPA extends JpaRepository<UserLogActivityEntity, Long> {
}
