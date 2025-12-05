/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.WarehouseManagersRequest;
import vn.hbtplus.models.response.WarehouseManagersResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_warehouse_managers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WarehouseManagersRepository extends BaseRepository {

    public BaseDataTableDto searchData(WarehouseManagersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warehouse_manager_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.warehouse_id,
                    a.role_id
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WarehouseManagersResponse.class);
    }

    public List<Map<String, Object>> getListExport(WarehouseManagersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warehouse_manager_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.warehouse_id,
                    a.role_id
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getListEmployee(Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warehouse_manager_id,
                    a.employee_id,
                    a.warehouse_id,
                    a.role_id
                FROM mat_warehouse_managers a
                WHERE a.warehouse_id = :warehouseId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, WarehouseManagersRequest.SearchForm dto) {
        sql.append("""
            FROM mat_warehouse_managers a
            JOIN hr_employees e ON e.employee_id = a.employee_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }
}
