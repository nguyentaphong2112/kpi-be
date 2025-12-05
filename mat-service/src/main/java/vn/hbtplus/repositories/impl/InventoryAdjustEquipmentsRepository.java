/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.InventoryAdjustEquipmentsRequest;
import vn.hbtplus.models.response.InventoryAdjustEquipmentsResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_inventory_adjust_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class InventoryAdjustEquipmentsRepository extends BaseRepository {

    public BaseDataTableDto searchData(InventoryAdjustEquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.inventory_adjust_equipment_id,
                    a.equipment_id,
                    a.quantity,
                    a.unit_price,
                    a.inventory_ajustment_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, InventoryAdjustEquipmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(InventoryAdjustEquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.inventory_adjust_equipment_id,
                    a.equipment_id,
                    a.quantity,
                    a.unit_price,
                    a.inventory_ajustment_id,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, InventoryAdjustEquipmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_inventory_adjust_equipments a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }
}
