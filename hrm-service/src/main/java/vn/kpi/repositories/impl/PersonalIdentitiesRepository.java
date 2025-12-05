/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.PersonalIdentitiesRequest;
import vn.kpi.models.response.PersonalIdentitiesResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.PersonalIdentitiesEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_personal_identities
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class PersonalIdentitiesRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<PersonalIdentitiesResponse.SearchResult> getPersonalIdentities(Long employeeId, BaseSearchRequest request) {
        String sql = """
                SELECT
                	hpi.personal_identity_id personalIdentityId,
                	hpi.employee_id employeeId,
                	sc.name identityTypeName,
                	hpi.identity_no identityNo,
                	hpi.identity_issue_date identityIssueDate,
                	hpi.identity_issue_place identityIssuePlace,
                	hpi.expired_date expiredDate,
                	hpi.created_by createdBy,
                 	hpi.created_time createdTime,
                 	hpi.modified_by modifiedBy,
                 	hpi.modified_time modifiedTime,
                	CASE
                		WHEN hpi.is_main = 'Y' THEN 'Có'
                		ELSE 'Không'
                	END AS isMain
                FROM
                	hr_personal_identities hpi
                LEFT JOIN sys_categories sc ON
                	sc.value = hpi.identity_type_id
                	AND sc.category_type = :categoryTypeCode
                WHERE nvl(hpi.is_deleted, :isDeleted) = :isDeleted
                	AND hpi.employee_id = :employeeId
                ORDER BY
                	hpi.personal_identity_id
                """;
        HashMap<String, Object> hashMapParams = new HashMap<>();
        hashMapParams.put("employeeId", employeeId);
        hashMapParams.put("categoryTypeCode", Constant.CATEGORY_CODES.LOAI_GIAY_TO);
        hashMapParams.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        return getListPagination(sql, hashMapParams, request, PersonalIdentitiesResponse.SearchResult.class);
    }

    public BaseDataTableDto<PersonalIdentitiesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        buildSql(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, PersonalIdentitiesResponse.SearchResult.class);
    }

    private void buildSql(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                SELECT
                    a.personal_identity_id,
                    a.identity_no,
                    a.identity_type_id,
                    a.identity_issue_date,
                    a.identity_issue_place,
                    a.expired_date,
                    a.is_main,
                    CASE
                		WHEN a.is_main = 'Y' THEN 'Có'
                		ELSE 'Không'
                	END AS isMain,
                    a.employee_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    e.employee_code,
                    e.full_name,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.identity_type_id and sc.category_type = :identityType) identityTypeName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName
                """);
        addCondition(sql, params, dto);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        buildSql(sql, params, dto);
        List<Map<String, Object>> dataList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_personal_identities a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("identityType", Constant.CATEGORY_CODES.LOAI_GIAY_TO);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getListIdentityType(), sql, params, "a.identity_type_id");
        QueryUtils.filter(dto.getIdentityNo(), sql, params, "a.identity_no");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.PERSONAL_IDENTITIES, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id");
    }

    public boolean checkDuplicateIdentityNo(PersonalIdentitiesRequest.SubmitForm identityInfo, Long employeeId) {
        String sql = """
                    select count(1)
                    from hr_personal_identities p
                    where NVL(p.is_deleted, :activeStatus) = :activeStatus
                    and p.identity_no = :identityNo
                    and p.employee_id != :employeeId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("identityNo", identityInfo.getIdentityNo());
        params.put("employeeId", Utils.NVL(employeeId));
        return queryForObject(sql, params, Integer.class) > 0;
    }

    public List<PersonalIdentitiesEntity> getPersonalIdentityList(Long personalIdentityId, String identityTypeId, Long employeeId) {
        String sql = """
                select * 
                from hr_personal_identities p
                where NVL(p.is_deleted, :activeStatus) = :activeStatus
                    and p.identity_type_id = :identityTypeId and p.employee_id = :employeeId
                    and p.personal_identity_id != :personalIdentityId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("identityTypeId", identityTypeId);
        params.put("employeeId", employeeId);
        params.put("personalIdentityId", personalIdentityId == null ? 0L : personalIdentityId);

        return getListData(sql, params, PersonalIdentitiesEntity.class);
    }

    public void updatePersonalIdentity(Long employeeId, Long personalIdentityId) {
        String sql = """
                    UPDATE hr_personal_identities
                    SET is_main = 'N', modified_by = :userName, modified_time = now()
                    WHERE is_deleted = 'N'
                    AND employee_id = :employeeId
                    AND personal_identity_id != :personalIdentityId
                    and is_main = 'Y'
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("personalIdentityId", personalIdentityId);
        params.put("employeeId", employeeId);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public void autoUpdateIsMain(Long employeeId) {
        String sql = """
                UPDATE hr_personal_identities pid
                    SET pid.is_main = 'Y', pid.modified_by = :userName, pid.modified_time = now()
                    WHERE pid.is_deleted = 'N'
                    AND pid.employee_id = :employeeId
                    and pid.is_main = 'N'
                    and not exists (
                        select 1 from hr_personal_identities pid1
                        where pid1.employee_id = :employeeId
                        and pid1.is_main = 'Y'
                        and pid1.is_deleted = 'N'
                    )
                    and pid.personal_identity_id = (
                        select max(pid1.personal_identity_id) from hr_personal_identities pid1
                        where pid1.employee_id = :employeeId
                        and pid1.is_main = 'Y'
                        and pid1.is_deleted = 'N'
                    )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public Map<Long, List<PersonalIdentitiesEntity>> getMapDataByCode(List<String> empCodes) {
        String sql = """
                select p.*
                from hr_personal_identities p
                where p.is_deleted = 'N'
                and p.employee_id in (
                    select employee_id from hr_employees where employee_code in (:empCodes)
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodes", empCodes);
        List<PersonalIdentitiesEntity> listData = getListData(sql, params, PersonalIdentitiesEntity.class);
        Map<Long, List<PersonalIdentitiesEntity>> result = new HashMap<>();
        for (PersonalIdentitiesEntity entity : listData) {
            result.computeIfAbsent(entity.getEmployeeId(), k -> new ArrayList<>());
            result.get(entity.getEmployeeId()).add(entity);
        }

        return result;
    }

    public Map<String, List<PersonalIdentitiesEntity>> getMapAllData() {
        String sql = """
                select p.*
                from hr_personal_identities p
                where p.is_deleted = 'N'
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        List<PersonalIdentitiesEntity> listData = getListData(sql, params, PersonalIdentitiesEntity.class);
        Map<String, List<PersonalIdentitiesEntity>> result = new HashMap<>();
        for (PersonalIdentitiesEntity entity : listData) {
            if (entity.getIdentityNo() != null) {
                if (result.get(entity.getIdentityNo().toLowerCase()) == null) {
                    List<PersonalIdentitiesEntity> data = new ArrayList<>();
                    data.add(entity);
                    result.put(entity.getIdentityNo().toLowerCase(), data);
                } else {
                    result.get(entity.getIdentityNo().toLowerCase()).add(entity);
                }
            }
        }

        return result;
    }
}
