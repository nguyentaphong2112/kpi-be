package vn.hbtplus.services;

import vn.hbtplus.models.response.EmployeesResponse;

public interface EmployeeService {
    Long getEmployeeIdByUserLogin();

    EmployeesResponse getEmployeeByEmpCode(String empCode);
}
