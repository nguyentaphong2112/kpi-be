package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.AttachmentEntity;
import vn.hbtplus.repositories.entity.ShedlockHistoriesEntity;

import java.util.List;

@Repository
public interface ShedlockHistoriesRepositoryJPA extends JpaRepository<ShedlockHistoriesEntity, Long> {


}
