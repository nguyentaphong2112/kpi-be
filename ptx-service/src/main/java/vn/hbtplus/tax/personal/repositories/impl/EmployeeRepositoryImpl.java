package vn.hbtplus.tax.personal.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EmployeeRepositoryImpl extends BaseRepository {

    public Long getEmployeeIdLogin() {
        String sql = """
                    SELECT
                        e.employee_id
                    FROM hr_employees e
                    WHERE e.employee_code = :empCode
                    AND IFNULL(e.is_deleted, :activeStatus) = :activeStatus
                    LIMIT 1
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empCode", Utils.getUserEmpCode());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return queryForObject(sql, params, Long.class);
    }

    public Map<String, EmployeeInfoResponse> getMapEmpByCodes(List<String> listEmployeeCode, List<PermissionDataDto> listOrgId) {
        Map<String, EmployeeInfoResponse> mapResult = new HashMap<>();
        if (listEmployeeCode == null || listEmployeeCode.isEmpty()) {
            return mapResult;
        }

        StringBuilder sql = new StringBuilder("SELECT e.employee_code empCode, e.employee_id, e.full_name empName, e.email, e.tax_no, e.personal_id "
                + "  FROM hr_employees e"
                + "  JOIN mp_organizations mo ON e.organization_id = mo.org_id"
                + "  WHERE e.employee_code IN (:listEmployeeCode)");
        List<List<String>> listPartition = Utils.partition(listEmployeeCode, Constant.SIZE_PARTITION);
        Map<String, Object> mapParams = new HashMap<>();
        QueryUtils.addConditionPermission(listOrgId, sql, mapParams);
        for (List<String> list : listPartition) {

            mapParams.put("listEmployeeCode", list);
            List<EmployeeInfoResponse> listEmp = getListData(sql.toString(), mapParams, EmployeeInfoResponse.class);
            if (listEmp != null && !listEmp.isEmpty()) {
                for (EmployeeInfoResponse dto : listEmp) {
                    mapResult.put(dto.getEmpCode().toLowerCase() + dto.getEmpName().toLowerCase(), dto);
                }
            }
        }
        return mapResult;
    }

    public Map<String, EmployeeInfoResponse> getMapEmpByCodes(List<String> listEmployeeCode) {
        Map<String, EmployeeInfoResponse> mapResult = new HashMap<>();
        if (listEmployeeCode == null || listEmployeeCode.isEmpty()) {
            return mapResult;
        }

        StringBuilder sql = new StringBuilder("SELECT e.employee_code empCode, e.employee_id, e.full_name empName, e.email, e.tax_no, e.personal_id "
                + "  FROM hr_employees e"
                + "  WHERE e.employee_code IN (:listEmployeeCode)");
        List<List<String>> listPartition = Utils.partition(listEmployeeCode, Constant.SIZE_PARTITION);
        Map<String, Object> mapParams = new HashMap<>();
        for (List<String> list : listPartition) {
            mapParams.put("listEmployeeCode", list);
            List<EmployeeInfoResponse> listEmp = getListData(sql.toString(), mapParams, EmployeeInfoResponse.class);
            if (listEmp != null && !listEmp.isEmpty()) {
                for (EmployeeInfoResponse dto : listEmp) {
                    mapResult.put(dto.getEmpCode().toLowerCase() + dto.getEmpName().toLowerCase(), dto);
                }
            }
        }
        return mapResult;
    }

    public Map<String, Long> getMapOrgManage(List<String> listOrgName, List<PermissionDataDto> permissionDataDtoList) {
        Map<String, Long> mapResult = new HashMap<>();
        if (listOrgName == null || listOrgName.isEmpty()) {
            return mapResult;
        }

        StringBuilder sql = new StringBuilder("SELECT mo.org_id, mo.org_name orgNameManage "
                + "  FROM mp_organizations mo"
                + "  WHERE LOWER(mo.org_name) IN (:listName)");
        List<List<String>> listPartition = Utils.partition(listOrgName, Constant.SIZE_PARTITION);
        Map<String, Object> mapParams = new HashMap<>();
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, mapParams);
        for (List<String> listName : listPartition) {
            mapParams.put("listName", listName);
            List<EmployeeInfoResponse> listEmp = getListData(sql.toString(), mapParams, EmployeeInfoResponse.class);
            if (listEmp != null && !listEmp.isEmpty()) {
                for (EmployeeInfoResponse dto : listEmp) {
                    mapResult.put(dto.getOrgNameManage().toLowerCase(), dto.getOrgId());
                }
            }
        }
        return mapResult;
    }

}
