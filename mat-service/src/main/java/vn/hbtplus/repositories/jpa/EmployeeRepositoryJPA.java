package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hbtplus.repositories.entity.EmployeesEntity;

public interface EmployeeRepositoryJPA extends JpaRepository<EmployeesEntity, Long> {
    EmployeesEntity findByEmployeeCodeAndIsDeleted(String employeeCode, String isDeleted);
}
