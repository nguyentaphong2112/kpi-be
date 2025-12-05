/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.PositionGroupsRequest;
import vn.hbtplus.models.response.PositionGroupsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import vn.hbtplus.repositories.entity.PositionGroupConfigsEntity;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_position_groups
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class PositionGroupsRepository extends BaseRepository {

    public BaseDataTableDto searchData(PositionGroupsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.position_group_id,
                    a.group_type_id,
                    a.code,
                    a.name,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    b.name groupTypeName
                    FROM hr_position_groups a
                    LEFT JOIN sys_categories b ON (a.group_type_id = b.value AND b.category_type = 'GROUP_NHOM_CHUC_DANH')
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, PositionGroupsResponse.class);
    }

    public List<Map<String, Object>> getListExport(PositionGroupsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.position_group_id,
                    a.group_type_id,
                    a.code,
                    a.name,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                    FROM hr_position_groups a
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, PositionGroupsRequest.SearchForm dto) {
        sql.append("""
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().trim() + "%");
        }
    }

    public PositionGroupConfigsEntity getConfig(String groupTypeId, String orgTypeId, Long organizationId, Long jobId) {
        StringBuilder sql = new StringBuilder("""
                select a.* from hr_position_group_configs a
                where a.organization_id = :organizationId
                and a.position_group_id in (
                    select position_group_id from hr_position_groups pg
                    where pg.group_type_id = :groupTypeId
                )
                """);
        Map map = new HashMap();
        map.put("organizationId", organizationId);
        map.put("groupTypeId", groupTypeId);
        if (Utils.isNullOrEmpty(orgTypeId)) {
            sql.append(" and a.org_type_id is null");
        } else {
            sql.append(" and a.org_type_id = :orgTypeId");
            map.put("orgTypeId", orgTypeId);
        }
        if (jobId == null) {
            sql.append(" and a.job_id is null");
        } else {
            sql.append(" and a.job_id = :jobId");
            map.put("jobId", jobId);
        }

        return queryForObject(sql.toString(), map, PositionGroupConfigsEntity.class);
    }

    public List<PositionGroupsResponse.ConfigDto> getConfigByPositionGroup(Long positionGroupId) {
        String sql = """
                select 
                	a.organization_id,
                	a.org_type_id,
                	a.position_group_id,
                	a.job_id,
                	jb.`name` job_name,
                	sc.`name` org_type_name,
                	org.`name` as organization_name
                from hr_position_group_configs a
                left join sys_categories sc on sc.`value` = a.org_type_id and sc.category_type = 'HR_LOAI_HINH_DON_VI'
                left join hr_organizations org on a.organization_id = org.organization_id
                left join hr_jobs jb on jb.job_id = a.job_id
                where a.is_deleted = 'N'
                and a.position_group_id = :positionGroupId
                order by org.path_order
                """;
        Map map = new HashMap();
        map.put("positionGroupId", positionGroupId);
        return getListData(sql, map, PositionGroupsResponse.ConfigDto.class);
    }
}
