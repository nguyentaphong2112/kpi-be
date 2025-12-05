package vn.hbtplus.services;

import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.response.EmployeeRequestsResponse;

public interface EmployeeRequestsService {
    EmployeeRequestsResponse getActiveRequest(String requestType, Long employeeId);

    boolean updateStatus(String toUpperCase, Long employeeId, Long id) throws BaseAppException;
    boolean validate(String toUpperCase, Long employeeId, Long id);
}
