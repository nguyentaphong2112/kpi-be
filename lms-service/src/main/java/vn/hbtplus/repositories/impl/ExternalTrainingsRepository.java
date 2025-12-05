/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.ExternalTrainingsRequest;
import vn.hbtplus.models.response.ExternalTrainingsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lms_external_trainings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ExternalTrainingsRepository extends BaseRepository {

    public BaseDataTableDto searchData(ExternalTrainingsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.external_training_id,
                    a.type_id,
                    a.full_name,
                    a.gender_id,
                    a.year_of_birth,
                    a.mobile_number,
                    a.personal_id_no,
                    a.address,
                    e.full_name as mentorName,
                    o.full_name organizationName,
                    (select name from sys_categories sc where sc.category_type = :doiTuong and sc.value = a.type_id) as typeName, 
                    (select name from sys_categories sc where sc.category_type = :gioiTinh and sc.value = a.gender_id) as genderName, 
                    (select name from sys_categories sc where sc.category_type = :hinhThuc and sc.value = a.trainning_type_id) as trainningTypeName, 
                    (select name from sys_categories sc where sc.category_type = :chuyenNganh and sc.value = a.training_major_id) as trainingMajorName, 
                    (select name from sys_categories sc where sc.category_type = :tinhTrang and sc.value = a.tuition_fee_status_id) as tuitionFeeStatusName, 
                    a.organization_address,
                    a.start_date,
                    a.end_date,
                    a.content,
                    a.admission_results,
                    a.graduated_results,
                    a.number_of_lessons,
                    a.certificate_no,
                    a.certificate_date,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ExternalTrainingsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(ExternalTrainingsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.external_training_id,
                    a.type_id,
                    a.full_name,
                    a.gender_id,
                    a.year_of_birth,
                    a.mobile_number,
                    a.personal_id_no,
                    a.address,
                    e.full_name as mentorName,
                    o.full_name organizationName,
                    (select name from sys_categories sc where sc.category_type = :doiTuong and sc.value = a.type_id) as typeName, 
                    (select name from sys_categories sc where sc.category_type = :gioiTinh and sc.value = a.gender_id) as genderName, 
                    (select name from sys_categories sc where sc.category_type = :hinhThuc and sc.value = a.trainning_type_id) as trainningTypeName, 
                    (select name from sys_categories sc where sc.category_type = :chuyenNganh and sc.value = a.training_major_id) as trainingMajorName, 
                    (select name from sys_categories sc where sc.category_type = :tinhTrang and sc.value = a.tuition_fee_status_id) as tuitionFeeStatusName, 
                    a.organization_address,
                    a.start_date,
                    a.end_date,
                    a.content,
                    a.admission_results,
                    a.graduated_results,
                    a.number_of_lessons,
                    a.certificate_no,
                    a.certificate_date,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ExternalTrainingsRequest.SearchForm dto) {
        sql.append("""
            FROM lms_external_trainings a
            LEFT JOIN hr_employees e ON e.employee_id = a.mentor_id
            LEFT JOIN hr_organizations o ON o.organization_id = a.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("doiTuong", Constant.CATEGORY_CODES.LMS_DOI_TUONG_NGOAI);
        params.put("gioiTinh", Constant.CATEGORY_CODES.GIOI_TINH);
        params.put("hinhThuc", Constant.CATEGORY_CODES.LMS_HINH_THUC_DAO_TAO);
        params.put("chuyenNganh", Constant.CATEGORY_CODES.LMS_CHUYEN_NGANH_NGOAI_VIEN);
        params.put("tinhTrang", Constant.CATEGORY_CODES.LMS_TINH_TRANG_HOC_PHI);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name", "a.address");
        QueryUtils.filter(dto.getTypeId(), sql, params, "a.type_id");
        if (dto.getStartDate() != null) {
            sql.append(" and NVL(a.end_date, :startDate) >= :startDate");
            params.put("startDate", dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            sql.append(" and NVL(a.start_date, :endDate) <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
    }
}
