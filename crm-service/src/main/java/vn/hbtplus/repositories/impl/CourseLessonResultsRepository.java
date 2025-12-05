/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.CoursesRequest;
import vn.hbtplus.models.response.CoursesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.CourseLessonResultsRequest;
import vn.hbtplus.models.response.CourseLessonResultsResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_course_lesson_results
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class CourseLessonResultsRepository extends BaseRepository {

    public BaseDataTableDto searchData(CourseLessonResultsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_lesson_result_id,
                    a.course_lesson_id,
                    a.trainee_id,
                    a.point,
                    a.note,
                    a.status_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CourseLessonResultsResponse.class);
    }

    public List<Map<String, Object>> getListExport(CourseLessonResultsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_lesson_result_id,
                    a.course_lesson_id,
                    a.trainee_id,
                    a.point,
                    a.note,
                    a.status_id,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, CourseLessonResultsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_course_lesson_results a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<CourseLessonResultsRequest.SubmitForm> getListLessonResult(List<Long> listCourseLessonId, Long traineeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.course_lesson_result_id,
                    a.course_lesson_id,
                    a.trainee_id,
                    a.point,
                    a.note,
                    a.status_id
                FROM crm_course_lesson_results a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.trainee_id = :traineeId
                AND a.course_lesson_id in (:listCourseLessonId)
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("listCourseLessonId", listCourseLessonId);
        params.put("traineeId", traineeId);
        return getListData(sql.toString(), params, CourseLessonResultsRequest.SubmitForm.class);
    }
}
