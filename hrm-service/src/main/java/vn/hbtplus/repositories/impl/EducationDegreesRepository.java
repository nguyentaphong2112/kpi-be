/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.request.EducationDegreesRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.EducationDegreesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.EducationDegreesEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Lop repository Impl ung voi bang hr_education_degrees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EducationDegreesRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<EducationDegreesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, EducationDegreesResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_degree_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.major_id,
                    a.major_name,
                    a.major_level_id,
                    a.major_level_name,
                    a.training_method_id,
                    a.training_school_id,
                    a.training_school_name,
                    a.is_highest,
                    CASE
                		WHEN a.is_highest = 'Y' THEN 'Có'
                		ELSE 'Không'
                	END AS isHighest,
                    a.graduated_year,
                    a.graduated_rank_id,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.training_method_id and sc.category_type = :trainingMethod) trainingMethodName,
                    NVL(a.graduated_rank_name, sc4.name) graduatedRankName,
                    NVL(a.major_name, sc1.name) majorName,
                    NVL(a.major_level_name, sc2.name) majorLevelName,
                    NVL(a.training_school_name, sc3.name) trainingSchoolName
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
                    FROM hr_education_degrees a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    LEFT JOIN hr_organizations o ON o.organization_id = e.organization_id
                    LEFT JOIN sys_categories sc1 ON (sc1.value = a.major_id AND sc1.category_type = :majorCode)
                    LEFT JOIN sys_categories sc2 ON (sc2.value = a.major_level_id AND sc2.category_type = :majorLevelCode)
                    LEFT JOIN sys_categories sc3 ON (sc3.value = a.training_school_id AND sc3.category_type = :trainingSchoolCode)
                    LEFT JOIN sys_categories sc4 ON (sc4.value = a.graduated_rank_id AND sc3.category_type = :graduatedRank)
                    WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("trainingMethod", Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO);
        params.put("graduatedRank", Constant.CATEGORY_CODES.XEP_LOAI_TN);
        params.put("majorCode", Constant.CATEGORY_CODES.CHUYEN_NGANH_DAO_TAO);
        params.put("majorLevelCode", Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO);
        params.put("trainingSchoolCode", Constant.CATEGORY_CODES.TRUONG_DAO_TAO);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getListTrainingSchool(), sql, params, "a.training_school_id");
        QueryUtils.filter(dto.getListMajor(), sql, params, "a.major_id");
        QueryUtils.filter(dto.getListMajorLevel(), sql, params, "a.major_level_id");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.EDUCATION_DEGREES, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.graduated_year DESC");
    }

    public BaseDataTableDto<EducationDegreesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_degree_id,
                    a.employee_id,
                    a.major_id,
                    a.major_name,
                    a.major_level_id,
                    a.major_level_name,
                    a.training_method_id,
                    a.training_school_id,
                    a.training_school_name,
                    CASE
                        WHEN a.is_highest = 'Y' THEN 'Có'
                        ELSE 'Không'
                    END as is_highest,
                    a.graduated_year,
                    a.graduated_rank_id,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    (select sc.name from sys_categories sc where sc.value = a.training_method_id and sc.category_type = :trainingMethod) trainingMethodName,
                    NVL(a.graduated_rank_name, (select sc.name from sys_categories sc where sc.value = a.graduated_rank_id and sc.category_type = :graduatedRank)) graduatedRankName,
                    NVL(a.major_name,sc1.name) majorName,
                    NVL(a.major_level_name, sc2.name) majorLevelName,
                    NVL(a.training_school_name, sc3.name) trainingSchoolName
                    FROM hr_education_degrees a
                    LEFT JOIN sys_categories sc1 ON (sc1.value = a.major_id AND sc1.category_type = :majorCode)
                    LEFT JOIN sys_categories sc2 ON (sc2.value = a.major_level_id AND sc2.category_type = :majorLevelCode)
                    LEFT JOIN sys_categories sc3 ON (sc3.value = a.training_school_id AND sc3.category_type = :trainingSchoolCode)
                    WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                    order by a.graduated_year desc
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("trainingMethod", Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO);
        params.put("graduatedRank", Constant.CATEGORY_CODES.XEP_LOAI_TN);
        params.put("majorCode", Constant.CATEGORY_CODES.CHUYEN_NGANH_DAO_TAO);
        params.put("majorLevelCode", Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO);
        params.put("trainingSchoolCode", Constant.CATEGORY_CODES.TRUONG_DAO_TAO);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, EducationDegreesResponse.DetailBean.class);
    }

    public List<EducationDegreesResponse.DetailBean> getListByType(String columnName) {
        String sql = String.format("""
            select DISTINCT %s
            from hr_education_degrees
            where %s is not null
            and is_deleted = :activeStatus
            """, columnName, columnName);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, EducationDegreesResponse.DetailBean.class);
    }

    public boolean isDuplicateEduDegree(EducationDegreesRequest.SubmitForm dto, Long employeeId, Long educationDegreeId) {
        String sql = """ 
                   SELECT COUNT(1)
                   FROM hr_education_degrees hed
                   WHERE hed.employee_id = :employeeId
                   AND NVL(hed.is_deleted, :activeStatus) = :activeStatus
                   AND hed.training_school_id = :schoolId
                   AND hed.major_id = :majorId
                   AND hed.major_level_id = :majorLevelId
                   AND hed.education_degree_id <> :educationDegreeId
                """;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("schoolId", dto.getTrainingSchoolId());
        paramMap.put("majorId", dto.getMajorId());
        paramMap.put("employeeId", employeeId);
        paramMap.put("majorLevelId", dto.getMajorLevelId());
        paramMap.put("educationDegreeId", Utils.NVL(educationDegreeId));
        return queryForObject(sql, paramMap, Integer.class) > 0;
    }

    public void updateCancelIsHighest(Long employeeId, Long educationDegreeId) {
        String sql = """
                    UPDATE hr_education_degrees
                    SET is_highest = 'N',
                        modified_by = :updatedBy,
                        modified_time = :updateDate
                    WHERE employee_id = :employeeId
                    AND is_highest = 'Y'
                    AND NVL(is_deleted, :activeStatus) = :activeStatus
                    and education_degree_id <> :educationDegreeId
                """;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("updatedBy", Utils.getUserNameLogin());
        paramMap.put("updateDate", new Date());
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("employeeId", employeeId);
        paramMap.put("educationDegreeId", Utils.NVL(educationDegreeId));
        executeSqlDatabase(sql, paramMap);
    }

    public void autoUpdateIsHighest(Long employeeId) {
        String sql = """
                    UPDATE hr_education_degrees ed
                    JOIN sys_categories sc ON sc.value = ed.major_level_id and sc.category_type = 'TRINH_DO_DAO_TAO'
                    SET ed.is_highest = 'Y',
                        ed.modified_by = :updatedBy,
                        ed.modified_time = :updateDate
                    WHERE ed.employee_id = :employeeId
                    and sc.value = ed.major_level_id                  
                    AND ed.is_highest = 'N'
                    AND ed.is_deleted = :activeStatus
                    and not exists (
                        select 1 from hr_education_degrees ed1
                        where ed1.employee_id = :employeeId
                        and ed1.is_highest = 'Y'
                        and ed1.is_deleted = :activeStatus
                    )
                    and not exists (
                        select 1 from hr_education_degrees ed1, sys_categories sc1
                        where ed1.employee_id = :employeeId
                        and ed1.is_deleted = :activeStatus
                        and sc1.value = ed1.major_level_id
                        and sc1.category_type = 'TRINH_DO_DAO_TAO'
                        and (sc1.order_number < sc.order_number
                            or (sc1.order_number = sc.order_number
                            and ed1.education_degree_id > ed.education_degree_id)
                        )
                    )
                """;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("updatedBy", Utils.getUserNameLogin());
        paramMap.put("updateDate", new Date());
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("employeeId", employeeId);
        executeSqlDatabase(sql, paramMap);
    }


    public Map<Long, List<EducationDegreesEntity>> getMapDataByCode(List<String> empCodes) {
        String sql = """
                select a.*
                from hr_education_degrees a
                where a.is_deleted = 'N'
                and a.employee_id in (
                    select employee_id from hr_employees where employee_code in (:empCodes)
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodes", empCodes);
        List<EducationDegreesEntity> listData = getListData(sql, params, EducationDegreesEntity.class);
        Map<Long, List<EducationDegreesEntity>> result = new HashMap<>();
        for (EducationDegreesEntity entity : listData) {
            result.computeIfAbsent(entity.getEmployeeId(), k -> new ArrayList<>());
            result.get(entity.getEmployeeId()).add(entity);
        }

        return result;
    }
}
