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
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.EducationPromotionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_education_promotions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class EducationPromotionsRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<EducationPromotionsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, EducationPromotionsResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_promotion_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.issued_year,
                    a.issued_place,
                    a.promotion_rank_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,o.full_name as orgName,
                    mj.name as jobName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = a.promotion_rank_id and sc.category_type = :promotionRankCode) promotionRankName
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
            FROM hr_education_promotions a
            JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
            LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            WHERE a.is_deleted = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("promotionRankCode", Constant.CATEGORY_CODES.HOC_HAM);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getListPromotionRank(), sql, params, "a.promotion_rank_id");
        if (dto.getListYear() != null && !dto.getListYear().isEmpty()) {
            QueryUtils.filterGe(dto.getListYear().get(0), sql, params, "a.issued_year", "fromYear");
            QueryUtils.filterLe(dto.getListYear().size() > 1 ? dto.getListYear().get(1) : null, sql, params, "a.issued_year", "toYear");
        }
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.EDUCATION_PROMOTIONS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY o.path_order, mj.order_number, e.employee_id");
    }

    public BaseDataTableDto<EducationPromotionsResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_promotion_id,
                    a.employee_id,
                    a.issued_year,
                    a.issued_place,
                    a.promotion_rank_id,                    
                    sc.name promotion_rank_name,                    
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                    FROM hr_education_promotions a
                    left join sys_categories sc on sc.category_type = :categoryTypeHocHam and sc.value = a.promotion_rank_id
                    where a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                    
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("categoryTypeHocHam", Constant.CATEGORY_CODES.HOC_HAM);
        return getListPagination(sql.toString(), params, request, EducationPromotionsResponse.DetailBean.class);
    }
}
