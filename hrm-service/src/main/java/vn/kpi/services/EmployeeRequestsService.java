package vn.kpi.services;

import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.response.EmployeeRequestsResponse;

public interface EmployeeRequestsService {
    EmployeeRequestsResponse getActiveRequest(String requestType, Long employeeId);

    boolean updateStatus(String toUpperCase, Long employeeId, Long id) throws BaseAppException;
    boolean validate(String toUpperCase, Long employeeId, Long id);
}
