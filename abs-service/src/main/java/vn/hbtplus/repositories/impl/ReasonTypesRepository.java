/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.ReasonTypesRequest;
import vn.hbtplus.models.response.ReasonTypesResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_reason_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ReasonTypesRepository extends BaseRepository {

    public BaseDataTableDto searchData(ReasonTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.reason_type_id,
                    a.code,
                    a.name,
                    a.workday_type_id,
                    a.default_time_off,
                    a.default_time_off_type,
                    a.max_time_off,
                    a.year_max_time_off_type,
                    a.max_time_off_type,
                    a.year_max_time_off,
                    CASE
                        WHEN a.is_over_holiday = 'Y' THEN 'Có'
                        ELSE 'Không'
                    END AS isOverHoliday
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ReasonTypesResponse.class);
    }

    public List<Map<String, Object>> getListExport(ReasonTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.reason_type_id,
                    a.code,
                    a.name,
                    a.workday_type_id,
                    a.default_time_off,
                    a.default_time_off_type,
                    a.max_time_off,
                    a.year_max_time_off_type,
                    a.max_time_off_type,
                    a.year_max_time_off,
                    a.is_over_holiday
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ReasonTypesRequest.SearchForm dto) {
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" , MATCH(code, name) AGAINST (:keySearch) relevance");
        }
        sql.append("""
            FROM abs_reason_types a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().trim() + "%");
        }
    }

    public List<ReasonTypesResponse> getListResponseType() {
        String sql = """
                    SELECT a.code, a.name,
                    	concat(a.reason_type_id, '') value
                    FROM abs_reason_types a
                    WHERE a.is_deleted = 'N'
                    ORDER BY a.reason_type_id
                """;
        return getListData(sql, new HashMap<>(), ReasonTypesResponse.class);
    }
}
