/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.JobsRequest;
import vn.kpi.models.response.JobsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_jobs
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class JobsRepository extends BaseRepository {

    public BaseDataTableDto<JobsResponse.SearchResult> searchData(JobsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.job_id,
                    a.code,
                    a.name,
                    a.is_deleted,
                    a.note,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (select sc.name from sys_categories sc where sc.value = a.job_type and sc.category_type = :jobTypeCode) jobTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("jobTypeCode", Constant.CATEGORY_CODES.LOAI_CHUC_DANH);
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, JobsResponse.SearchResult.class);
    }

    public Pair<String, Map<String, Object>> buildSql(JobsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                     a.job_id,
                     a.code,
                     a.name,
                     a.is_deleted,
                     a.note,
                     a.created_by,
                     a.created_time,
                     a.modified_by,
                     a.modified_time,
                     a.last_update_time,
                     (select sc.name from sys_categories sc where sc.value = a.job_type and sc.category_type = :jobTypeCode) jobTypeName
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("jobTypeCode", Constant.CATEGORY_CODES.LOAI_CHUC_DANH);
        addCondition(sql, params, dto);
        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(JobsRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());

        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getLeft()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, JobsRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_jobs a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        QueryUtils.filter(dto.getListJobType(), sql, params, "a.job_type");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" and (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch() + "%");
        }
        sql.append(" ORDER BY a.order_number");
    }

    public List<JobsResponse.DetailBean> getListJobs(List<String> jobType, Long organizationId) {
        StringBuilder sql = new StringBuilder("""
                    select a.job_id, a.code, a.name, a.job_type
                    from hr_jobs a
                    where a.is_deleted = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(jobType)) {
            sql.append(" and lower(a.job_type) in (:jobType)");
            params.put("jobType", jobType.stream().map(String::toLowerCase).toList());
        }
        if(organizationId != null) {
            sql.append("""
                      and exists (
                        select 1 from hr_positions p, 
                        hr_organizations o,
                        hr_organizations op
                        where o.organization_id = p.organization_id 
                        and op.organization_id = :organizationId
                        and o.path_id like concat(op.path_id, '%')
                        and p.job_id = a.job_id
                        and p.is_deleted = :activeStatus
                     )""");
            params.put("organizationId", organizationId);
        }
        List result = getListData(sql.toString(), params, JobsResponse.DetailBean.class);
        if(Utils.isNullOrEmpty(result) && organizationId != null) {
            return getListJobs(jobType, null);
        }
        return result;
    }
}
