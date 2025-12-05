package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.UserEntity;

import java.util.List;

@Repository
public interface UserRepositoryJPA  extends JpaRepository<UserEntity, Long> {
    @Query("From UserEntity where password is null")
    List<UserEntity> findToResetPassword();
}