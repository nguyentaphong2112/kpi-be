/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.FamilyRelationshipsRequest;
import vn.hbtplus.models.response.FamilyRelationshipsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_family_relationships
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class FamilyRelationshipsRepository extends BaseRepository {

    public BaseDataTableDto searchData(FamilyRelationshipsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.family_relationship_id,
                    a.object_type,
                    a.object_id,
                    a.relation_type_id,
                    a.date_of_birth,
                    a.full_name,
                    a.mobile_number,
                    a.email,
                    a.zalo_account,
                    a.facebook_account,
                    a.current_address,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, FamilyRelationshipsResponse.class);
    }

    public List<Map<String, Object>> getListExport(FamilyRelationshipsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.family_relationship_id,
                    a.object_type,
                    a.object_id,
                    a.relation_type_id,
                    a.date_of_birth,
                    a.full_name,
                    a.mobile_number,
                    a.email,
                    a.zalo_account,
                    a.facebook_account,
                    a.current_address,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, FamilyRelationshipsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_family_relationships a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public void deleteByFamilyRelationshipIds(List<Long> familyRelationshipIds, String objectType, Long objectId) {
        String sql = """
                update crm_family_relationships a
                set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()
                where a.is_deleted = 'N'
                and a.object_type = :objectType
                and a.object_id = :objectId
                and a.family_relationship_id in (:ids)
                """;
        Map params = new HashMap();
        params.put("userName", Utils.getUserNameLogin());
        params.put("objectType", objectType);
        params.put("objectId", objectId);
        params.put("ids", familyRelationshipIds);
        executeSqlDatabase(sql, params);
    }

    public void deActiveFamilyRelationship(List<Long> listId, String objectType, Long objectId, String userName, Date curDate) {
        StringBuilder sql = new StringBuilder("""
                    UPDATE
                        crm_family_relationships
                    SET 
                        is_deleted = 'Y',
                        modified_time = :curDate,
                        modified_by = :userName
                    WHERE is_deleted = 'N'
                    AND object_type = :objectType
                    AND object_id = :objectId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("curDate", curDate);
        params.put("userName", userName);
        params.put("objectType", objectType);
        params.put("objectId", objectId);
        if (!Utils.isNullOrEmpty(listId)) {
            sql.append(" AND family_relationship_id NOT IN (:listId)");
            params.put("listId", listId);
        }
        executeSqlDatabase(sql.toString(), params);
    }

    public List<FamilyRelationshipsResponse> getListFamilyRelationship(String objectType, Long objectId) {
        String sql = """
                    SELECT
                        a.*,
                        (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.relation_type_id AND sc.category_type = :relationTypeCode) relationTypeName
                    FROM crm_family_relationships a
                    WHERE a.is_deleted = 'N'
                    AND a.object_type = :objectType
                    AND a.object_id = :objectId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("objectType", objectType);
        params.put("objectId", objectId);
        params.put("relationTypeCode", Constant.CATEGORY_TYPES.MOI_QUAN_HE_TN);
        return getListData(sql, params, FamilyRelationshipsResponse.class);
    }
}
