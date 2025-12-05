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
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.EducationCertificatesRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.EducationCertificatesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ObjectAttributesEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_education_certificates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class EducationCertificatesRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<EducationCertificatesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, EducationCertificatesResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_certificate_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.certificate_type_id,
                    a.certificate_id,
                    a.issued_place,
                    a.issued_date,
                    a.expired_date,
                    a.result,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.certificate_type_id and sc.category_type = :certificateType) certificateTypeName,
                    NVL(a.certificate_name, (select sc.name from sys_categories sc where sc.value = a.certificate_id and sc.category_type = :certificateCode limit 1)) certificate_name
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
                    FROM hr_education_certificates a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("certificateType", Constant.CATEGORY_CODES.LOAI_CHUNG_CHI);
        params.put("certificateCode", Constant.CATEGORY_CODES.TEN_CHUNG_CHI);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getListCertificate(), sql, params, "a.certificate_id");
        QueryUtils.filter(dto.getListCertificateType(), sql, params, "a.certificate_type_id");
        QueryUtils.filter(dto.getIssuedPlace(), sql, params, "a.issued_place");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.EDUCATION_CERTIFICATES, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.issued_date DESC");
    }

    public BaseDataTableDto<EducationCertificatesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_certificate_id,
                    a.employee_id,
                    a.certificate_type_id,
                    a.certificate_id,
                    a.issued_place,
                    a.issued_date,
                    a.expired_date,
                    a.result,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    (select sc.name from sys_categories sc where sc.value = a.certificate_type_id and sc.category_type = :certificateType) certificateTypeName,
                    NVL(a.certificate_name, (select sc.name from sys_categories sc where sc.value = a.certificate_id and sc.category_type = :certificateCode limit 1)) certificateName
                    FROM hr_education_certificates a
                    WHERE a.is_deleted = :activeStatus
                    AND a.employee_id = :employeeId
                    order by a.issued_date desc, a.education_certificate_id desc
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("certificateType", Constant.CATEGORY_CODES.LOAI_CHUNG_CHI);
        params.put("certificateCode", Constant.CATEGORY_CODES.TEN_CHUNG_CHI);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, EducationCertificatesResponse.DetailBean.class);
    }

    public boolean isDuplicateEduCertificate(EducationCertificatesRequest.SubmitForm dto, Long employeeId, Long educationCertificateId) {
        StringBuilder sql = new StringBuilder(""" 
                   SELECT COUNT(1)
                   FROM hr_education_certificates hec
                   WHERE hec.employee_id = :employeeId
                   AND NVL(hec.is_deleted, :activeStatus) = :activeStatus
                   AND hec.certificate_type_id = :certificateType
                   AND hec.issued_place = :issuedPlace
                   AND hec.issued_date = :issueDate
                   AND hec.education_certificate_id != :educationCertificateId
                """);
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("employeeId", employeeId);
        paramMap.put("certificateType", dto.getCertificateTypeId());
        paramMap.put("issuedPlace", dto.getIssuedPlace());
        paramMap.put("issueDate", Utils.truncDate(dto.getIssuedDate()));
        paramMap.put("educationCertificateId", Utils.NVL(educationCertificateId));
        if (!Utils.isNullOrEmpty(dto.getCertificateName())) {
            sql.append(" AND hec.certificate_name = :certificateName");
            paramMap.put("certificateName", dto.getCertificateName());
        } else {
            sql.append(" AND hec.certificate_id = :certificateId");
            paramMap.put("certificateId", dto.getCertificateId());
        }
        return queryForObject(sql.toString(), paramMap, Integer.class) > 0;
    }

    public List<BaseCategoryDto> getListCategoriesByParent(String categoryType, String parentTypeCode, String parentValue) {
        String sql = """
                     select sc.value, sc.name, sc.code, sc.category_id
                     from sys_categories sc
                     where sc.is_deleted = 'N'
                     and sc.category_type = :categoryType
                     and exists (
                        select 1 from sys_category_attributes ct
                        where ct.category_id = sc.category_id
                        and ct.attribute_code = :parentTypeCode
                        and ct.is_deleted = 'N'
                        and ct.attribute_value = :parentValue
                     )
                     order by ifnull(sc.order_number,:maxInteger), name
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("parentTypeCode", parentTypeCode);
        map.put("parentValue", parentValue);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, BaseCategoryDto.class);
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

    public List<EducationCertificatesResponse.AttributeDto> getAttributeOfCategory(Long categoryId) {
        String sql = """
                select attribute_code, attribute_value, data_type, category_id
                from  sys_category_attributes a
                where a.category_id = :categoryId
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        return getListData(sql, map, EducationCertificatesResponse.AttributeDto.class);
    }

    public void insertValues(Map<String, List<ObjectAttributesEntity>> mapValues) {
        String sqlInsertValue = """
            INSERT INTO hr_object_attributes(
                attribute_code, attribute_value, object_id, table_name, 
                data_type, is_deleted, created_by, created_time, modified_by, modified_time
            )
            SELECT 
                :attribute_code, :attribute_value, a.education_certificate_id, :tableName, 
                :data_type, a.is_deleted, a.created_by, a.created_time, a.modified_by, NOW() 
            FROM hr_education_certificates a
            WHERE a.is_deleted = :isDeleted
            AND a.certificate_id = :certificateId
            AND a.employee_id = :employeeId
            """;

        List<Map<String, Object>> listParams = new ArrayList<>();
        mapValues.forEach((key, values) -> {
            Long employeeId = Long.valueOf(key.split("-")[0]);
            String certificateId = key.split("-")[1];

            values.forEach(dto -> {
                HashMap<String, Object> params = new HashMap<>();
                params.put("employeeId", employeeId);
                params.put("certificateId", certificateId);
                params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
                params.put("tableName", "hr_education_certificates");
                params.put("data_type", "string");
                params.put("attribute_code", dto.getAttributeCode());
                params.put("attribute_value", dto.getAttributeValue());
                listParams.add(params);
            });
        });

        jdbcTemplate.batchUpdate(sqlInsertValue, listParams.toArray(new Map[0]));
    }
}
