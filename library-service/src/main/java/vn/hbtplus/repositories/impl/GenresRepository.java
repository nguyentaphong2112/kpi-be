/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.TreeDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.GenresRequest;
import vn.hbtplus.models.response.GenresResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lib_genres
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class GenresRepository extends BaseRepository {

    public BaseDataTableDto searchData(GenresRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.genre_id,
                    a.code,
                    a.name,
                    a.parent_id,
                    a.path_id,
                    a.path_level,
                    a.path_order,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, GenresResponse.class);
    }

    public List<Map<String, Object>> getListExport(GenresRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.genre_id,
                    a.code,
                    a.name,
                    a.parent_id,
                    a.path_id,
                    a.path_level,
                    a.path_order,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, GenresRequest.SearchForm dto) {
        sql.append("""
            FROM lib_genres a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<TreeDto> getAllGenres() {
        String sql = """
                SELECT
                    rc.genre_id nodeId,
                    rc.name,
                    rc.code,
                    rc.parent_id parentId,
                    rc.path_id
                FROM lib_genres rc 
                    WHERE rc.is_deleted = 'N'
                    order by rc.path_order
                """;
        return getListData(sql, new HashMap<>(), TreeDto.class);
    }
}
