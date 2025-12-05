/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.PytagoResearchsRequest;
import vn.hbtplus.models.response.PytagoResearchsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_pytago_researchs
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class PytagoResearchsRepository extends BaseRepository {

    public BaseDataTableDto searchData(PytagoResearchsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.pytago_research_id,
                    a.full_name,
                    a.date_of_birth,
                    a.parent_name,
                    a.mobile_number,
                    (case 
                        when exists (
                            select 1 from crm_customers c 
                            where c.mobile_number = a.mobile_number
                            and c.is_deleted = 'N' 
                        ) then 'X'
                    end) as isCustomer,
                    a.email,
                    a.current_address,
                    case when a.type = 'NGUOI_LON' then 'Người lớn' else 'Trẻ em' end type,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, PytagoResearchsResponse.class);
    }

    public List<Map<String, Object>> getListExport(PytagoResearchsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.full_name,
                    a.date_of_birth,
                    a.parent_name,
                    a.mobile_number,
                    a.email,
                    a.current_address,
                    CASE
                		WHEN a.type = 'NGUOI_LON' THEN 'Người lớn'
                		WHEN a.type = 'TRE_EM' THEN 'Trẻ em'
                		ELSE ''
                	END AS type
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, PytagoResearchsRequest.SearchForm dto) {
        sql.append("""
                    FROM crm_pytago_researchs a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name");
        QueryUtils.filter(dto.getDateOfBirth(), sql, params, "a.date_of_birth");
        QueryUtils.filter(dto.getParentName(), sql, params, "a.parent_name");
        QueryUtils.filter(dto.getMobileNumber(), sql, params, "a.mobile_number");
        QueryUtils.filter(dto.getEmail(), sql, params, "a.email");
        QueryUtils.filter(dto.getCurrentAddress(), sql, params, "a.current_address");
        QueryUtils.filter(dto.getType(), sql, params, "a.type");

        QueryUtils.filter(dto.getFullNameFilter(), sql, params, "a.full_name");
        QueryUtils.filter(dto.getParentNameFilter(), sql, params, "a.parent_name");
        QueryUtils.filter(dto.getMobileNumberFilter(), sql, params, "a.mobile_number");
        QueryUtils.filter(dto.getEmailFilter(), sql, params, "a.email");
        QueryUtils.filter(dto.getCurrentAddressFilter(), sql, params, "a.current_address");


        List<String> roleCodeList = Utils.getRoleCodeList();
        if (!roleCodeList.contains(Constant.Role.CRM_ADMIN)) {
            sql.append(" AND a.created_by = :createdBy ");
            params.put("createdBy", Utils.getUserNameLogin());
        }
        sql.append(" order by a.created_time desc");
    }

    public int updateSearchCount(Long objectId, String tableName) {
        String sql = """
                update crm_object_attributes a 
                set a.attribute_value = CAST(a.attribute_value AS UNSIGNED) + 1,
                    a.is_deleted = 'N',
                    a.modified_by = :userName,
                    a.modified_time = now()
                where a.object_id = :objectId
                and a.table_name = :tableName
                and a.attribute_code = 'SO_LAN_TRA_CUU'
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", objectId);
        params.put("tableName", tableName);
        params.put("userName", Utils.getUserNameLogin());
        return executeSqlDatabase(sql, params);
    }

    public PytagoResearchsResponse.SearchCount getSearchCount(Long objectId, String tableName) {
        String sql = """
                select
                    ifnull((select attribute_value from crm_object_attributes a where a.is_deleted = 'N' 
                        and a.table_name = :tableName and a.object_id = :objectId
                        and a.attribute_code = 'SO_LAN_DUOC_TRA_CUU' LIMIT 1),0) as  totalSearch,
                    ifnull((select attribute_value from crm_object_attributes a where a.is_deleted = 'N' 
                        and a.table_name = :tableName and a.object_id = :objectId
                        and a.attribute_code = 'SO_LAN_TRA_CUU' LIMIT 1),0) as  totalSearched,
                    ifnull((select attribute_value from crm_object_attributes a where a.is_deleted = 'N' 
                        and a.table_name = :tableName and a.object_id = :objectId
                        and a.attribute_code = 'SO_LAN_DUOC_IN' LIMIT 1),0) as  totalExport,
                    ifnull((select attribute_value from crm_object_attributes a where a.is_deleted = 'N' 
                        and a.table_name = :tableName and a.object_id = :objectId
                        and a.attribute_code = 'SO_LAN_IN' LIMIT 1),0) as  totalExported
                from dual
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", objectId);
        params.put("tableName", tableName);
        return queryForObject(sql, params, PytagoResearchsResponse.SearchCount.class);
    }
}
