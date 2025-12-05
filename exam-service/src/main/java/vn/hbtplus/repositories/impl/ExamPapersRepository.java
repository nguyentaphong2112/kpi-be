/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.ExamPapersRequest;
import vn.hbtplus.models.response.ExamPapersResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang exm_exam_papers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ExamPapersRepository extends BaseRepository {

    public BaseDataTableDto searchData(ExamPapersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.exam_paper_id,
                    a.code,
                    a.name,
                    a.subject_code,
                    a.topic_code,
                    a.description,
                    a.total_questions,
                    a.total_score,
                    a.duration_minutes,
                    a.difficulty_distribution,
                    a.skill_distribution,
                    a.random_order,
                    a.random_option_order,
                    a.generation_mode,
                    a.generation_strategy,
                    a.status_code,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ExamPapersResponse.class);
    }

    public List<Map<String, Object>> getListExport(ExamPapersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.exam_paper_id,
                    a.code,
                    a.name,
                    a.subject_code,
                    a.topic_code,
                    a.description,
                    a.total_questions,
                    a.total_score,
                    a.duration_minutes,
                    a.difficulty_distribution,
                    a.skill_distribution,
                    a.random_order,
                    a.random_option_order,
                    a.generation_mode,
                    a.generation_strategy,
                    a.status_code,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ExamPapersRequest.SearchForm dto) {
        sql.append("""
            FROM exm_exam_papers a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }
}
