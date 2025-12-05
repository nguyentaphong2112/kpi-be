/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.MembersRequest;
import vn.hbtplus.models.response.MembersResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lib_members
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class MembersRepository extends BaseRepository {

    public BaseDataTableDto searchData(MembersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.member_id,
                    a.code,
                    a.name,
                    a.date_of_birth,
                    a.gender_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    c.name gender_name
                    FROM lib_members a
                    LEFT JOIN sys_categories c ON (a.gender_id = c.value AND c.category_type = :categoryType)
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("categoryType", Constant.CATEGORY_TYPE.GIOI_TINH);
        return getListPagination(sql.toString(), params, dto, MembersResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(MembersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.member_id,
                    a.code,
                    a.name,
                    a.date_of_birth,
                    a.gender_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                    FROM lib_members a
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, MembersRequest.SearchForm dto) {
        sql.append("""
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().trim() + "%");
        }
        if (!Utils.isNullOrEmpty(dto.getGenderId())) {
            sql.append(" AND (lower(a.gender_id) = :genderId)");
            params.put("genderId", dto.getGenderId());
        }
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public BaseDataTableDto<MembersResponse.DataPickerResult> getPageable(BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.member_id,
                    a.code,
                    a.name
                    FROM lib_members a
                    where a.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        if(!Utils.isNullOrEmpty(request.getKeySearch())){
            sql.append(" and (lower(a.code) like :keySearch or lower(a.name) like :keySearch )");
            params.put("keySearch", "%" + request.getKeySearch().toLowerCase() + "%");
        }
        return getListPagination(sql.toString(), params, request, MembersResponse.DataPickerResult.class);
    }
}
