package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.jpa.EmployeeRepositoryJPA;
import vn.hbtplus.services.EmployeeService;
import vn.hbtplus.utils.Utils;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepositoryJPA employeeRepository;

    public Long getEmployeeIdByUserLogin() {
        String employeeCode = Utils.getUserEmpCode();
        employeeCode = employeeCode.replaceAll("[^\\d.]", "");
        EmployeesEntity employeeBO = employeeRepository.findByEmployeeCodeAndIsDeleted(employeeCode, BaseConstants.STATUS.NOT_DELETED);
        return employeeBO.getEmployeeId();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeesResponse getEmployeeByEmpCode(String empCode) {
        EmployeesEntity employee = employeeRepository.findByEmployeeCodeAndIsDeleted(empCode, BaseConstants.STATUS.NOT_DELETED);
        EmployeesResponse response = new EmployeesResponse();
        if (employee != null) {
            Utils.copyProperties(employee, response);
            return response;
        }

        return null;
    }
}
