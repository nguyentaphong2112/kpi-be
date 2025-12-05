/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.WorkdayTypesRequest;
import vn.hbtplus.models.response.WorkdayTypesResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_workday_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WorkdayTypesRepository extends BaseRepository {

    public BaseDataTableDto searchData(WorkdayTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.workday_type_id,
                    a.code,
                    a.name
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WorkdayTypesResponse.class);
    }

    public List<Map<String, Object>> getListExport(WorkdayTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.workday_type_id,
                    a.code,
                    a.name
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, WorkdayTypesRequest.SearchForm dto) {
        sql.append("""
            FROM abs_workday_types a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
    }

    public List<WorkdayTypesResponse> getList() {
        String sql = """
                select a.* from abs_workday_types a
                where a.is_deleted = :activeStatus
                order by a.code
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, WorkdayTypesResponse.class);
    }
}
