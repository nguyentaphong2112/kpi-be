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
import vn.hbtplus.models.request.OutgoingShipmentsRequest;
import vn.hbtplus.models.response.OutgoingEquipmentsResponse;
import vn.hbtplus.models.response.OutgoingShipmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.IncomingShipmentsEntity;
import vn.hbtplus.repositories.entity.OutgoingShipmentsEntity;
import vn.hbtplus.repositories.jpa.EmployeeRepositoryJPA;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_outgoing_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class OutgoingShipmentsRepository extends BaseRepository {

    private final EmployeeRepositoryJPA employeeRepository;

    public BaseDataTableDto searchData(OutgoingShipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.outgoing_shipment_id,
                    a.warehouse_id,
                    sw.name as warehouse_name,
                    a.picking_no,
                    a.outgoing_date,
                    a.picking_employee_id,
                    (select full_name from hr_employees where employee_id=a.picking_employee_id) as picking_employee_name,
                    a.receiver_id,
                    (select full_name from hr_employees where employee_id=a.receiver_id) as receiver_name,
                    a.note,
                    a.incoming_shipment_id,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) statusName,
                    a.type,
                    (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :type) typeName,
                    a.approved_by,
                    a.approved_time,
                    a.transferring_shipment_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    wm.has_approve_export
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OutgoingShipmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(OutgoingShipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.outgoing_shipment_id,
                    a.warehouse_id,
                    sw.name as warehouse_name,
                    (select name from hr_organizations where sw.department_id = organization_id) as department_name,
                    a.picking_no,
                    a.outgoing_date,
                    a.picking_employee_id,
                    (select full_name from hr_employees where employee_id=a.picking_employee_id) as picking_employee_name,
                    a.receiver_id,
                    (select full_name from hr_employees where employee_id=a.receiver_id) as receiver_name,
                    a.note,
                    a.incoming_shipment_id,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :status) status_name,
                    a.type,
                    (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :type) type_name,
                    a.approved_by,
                    a.approved_time,
                    a.transferring_shipment_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("typeCode", Constants.CATEGORY_TYPE.BUILDING_GROUP);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<OutgoingEquipmentsResponse> getListEquipmentByOutgoingShipments(Long outgoingShipmentId, Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    oe.outgoing_shipment_id outgoingShipmentId,
                    fe.code,
                    fe.name,
                    fe.note,
                    fe.description,
                    fe.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    fe.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    oe.quantity,
                    oe.unit_price unitPrice,
                    oe.inventory_quantity
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        sql.append("""
                FROM mat_equipments fe
                LEFT JOIN mat_outgoing_equipments oe ON oe.equipment_id = fe.equipment_id
                """);
        sql.append(" WHERE oe.outgoing_shipment_id = :outgoingShipmentId and IFNULL(oe.is_deleted, :activeStatus) = :activeStatus ");

        params.put("outgoingShipmentId", outgoingShipmentId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, OutgoingEquipmentsResponse.class);
    }

    public void sendToApprove(List<Long> ids) {
        String sql = """
                    update mat_outgoing_shipments a
                    set modified_by = :approvedBy,
                    modified_time = :approvedTime,
                    status_id = :choDuyet
                    where outgoing_shipment_id in (:outgoingShipmentIds)
                    and status_id = :duThao
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("outgoingShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", OutgoingShipmentsEntity.STATUS.CHO_DUYET);
        params.put("duThao", OutgoingShipmentsEntity.STATUS.DU_THAO);

        executeSqlDatabase(sql, params);

    }

    public void approve(List<Long> ids) {
        String sql = """
                    update mat_outgoing_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    status_id = :pheDuyet
                    where outgoing_shipment_id in (:outgoingShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("outgoingShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", OutgoingShipmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", OutgoingShipmentsEntity.STATUS.PHE_DUYET);

        executeSqlDatabase(sql, params);
    }


    public void reject(List<Long> ids, String note) {
        String sql = """
                    update mat_outgoing_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    approved_note = :note,
                    status_id = :pheDuyet
                    where outgoing_shipment_id in (:outgoingShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("outgoingShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("note", note);
        params.put("choDuyet", OutgoingShipmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", OutgoingShipmentsEntity.STATUS.TU_CHOI);

        executeSqlDatabase(sql, params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, OutgoingShipmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_outgoing_shipments a
            LEFT JOIN mat_warehouses sw ON sw.warehouse_id = a.warehouse_id and ifnull(sw.is_deleted, :isDeleted) = :isDeleted
            left join mat_warehouse_managers wm on sw.warehouse_id = wm.warehouse_id
            WHERE IFNULL(a.is_deleted, :isDeleted) = :isDeleted
            
        """);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constants.CATEGORY_TYPE.MAT_STATUS);
        params.put("type", Constants.CATEGORY_TYPE.MAT_OUTGOING_SHIPMENT_TYPE);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.picking_no", "sw.name");
        QueryUtils.filter(dto.getPickingNo(), sql, params, "a.picking_no");
        QueryUtils.filter(dto.getWarehouseId(), sql, params, "a.warehouse_id");
        QueryUtils.filterEq(dto.getType(), sql, params, "a.type");
        QueryUtils.filterEq(dto.getStatusId(), sql, params, "a.status_id");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.outgoing_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "a.outgoing_date", "toDate");

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
                SELECT wm.has_approve_export
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

    public OutgoingShipmentsResponse getIncomingWarehouseByTransferShipmentId(Long transferShipmentId) {
        String sql = """
                select sis.warehouse_id incomingWarehouseId,
                    ts.picking_no incomingTransferPickingNo
                from mat_incoming_shipments sis
                    JOIN mat_transferring_shipments ts ON sis.transferring_shipment_id = ts.transferring_shipment_id
                            and ifnull(ts.is_deleted, :isDeleted) = :isDeleted
                where sis.transferring_shipment_id = :transferShipmentId
                    and sis.type = :transferType and ifnull(sis.is_deleted, :isDeleted) = :isDeleted
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferShipmentId", transferShipmentId);
        params.put("transferType", IncomingShipmentsEntity.TYPES.DIEU_CHUYEN);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        return queryForObject(sql, params, OutgoingShipmentsResponse.class);
    }

    public List<WarehouseNotifyBean> getNotifySenToApprove(List<Long> ids) {
        String sql = """
                select 
                	a.outgoing_shipment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.picking_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name
                from mat_outgoing_shipments a , mat_warehouse_managers wm,
                	hr_employees e,
                	mat_warehouses wh,
                	hr_employees sd
                where a.warehouse_id = wm.warehouse_id
                and wm.employee_id = e.employee_id
                and wh.warehouse_id = a.warehouse_id
                and wm.has_approve_export = 'Y' and e.employee_code != :loginName
                and a.outgoing_shipment_id in (:ids)
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
                	a.outgoing_shipment_id as id,
                	e.employee_code as receiver_code,
                	wh.`name` as warehouse_name,
                	a.picking_no as picking_no,
                	sd.employee_code as sender_code,
                	sd.full_name as sender_name,
                	a.approved_note as reason
                from mat_outgoing_shipments a ,
                	hr_employees e,
                	mat_warehouses wh,
                	hr_employees sd
                where a.created_by = e.employee_code
                and a.outgoing_shipment_id in (:ids)
                and sd.employee_code = :loginName
                and wh.warehouse_id = a.warehouse_id
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
                from mat_outgoing_shipments a
                left join mat_outgoing_equipments eq on a.outgoing_shipment_id =  eq.outgoing_shipment_id
                left join mat_warehouse_equipments we on we.warehouse_id = a.warehouse_id and we.equipment_id = eq.equipment_id and we.is_deleted = 'N'
                left join mat_equipments e on eq.equipment_id = e.equipment_id
                where eq.is_deleted = 'N'
                and eq.quantity > we.quantity
                and a.outgoing_shipment_id in (:ids)
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
                validate.append(item.getEquipmentName()).append(" của phiếu xuất ").append(item.getPickingNo());
            });
            return validate.toString();
        }
    }
}
