package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.models.response.EmployeeRequestsResponse;
import vn.kpi.repositories.BaseRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EmployeeRequestsRepository extends BaseRepository {
    public EmployeeRequestsResponse getActiveRequest(String requestType, Long employeeId) {
        String sql = """
                select a.* from hr_employee_requests a
                where a.request_type = :requestType
                and a.employee_id = :employeeId
                and a.is_deleted = 'N'
                and ifnull(a.end_date, now()) >= DATE(now())
                and a.status IN (:activeStatus)
                """;
        Map params = new HashMap<>();
        params.put("requestType", requestType);
        params.put("employeeId", employeeId);
        params.put("activeStatus", Arrays.asList("DANG_THUC_HIEN"));
        return getFirstData(sql, params, EmployeeRequestsResponse.class);

    }

    public String validate(String requestType, Long employeeId, Long id) {
        String sql = "select f_validate_employee_info(:requestType, :employeeId, :requestId) from dual";
        Map params = new HashMap<>();
        params.put("requestType", requestType);
        params.put("employeeId", employeeId);
        params.put("requestId", id);
        return queryForObject(sql, params, String.class);
    }
}
