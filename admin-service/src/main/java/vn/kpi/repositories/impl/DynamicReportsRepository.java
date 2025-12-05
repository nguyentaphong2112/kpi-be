/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.request.DynamicReportsRequest;
import vn.kpi.models.response.DynamicReportsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_dynamic_reports
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class DynamicReportsRepository extends BaseRepository {

    public BaseDataTableDto<DynamicReportsResponse> searchData(DynamicReportsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    dr.dynamic_report_id,
                    dr.code,
                    dr.name,
                    dr.report_type,
                    dr.is_deleted,
                    dr.created_by,
                    dr.created_time,
                    dr.modified_by,
                    dr.modified_time,
                    dr.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, DynamicReportsResponse.class);
    }

    public List<Map<String, Object>> getListExport(DynamicReportsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    dr.dynamic_report_id,
                    dr.code,
                    dr.name,
                    dr.report_type,
                    dr.is_deleted,
                    dr.created_by,
                    dr.created_time,
                    dr.modified_by,
                    dr.modified_time,
                    dr.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, DynamicReportsRequest.SearchForm dto) {
        sql.append("""
            FROM sys_dynamic_reports dr
            WHERE IFNULL(dr.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getCode(), sql, params, "dr.code");
        QueryUtils.filter(dto.getName(), sql, params, "dr.name");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "dr.code", "dr.name");
        QueryUtils.filterEq(dto.getReportType(), sql, params, "dr.");
        sql.append(" ORDER BY dr.code, dr.name");
    }

    public List<ReportConfigDto> getListReportByCode(List<String> reportCodes) {
        String sql = "select a.* from sys_dynamic_reports a " +
                     " where a.is_deleted = 'N'" +
                     " and upper(a.code) in (:reportCodes)" +
                     " order by a.name";
        Map<String, Object> params = new HashMap<>();
        params.put("reportCodes", reportCodes);
        return getListData(sql, params, ReportConfigDto.class);
    }
}
