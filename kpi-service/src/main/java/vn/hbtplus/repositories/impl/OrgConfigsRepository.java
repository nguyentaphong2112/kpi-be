/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.OrgConfigsRequest;
import vn.hbtplus.models.response.OrgConfigsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.OrgConfigsEntity;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_org_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrgConfigsRepository extends BaseRepository {

    public BaseDataTableDto searchData(OrgConfigsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.org_config_id,
                    a.organization_id,
                    o.full_name org_name,
                    a.year,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (select name from sys_categories sc1 where sc1.value = a.org_type_id and sc1.category_type = :loaiHinhDonVi) as orgTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        params.put("tableName", getSQLTableName(OrgConfigsEntity.class));
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OrgConfigsResponse.class);
    }

    public List<Map<String, Object>> getListExport(OrgConfigsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.org_config_id,
                    a.organization_id,
                    o.org_name,
                    a.year,
                    a.note,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrgConfigsRequest.SearchForm dto) {
        sql.append("""
            FROM kpi_org_configs a
            JOIN hr_organizations o ON o.organization_id = a.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getYear(),sql,params,"year");
        sql.append(" ORDER BY a.year desc, o.path_order, a.created_time desc");
    }
    
    public Integer getMinTotalKpi(Long organizationId, Integer year) {
        String sql = """
                select 
                	(
                		select attribute_value from kpi_object_attributes obj
                		where obj.is_deleted = 'N'
                		and obj.table_name = 'kpi_org_configs'
                		and obj.attribute_code = 'SO_LUONG_KPI_DON_VI'
                		and obj.object_id = a.org_config_id
                	) as counter
                from kpi_org_configs a
                join hr_organizations org on a.organization_id = org.organization_id,
                (
                	select * from hr_organizations org
                	where org.organization_id = :organizationId
                )  op
                where a.is_deleted = 'N'
                and op.path_id like CONCAT(org.path_id,'%')
                and (a.org_type_id is null or op.org_type_id = a.org_type_id)
                and a.year = :year
                order by org.path_level desc, op.org_type_id desc
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("organizationId", organizationId);
        params.put("year", year);
        return getFirstData(sql, params, Integer.class);
    }
}
