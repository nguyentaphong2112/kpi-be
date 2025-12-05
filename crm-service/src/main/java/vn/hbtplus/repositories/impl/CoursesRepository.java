/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.CoursesRequest;
import vn.hbtplus.models.response.CoursesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_courses
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class CoursesRepository extends BaseRepository {

    private final UtilsService utilsService;

    public BaseDataTableDto searchData(CoursesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_id,
                    b.course_trainee_id,
                    a.training_program_id,
                    a.start_date,
                    a.end_date,
                    a.name as courseName,
                    c.full_name as name,
                    c.mobile_number,
                    c.date_of_birth,
                    SUM(cclr.point) totalPoint,
                    DENSE_RANK() OVER (PARTITION BY a.course_id ORDER BY SUM(cclr.point) DESC) as rank,
                    COUNT(ccl.course_lesson_id) as totalLessons,
                    CONCAT(ROUND((SUM(cclr.point) / COUNT(ccl.course_lesson_id) * 100) / 100, 2), '%') as completionRate, 	
                    b.trainee_id,
                    u.full_name as instructorName,
                    u.mobile_number as phoneNumberInstructor,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    CONCAT(DATE_FORMAT(a.start_date, '%d/%m/%Y'), ' - ', DATE_FORMAT(a.end_date, '%d/%m/%Y')) AS date_range,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CoursesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(CoursesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_id,
                    a.training_program_id,
                    a.start_date,
                    a.end_date,
                    a.name,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, CoursesRequest.SearchForm dto) {
        sql.append("""
                    FROM crm_courses a
                    JOIN crm_course_trainees b ON a.course_id = b.course_id AND IFNULL(b.is_deleted, :activeStatus) = :activeStatus
                    JOIN crm_course_lessons ccl ON a.course_id = ccl.course_id AND IFNULL(ccl.is_deleted, :activeStatus) = :activeStatus
                    LEFT JOIN crm_course_lesson_results cclr ON ccl.course_lesson_id = cclr.course_lesson_id AND b.trainee_id = cclr.trainee_id
                    JOIN crm_customers c ON b.trainee_id = c.customer_id
                    JOIN crm_customers u ON u.customer_id = b.instructor_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (dto.getCourseId() != null && dto.getCourseId() > 0L) {
            sql.append(" AND a.course_id = :courseId");
            params.put("courseId", dto.getCourseId());
        }
        QueryUtils.filter(dto.getKeySearch(), sql, params, "c.full_name", "c.mobile_number");
        QueryUtils.filter(dto.getPhoneNumber(), sql, params, "c.mobile_number");
        QueryUtils.filter(dto.getTraineeId(), sql, params, "c.customer_id");
        QueryUtils.filter(dto.getInstructorId(), sql, params, "u.customer_id");
        if (dto.getDate() != null) {
            sql.append(" AND a.start_date <= :date AND a.end_date >= :date");
            params.put("date", dto.getDate());
        }
        //check phan quyen
        if (!utilsService.hasRole(Constant.Role.CRM_ADMIN)) {
            //neu khong phai quyen admin thi chi cho tim kiem khach hang minh cham soc
            sql.append(" and u.login_name like :userLoginName");
            params.put("userLoginName", Utils.getUserNameLogin());
        }
        sql.append(" GROUP BY a.course_id, b.trainee_id, b.instructor_id");
        sql.append(" ORDER BY a.course_id, rank");
    }


    public List<CoursesResponse.UserDataSelected> getListUserData() {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.user_id,
                    a.full_name,
                    CASE WHEN a.mobile_number IS NOT NULL
                 	    THEN CONCAT(a.full_name, ' - ', a.mobile_number) ELSE a.full_name
                    END AS fullName
                FROM sys_users a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, CoursesResponse.UserDataSelected.class);
    }

    public List<CoursesResponse.DataSelected> getListData(CoursesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_id,
                    a.name
                FROM crm_courses a
                JOIN crm_course_trainees b ON a.course_id = b.course_id AND IFNULL(b.is_deleted, :activeStatus) = :activeStatus
                JOIN crm_course_lessons ccl ON a.course_id = ccl.course_id AND IFNULL(ccl.is_deleted, :activeStatus) = :activeStatus
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getTrainingProgramId(), sql, params, "a.training_program_id");
        sql.append(" GROUP BY a.course_id");
        return getListData(sql.toString(), params, CoursesResponse.DataSelected.class);
    }

    public List<CoursesResponse.StatusData> getListCategories(String categoryType) {
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
        return getListData(sql, map, CoursesResponse.StatusData.class);
    }

}
