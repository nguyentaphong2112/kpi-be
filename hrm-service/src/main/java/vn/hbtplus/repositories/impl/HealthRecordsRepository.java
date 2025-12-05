/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.HealthRecordsRequest;
import vn.hbtplus.models.response.HealthRecordsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.entity.ObjectAttributesEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang hr_health_records
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class HealthRecordsRepository extends BaseRepository {

    public BaseDataTableDto searchData(HealthRecordsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.health_record_id,
                        a.employee_id,
                        e.employee_code,
                        e.full_name employee_name,
                        e.date_of_birth,
                        o.full_name orgName,
                        mj.name jobName,
                        (SELECT sc.name FROM sys_categories sc WHERE sc.value = e.gender_id AND sc.category_type = :genderCode) genderName,
                        a.examination_period_id,
                        (SELECT sc.name FROM sys_categories sc WHERE a.examination_period_id = sc.value and sc.category_type = :examinationPeriodType) examinationPeriodName,
                        a.examination_date,
                        a.result_id,
                        (SELECT sc.name FROM sys_categories sc WHERE a.result_id = sc.value and sc.category_type = :resultType) resultName,
                        a.disease_ids,
                        (SELECT GROUP_CONCAT(sc.name SEPARATOR ', ')
                        FROM sys_categories sc
                        WHERE FIND_IN_SET(sc.value, a.disease_ids)
                        AND sc.category_type = :diseaseType) as disease_names,
                        a.patient_id,
                        a.is_deleted,
                        a.created_by,
                        a.created_time,
                        a.modified_by,
                        a.modified_time,
                        a.last_update_time
                        FROM hr_health_records a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                JOIN hr_organizations o ON o.organization_id = e.organization_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, HealthRecordsResponse.class);
    }

    public List<Map<String, Object>> getListExport(String sqlSelect, HealthRecordsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.health_record_id,
                        a.employee_id,
                        e.employee_code,
                        e.full_name employee_name,
                        e.date_of_birth,
                        o.full_name orgName,
                        mj.name jobName,
                        (SELECT sc.name FROM sys_categories sc WHERE sc.value = e.gender_id AND sc.category_type = :genderCode) genderName,
                        a.examination_period_id,
                        (SELECT sc.name FROM sys_categories sc WHERE a.examination_period_id = sc.value and sc.category_type = :examinationPeriodType) examinationPeriodName,
                        a.examination_date,
                        a.result_id,
                        (SELECT sc.name FROM sys_categories sc WHERE a.result_id = sc.value and sc.category_type = :resultType) resultName,
                        a.disease_ids,
                        (SELECT GROUP_CONCAT(sc.name SEPARATOR ', ')
                        FROM sys_categories sc
                        WHERE FIND_IN_SET(sc.value, a.disease_ids)
                        AND sc.category_type = :diseaseType) as disease_names,
                        a.patient_id,
                        a.is_deleted,
                        a.created_by,
                        a.created_time,
                        a.modified_by,
                        a.modified_time,
                        a.last_update_time
                        FROM hr_health_records a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                JOIN hr_organizations o ON o.organization_id = e.organization_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus 
                """);
        if (sqlSelect != null) {
            sql = new StringBuilder(sqlSelect + " ");
        }

        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, HealthRecordsRequest.SearchForm dto) {
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("examinationPeriodType", Constant.CATEGORY_CODES.HRM_DOT_KHAM_SUC_KHOE);
        params.put("resultType", Constant.CATEGORY_CODES.HRM_XEP_LOAI_SUC_KHOE);
        params.put("diseaseType", Constant.CATEGORY_CODES.HRM_BENH_GAP_PHAI);
        params.put("genderCode", Constant.CATEGORY_CODES.GIOI_TINH);
        QueryUtils.filter(dto.getEmployeeCode(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getExaminationPeriodId(), sql, params, "a.examination_period_id");
        sql.append(" ORDER BY o.path_order, mj.order_number, e.employee_id, a.examination_period_id");
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

    public void insertValues(Map<Long, List<ObjectAttributesEntity>> mapValues, String periodId) {
        String sqlInsertValue = """
                INSERT INTO hr_object_attributes(
                    attribute_code, attribute_value, object_id, table_name, 
                    data_type, is_deleted, created_by, created_time, modified_by, modified_time
                )
                SELECT 
                    :attribute_code, :attribute_value, a.health_record_id, :tableName, 
                    :data_type, a.is_deleted, a.created_by, a.created_time, a.modified_by, NOW() 
                FROM hr_health_records a
                WHERE a.is_deleted = :isDeleted
                AND a.examination_period_id = :examinationPeriodId
                AND a.employee_id = :employeeId
                """;

        List<Map<String, Object>> listParams = new ArrayList<>();
        mapValues.forEach((employeeId, values) -> {

            values.forEach(dto -> {
                HashMap<String, Object> params = new HashMap<>();
                params.put("employeeId", employeeId);
                params.put("examinationPeriodId", periodId);
                params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
                params.put("tableName", "hr_health_records");
                params.put("data_type", "string");
                params.put("attribute_code", dto.getAttributeCode());
                params.put("attribute_value", dto.getAttributeValue());
                listParams.add(params);
            });
        });

        jdbcTemplate.batchUpdate(sqlInsertValue, listParams.toArray(new Map[0]));
    }

    public List<HealthRecordsResponse> getListHealthRecordsByEmpCodes(List<String> listEmployeeCode) {
        String sql = "select e.employee_code, a.* from hr_health_records a, hr_employees e" +
                     " where a.employee_id = e.employee_id" +
                     " and a.is_deleted = :isDeleted" +
                     " and e.employee_code in (:empCodes)";
        List<List<String>> partitions = Utils.partition(listEmployeeCode, 999);
        List<HealthRecordsResponse> result = new ArrayList<>();
        partitions.forEach(partition -> {
            Map<String, Object> map = new HashMap<>();
            map.put("empCodes", partition);
            map.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
            result.addAll(getListData(sql, map, HealthRecordsResponse.class));
        });
        return result;
    }

    public void deleteOldData(List<String> empCodeList, String periodId) {
        String sql = """
                update hr_health_records a set a.is_deleted = 'Y',
                    a.modified_by = :userName,
                    a.modified_time = now()
                where a.examination_period_id = :periodId
                and a.is_deleted = 'N'
                and a.employee_id in (
                    select e.employee_id from hr_employees e
                    where e.employee_code in (:empCodes)
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("userName", Utils.getUserNameLogin());
        params.put("empCodes", empCodeList);
        executeSqlDatabase(sql, params);
    }
}
