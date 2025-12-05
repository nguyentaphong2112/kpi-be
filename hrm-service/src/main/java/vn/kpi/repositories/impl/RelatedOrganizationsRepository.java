/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.RelatedOrganizationsRequest;
import vn.kpi.models.response.RelatedOrganizationsResponse;
import vn.kpi.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_related_organizations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class RelatedOrganizationsRepository extends BaseRepository {

    public BaseDataTableDto searchData(RelatedOrganizationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.related_organization_id,
                    a.organization_id,
                    o.org_name,
                    a.constraint_org_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, RelatedOrganizationsResponse.class);
    }

    public List<Map<String, Object>> getListExport(RelatedOrganizationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.related_organization_id,
                    a.organization_id,
                    o.org_name,
                    a.constraint_org_id,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, RelatedOrganizationsRequest.SearchForm dto) {
        sql.append("""
            FROM hr_related_organizations a
            
            JOIN hr_organizations o ON o.organization_id = a.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }
}
