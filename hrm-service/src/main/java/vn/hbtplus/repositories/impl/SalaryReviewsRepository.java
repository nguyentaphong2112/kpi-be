/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.annotations.Attribute;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.SalaryReviewPeriodDto;
import vn.hbtplus.models.request.SalaryReviewsRequest;
import vn.hbtplus.models.response.SalaryReviewsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.InsuranceSalaryProcessEntity;
import vn.hbtplus.repositories.entity.SalaryReviewsEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Lop repository Impl ung voi bang hr_salary_reviews
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class SalaryReviewsRepository extends BaseRepository {

    public BaseDataTableDto searchData(SalaryReviewsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.salary_review_id,
                    a.period_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    mj.name as jobName,
                    a.position_title,
                    a.organization_id,
                    o.full_name as organizationName,
                    a.salary_rank_id,
                    (select name from hr_salary_ranks sr1 where sr1.salary_rank_id = a.salary_rank_id) as salary_rank_name,
                    a.salary_grade_id,
                    sg.name as salary_grade_name,
                    sg.factor as factor_salary_grade,
                    a.proposed_salary_grade_id,
                    sg.amount as salary_grade_amount,
                    sg1.amount as proposed_salary_grade_amount,
                    sg1.name as proposed_salary_grade_name,
                    sg1.factor as factor_proposed_salary_grade,
                    a.award_infos,
                    a.punishment_infos,
                    a.r0_timekeeping_months,
                    a.increment_date,
                    a.apply_date,
                    a.proposed_apply_date,
                    a.base_proposed_apply_date,
                    a.review_status_id,
                    a.status_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.note,
                    (select sc.name from sys_categories sc where sc.value = a.period_id and sc.category_type = :period) periodName,
                    (select sc.name from sys_categories sc where sc.value = a.type and sc.category_type = :type) typeName,
                    a.last_update_time
                    FROM hr_salary_reviews a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    join hr_salary_grades sg on sg.salary_grade_id = a.salary_grade_id
                    join hr_salary_grades sg1 on sg1.salary_grade_id = a.proposed_salary_grade_id
                    WHERE a.is_deleted = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        addCondition(sql, params, dto);
        sql.append(" ORDER BY o.path_order, a.employee_id");
        return getListPagination(sql.toString(), params, dto, SalaryReviewsResponse.SearchResult.class);
    }


    public SalaryReviewsResponse.SearchResult findById(SalaryReviewsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.salary_review_id,
                    a.period_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    mj.name as jobName,
                    a.position_title,
                    a.organization_id,
                    o.full_name as organizationName,
                    a.salary_rank_id,
                    (select name from hr_salary_ranks sr1 where sr1.salary_rank_id = a.salary_rank_id) as salary_rank_name,
                    a.salary_grade_id,
                    sg.name as salary_grade_name,
                    a.proposed_salary_grade_id,
                    sg.amount as salary_grade_amount,
                    sg1.amount as proposed_salary_grade_amount,
                    sg1.name as proposed_salary_grade_name,
                    a.award_infos,
                    a.punishment_infos,
                    a.r0_timekeeping_months,
                    a.increment_date,
                    a.apply_date,
                    a.proposed_apply_date,
                    a.base_proposed_apply_date,
                    a.review_status_id,
                    a.status_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.note,
                    a.last_update_time
                    FROM hr_salary_reviews a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    join hr_salary_grades sg on sg.salary_grade_id = a.salary_grade_id
                    join hr_salary_grades sg1 on sg1.salary_grade_id = a.proposed_salary_grade_id
                    WHERE a.is_deleted = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        addCondition(sql, params, dto);
        return getFirstData(sql.toString(), params, SalaryReviewsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(String sqlSelect, SalaryReviewsRequest.SearchForm dto, List<String> types) {
        StringBuilder sql = new StringBuilder(sqlSelect);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        if (!types.isEmpty()) {
            sql.append(" and a.type in (:typeFilters)");
            params.put("typeFilters", types);
        }
        sql.append(" ORDER BY o.path_order, a.employee_id");
        List results = getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, SalaryReviewsRequest.SearchForm dto) {
        if (!Utils.isNullOrEmpty(dto.getPeriodId())) {
            sql.append(" and a.period_id = :periodId");
            params.put("periodId", dto.getPeriodId());
        }
        if (!Utils.isNullOrEmpty(dto.getTypes())) {
            sql.append(" and a.type IN (:types)");
            params.put("types", dto.getTypes());
        }
        if (!Utils.isNullOrEmpty(dto.getStatus())) {
            sql.append(" and a.status_id IN (:status)");
            params.put("status", dto.getStatus());
        }
        if (!Utils.isNullOrEmpty(dto.getReviewStatus())) {
            sql.append(" and a.review_status_id IN (:review_status)");
            params.put("review_status", dto.getReviewStatus());
        }
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(e.employee_code) like :keySearch " +
                       "    or lower(e.full_name) like :keySearch" +
                       "    or lower(e.email) like :keySearch )");
            params.put("keySearch", "%" + dto.getKeySearch().trim() + "%");
        }


        params.put("period", Constant.CATEGORY_CODES.HR_KY_NANG_LUONG);
        params.put("type", Constant.CATEGORY_CODES.HR_LOAI_XET_NANG_LUONG);
        QueryUtils.filter(dto.getSalaryReviewId(), sql, params, "a.salary_review_id");


    }

    public SalaryReviewPeriodDto getCategory(String categoryType, String value) throws IllegalAccessException {
        String sql = "select sc.* from sys_categories sc where sc.category_type = :categoryType " +
                     " and sc.value = :value" +
                     " and sc.is_deleted = 'N'";
        Map mapParams = new HashMap();
        mapParams.put("value", value);
        mapParams.put("categoryType", categoryType);

        SalaryReviewPeriodDto periodDto = queryForObject(sql, mapParams, SalaryReviewPeriodDto.class);

        List<String> codes = new ArrayList<>();
        for (Field field : SalaryReviewPeriodDto.class.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(Attribute.class) != null) {
                codes.add(field.getAnnotation(Attribute.class).code().toUpperCase());
            }
        }
        String sqlAttributes = "select attribute_code, attribute_value " +
                               " from sys_category_attributes st" +
                               " where st.is_deleted = 'N'" +
                               " and st.category_id = :categoryId";
        mapParams.put("categoryId", periodDto.getCategoryId());
        List<Map<String, Object>> entities = getListData(sqlAttributes, mapParams);
        Map<String, String> mapValues = new HashMap<>();
        entities.stream().forEach(item -> {
            mapValues.put(((String) item.get("ATTRIBUTE_CODE")).toUpperCase(), (String) item.get("ATTRIBUTE_VALUE"));
        });
        // Iterate through the fields and set their values from mapValues
        for (Field field : SalaryReviewPeriodDto.class.getDeclaredFields()) {
            field.setAccessible(true);
            Attribute parameter = field.getAnnotation(Attribute.class);
            if (parameter != null) {
                String attributeValue = mapValues.get(parameter.code().toUpperCase());
                if (attributeValue == null) {
                    attributeValue = "";
                }
                if (attributeValue != null) {
                    Class<?> fieldType = field.getType();
                    if ("java.lang.String".equalsIgnoreCase(fieldType.getName())) {
                        field.set(periodDto, attributeValue);
                    } else if ("java.lang.Double".equalsIgnoreCase(fieldType.getName())) {
                        field.set(periodDto, Double.valueOf(attributeValue));
                    } else if ("java.lang.Long".equalsIgnoreCase(fieldType.getName())) {
                        field.set(periodDto, Long.valueOf(attributeValue));
                    } else if ("java.lang.Integer".equalsIgnoreCase(fieldType.getName())) {
                        field.set(periodDto, Integer.valueOf(attributeValue));
                    } else if ("java.util.Date".equalsIgnoreCase(fieldType.getName())) {
                        field.set(periodDto, Utils.stringToDate(attributeValue));
                    } else if (fieldType.isAssignableFrom(List.class) && field.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Class<?> genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        if (genericType.equals(Long.class)) {
                            List<Long> longList = Arrays.stream(attributeValue.split(","))
                                    .map(Long::valueOf)
                                    .collect(Collectors.toList());
                            field.set(periodDto, longList);
                        } else if (genericType.equals(String.class)) {
                            attributeValue = attributeValue.replace(" ", "");
                            field.set(periodDto, Arrays.asList(attributeValue.split(",")));
                        }
                    }
                } else {
                    throw new BaseAppException("Chưa có dữ dữ liệu cấu hình của " + field.getAnnotation(Attribute.class).code());
                }
            }
        }


        return periodDto;
    }

    public List<SalaryReviewsEntity> getListProposed(SalaryReviewPeriodDto periodEntity, String type) {
        StringBuilder sql = new StringBuilder("""
                select 
                	a.employee_id,
                	e.organization_id organization_id,
                	e.job_id job_id,
                	e.job_title position_title,
                	a.increment_date increment_date,
                	a.start_date as apply_date,	
                	a.salary_rank_id,
                	sg.salary_grade_id,
                	sg1.salary_grade_id proposed_salary_grade_id,
                	{thoi_gian_de_xuat} proposed_apply_date,
                	:type as type
                from hr_insurance_salary_process a, hr_salary_grades sg ,
                hr_salary_grades sg1, hr_employees e ,
                hr_work_process wp
                where a.salary_grade_id = sg.salary_grade_id
                and sg.duration > 0
                and a.is_deleted = 'N'
                and wp.is_deleted = 'N'
                and wp.employee_id = e.employee_id
                and wp.document_type_id in (
                	select document_type_id from hr_document_types where type <> 'OUT'
                )
                and :configDate BETWEEN wp.start_date and IFNULL(wp.end_date, :configDate)
                and e.employee_id = a.employee_id
                and not exists (
                	select 1 from hr_insurance_salary_process isp1
                	where isp1.employee_id = a.employee_id
                	and isp1.start_date > a.start_date
                	and isp1.is_deleted = 'N'
                )
                                
                and sg1.salary_rank_id = sg.salary_rank_id
                and sg1.amount > sg.amount
                and sg1.is_deleted = 'N'
                and not exists (
                	select 1 from hr_salary_grades sg2
                	where sg2.salary_rank_id = sg.salary_rank_id
                	and sg2.is_deleted = 'N'
                	and sg2.amount > sg.amount
                	and sg2.amount < sg1.amount
                )
                and not exists (
                    select 1 from hr_salary_reviews a1
                    where a1.employee_id = a.employee_id
                    and a1.is_deleted = 'N'
                    and a1.period_id = :periodId
                    and a1.status_id in (:trangThaiPheDuyet)
                )
                """);
        String thoiGianDexuat = "DATE_ADD(IFNULL(a.increment_date, a.start_date),INTERVAL sg.duration MONTH)";
        if (SalaryReviewsEntity.TYPES.TRUOC_HAN.equalsIgnoreCase(type)) {
            sql.append(" and DATE_ADD(IFNULL(a.increment_date, a.start_date),INTERVAL sg.duration MONTH) > :endDate");
            sql.append(" and DATE_ADD(IFNULL(a.increment_date, a.start_date)" +
                       "    ,INTERVAL (sg.duration - f_get_thoi_gian_rut_ngan(a.employee_id,DATE_ADD(IFNULL(a.increment_date, a.start_date), INTERVAL -2*sg.duration MONTH))) MONTH) <= :endDate");
            thoiGianDexuat = "DATE_ADD(IFNULL(a.increment_date, a.start_date),INTERVAL (sg.duration - f_get_thoi_gian_rut_ngan(a.employee_id, DATE_ADD(IFNULL(a.increment_date, a.start_date), INTERVAL -2*sg.duration MONTH))) MONTH)";
            sql.append(" and IFNULL(sg.seniority_percent,0) = 0");
            sql.append(" and ifnull(a.is_early_increased,'N') = 'N'"); //khong phai nang luong truoc han
        } else if (SalaryReviewsEntity.TYPES.THUONG_XUYEN.equalsIgnoreCase(type)) {
            sql.append(" and DATE_ADD(IFNULL(a.increment_date, a.start_date),INTERVAL sg.duration MONTH) <= :endDate");
            sql.append(" and IFNULL(sg.seniority_percent,0) = 0");
        } else {
            sql.append(" and DATE_ADD(IFNULL(a.increment_date, a.start_date),INTERVAL sg.duration MONTH) <= :endDate");
            sql.append(" and IFNULL(sg.seniority_percent,0) > 0");
        }

        Map mapParams = new HashMap();
        mapParams.put("trangThaiPheDuyet", Arrays.asList(SalaryReviewsEntity.STATUS.DA_PHE_DUYET, SalaryReviewsEntity.STATUS.DA_KY));
        mapParams.put("periodId", periodEntity.getValue());
        mapParams.put("endDate", periodEntity.getEndDate());
        mapParams.put("type", type);
        mapParams.put("configDate", periodEntity.getPeriodDate());
        String sqlQuery = sql.toString().replace("{thoi_gian_de_xuat}", thoiGianDexuat);
        return getListData(sqlQuery, mapParams, SalaryReviewsEntity.class);
    }

    public List<Map<String, Object>> getListForImport(String periodId) {
        String sql = """
                select e.employee_code as ma_nhan_vien,
                e.full_name as ten_nhan_vien
                from hr_employees e, hr_salary_reviews a, hr_organizations o
                where a.is_deleted = 'N'
                and a.employee_id = e.employee_id
                and a.organization_id = o.organization_id
                and a.period_id = :periodId
                and a.status_id not in (:daKy)
                and a.review_status_id = 'OK'
                order by o.path_order, e.employee_id
                """;
        Map mapParams = new HashMap();
        mapParams.put("periodId", periodId);
        mapParams.put("daKy", SalaryReviewsEntity.STATUS.DA_KY);
        return getListData(sql, mapParams);
    }

    public List<InsuranceSalaryProcessEntity> getLastInsuranceSalaryProcess(List<Long> empIds) {
        String sql = "select a.* from hr_insurance_salary_process a" +
                     " where a.employee_id in (:empIds)" +
                     " and a.is_deleted = 'N'";
        Map mapParams = new HashMap();
        mapParams.put("empIds", empIds);
        return getListData(sql, mapParams, InsuranceSalaryProcessEntity.class);
    }

    public List<SalaryReviewsEntity> getListSalaryReviewsEntityByEmpCode(String periodId, List<String> empCodes) {
        String sql = "select a.*, e.employee_code from hr_salary_reviews a, hr_employees e" +
                     " where a.is_deleted = 'N'" +
                     " and a.employee_id = e.employee_id" +
                     " and a.period_id = :periodId" +
                     " and e.employee_code in (:empCodes)";
        Map mapParams = new HashMap();
        mapParams.put("empCodes", empCodes);
        mapParams.put("periodId", periodId);
        return getListData(sql, mapParams, SalaryReviewsEntity.class);
    }

    public void insertAttachmentForProcess(List<Long> salaryReviewIds) {
        String sql = """
                insert into hr_attachments(file_id, object_id, table_name, function_code, is_deleted, created_by, created_time, file_name)
                select
                	a.file_id, isp.insurance_salary_process_id, 'hr_insurance_salary_process', :functionCode, 'N', a.created_by, a.created_time, a.file_name
                from hr_insurance_salary_process isp, hr_attachments a , hr_salary_reviews rv
                where a.table_name = 'hr_salary_reviews'
                and a.object_id in (:reviewIds)
                and a.function_code = :functionCodeReview
                and a.is_deleted = 'N'
                and a.object_id = rv.salary_review_id
                and rv.employee_id = isp.employee_id
                and isp.start_date = rv.proposed_apply_date
                and isp.is_deleted = 'N'
                and not exists (
                	select 1 from hr_attachments a1
                	where a1.table_name = 'hr_insurance_salary_process'
                	and a1.file_id = a.file_id
                	and a1.object_id = isp.insurance_salary_process_id
                	and a1.function_code = :functionCode
                )
                """;
        Map mapParams = new HashMap();
        mapParams.put("functionCode", Constant.ATTACHMENT.FILE_TYPES.INSURANCE_SALARY_PROCESS_EMP);
        mapParams.put("functionCodeReview", Constant.ATTACHMENT.FILE_TYPES.SALARY_REVIEW_DOCUMENT_SIGNED);
        mapParams.put("reviewIds", salaryReviewIds);
        executeSqlDatabase(sql, mapParams);
    }
}
