package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.hbtplus.repositories.entity.EmployeeEntity;

public interface EmployeesRepositoryJPA extends JpaRepository<EmployeeEntity, Long> {
    @Query("select employeeId from EmployeeEntity where employeeCode = :employeeCode")
    Long getIdByEmployeeCode(String employeeCode);
}
