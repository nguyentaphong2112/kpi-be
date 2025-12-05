package vn.hbtplus.tax.income.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.tax.income.repositories.entity.TaxCommitmentEntity;

@Repository
public interface TaxCommitmentRepositoryJPA extends JpaRepository<TaxCommitmentEntity, Long> {
}
