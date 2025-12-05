/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.EmpTypesRequest;
import vn.hbtplus.models.response.EmpTypesResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_emp_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmpTypesRepository extends BaseRepository {

    public BaseDataTableDto searchData(EmpTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.emp_type_id,
                    a.code,
                    a.name,
                    a.order_number,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmpTypesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(EmpTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.emp_type_id,
                    a.code,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmpTypesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_emp_types a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch() + "%");
        }
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<EmpTypesResponse.DetailBean> getListEmpType() {
        String sql = """
                    SELECT a.*, concat(a.emp_type_id, '') value 
                    FROM hr_emp_types a
                    WHERE a.is_deleted = 'N'
                    ORDER BY a.order_number
                """;
        return getListData(sql, new HashMap<>(), EmpTypesResponse.DetailBean.class);
    }
}
