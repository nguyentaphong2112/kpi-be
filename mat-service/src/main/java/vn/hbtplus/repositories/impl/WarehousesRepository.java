/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.WarehousesRequest;
import vn.hbtplus.models.response.WarehousesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_warehouses
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class WarehousesRepository extends BaseRepository {

    public List<WarehousesResponse> searchData(WarehousesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warehouse_id,
                    a.code,
                    a.name,
                    a.parent_id,
                    (select name from mat_warehouses sw where sw.warehouse_id = a.parent_id) parent_name,
                    a.address,
                    a.department_id,
                    og.name as department_name,
                    a.type,
                    (select name from sys_categories where code = a.type and category_type = :typeCode) type_name,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    e.full_name as manager_name,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    GROUP_CONCAT(DISTINCT fe.name ORDER BY fe.name SEPARATOR ', ')
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params, WarehousesResponse.class);
    }

    public List<Map<String, Object>> getListExport(WarehousesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warehouse_id,
                    a.code,
                    a.name,
                    a.parent_id,
                    (select name from mat_warehouses sw where sw.warehouse_id = a.parent_id) parent_name,
                    a.address,
                    a.department_id,
                    og.name as department_name,
                    a.type,
                    (select name from sys_categories where code = a.type and category_type = :typeCode) type_name,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    e.full_name as manager_name,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    GROUP_CONCAT(DISTINCT fe.name ORDER BY fe.name SEPARATOR ', ')
                """);
        Map<String, Object> params = new HashMap<>();
        dto.setOrderPath(true);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<WarehousesResponse.warehouseEmployeeDTO> getListEmpByWarehouse(Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    e.employee_id,
                    e.full_name,
                    e.employee_code,
                    e.email,
                    e.mobile_number,
                    o.name organizationName,
                    po.name position_name,
                    (wm.role_id = 'THU_KHO') AS isManager,
                    (wm.has_approve_import = 'Y') AS hasApproveImport,
                    (wm.has_approve_export = 'Y') AS hasApproveExport,
                    (wm.has_approve_transfer = 'Y') AS hasApproveTransfer,
                    (wm.has_approve_adjustment = 'Y') AS hasApproveAdjustment
                FROM hr_employees e
                JOIN mat_warehouse_managers wm ON wm.employee_id = e.employee_id
                LEFT JOIN hr_organizations o ON e.organization_id = o.organization_id
                LEFT JOIN hr_positions po ON e.position_id = po.position_id
                WHERE wm.warehouse_id = :warehouseId and IFNULL(wm.is_deleted, :activeStatus) = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, WarehousesResponse.warehouseEmployeeDTO.class);
    }

    public List<WarehousesResponse.warehouseEquipmentDTO> getListEquipmentByWarehouse(Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    we.warehouse_equipment_id,
                    fe.code,
                    fe.name,
                    we.equipment_id,
                    we.warehouse_id,
                    fe.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    fe.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    we.quantity,
                    we.unit_price
                FROM mat_equipments fe
                LEFT JOIN mat_warehouse_equipments we ON we.equipment_id = fe.equipment_id and IFNULL(we.is_deleted, :activeStatus) = :activeStatus
                WHERE we.warehouse_id = :warehouseId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, WarehousesResponse.warehouseEquipmentDTO.class);
    }

    public List<WarehousesResponse.warehouseIncomingShipmentDTO> getListIncomingShipmentByWarehouse(Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    sis.incoming_shipment_id,
                    sis.picking_no,
                    sis.incoming_date,
                    e1.full_name pickingEmployeeName,
                    sis.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE sis.status_id = sc.value and sc.category_type = :status) statusName,
                    sis.type,
                    (SELECT sc.name FROM sys_categories sc WHERE sis.type = sc.value and sc.category_type = :type) typeName,
                    (select e.full_name from hr_employees e WHERE sis.approved_by = e.employee_code) approvedName,
                    sis.approved_time
                FROM mat_incoming_shipments sis
                LEFT JOIN hr_employees e1 ON e1.employee_id = sis.picking_employee_id
                WHERE sis.warehouse_id = :warehouseId and IFNULL(sis.is_deleted, :activeStatus) = :activeStatus
                ORDER BY sis.created_time DESC
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        params.put("type", Constants.CATEGORY_TYPE.INCOMING_SHIPMENTS_TYPE);
        return getListData(sql.toString(), params, WarehousesResponse.warehouseIncomingShipmentDTO.class);
    }

    public List<WarehousesResponse.warehouseOutgoingShipmentDTO> getListOutgoingShipmentByWarehouse(Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    sos.outgoing_shipment_id,
                    sos.picking_no,
                    sos.outgoing_date,
                    e1.full_name pickingEmployeeName,
                    sos.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE sos.status_id = sc.value and sc.category_type = :status) statusName,
                    sos.type,
                    (SELECT sc.name FROM sys_categories sc WHERE sos.type = sc.value and sc.category_type = :type) typeName,
                    (select e.full_name from hr_employees e WHERE sos.approved_by = e.employee_code) approvedName,
                    sos.approved_time
                FROM mat_outgoing_shipments sos
                LEFT JOIN hr_employees e1 ON e1.employee_id = sos.picking_employee_id
                WHERE sos.warehouse_id = :warehouseId and IFNULL(sos.is_deleted, :activeStatus) = :activeStatus
                ORDER BY sos.created_time DESC
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        params.put("type", Constants.CATEGORY_TYPE.MAT_OUTGOING_SHIPMENT_TYPE);
        return getListData(sql.toString(), params, WarehousesResponse.warehouseOutgoingShipmentDTO.class);
    }

    public List<WarehousesResponse.warehouseInventoryAdjustmentDTO> getListInventoryAdjustmentByWarehouse(Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    sia.inventory_adjustment_id,
                    sia.inventory_adjustment_no,
                    sia.type,
                    (SELECT sc.name FROM sys_categories sc WHERE sia.type = sc.value and sc.category_type = :type) typeName,
                    sia.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE sia.status_id = sc.value and sc.category_type = :status) statusName,
                    sia.start_date,
                    sia.end_date,
                    sia.note,
                    (select e.full_name from hr_employees e WHERE sia.approved_by = e.employee_code) approvedName,
                    sia.approved_time
                FROM mat_inventory_adjustments sia
                WHERE sia.warehouse_id = :warehouseId and IFNULL(sia.is_deleted, :activeStatus) = :activeStatus
                ORDER BY sia.created_time DESC
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        params.put("type", Constants.CATEGORY_TYPE.MAT_INVENTORY_ADJUSTMENT_TYPE);
        return getListData(sql.toString(), params, WarehousesResponse.warehouseInventoryAdjustmentDTO.class);
    }

    public List<Map<String, Object>> getEmpByCode(String empCode) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    e.employee_id employeeId,
                    e.full_name fullName,
                    e.employee_code employeeCode,
                    e.email,
                    e.mobile_number mobileNumber,
                    o.name organizationName,
                    po.name positionName
                FROM hr_employees e
                LEFT JOIN hr_organizations o ON e.organization_id = o.organization_id
                LEFT JOIN hr_positions po ON e.position_id = po.position_id
                WHERE e.employee_code = :empCode
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("empCode", empCode);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, WarehousesRequest.SearchForm dto) {
        params.put("typeCode", Constants.CATEGORY_TYPE.WAREHOUSE_TYPE);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        sql.append("""
                    FROM mat_warehouses a
                    LEFT JOIN hr_organizations og ON og.organization_id = a.department_id
                    LEFT JOIN mat_warehouse_managers wm ON wm.warehouse_id = a.warehouse_id and wm.role_id = 'THU_KHO'
                    LEFT JOIN hr_employees e ON e.employee_id = wm.employee_id
                    LEFT JOIN mat_warehouse_equipments we ON a.warehouse_id = we.warehouse_id
                    LEFT JOIN mat_equipments fe ON fe.equipment_id = we.equipment_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if ("Y".equals(dto.getIsUserLogin())) {
            String empCode = Utils.getUserEmpCode();
            sql.append("""
                    AND exists (select e1.employee_id from hr_employees e1
                    JOIN mat_warehouse_managers we1 ON we1.employee_id = e1.employee_id  and IFNULL(we1.is_deleted, :activeStatus) = :activeStatus
                    where e1.employee_code = :empCode and a.warehouse_id = we1.warehouse_id )
                    """);
            params.put("empCode", empCode);
        }

        QueryUtils.filterNotEq(dto.getWarehouseId(), sql, params, "a.warehouse_id");

        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.code", "a.name", "e.full_name");
        QueryUtils.filter(dto.getCode(), sql, params, "a.code");
        QueryUtils.filter(dto.getName(), sql, params,"a.name");
        QueryUtils.filter(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getEquipmentName(), sql, params, "fe.name");
        QueryUtils.filterEq(dto.getEquipmentTypeId(), sql, params, "fe.equipment_type_id");
        if ("Y".equalsIgnoreCase(dto.getIsGeneralWarehouse())) {
            sql.append(" AND a.type = :generalType ");
            params.put("generalType", Constants.WAREHOUSE_TYPE.KHO_TONG);
        }
//        if ("Y".equalsIgnoreCase(dto.getIsActive())) {
//            sql.append(" AND a.status_id = :statusId ");
//            params.put("statusId", Constants.WAREHOUSE_STATUS.HOAT_DONG);
//        }
        sql.append(" GROUP BY a.warehouse_id");
        if (dto.isOrderPath()) {
            sql.append(" ORDER BY a.path_id ");
        } else {
            sql.append(" ORDER BY a.created_time DESC");
        }
    }

    public void updateHistories(Long warehouseId, Long objectId, String type) {
        String sql = "call proc_mat_update_warehouse_history(:warehouseId, :objectId, :type)";
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("objectId", objectId);
        params.put("type", type);
        executeSqlDatabase(sql, params);
    }

    public void updateWarehouseEquipment(Long warehouseId, Long objectId, String type) {
        String sql = "call proc_mat_update_warehouse_equipments(:warehouseId, :objectId, :type)";
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("objectId", objectId);
        params.put("type", type);
        executeSqlDatabase(sql, params);
    }

    public void updatePath(Long warehouseId) {
        String sql = "call proc_mat_update_warehouse_path_id(:warehouseId)";
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        executeSqlDatabase(sql, params);
    }


    public boolean checkDuplicate(String code, Long id) {
        Map<String, Object> mapParams = new HashMap<>();
        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(1)
                    FROM mat_warehouses a
                        WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                        AND a.warehouse_id != :warehouseId
                        AND a.code = :code
                """);
        mapParams.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        mapParams.put("warehouseId", Utils.NVL(id));
        mapParams.put("code", code);
        return queryForObject(sql.toString(), mapParams, Integer.class) > 0;
    }

    public void initHistory(Date periodDate) {
        String sql = """
                insert into mat_warehouse_equipment_histories(
                	warehouse_equipment_history_id, equipment_id, warehouse_id, quantity, unit_price, is_deleted, created_by, created_time,period_date
                )
                select
                	null, a.equipment_id, a.warehouse_id, a.quantity, a.unit_price, a.is_deleted, 'system', now(), :periodDate
                from mat_warehouse_equipment_histories a
                where a.is_deleted = 'N'
                and a.quantity > 0
                and a.period_date = :prePeriodDate
                and not exists (
                	select 1 from mat_warehouse_equipment_histories h
                	where h.period_date = :periodDate
                	and h.is_deleted = 'N'
                	and h.warehouse_id = a.warehouse_id
                	and h.equipment_id = a.equipment_id
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodDate", periodDate);
        params.put("prePeriodDate", Utils.getLastDay(DateUtils.addMonths(periodDate, -1)));
        executeSqlDatabase(sql, params);
    }
}
