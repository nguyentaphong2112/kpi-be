/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.TrainingProgramsRequest;
import vn.hbtplus.models.response.TrainingProgramsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_training_programs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class TrainingProgramsRepository extends BaseRepository {

    public BaseDataTableDto searchData(TrainingProgramsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.training_program_id,
                    a.title,
                    a.lessons,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, TrainingProgramsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(TrainingProgramsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.training_program_id,
                    a.title,
                    a.lessons,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, TrainingProgramsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_training_programs a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.title");
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }


    public List<TrainingProgramsResponse.DataSelected> getListData() {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.training_program_id,
                    a.title,
                    a.lessons
                FROM crm_training_programs a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, TrainingProgramsResponse.DataSelected.class);
    }
}
