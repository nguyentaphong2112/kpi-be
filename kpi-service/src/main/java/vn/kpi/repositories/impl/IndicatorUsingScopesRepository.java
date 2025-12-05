/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.IndicatorUsingScopesRequest;
import vn.kpi.models.response.IndicatorUsingScopesResponse;
import vn.kpi.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_indicator_using_scopes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class IndicatorUsingScopesRepository extends BaseRepository {

    public BaseDataTableDto searchData(IndicatorUsingScopesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.indicator_using_id,
                    a.indicator_id,
                    a.organization_id,
                    o.org_name,
                    a.position_id,
                    a.job_id
                    mj.job_name,
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, IndicatorUsingScopesResponse.class);
    }

    public List<Map<String, Object>> getListExport(IndicatorUsingScopesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.indicator_using_id,
                    a.indicator_id,
                    a.organization_id,
                    o.org_name,
                    a.position_id,
                    a.job_id
                    mj.job_name,
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, IndicatorUsingScopesRequest.SearchForm dto) {
        sql.append("""
            FROM kpi_indicator_using_scopes a
            LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
            JOIN hr_organizations o ON o.organization_id = a.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }
}
