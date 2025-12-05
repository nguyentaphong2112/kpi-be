/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.OrgDto;
import vn.hbtplus.models.request.IndicatorsRequest;
import vn.hbtplus.models.response.IndicatorsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.CategoryAttributeEntity;
import vn.hbtplus.repositories.entity.ObjectRelationsEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class IndicatorsRepository extends BaseRepository {

    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;


    public BaseDataTableDto searchData(IndicatorsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.*,
                    CASE
                        WHEN a.indicator_id IN (:selectedValue) THEN 1
                        ELSE 0
                    END AS valueSelect,
                    (SELECT GROUP_CONCAT(CONCAT('- ', kis.name) ORDER BY kis.name SEPARATOR '\n')
                        FROM kpi_indicators kis
                        JOIN kpi_object_relations kor2 ON (a.indicator_id = kor2.object_id 
                        and kor2.table_name = :tableName
                        and kor2.refer_object_id = kis.indicator_id
                        and kor2.refer_table_name = :referTableName
                        and kor2.function_code = :functionCode
                        and IFNULL(kor2.is_deleted, :activeStatus) = :activeStatus)) AS relatedNames,
                      (SELECT GROUP_CONCAT(CONCAT('- ', o.name) ORDER BY o.name SEPARATOR '\n')
                        FROM hr_organizations o
                        JOIN kpi_object_relations kor ON (a.indicator_id = kor.object_id 
                        and kor.table_name = :tableName
                        and kor.refer_object_id = o.organization_id
                        and kor.refer_table_name = :referTableName2
                        and kor.function_code = :functionCode2
                        and IFNULL(kor.is_deleted, :activeStatus) = :activeStatus)) AS scopeNames,
                    (SELECT sc.name FROM sys_categories sc WHERE a.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE a.period_type = sc.value and sc.category_type = :chuKy) periodTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :phanLoai) typeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("selectedValue", Utils.isNullOrEmpty(dto.getSelectedValue()) ? List.of(0) : dto.getSelectedValue());
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("phanLoai", Constant.CATEGORY_TYPES.PHAN_LOAI);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN);
        params.put("referTableName2", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode2", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, IndicatorsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(IndicatorsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.*,
                    CASE
                        WHEN a.indicator_id IN (:selectedValue) THEN 1
                        ELSE 0
                    END AS valueSelect,
                    (SELECT GROUP_CONCAT(CONCAT('- ', kis.name) ORDER BY kis.name SEPARATOR '\n')
                        FROM kpi_indicators kis
                        JOIN kpi_object_relations kor2 ON (a.indicator_id = kor2.object_id 
                        and kor2.table_name = :tableName
                        and kor2.refer_object_id = kis.indicator_id
                        and kor2.refer_table_name = :referTableName
                        and kor2.function_code = :functionCode
                        and IFNULL(kor2.is_deleted, :activeStatus) = :activeStatus)) AS relatedNames,
                      (SELECT GROUP_CONCAT(CONCAT('- ', o.name) ORDER BY o.name SEPARATOR '\n')
                        FROM hr_organizations o
                        JOIN kpi_object_relations kor ON (a.indicator_id = kor.object_id 
                        and kor.table_name = :tableName
                        and kor.refer_object_id = o.organization_id
                        and kor.refer_table_name = :referTableName2
                        and kor.function_code = :functionCode2
                        and IFNULL(kor.is_deleted, :activeStatus) = :activeStatus)) AS scopeNames,
                    (SELECT sc.name FROM sys_categories sc WHERE a.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE a.period_type = sc.value and sc.category_type = :chuKy) periodTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :phanLoai) typeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("selectedValue", Utils.isNullOrEmpty(dto.getSelectedValue()) ? List.of(0) : dto.getSelectedValue());
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("phanLoai", Constant.CATEGORY_TYPES.PHAN_LOAI);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN);
        params.put("referTableName2", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode2", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, IndicatorsRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_indicators a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND lower(a.name) like :keySearch ");
            params.put("keySearch", "%" + dto.getKeySearch().toLowerCase() + "%");
        }
        QueryUtils.filter(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getUnitId(), sql, params, "a.unit_id");
        QueryUtils.filter(dto.getPeriodType(), sql, params, "a.period_type");
        QueryUtils.filter(dto.getMeasurement(), sql, params, "a.measurement");
        QueryUtils.filter(dto.getSystemInfo(), sql, params, "a.system_info");
        QueryUtils.filter(dto.getSignificance(), sql, params, "a.significance");
        if (!Utils.isNullOrEmpty(dto.getValueFilter())) {
            sql.append(" AND a.indicator_id NOT IN (:valueFilter) ");
            params.put("valueFilter", dto.getValueFilter());
        }
//        sql.append("""
//
//                AND exists (SELECT 1
//                        FROM hr_organizations o
//                        JOIN kpi_object_relations kor ON (a.indicator_id = kor.object_id
//                        and kor.table_name = :tableName
//                        and kor.refer_object_id = o.organization_id
//                        and kor.refer_table_name = :referTableName2
//                        and kor.function_code = :functionCode2
//                        and IFNULL(kor.is_deleted, :activeStatus) = :activeStatus)
//                        WHERE 1 = 1
//
//                """);
//        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
//                Scope.VIEW, Constant.RESOURCES.INDICATOR, Utils.getUserNameLogin()
//        );
//        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
//        sql.append(" )");
        sql.append(" ORDER BY valueSelect DESC, a.created_time DESC");

        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
    }

    public BaseDataTableDto<IndicatorsResponse.SearchResult> getIndicatorPicker(Long organizationId, IndicatorsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    CASE
                        WHEN i.indicator_id IN (:selectedValue) THEN 1
                        ELSE 0
                    END AS valueSelect,
                    i.indicator_id,
                    i.name,
                    i.unit_id,
                    i.period_type,
                    i.significance,
                    i.measurement,
                    i.system_info,
                    i.type,
                    i.note,
                    i.rating_type,
                    i.list_values,
                    (SELECT sc.name FROM sys_categories sc WHERE i.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE i.period_type = sc.value and sc.category_type = :chuKy) periodTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE i.type = sc.value and sc.category_type = :phanLoai) typeName          
                FROM 
                    kpi_indicators i
                WHERE IFNULL(i.is_deleted, :activeStatus) = :activeStatus        
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        params.put("phanLoai", Constant.CATEGORY_TYPES.PHAN_LOAI);
        params.put("selectedValue", Utils.isNullOrEmpty(dto.getSelectedValue()) ? List.of(0) : dto.getSelectedValue());
//        List<Long> listOrg = getRelativeIds(id, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS, ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
//        QueryUtils.filterLikeOrg();
        sql.append("""
                AND exists (SELECT 1
                            FROM hr_organizations o
                            JOIN kpi_object_relations kor
                            ON (i.indicator_id = kor.object_id
                            and kor.table_name = :tableName
                            and kor.refer_object_id = o.organization_id
                            and kor.refer_table_name = :referTableName
                            and kor.function_code = :functionCode
                            and IFNULL(kor.is_deleted, :activeStatus) = :activeStatus)
                            and exists (
                                    select 1 from hr_organizations pOrg
                                    where pOrg.organization_id = :referObjectId
                                    and pOrg.path_id like concat(o.path_id,'%')
                                ))
                """);
        params.put("referObjectId", organizationId);
        this.addConditionIndicatorPicker(dto, sql, params);
        return getListPagination(sql.toString(), params, dto, IndicatorsResponse.SearchResult.class);
    }


    private void addConditionIndicatorPicker(IndicatorsRequest.SearchForm dto, StringBuilder sql, HashMap<String, Object> params) {
        QueryUtils.filter(dto.getKeySearch(), sql, params, "i.name");
        sql.append(" ORDER BY valueSelect desc");
    }

    public boolean isDuplicate(String name, Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    COUNT(*)
                FROM kpi_indicators a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.name = :name
                AND exists (SELECT 1
                        FROM hr_organizations o
                        JOIN kpi_object_relations kor ON (a.indicator_id = kor.object_id
                        and kor.table_name = :tableName
                        and kor.refer_object_id = o.organization_id
                        and kor.refer_table_name = :referTableName2
                        and kor.function_code = :functionCode2
                        and IFNULL(kor.is_deleted, :activeStatus) = :activeStatus)
                        WHERE 1 = 1
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("name", name);
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.INDICATOR, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" )");
        if (id != null && id > 0L) {
            sql.append(" AND a.indicator_id != :id");
            params.put("id", id);
        }
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName2", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode2", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        return queryForObject(sql.toString(), params, Long.class) > 0;
    }

    public List<IndicatorsResponse.DetailList> getList(Long organizationId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.*,
                    ic.indicator_conversion_id,
                    (
                    SELECT
                        sc.name
                    FROM
                        sys_categories sc
                    WHERE
                        p.unit_id = sc.value
                        and sc.category_type = :donViTinh) unitName
                 FROM
                    kpi_indicators p
                 JOIN kpi_indicator_conversions ic ON
                    ic.indicator_id = p.indicator_id
                 JOIN hr_organizations org ON
                    ic.organization_id = org.organization_id
                    AND org.organization_id = :organizationId
                    AND ic.org_type_id = org.org_type_id
                 WHERE
                    ic.is_deleted = 'N'
                    AND ic.job_id IS NULL
                    AND p.is_deleted = :activeStatus
                 ORDER BY
                    p.name
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("organizationId", organizationId);
        return getListData(sql.toString(), params, IndicatorsResponse.DetailList.class);
    }

    public List<CategoryDto> getListCategories(String categoryType) {
        String sql = """
                select *
                from sys_categories
                where is_deleted = 'N'
                  and category_type = :categoryType
                  order by ifnull(order_number,:maxInteger)
                  """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, CategoryDto.class);
    }

    public List<CategoryAttributeEntity> getAllAttributeByCategoryType(String categoryType) {
        String sql = """
                select attribute_code, attribute_value, data_type, category_id
                from  sys_category_attributes a
                where a.category_id in (
                    select category_id from sys_categories sc 
                    where sc.category_type = :categoryType
                )
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        return getListData(sql, map, CategoryAttributeEntity.class);
    }

    public List<IndicatorsResponse.DetailList> getListEmployee(Long employeeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.*,
                    ic.indicator_conversion_id,
                    (
                    SELECT
                        sc.name
                    FROM
                        sys_categories sc
                    WHERE
                        p.unit_id = sc.value
                        and sc.category_type = :donViTinh) unitName
                  FROM
                    kpi_indicators p
                  JOIN kpi_indicator_conversions ic ON
                    ic.indicator_id = p.indicator_id
                  JOIN hr_organizations org ON
                    ic.organization_id = org.organization_id
                  JOIN (
                    SELECT
                        organization_id
                    FROM
                         hr_employees
                    WHERE
                        employee_id = :employeeId) emp
                  WHERE
                    ic.is_deleted = 'N'
                    AND ic.org_type_id = org.org_type_id
                    AND org.organization_id = emp.organization_id
                    AND p.is_deleted = :activeStatus
                    AND ic.job_id IS NOT NULL
                  ORDER BY
                    p.name
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("employeeId", employeeId);
        return getListData(sql.toString(), params, IndicatorsResponse.DetailList.class);
    }

    public List<IndicatorsResponse.DetailList> getListEmployee2(Long employeeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.*,
                    ic.indicator_conversion_id,
                    (
                    SELECT
                        sc.name
                    FROM
                        sys_categories sc
                    WHERE
                        p.unit_id = sc.value
                        and sc.category_type = :donViTinh) unitName
                  FROM
                    kpi_indicators p
                  JOIN kpi_indicator_conversions ic ON
                    ic.indicator_id = p.indicator_id
                  JOIN hr_organizations org ON
                    ic.organization_id = org.organization_id
                  JOIN (
                    SELECT
                        organization_id
                    FROM
                         hr_employees
                    WHERE
                        employee_id = :employeeId) emp
                  WHERE
                    ic.is_deleted = 'N'
                    AND ic.org_type_id = org.org_type_id
                    AND org.organization_id = emp.organization_id
                    AND p.is_deleted = :activeStatus
                    AND ic.job_id IS NULL
                  ORDER BY
                    p.name
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("employeeId", employeeId);
        return getListData(sql.toString(), params, IndicatorsResponse.DetailList.class);
    }

    public List<Long> getRelativeIds(Long indicatorId, String tableName, String referTableName, String functionCode) {
        String sql = """
                select sg.refer_object_id
                from kpi_object_relations sg
                where sg.object_id = :indicatorId
                and sg.table_name = :tableName
                and sg.refer_table_name = :referTableName
                and sg.function_code = :functionCode
                and sg.is_deleted = 'N'
                """;
        Map params = new HashMap();
        params.put("indicatorId", indicatorId);
        params.put("tableName", tableName);
        params.put("referTableName", referTableName);
        params.put("functionCode", functionCode);
        return getListData(sql, params, Long.class);
    }

    public List<OrgDto> getListOrg() {
        String sql = """
                select organization_id, full_name
                from hr_organizations
                where is_deleted = 'N'
                  order by ifnull(order_number,:maxInteger), name
                  """;
        Map<String, Object> map = new HashMap<>();
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, OrgDto.class);
    }

    public String getMappingValue(String parameter, String configCode, Date configDate) {
        String sql = """
                select value from sys_mapping_values a
                where a.parameter = :parameter
                and a.config_mapping_code = :configCode
                and a.is_deleted = 'N'
                and :configDate between ifnull(a.start_date,:configDate) and ifnull(a.end_date,:configDate)
                """;
        Map params = new HashMap();
        params.put("parameter", parameter);
        params.put("configCode", configCode);
        params.put("configDate", configDate);
        return getFirstData(sql, params, String.class);
    }
}
