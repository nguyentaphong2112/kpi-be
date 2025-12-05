package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.repositories.jpa.EmployeesRepositoryJPA;
import vn.hbtplus.services.EmployeesService;


@Service
@Transactional
@RequiredArgsConstructor
public class EmployeesServiceImpl implements EmployeesService {

    private final EmployeesRepositoryJPA employeesRepositoryJPA;



    @Override
    public Long getEmployeeId(String employeeCode) {
        return employeesRepositoryJPA.getIdByEmployeeCode(employeeCode);
    }
}
