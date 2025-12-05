/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.CourseTraineesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.CourseLessonsRequest;
import vn.hbtplus.models.response.CourseLessonsResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_course_lessons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class CourseLessonsRepository extends BaseRepository {

    public BaseDataTableDto searchData(CourseLessonsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_lesson_id,
                    a.name,
                    a.course_id
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CourseLessonsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(CourseLessonsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_lesson_id,
                    a.name,
                    a.course_id
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, CourseLessonsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_course_lessons a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public void deleteLesson(List<Long> id, Long courseId) {
        StringBuilder sql = new StringBuilder("""
                UPDATE crm_course_lessons a
                SET a.is_deleted = 'Y'
                WHERE a.course_lesson_id NOT IN (:id)
                AND a.course_id = :courseId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("courseId", courseId);
        executeSqlDatabase(sql.toString(), params);
    }

    public List<CourseLessonsResponse.Selected> getListData(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_lesson_id,
                    CONCAT('Ngày ', ROW_NUMBER() OVER (ORDER BY a.course_lesson_id), ': ', a.name) AS name
                FROM crm_course_lessons a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.course_id = :courseId
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("courseId", id);
        return getListData(sql.toString(), params, CourseLessonsResponse.Selected.class);
    }

    public List<CourseTraineesResponse> getListCourseTrainees(Long id) {
        String sql = """
                select
                a.course_trainee_id,
                a.trainee_id,
                a.instructor_id,
                concat(c.mobile_number,' - ', c.full_name) as trainee_name,  
                concat(c1.mobile_number,' - ', c1.full_name) as instructor_name  
                from crm_course_trainees a
                left join crm_customers c on a.trainee_id = c.customer_id
                left join crm_customers c1 on a.instructor_id = c1.customer_id
                where a.course_id = :courseId
                and a.is_deleted = 'N'                
                order by a.trainee_id asc
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("courseId", id);
        return getListData(sql, params, CourseTraineesResponse.class);
    }
}
