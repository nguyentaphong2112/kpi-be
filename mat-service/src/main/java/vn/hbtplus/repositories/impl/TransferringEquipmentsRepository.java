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
import vn.hbtplus.models.request.TransferringEquipmentsRequest;
import vn.hbtplus.models.response.TransferringEquipmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.TransferringShipmentsEntity;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_transferring_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class TransferringEquipmentsRepository extends BaseRepository {

    public BaseDataTableDto searchData(TransferringEquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.transferring_equipment_id,
                    a.equipment_id,
                    a.quantity,
                    a.unit_price,
                    a.transferring_shipment_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, TransferringEquipmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(TransferringEquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.transferring_equipment_id,
                    a.equipment_id,
                    a.quantity,
                    a.unit_price,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, TransferringEquipmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_transferring_equipments a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<TransferringEquipmentsResponse> getListEquipmentByTransferringShipmentId(Long transferringShipmentId, Long warehouseId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    te.transferring_equipment_id,
                    fe.code equipmentCode,
                    fe.name equipmentName,
                    fe.note,
                    fe.description,
                    te.equipment_id,
                    fe.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    fe.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE fe.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    te.quantity,
                    te.unit_price unit_price,
                    te.inventory_quantity
                FROM mat_equipments fe
                LEFT JOIN mat_transferring_equipments te ON te.equipment_id = fe.equipment_id
                WHERE te.transferring_shipment_id = :transferringShipmentId and IFNULL(fe.is_deleted, :activeStatus) = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("transferringShipmentId", transferringShipmentId);
        params.put("warehouseId", warehouseId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, TransferringEquipmentsResponse.class);
    }

    public void approve(List<Long> ids) {
        String sql = """
                    update mat_transferring_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    status_id = :pheDuyet
                    where transferring_shipment_id in (:transferringShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferringShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", TransferringShipmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", TransferringShipmentsEntity.STATUS.PHE_DUYET);

        executeSqlDatabase(sql, params);
    }

    public void undoApprove(List<Long> ids) {
        String sql = """
                    update mat_transferring_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    status_id = :pheDuyet
                    where transferring_shipment_id in (:transferringShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferringShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", TransferringShipmentsEntity.STATUS.PHE_DUYET);
        params.put("pheDuyet", TransferringShipmentsEntity.STATUS.TU_CHOI);

        executeSqlDatabase(sql, params);
    }

    public void sendToApprove(List<Long> ids) {
        String sql = """
                    update mat_transferring_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    status_id = :pheDuyet
                    where transferring_shipment_id in (:transferringShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferringShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", TransferringShipmentsEntity.STATUS.DU_THAO);
        params.put("pheDuyet", TransferringShipmentsEntity.STATUS.CHO_DUYET);

        executeSqlDatabase(sql, params);
    }

    public void reject(List<Long> ids, String note) {
        String sql = """
                    update mat_transferring_shipments a
                    set approved_by = :approvedBy,
                    approved_time = :approvedTime,
                    approved_note = :note,
                    status_id = :pheDuyet
                    where transferring_shipment_id in (:transferringShipmentIds)
                    and status_id = :choDuyet
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("transferringShipmentIds", ids);
        params.put("approvedBy", Utils.getUserNameLogin());
        params.put("approvedTime", new Date());
        params.put("choDuyet", TransferringShipmentsEntity.STATUS.CHO_DUYET);
        params.put("pheDuyet", TransferringShipmentsEntity.STATUS.TU_CHOI);
        params.put("note", note);

        executeSqlDatabase(sql, params);
    }

    public String getPermissionApprove(Long warehouseId) {
        String sql = """
                SELECT wm.has_approve_transfer
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
}
