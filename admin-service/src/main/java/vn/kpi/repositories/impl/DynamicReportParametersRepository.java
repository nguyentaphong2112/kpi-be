/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.DynamicReportParametersRequest;
import vn.kpi.models.response.DynamicReportParametersResponse;
import vn.kpi.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_dynamic_report_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class DynamicReportParametersRepository extends BaseRepository {

    public BaseDataTableDto searchData(DynamicReportParametersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.dynamic_report_parameter_id,
                    a.dynamic_report_id,
                    a.order_number,
                    a.append_query,
                    a.data_type,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, DynamicReportParametersResponse.class);
    }

    public List<Map<String, Object>> getListExport(DynamicReportParametersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.dynamic_report_parameter_id,
                    a.dynamic_report_id,
                    a.order_number,
                    a.append_query,
                    a.data_type,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, DynamicReportParametersRequest.SearchForm dto) {
        sql.append("""
            FROM sys_dynamic_report_parameters a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }
}
