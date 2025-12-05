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
import vn.hbtplus.models.request.TransferringShipmentsRequest;
import vn.hbtplus.models.response.OutgoingEquipmentsResponse;
import vn.hbtplus.models.response.TransferringShipmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.jpa.EmployeeRepositoryJPA;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_transferring_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class TransferringShipmentsRepository extends BaseRepository {

    private final EmployeeRepositoryJPA employeeRepository;
    public BaseDataTableDto searchData(TransferringShipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.transferring_shipment_id,
                    a.warehouse_id,
                    a.transferring_date,
                    a.received_warehouse_id,
                    (select name from mat_warehouses where warehouse_id = a.received_warehouse_id) received_warehouse_name,
                    a.transferred_employee_id,
                    (select name from mat_warehouses where warehouse_id = a.warehouse_id) transferred_warehouse_name,
                    e.employee_code,
                    e.full_name,
                    a.approved_by,
                    a.approved_time,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.picking_no,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    a.name,
                    a.received_employee_id,
                    a.created_employee_id,
                    a.note,
                    wm.has_approve_transfer
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, TransferringShipmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(TransferringShipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.transferring_shipment_id,
                    a.warehouse_id,
                    a.transferring_date,
                    a.received_warehouse_id,
                    (select name from mat_warehouses where warehouse_id = a.received_warehouse_id) received_warehouse_name,
                    a.transferred_employee_id,
                    (select name from mat_warehouses where warehouse_id = a.warehouse_id) transferred_warehouse_name,
                    e.employee_code,
                    e.full_name,
                    a.approved_by,
                    a.approved_time,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.picking_no,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    a.name,
                    a.received_employee_id,
                    a.created_employee_id,
                    a.note
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, TransferringShipmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_transferring_shipments a
            left JOIN hr_employees e ON e.employee_id = a.transferred_employee_id
            LEFT JOIN mat_warehouses sw ON sw.warehouse_id = a.warehouse_id and ifnull(sw.is_deleted, :isDeleted) = :isDeleted
            left join mat_warehouse_managers wm on wm.warehouse_id = sw.warehouse_id
            WHERE IFNULL(a.is_deleted, :isDeleted) = :isDeleted
        """);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        QueryUtils.filter(dto.getWarehouseId(), sql, params, "a.warehouse_id");
        QueryUtils.filter(dto.getReceivedWarehouseId(), sql, params, "a.received_warehouse_id");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.picking_no", "a.name", "sw.name");
        QueryUtils.filter(dto.getPickingNo(), sql, params, "a.picking_no");
        QueryUtils.filter(dto.getName(), sql, params, "a.name");
        QueryUtils.filterEq(dto.getStatusId(), sql, params, "a.status_id");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.transferring_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "a.transferring_date", "toDate");
        // add check quyền quản lý kho
        EmployeesEntity employeeEntity = employeeRepository.findByEmployeeCodeAndIsDeleted(Utils.getUserEmpCode(), BaseConstants.STATUS.NOT_DELETED);
        Long employeeId = employeeEntity == null ? null : employeeEntity.getEmployeeId();
        sql.append("""
                 AND wm.employee_id = :employeeId and ifnull(wm.is_deleted, :isDeleted) = :isDeleted
                """);
        params.put("employeeId", employeeId);
        sql.append(" ORDER BY a.created_time desc");
    }

    public void insertOutGoingEquipments(Long transferringShipmentId, Long outgoingShipmentId, Long warehouseId) {
        String sql = """
                insert into mat_outgoing_equipments(equipment_id, quantity, unit_price, outgoing_shipment_id, is_deleted, created_by, created_time, inventory_quantity)
                select a.equipment_id, a.quantity, a.unit_price, :outgoing_shipment_id, a.is_deleted, :created_by, now(), we.quantity
                from mat_transferring_equipments a
                LEFT JOIN mat_warehouse_equipments we ON we.equipment_id = a.equipment_id AND IFNULL(we.is_deleted, 'N') = 'N' AND we.warehouse_id = :warehouseId 
                where a.is_deleted = 'N'
                and a.transferring_shipment_id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("outgoing_shipment_id", outgoingShipmentId);
        params.put("created_by", Utils.getUserNameLogin());
        params.put("warehouseId", warehouseId);
        params.put("id", transferringShipmentId);
        executeSqlDatabase(sql, params);
    }

    public void insertIncomingEquipments(Long transferringShipmentId, Long incomingShipmentId) {
        String sql = """
                insert into mat_incoming_equipments(equipment_id, quantity, unit_price, incoming_shipment_id, is_deleted, created_by, created_time)
                select a.equipment_id, a.quantity, a.unit_price, :incoming_shipment_id, a.is_deleted, :created_by, now()
                from mat_transferring_equipments a
                where a.is_deleted = 'N'
                and a.transferring_shipment_id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incoming_shipment_id", incomingShipmentId);
        params.put("created_by", Utils.getUserNameLogin());
        params.put("id", transferringShipmentId);
        executeSqlDatabase(sql, params);
    }

    public TransferringShipmentsResponse getDataById(Long transferringShipmentId) {
        String sql = """
                SELECT ts.*, wm.has_approve_transfer,
                (select picking_no from mat_incoming_shipments sis where sis.transferring_shipment_id = ts.transferring_shipment_id 
                    and ifnull(sis.is_deleted, :isDeleted) = :isDeleted limit 1) incoming_picking_no,
                (select picking_no from mat_outgoing_shipments os where os.transferring_shipment_id = ts.transferring_shipment_id 
                    and ifnull(os.is_deleted, :isDeleted) = :isDeleted limit 1) outgoing_picking_no
                FROM mat_transferring_shipments ts
                left join mat_warehouse_managers wm on wm.warehouse_id = ts.warehouse_id
                WHERE ts.transferring_shipment_id = :transferringShipmentId and ifnull(ts.is_deleted, :isDeleted) = :isDeleted
                LIMIT 1
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferringShipmentId", transferringShipmentId);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);

        return queryForObject(sql, params, TransferringShipmentsResponse.class);
    }
    public List<WarehouseNotifyBean> getNotifySenToApprove(List<Long> ids) {
        String sql = """
                select 
                	a.transferring_shipment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.picking_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name
                from mat_transferring_shipments a , mat_warehouse_managers wm,
                	hr_employees e,
                	mat_warehouses wh,
                	hr_employees sd
                where a.warehouse_id = wm.warehouse_id
                and wm.employee_id = e.employee_id
                and wh.warehouse_id = a.warehouse_id
                and wm.has_approve_transfer = 'Y' and e.employee_code != :loginName
                and a.transferring_shipment_id in (:ids)
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
                	a.transferring_shipment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.picking_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name,
                	a.approved_note as reason
                from mat_transferring_shipments a ,
                	hr_employees e,
                	mat_warehouses wh,
                	hr_employees sd
                where a.created_by = e.employee_code
                and a.transferring_shipment_id in (:ids)
                and wh.warehouse_id = a.warehouse_id
                and sd.employee_code = :loginName
                and wh.parent_id is not null
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("loginName", Utils.getUserNameLogin());
        return getListData(sql, params, WarehouseNotifyBean.class);
    }

    public String validateQuantity(List<Long> ids) {
        String sql = """
                select a.picking_no , GROUP_CONCAT(e.`name` SEPARATOR ', ') as equipment_name
                from mat_transferring_shipments a
                left join mat_transferring_equipments eq on a.transferring_shipment_id =  eq.transferring_shipment_id
                left join mat_warehouse_equipments we on we.warehouse_id = a.warehouse_id and we.equipment_id = eq.equipment_id and we.is_deleted = 'N'
                left join mat_equipments e on eq.equipment_id = e.equipment_id
                where eq.is_deleted = 'N'
                and eq.quantity > we.quantity
                and a.transferring_shipment_id in (:ids)
                group by a.picking_no
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        List<OutgoingEquipmentsResponse> result = getListData(sql, params, OutgoingEquipmentsResponse.class);
        if(result.isEmpty()){
            return null;
        } else {
            StringBuilder validate  = new StringBuilder();
            result.forEach(item -> {
                validate.append(item.getEquipmentName()).append(" của phiếu ").append(item.getPickingNo());
            });
            return validate.toString();
        }
    }
}
