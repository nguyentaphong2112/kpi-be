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
import vn.hbtplus.models.request.IncomingShipmentsRequest;
import vn.hbtplus.models.response.IncomingEquipmentsResponse;
import vn.hbtplus.models.response.IncomingShipmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.IncomingShipmentsEntity;
import vn.hbtplus.repositories.jpa.EmployeeRepositoryJPA;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_incoming_shipments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class IncomingShipmentsRepository extends BaseRepository {

    private final EmployeeRepositoryJPA employeeRepository;

    public BaseDataTableDto searchData(IncomingShipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.incoming_shipment_id,
                    a.warehouse_id,
                    sw.name as warehouse_name,
                    a.picking_no,
                    a.incoming_date,
                    a.picking_employee_id,
                    (select full_name from hr_employees where employee_id=a.picking_employee_id) as picking_employee_name,
                    a.author_id,
                    a.contract_id,
                    a.partner_name,
                    a.invoice_id,
                    a.shipped_by,
                    a.note,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    a.type,
                    (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :type) typeName,
                    a.approved_by,
                    a.approved_time,
                    a.transfer_warehouse_id,
                    a.transferred_date,
                    a.receiver_id,
                    a.transferring_shipment_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.contract_no,
                    wm.has_approve_import
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, IncomingShipmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(IncomingShipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.incoming_shipment_id,
                    a.warehouse_id,
                    sw.name as warehouse_name,
                    a.picking_no,
                    a.incoming_date,
                    a.picking_employee_id,
                    (select full_name from hr_employees where employee_id=a.picking_employee_id) as picking_employee_name,
                    a.author_id,
                    a.contract_id,
                    a.partner_name,
                    a.invoice_id,
                    a.shipped_by,
                    a.note,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    a.type,
                    (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :type) typeName,
                    a.approved_by,
                    a.approved_time,
                    a.transfer_warehouse_id,
                    a.transferred_date,
                    a.contract_no,
                    a.receiver_id,
                    a.transferring_shipment_id,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, IncomingShipmentsRequest.SearchForm dto) {
        sql.append("""
                    FROM mat_incoming_shipments a
                    LEFT JOIN mat_warehouses sw ON sw.warehouse_id = a.warehouse_id and ifnull(sw.is_deleted, :isDeleted) = :isDeleted
                    LEFT JOIN mat_warehouse_managers wm on sw.warehouse_id = wm.warehouse_id
                    WHERE a.is_deleted = :isDeleted
                """);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("type", Constants.CATEGORY_TYPE.INCOMING_SHIPMENTS_TYPE);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.picking_no", "sw.name");
        QueryUtils.filter(dto.getPickingNo(), sql, params, "a.picking_no");
        QueryUtils.filter(dto.getWarehouseId(), sql, params, "a.warehouse_id");
        QueryUtils.filter(dto.getDepartmentId(), sql, params, "sw.department_id");
        QueryUtils.filterEq(dto.getType(), sql, params, "a.type");
        QueryUtils.filterEq(dto.getStatusId(), sql, params, "a.status_id");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.incoming_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "a.incoming_date", "toDate");
        // add check quyền quản lý kho
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
                SELECT wm.has_approve_import
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

    public void sendToApprove(List<Long> ids) {
        String sql = """
                    update mat_incoming_shipments a
                    set modified_by = :approvedBy,
                    modified_time = :approvedTime,
                    status_id = :choDuyet
                    where incoming_shipment_id in (:incomingShipmentIds)
                    and status_id = :duThao
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incomingShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", IncomingShipmentsEntity.STATUS.CHO_DUYET);
        params.put("duThao", IncomingShipmentsEntity.STATUS.DU_THAO);

        executeSqlDatabase(sql, params);

    }

    public void approve(List<Long> ids) {
        String sql = """
                    update mat_incoming_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    status_id = :pheDuyet
                    where incoming_shipment_id in (:incomingShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incomingShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", IncomingShipmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", IncomingShipmentsEntity.STATUS.PHE_DUYET);

        executeSqlDatabase(sql, params);
    }

    public void undoApprove(List<Long> ids) {
        String sql = """
                    update mat_incoming_shipments a
                    set approved_by = null,
                    approved_time = null,
                    modified_by = :approvedBy,
                    modified_time = :approvedTime,
                    status_id = :choDuyet
                    where incoming_shipment_id in (:incomingShipmentIds)
                    and status_id = :pheDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incomingShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", IncomingShipmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", IncomingShipmentsEntity.STATUS.PHE_DUYET);

        executeSqlDatabase(sql, params);
    }

    public void reject(List<Long> ids, String note) {
        String sql = """
                    update mat_incoming_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    approved_note = :note,
                    status_id = :pheDuyet
                    where incoming_shipment_id in (:incomingShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incomingShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("note", note);
        params.put("choDuyet", IncomingShipmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", IncomingShipmentsEntity.STATUS.TU_CHOI);

        executeSqlDatabase(sql, params);
    }


    public void insertTransferEquipments(Long transferringShipmentId, Long incomingShipmentId, Long warehouseId) {
        String sql = """
                insert into mat_transferring_equipments(equipment_id, quantity, unit_price, transferring_shipment_id, is_deleted, created_by, created_time, inventory_quantity)
                select a.equipment_id, a.quantity, a.unit_price, :transferring_shipment_id, a.is_deleted, :created_by, now(), we.quantity
                from mat_incoming_equipments a
                LEFT JOIN mat_warehouse_equipments we ON we.equipment_id = a.equipment_id AND IFNULL(we.is_deleted, 'N') = 'N' AND we.warehouse_id = :warehouseId 
                where a.is_deleted = 'N'
                and a.incoming_shipment_id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferring_shipment_id", transferringShipmentId);
        params.put("warehouseId", warehouseId);
        params.put("created_by", Utils.getUserNameLogin());
        params.put("id", incomingShipmentId);
        executeSqlDatabase(sql, params);
    }



    public void insertOutGoingEquipments(Long outgoingShipmentId, Long incomingShipmentId, Long warehouseId) {
        String sql = """
                insert into mat_outgoing_equipments(equipment_id, quantity, unit_price, outgoing_shipment_id, is_deleted, created_by, created_time, inventory_quantity)
                select a.equipment_id, a.quantity, a.unit_price, :outgoing_shipment_id, a.is_deleted, :created_by, now(), we.quantity
                from mat_incoming_equipments a
                LEFT JOIN mat_warehouse_equipments we ON we.equipment_id = a.equipment_id AND IFNULL(we.is_deleted, 'N') = 'N' AND we.warehouse_id = :warehouseId 
                where a.is_deleted = 'N'
                and a.incoming_shipment_id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("outgoing_shipment_id", outgoingShipmentId);
        params.put("created_by", Utils.getUserNameLogin());
        params.put("warehouseId", warehouseId);
        params.put("id", incomingShipmentId);
        executeSqlDatabase(sql, params);
    }

    public void insertIncomingEquipments(Long incomingShipmentId, Long id) {
        String sql = """
                insert into mat_incoming_equipments(equipment_id, quantity, unit_price, incoming_shipment_id, is_deleted, created_by, created_time)
                select a.equipment_id, a.quantity, a.unit_price, :incoming_shipment_id, a.is_deleted, :created_by, now()
                from mat_incoming_equipments a
                where a.is_deleted = 'N'
                and a.incoming_shipment_id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incoming_shipment_id", incomingShipmentId);
        params.put("created_by", Utils.getUserNameLogin());
        params.put("id", id);
        executeSqlDatabase(sql, params);
    }

    public List<IncomingEquipmentsResponse> getListEquipmentByIncomingShipments(Long incomingShipmentId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ie.incoming_shipment_id,
                    fe.code,
                    fe.name,
                    ie.equipment_id,
                    fe.equipment_group_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_group_id = sc.value and sc.category_type = :equipmentGroup) equipmentGroupName,
                    fe.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    fe.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    ie.quantity,
                    ie.unit_price
                FROM mat_equipments fe
                LEFT JOIN mat_incoming_equipments ie ON ie.equipment_id = fe.equipment_id and IFNULL(ie.is_deleted, :activeStatus) = :activeStatus
                WHERE ie.incoming_shipment_id = :incomingShipmentId and IFNULL(fe.is_deleted, :activeStatus) = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("incomingShipmentId", incomingShipmentId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentGroup", Constants.CATEGORY_TYPE.EQUIPMENT_GROUP);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, IncomingEquipmentsResponse.class);
    }

    public IncomingShipmentsResponse getOutgoingWarehouseByTransferShipmentId(Long transferringShipmentId) {
        String sql = """
                select os.warehouse_id outgoingWarehouseId, ts.picking_no outgoingTransferPickingNo
                from mat_outgoing_shipments os
                    JOIN mat_transferring_shipments ts ON os.transferring_shipment_id = ts.transferring_shipment_id
                            and ifnull(ts.is_deleted, :isDeleted) = :isDeleted
                where os.transferring_shipment_id = :transferringShipmentId
                    and os.type = :transferType and ifnull(os.is_deleted, :isDeleted) = :isDeleted
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferringShipmentId", transferringShipmentId);
        params.put("transferType", IncomingShipmentsEntity.TYPES.DIEU_CHUYEN);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        return queryForObject(sql, params, IncomingShipmentsResponse.class);
    }

    public List<WarehouseNotifyBean> getNotifySenToApprove(List<Long> ids) {
        String sql = """
                select 
                	a.incoming_shipment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.picking_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name
                from mat_incoming_shipments a , mat_warehouse_managers wm,
                	hr_employees e,
                	mat_warehouses wh,
                	hr_employees sd
                where a.warehouse_id = wm.warehouse_id
                and wm.employee_id = e.employee_id
                and wh.warehouse_id = a.warehouse_id
                and wm.has_approve_import = 'Y' and e.employee_code != :loginName
                and a.incoming_shipment_id in (:ids)
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
                	a.incoming_shipment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.picking_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name,
                	a.approved_note as reason
                from mat_incoming_shipments a ,
                	hr_employees e,
                	mat_warehouses wh,
                	hr_employees sd
                where a.created_by = e.employee_code
                and a.incoming_shipment_id in (:ids)
                and sd.employee_code = :loginName
                and wh.warehouse_id = a.warehouse_id
                and wh.parent_id is not null
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("loginName", Utils.getUserNameLogin());
        return getListData(sql, params, WarehouseNotifyBean.class);
    }
}
