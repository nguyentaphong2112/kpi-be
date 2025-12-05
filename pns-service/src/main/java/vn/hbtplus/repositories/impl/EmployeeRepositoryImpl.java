package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.response.EmployeeInfoResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EmployeeRepositoryImpl extends BaseRepository {
    private final AuthorizationService authorizationService;

    public Long getEmployeeIdLogin() {
        String sql = " SELECT e.employee_id FROM hr_employees e WHERE e.employee_code = :employeeCode";
        Map<String, Object> params = new HashMap<>();
        params.put("employeeCode", Utils.getUserEmpCode());
        return queryForObject(sql, params, Long.class);
    }

    public Map<String, EmployeeInfoResponse> getMapEmpByCodes(List<String> listEmployeeCode, String scope, String functionCode, String... empTypeCodes) {
        Map<String, EmployeeInfoResponse> mapResult = new HashMap<>();
        if (listEmployeeCode == null || listEmployeeCode.isEmpty()) {
            return mapResult;
        }

        StringBuilder sql = new StringBuilder("SELECT e.employee_code empCode, e.employee_id, e.full_name empName"
                + "  FROM hr_employees e"
                + "  inner join hr_organizations o on e.organization_id = o.organization_id "
                + "  WHERE e.employee_code IN (:listEmployeeCode)"
                + (empTypeCodes == null || empTypeCodes.length == 0 ? "" : " and e.emp_type_code in (:empTypeCodes)"));
        List<List<String>> listPartition = Utils.partition(listEmployeeCode, Constant.SIZE_PARTITION);

        Map<String, Object> mapParams = new HashMap<>();

        List<PermissionDataDto> permissionDataDTOs = authorizationService.getPermissionData(scope, functionCode, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDTOs, sql, mapParams, "o.path_id");

        for (List<String> list : listPartition) {
            mapParams.put("listEmployeeCode", list);

            if (empTypeCodes != null && empTypeCodes.length > 0) {
                mapParams.put("empTypeCodes", Arrays.asList(empTypeCodes));
            }

            List<EmployeeInfoResponse> listEmp = getListData(sql.toString(), mapParams, EmployeeInfoResponse.class);
            if (listEmp != null && !listEmp.isEmpty()) {
                for (EmployeeInfoResponse dto : listEmp) {
                    mapResult.put(dto.getEmpCode(), dto);
                }
            }
        }

        return mapResult;
    }
}
