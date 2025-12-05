package vn.hbtplus.tax.personal.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EmployeeTaxRepositoryImpl extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<EmployeeInfoResponse> searchData(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT "
                + "    e.employee_code as empCode,"
                + "    (   select GROUP_CONCAT(pi.id_no) "
                + "        from hr_personal_identities pi  "
                + "        where pi.employee_id = e.employee_id  "
                + "        and IFNULL(pi.is_deleted, :activeStatus) = :activeStatus "
                + "        ORDER BY id_no"
                + "    ) idNo, "
                + "    e.TAX_NO, e.tax_place, e.tax_date, "
                + "    e.full_name empName,"
                + "    IFNULL(mo.full_name, mo.org_name) orgName,"
                + "    (select vl.label from v_lookup vl where vl.code = e.emp_type_code and vl.type_code = :typeEmpTypeCode ) empTypeName,"
                + "    (select mj.job_name from mp_jobs mj where mj.job_id = e.job_id) jobName,"
                + "    e.status empStatus");
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY mo.display_seq, e.full_name ");
        return getListPagination(sql.toString(), params, dto, EmployeeInfoResponse.class);
    }

    public List<Map<String, Object>> getEmployeeTaxList(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT "
                + "    e.employee_code as empCode,"
                + "    (   select GROUP_CONCAT(pi.id_no) "
                + "        from hr_personal_identities pi  "
                + "        where pi.employee_id = e.employee_id  "
                + "        and IFNULL(pi.is_deleted, :activeStatus) = :activeStatus "
                + "        ORDER BY id_no"
                + "    ) idNo, "
                + "    CASE e.status"
                + "         WHEN 1 THEN 'Đang làm việc'"
                + "         WHEN 2 THEN 'Tạm hoãn'"
                + "         WHEN 3 THEN 'Đã nghỉ việc'"
                + "    END trang_thai_lv,"
                + "    mo.org_name_level1 don_vi_cap_1,"
                + "    mo.org_name_level2 don_vi_cap_2,"
                + "    mo.org_name_level3 don_vi_cap_3,"
                + "    mo.org_name_level4 don_vi_cap_4,"
                + "    mo.org_name_manage don_vi_quan_ly,"
                + "    e.TAX_NO, e.tax_place, e.tax_date, "
                + "    e.full_name empName,"
                + "    (select vl.label from v_lookup vl where vl.code = e.emp_type_code and vl.type_code = :typeEmpTypeCode ) empTypeName,"
                + "    (select mj.job_name from mp_jobs mj where mj.job_id = e.job_id) jobName,"
                + "    e.status empStatus");
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY mo.full_name, e.employee_code ");
        return getListData(sql.toString(), params);
    }

    private void addCondition(AdminSearchDTO dto, StringBuilder sql, HashMap<String, Object> params) {
        sql.append("   FROM hr_employees e"
                + "    JOIN mp_organizations mo ON mo.org_id = e.organization_id"
                + "    WHERE IFNULL(e.is_deleted, :activeStatus) = :activeStatus");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        if (Utils.isNullObject(dto.getOrgId())) {
            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_SEARCH, Utils.getUserNameLogin());
            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_SEARCH);
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }


    }
}
