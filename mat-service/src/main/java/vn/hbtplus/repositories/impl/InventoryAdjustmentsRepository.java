/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.bean.WarehouseNotifyBean;
import vn.hbtplus.models.request.InventoryAdjustmentsRequest;
import vn.hbtplus.models.response.InventoryAdjustEquipmentsResponse;
import vn.hbtplus.models.response.InventoryAdjustmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.InventoryAdjustmentsEntity;
import vn.hbtplus.repositories.jpa.EmployeeRepositoryJPA;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_inventory_adjustments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class InventoryAdjustmentsRepository extends BaseRepository {

    private final EmployeeRepositoryJPA employeeRepository;
    public BaseDataTableDto searchData(InventoryAdjustmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.inventory_adjustment_id,
                    a.warehouse_id,
                    sw.name as warehouse_name,
                    a.type,
                    (case when a.type = 'TANG' then 'Phiếu ghi tăng' else 'Phiếu ghi giảm' end) type_name,
                    a.start_date,
                    a.end_date,
                    a.note,
                    a.inventory_adjustment_no,
                    a.checked_employee_id,
                    (select full_name from hr_employees where employee_id=a.checked_employee_id) as checked_employee_name,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    a.approved_by,
                    a.approved_time,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    wm.has_approve_adjustment
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, InventoryAdjustmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(InventoryAdjustmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.inventory_adjustment_id,
                    a.warehouse_id,
                    sw.name as warehouse_name,
                    a.type,
                    (case when a.type = 'TANG' then 'Phiếu ghi tăng' else 'Phiếu ghi giảm' end) type_name,
                    a.start_date,
                    a.end_date,
                    concat(date_format(a.start_date, '%d/%m/%Y'), ' - ', date_format(a.end_date, '%d/%m/%Y')) date_check,
                    a.note,
                    a.inventory_adjustment_no,
                    a.checked_employee_id,
                    (select full_name from hr_employees where employee_id=a.checked_employee_id) as checked_employee_name,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    a.approved_by,
                    a.approved_time,
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

    public List<InventoryAdjustEquipmentsResponse> getListEquipmentByInventoryAdjustment(Long inventoryAdjustmentId, Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    iae.inventory_adjust_equipment_id inventoryAdjustEquipmentId,
                    fe.code,
                    fe.name,
                    iae.equipment_id equipmentId,
                    fe.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    fe.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    iae.quantity,
                    iae.unit_price unitPrice,
                    iae.inventory_quantity
                FROM mat_equipments fe
                LEFT JOIN mat_inventory_adjust_equipments iae ON iae.equipment_id = fe.equipment_id and IFNULL(iae.is_deleted, :activeStatus) = :activeStatus
                LEFT JOIN mat_inventory_adjustments im ON im.inventory_adjustment_id = :inventoryAdjustmentId
                Left JOIN mat_warehouse_equipments we ON we.equipment_id = fe.equipment_id AND we.warehouse_id = :warehouseId AND IFNULL(we.is_deleted, :activeStatus) = :activeStatus
                WHERE iae.inventory_ajustment_id = :inventoryAdjustmentId and IFNULL(fe.is_deleted, :activeStatus) = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("inventoryAdjustmentId", inventoryAdjustmentId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        params.put("warehouseId", warehouseId);
        return getListData(sql.toString(), params, InventoryAdjustEquipmentsResponse.class);
    }

    public void sendToApprove(List<Long> ids) {
        String sql = """
                    update mat_inventory_adjustments a
                    set modified_by = :approvedBy,
                    modified_time = :approvedTime,
                    status_id = :choDuyet
                    where inventory_adjustment_id in (:inventoryAdjustmentIds)
                    and status_id = :duThao
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("inventoryAdjustmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", InventoryAdjustmentsEntity.STATUS.CHO_DUYET);
        params.put("duThao", InventoryAdjustmentsEntity.STATUS.DU_THAO);

        executeSqlDatabase(sql, params);

    }

    public void approve(List<Long> ids) {
        String sql = """
                    update mat_inventory_adjustments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    status_id = :pheDuyet
                    where inventory_adjustment_id in (:inventoryAdjustmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("inventoryAdjustmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", InventoryAdjustmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", InventoryAdjustmentsEntity.STATUS.PHE_DUYET);

        executeSqlDatabase(sql, params);
    }

    public void undoApprove(List<Long> ids) {
        String sql = """
                    update mat_inventory_adjustments a
                    set approved_by = null,
                    approved_time = null,
                    modified_by = :approvedBy,
                    modified_time = :approvedTime,
                    status_id = :choDuyet
                    where inventory_adjustment_id in (:inventoryAdjustmentIds)
                    and status_id = :pheDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("inventoryAdjustmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", InventoryAdjustmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", InventoryAdjustmentsEntity.STATUS.PHE_DUYET);

        executeSqlDatabase(sql, params);
    }

    public void reject(List<Long> ids, String note) {
        String sql = """
                    update mat_inventory_adjustments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    approved_note = :note,
                    status_id = :pheDuyet
                    where inventory_adjustment_id in (:inventoryAdjustmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("inventoryAdjustmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("note", note);
        params.put("choDuyet", InventoryAdjustmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", InventoryAdjustmentsEntity.STATUS.TU_CHOI);

        executeSqlDatabase(sql, params);
    }

    public Long getEmpIdByUserName(String userName) {
        StringBuilder sql = new StringBuilder("""
                SELECT e.employee_id
                FROM hr_employees e
                WHERE e.user_name = :userName
                LIMIT 1
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        return queryForObject(sql.toString(), params, Long.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, InventoryAdjustmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_inventory_adjustments a
            LEFT JOIN mat_warehouses sw ON sw.warehouse_id = a.warehouse_id and ifnull(sw.is_deleted, :isDeleted) = :isDeleted
            left join mat_warehouse_managers wm on a.warehouse_id = wm.warehouse_id
            WHERE IFNULL(a.is_deleted, :isDeleted) = :isDeleted
        """);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.inventory_adjustment_no", "sw.name");
        QueryUtils.filter(dto.getInventoryAdjustmentNo(), sql, params, "a.inventory_adjustment_no");
        QueryUtils.filter(dto.getWarehouseId(), sql, params, "a.warehouse_id");
        QueryUtils.filterEq(dto.getType(), sql, params, "a.type");
        QueryUtils.filterEq(dto.getStatusId(), sql, params, "a.status_id");
        QueryUtils.filterGe(dto.getStartDate(), sql, params, "a.start_date", "fromDate");
        QueryUtils.filterLe(dto.getEndDate(), sql, params, "a.end_date", "toDate");

        EmployeesEntity employeeEntity = employeeRepository.findByEmployeeCodeAndIsDeleted(Utils.getUserEmpCode(), BaseConstants.STATUS.NOT_DELETED);
        Long employeeId = employeeEntity == null ? null : employeeEntity.getEmployeeId();
        sql.append("""
                 AND wm.employee_id = :employeeId and ifnull(wm.is_deleted, :isDeleted) = :isDeleted
                """);
        params.put("employeeId", employeeId);

        sql.append(" ORDER BY a.created_time desc");
    }

    public String getPermissionApprove(Long warehouseId) {
        String sql = """
                SELECT wm.has_approve_adjustment
                FROM mat_warehouse_managers wm, hr_employees e
                WHERE wm.warehouse_id = :warehouseId
                and wm.employee_id = e.employee_id
                and wm.is_deleted = 'N'
                and e.employee_code = :userName
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("userName", Utils.getUserNameLogin());
        return queryForObject(sql, params, String.class);
    }

    public List<WarehouseNotifyBean> getNotifySenToApprove(List<Long> ids) {
        String sql = """
                select 
                	a.inventory_adjustment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.inventory_adjustment_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name
                from mat_inventory_adjustments a , mat_warehouse_managers wm,
                	hr_employees e ,
                	mat_warehouses wh,
                	hr_employees sd
                where a.warehouse_id = wm.warehouse_id
                and wm.employee_id = e.employee_id
                and wh.warehouse_id = a.warehouse_id
                and wm.has_approve_adjustment = 'Y' and e.employee_code != :loginName
                and a.inventory_adjustment_id in (:ids)
                and sd.employee_code = :loginName
                and wh.parent_id is not null
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("loginName", Utils.getUserNameLogin());
        return getListData(sql, params, WarehouseNotifyBean.class);
    }

    public List<WarehouseNotifyBean> getNotifyApprove(List<Long> ids) {
        String sql = """
                select 
                	a.inventory_adjustment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.inventory_adjustment_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name,
                	a.approved_note as reason
                from mat_inventory_adjustments a ,
                	hr_employees e ,
                	mat_warehouses wh,
                	hr_employees sd
                where a.created_by = e.employee_code
                and a.inventory_adjustment_id in (:ids)
                and wh.warehouse_id = a.warehouse_id
                and sd.employee_code = :loginName
                and wh.parent_id is not null
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("loginName", Utils.getUserNameLogin());
        return getListData(sql, params, WarehouseNotifyBean.class);
    }
}
