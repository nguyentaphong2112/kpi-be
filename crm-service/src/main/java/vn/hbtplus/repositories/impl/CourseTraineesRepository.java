/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.CustomersResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.CourseTraineesRequest;
import vn.hbtplus.models.response.CourseTraineesResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_course_trainees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class CourseTraineesRepository extends BaseRepository {

    public BaseDataTableDto searchData(CourseTraineesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_trainee_id,
                    a.course_id,
                    a.trainee_id,
                    a.instructor_id
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CourseTraineesResponse.class);
    }

    public List<Map<String, Object>> getListExport(CourseTraineesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_trainee_id,
                    a.course_id,
                    a.trainee_id,
                    a.instructor_id
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, CourseTraineesRequest.SearchForm dto) {
        sql.append("""
            FROM crm_course_trainees a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public void deleteTrainee(List<Long> id, Long courseId) {
        StringBuilder sql = new StringBuilder("""
                UPDATE crm_course_trainees a
                SET a.is_deleted = 'Y'
                WHERE a.course_trainee_id NOT IN (:id)
                AND a.course_id = :courseId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("courseId", courseId);
        executeSqlDatabase(sql.toString(), params);
    }

    public List<CustomersResponse.DataSelected> getListData(Long courseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.customer_id,
                    CONCAT(a.full_name, ' - ', a.mobile_number) as fullName,
                    a.mobile_number,
                    a.date_of_birth
                FROM crm_customers a
                JOIN crm_course_trainees b ON a.customer_id = b.trainee_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND b.course_id = :courseId
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("courseId", courseId);
        return getListData(sql.toString(), params, CustomersResponse.DataSelected.class);
    }






}
