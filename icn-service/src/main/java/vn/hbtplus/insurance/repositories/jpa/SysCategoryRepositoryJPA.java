package vn.hbtplus.insurance.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.insurance.repositories.entity.SysCategoryEntity;

import java.util.List;

@Repository
public interface SysCategoryRepositoryJPA  extends JpaRepository<SysCategoryEntity, Long> {

    @Query("select a from SysCategoryEntity a where a.categoryType = :categoryType and a.isDeleted = 'N' and a.value IN(:values)" +
            " order by a.orderNumber")
    List<SysCategoryEntity> getListDataByIds(String categoryType, List<String> values);
    @Query("select a from SysCategoryEntity a where a.categoryType = :categoryType and a.isDeleted = 'N' order by a.orderNumber")
    List<SysCategoryEntity> getListCategories(String categoryType);
}
