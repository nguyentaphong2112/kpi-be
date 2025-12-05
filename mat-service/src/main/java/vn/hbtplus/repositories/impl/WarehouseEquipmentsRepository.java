/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.WarehouseEquipmentsRequest;
import vn.hbtplus.models.response.WarehouseEquipmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_warehouse_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WarehouseEquipmentsRepository extends BaseRepository {

    public BaseDataTableDto searchData(WarehouseEquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warehouse_equipment_id,
                    a.equipment_id,
                    a.warehouse_id,
                    a.quantity,
                    a.unit_price,
                    a.update_price_time,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WarehouseEquipmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(WarehouseEquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warehouse_equipment_id,
                    a.equipment_id,
                    a.warehouse_id,
                    a.quantity,
                    a.unit_price,
                    a.update_price_time,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, WarehouseEquipmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_warehouse_equipments a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public void updateHistory(Long warehouseId, Long equipmentId, Long unitPrice) {
        String sql = "update mat_warehouse_equipment_histories h" +
                     " set h.unit_price = :unitPrice," +
                     "  h.modified_by = :userName," +
                     "  h.modified_time = now()" +
                     " where h.warehouse_id = :warehouseId" +
                     " and h.equipment_id = :equipmentId" +
                     " and h.is_deleted = :isDeleted" +
                     " and h.period_date = last_day(:date)";
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("unitPrice", unitPrice);
        params.put("equipmentId", equipmentId);
        params.put("warehouseId", warehouseId);
        params.put("date", new Date());
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }
}
