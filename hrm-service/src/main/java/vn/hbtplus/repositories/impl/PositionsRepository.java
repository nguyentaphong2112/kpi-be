/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.PositionsRequest;
import vn.hbtplus.models.response.PositionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang hr_positions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class PositionsRepository extends BaseRepository {

    public BaseDataTableDto<PositionsResponse.SearchResult> searchData(PositionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.position_id,
                    a.job_id,
                    mj.name jobName,
                    a.organization_id,
                    IFNULL(o.full_name, o.name) orgName,
                    a.name,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.quota_number
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, PositionsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(PositionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.position_id,
                    a.job_id,
                    mj.job_name,
                    a.organization_id,
                    o.org_name,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, PositionsRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_positions a
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("sysDate", Utils.truncDate(new Date()));
        QueryUtils.filter(dto.getJobType(), sql, params, "mj.job_type");
        QueryUtils.filter(dto.getOrganizationId(), sql, params, "o.organization_id");
        QueryUtils.filter(dto.getListJobIds(), sql, params, "a.job_id");
        sql.append(" ORDER BY o.path_order, o.order_number");
    }

    public PositionsResponse.DetailBean getDataById(Long positionId) {
        String sql = """
                    SELECT hp.*, hj.job_type jobType
                    FROM hr_positions hp
                    JOIN hr_jobs hj ON hj.job_id = hp.job_id
                    WHERE IFNULL(hp.is_deleted, :activeStatus) = :activeStatus
                    AND hp.position_id = :positionId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("positionId", positionId);

        return queryForObject(sql, params, PositionsResponse.DetailBean.class);
    }

    public List<PositionsResponse.DetailBean> getListDataByOrg(Long organizationId, String jobType, List<String> listJobType) {
        StringBuilder sql = new StringBuilder("""
                    select a.* from hr_positions a
                    where a.organization_id = :organizationId
                    and a.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationId", organizationId);
        if (!Utils.isNullOrEmpty(jobType) || !Utils.isNullOrEmpty(listJobType)) {
            sql.append("""
                        and exists (
                            select 1 from hr_jobs
                            where job_id = a.job_id
                            and job_type IN (:listJobType)
                        )
                    """);
            params.put("listJobType", !Utils.isNullOrEmpty(jobType) ? List.of(jobType) : listJobType);
        }
        return getListData(sql.toString(), params, PositionsResponse.DetailBean.class);
    }


    public List<PositionsResponse.DetailBean> getListPosition() {
        StringBuilder sql = new StringBuilder("""
                    select a.*
                    from hr_positions a
                    where a.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, PositionsResponse.DetailBean.class);
    }

    public Map<String, List<PositionsResponse.DetailBean>> getMapPosition() {
        List<PositionsResponse.DetailBean> listData = getListPosition();
        Map<String, List<PositionsResponse.DetailBean>> result = new HashMap<>();
        for (PositionsResponse.DetailBean detailBean : listData) {
            result.computeIfAbsent(detailBean.getName().toLowerCase(), k -> new ArrayList<>());
            result.get(detailBean.getName().toLowerCase()).add(detailBean);
        }
        return result;
    }
}
