package vn.hbtplus.insurance.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.EmployeeDto;
import vn.hbtplus.insurance.models.response.EmployeesResponse;
import vn.hbtplus.insurance.repositories.entity.*;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Repository
public class EmployeeRepository extends BaseRepository {

    public List<EmployeeDto> getEmployeeDtos(List<String> empCodes) throws ExecutionException, InterruptedException {
        String sql = "select e.employee_code, e.full_name, e.employee_id from hr_employees e" +
                " where e.is_deleted = 'N'" +
                "   and e.employee_code in (:empCodes)";

        return getListData(sql, new HashMap<>(), EmployeeDto.class, "empCodes", empCodes);
    }

    public List<EmployeesResponse> getDataByEmpId(Long empId) {
        String sql = """
            SELECT
                a.employee_code,
                a.full_name,
                a.date_of_birth,
                a.email,
                a.mobile_number,
                a.personal_id_no,
                a.personal_id_place,
                a.personal_id_date,
                a.current_address,
                a.permanent_address,
                a.status,
                o.full_name orgName,
                (select j.name from hr_jobs j where j.job_id = a.job_id) jobName,
                (select s.name from sys_categories s where s.value = a.gender and s.category_type = :categoryType) genderName,
                a.tax_number
            FROM hr_employees a
            JOIN hr_organizations o ON o.organization_id = a.org_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
            AND a.employee_id = :empId
        """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("categoryType", Constant.CATEGORY_TYPE.GIOI_TINH);
        params.put("empId", empId);
        return getListData(sql, params, EmployeesResponse.class);
    }
}
