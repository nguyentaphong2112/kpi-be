/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.dto.BaseCategoryDto;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.FamilyRelationshipsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_family_relationships
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class FamilyRelationshipsRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<FamilyRelationshipsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, FamilyRelationshipsResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_id,
                    a.family_relationship_id,
                    a.relation_type_id,
                    a.full_name familyRelationshipName,
                    DATE_FORMAT(
                        a.date_of_birth,
                        CASE a.type_date_of_birth
                            WHEN 'MONTH' THEN '%m/%Y'
                            WHEN 'YEAR' THEN '%Y'
                            ELSE '%d/%m/%Y'
                        END
                    )  dateOfBirthStr,
                    a.job,
                    a.organization_address,
                    a.current_address,
                    a.personal_id_no,
                    a.mobile_number,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    e.employee_code,
                    e.full_name,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.relation_type_id and sc.category_type = :relationType) relationTypeName,
                    (select sc.name from sys_categories sc where sc.value = a.relation_status_id and sc.category_type = :relationStatus) relationStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.policy_type_id and sc.category_type = :policyType) policyTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());
        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getLeft()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
            FROM hr_family_relationships a
            JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
            LEFT JOIN hr_jobs mj ON (mj.job_id = e.job_id AND NVL(mj.is_deleted, :activeStatus) = :activeStatus)
            JOIN hr_organizations o ON (o.organization_id = e.organization_id AND NVL(o.is_deleted, :activeStatus) = :activeStatus)
            WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("relationType", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        params.put("relationStatus", Constant.CATEGORY_CODES.TINH_TRANG_TN);
        params.put("policyType", Constant.CATEGORY_CODES.DOI_TUONG_CHINH_SACH);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getFullName(), sql, params, "a.full_name");
        QueryUtils.filter(dto.getPersonalIdNo(), sql, params, "a.personal_id_no");
        QueryUtils.filter(dto.getFamilyMobileNumber(), sql, params, "a.mobile_number");
        QueryUtils.filter(dto.getListPolicyType(), sql, params, "a.policy_type_id");
        QueryUtils.filter(dto.getListRelationStatus(), sql, params, "a.relation_status_id");
        QueryUtils.filter(dto.getListRelationType(), sql, params, "a.relation_type_id");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                 Scope.VIEW, Constant.RESOURCES.FAMILY_RELATIONSHIPS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id");
    }

    public FamilyRelationshipsResponse.DetailBean getDataById(Long familyRelationshipId) {
        String sql = """
                    SELECT fr.*, e.employee_code, e.full_name employeeName
                    FROM hr_family_relationships fr
                    JOIN hr_employees e on e.employee_id = fr.employee_id
                    WHERE NVL(fr.is_deleted, :activeStatus) = :activeStatus
                    AND fr.family_relationship_id = :familyRelationshipId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("familyRelationshipId", familyRelationshipId);
        return queryForObject(sql, params, FamilyRelationshipsResponse.DetailBean.class);
    }

    public BaseDataTableDto<FamilyRelationshipsResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.family_relationship_id,
                    a.employee_id,
                    a.relation_type_id,
                    a.full_name familyRelationshipName,
                    a.date_of_birth,
                    DATE_FORMAT(
                        a.date_of_birth,
                        CASE a.type_date_of_birth
                            WHEN 'MONTH' THEN '%m/%Y'
                            WHEN 'YEAR' THEN '%Y'
                            ELSE '%d/%m/%Y'
                        END
                    )  dateOfBirthStr,
                    a.job,
                    a.organization_address,
                    a.current_address,
                    a.personal_id_no,
                    a.mobile_number,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    (select sc.name from sys_categories sc where sc.value = a.relation_type_id and sc.category_type = :relationType) relationTypeName,
                    (select sc.name from sys_categories sc where sc.value = a.relation_status_id and sc.category_type = :relationStatus) relationStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.policy_type_id and sc.category_type = :policyType) policyTypeName,
                    (select CASE b.attribute_value
                            WHEN 'Y' THEN 'Có'
                            ELSE 'Không'
                        END
                    from hr_object_attributes b where b.attribute_code = :attributeCode and b.object_id = a.family_relationship_id) isForeign
                FROM hr_family_relationships a
                WHERE a.is_deleted = :activeStatus
                AND a.employee_id = :employeeId
                ORDER BY a.date_of_birth
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("relationType", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        params.put("relationStatus", Constant.CATEGORY_CODES.TINH_TRANG_TN);
        params.put("policyType", Constant.CATEGORY_CODES.DOI_TUONG_CHINH_SACH);
        params.put("attributeCode", Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.SONG_NUOC_NGOAI);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, FamilyRelationshipsResponse.DetailBean.class);
    }

    public List<BaseCategoryDto> getListCategories(String categoryType) {
        String sql = """
                select value, name
                from sys_categories
                where is_deleted = 'N'
                  and category_type = :categoryType
                  order by ifnull(order_number,:maxInteger), name
                  """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, BaseCategoryDto.class);
    }


}
