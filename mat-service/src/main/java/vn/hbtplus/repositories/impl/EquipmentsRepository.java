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
import vn.hbtplus.models.request.EquipmentsRequest;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang mat_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class EquipmentsRepository extends BaseRepository {

    public BaseDataTableDto searchData(EquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.equipment_id,
                    a.name,
                    a.equipment_group_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_group_id = sc.value and sc.category_type = :equipmentGroup) equipmentGroupName,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    a.warning_days,
                    a.is_serial_checking,
                    a.serial_no,
                    a.unit_price,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.code,
                    a.location,
                    a.description
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (!Utils.isNullObject(dto.getWarehouseId())) {
            sql.append(", we.quantity inventoryQuantity");
        }
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EquipmentsResponse.class);
    }


    public BaseDataTableDto searchListData(EquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.equipment_id,
                    a.name,
                    a.equipment_group_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_group_id = sc.value and sc.category_type = :equipmentGroup) equipmentGroupName,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    a.warning_days,
                    a.is_serial_checking,
                    a.serial_no,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.code,
                    a.location,
                    a.description,
                """);
        if (dto.getWarehouseId() == null){
            sql.append("a.unit_price");
        } else {
            sql.append("we.unit_price");
        }

        HashMap<String, Object> params = new HashMap<>();
        if (!Utils.isNullObject(dto.getWarehouseId())) {
            sql.append(", IFNULL(we.quantity, 0) inventoryQuantity");
        }

        addConditionList(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EquipmentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(EquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.equipment_id,
                    a.name,
                    a.equipment_group_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_group_id = sc.value and sc.category_type = :equipmentGroup) equipmentGroupName,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    a.warning_days,
                    a.is_serial_checking,
                    a.serial_no,
                    a.unit_price,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.code,
                    a.location,
                    a.description
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        List<Map<String, Object>>  resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;
    }

    private void addConditionList(StringBuilder sql, Map<String, Object> params, EquipmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_equipments a
        """);
        if (!Utils.isNullObject(dto.getWarehouseId())) {
            if ("Y".equals(dto.getIsIncrease())) {
                sql.append(" LEFT ");
            }
            sql.append("""
                     JOIN mat_warehouse_equipments we ON we.equipment_id = a.equipment_id
                     AND we.warehouse_id = :warehouseId 
                     AND IFNULL(we.is_deleted, :activeStatus) = :activeStatus
                     AND we.quantity > 0
                     """);
            params.put("warehouseId", dto.getWarehouseId());
        }
        sql.append(" WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentGroup", Constants.CATEGORY_TYPE.EQUIPMENT_GROUP);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);



        QueryUtils.filter(dto.getName(), sql, params, "a.name");
        QueryUtils.filter(dto.getEquipmentTypeId(), sql, params, "et.equipment_type_id");
        QueryUtils.filter(List.of("VTTH", "VTTHDV", "VTTHKT"), sql, params, "a.equipment_group_id");
        sql.append(" ORDER BY a.created_time DESC");
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EquipmentsRequest.SearchForm dto) {
        sql.append("""
            FROM mat_equipments a
        """);
        if (!Utils.isNullObject(dto.getWarehouseId())) {
            sql.append("""
                     JOIN mat_warehouse_equipments we ON we.equipment_id = a.equipment_id 
                     AND we.warehouse_id = :warehouseId 
                     AND IFNULL(we.is_deleted, :activeStatus) = :activeStatus
                     AND we.quantity > 0
                     """);
            params.put("warehouseId", dto.getWarehouseId());
        }
        sql.append(" WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentGroup", Constants.CATEGORY_TYPE.EQUIPMENT_GROUP);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);

        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.name", "a.code", "a.serial_no");
        QueryUtils.filter(dto.getName(), sql, params, "a.name");
        QueryUtils.filter(dto.getEquipmentTypeId(), sql, params, "a.equipment_type_id");
        sql.append(" ORDER BY a.created_time DESC");
    }

    public List<EquipmentsResponse> getAllEquipment(Long warehouseId, String isIncrease) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.equipment_id,
                    a.name,
                    a.is_serial_checking,
                    a.equipment_group_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_group_id = sc.value and sc.category_type = :equipmentGroup) equipmentGroupName,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    a.code,
                    a.unit_price,
                    a.description
               
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (!Utils.isNullObject(warehouseId)) {
            sql.append(", we.quantity inventoryQuantity ");
        }
        sql.append("""
                FROM mat_equipments a
                """);
        if (!Utils.isNullObject(warehouseId)) {
            if ("Y".equals(isIncrease)) {
                sql.append(" LEFT ");
            }
            sql.append("""
                     JOIN mat_warehouse_equipments we ON we.equipment_id = a.equipment_id
                     AND we.warehouse_id = :warehouseId
                     AND IFNULL(we.is_deleted, :activeStatus) = :activeStatus
                     AND we.quantity > 0
                     """);
            params.put("warehouseId", warehouseId);
        }
        sql.append(" WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus");
        sql.append(" AND a.equipment_group_id in ('VTTH', 'VTTHDV', 'VTTHKT')");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentGroup", Constants.CATEGORY_TYPE.EQUIPMENT_GROUP);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, EquipmentsResponse.class);
    }


    public List<EquipmentsResponse> getListEquipment(EquipmentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.equipment_id,
                    a.name,
                    a.is_serial_checking,
                    a.equipment_group_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_group_id = sc.value and sc.category_type = :equipmentGroup) equipmentGroupName,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                    a.code,
                    a.unit_price,
                    a.description
                    FROM mat_equipments a
                """);
        HashMap<String, Object> params = new HashMap<>();
        sql.append(" WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentGroup", Constants.CATEGORY_TYPE.EQUIPMENT_GROUP);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        QueryUtils.filter(dto.getListEquipmentId(), sql, params, "a.equipment_id");
        return getListData(sql.toString(), params, EquipmentsResponse.class);
    }

    public Map<String, Long> getMapCodeByCodes(List<String> codeList) {
        String sql = """
                select lower(code) code, equipment_id 
                from mat_equipments
                    WHERE IFNULL(is_deleted, :activeStatus) = :activeStatus and lower(code) in (:codeList) 
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("codeList", codeList);

        List<EquipmentsResponse> equipmentCodeNameList = getListData(sql, params, EquipmentsResponse.class);
        Map<String, Long> mapEquipmentUnitCode = new HashMap<>();
        equipmentCodeNameList.forEach(item -> mapEquipmentUnitCode.put(item.getCode(), item.getEquipmentId()));
        return mapEquipmentUnitCode;
    }

    public Map<String, Long> getMapNameByNames(List<String> nameList) {
        String sql = """
                select lower(name) name, equipment_id 
                from mat_equipments et
                    WHERE IFNULL(et.is_deleted, :activeStatus) = :activeStatus and lower(name) in (:nameList) 
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("nameList", nameList);

        List<EquipmentsResponse> equipmentTypeNameList = getListData(sql, params, EquipmentsResponse.class);
        Map<String, Long> mapEquipmentTypeName = new HashMap<>();
        equipmentTypeNameList.forEach(item -> mapEquipmentTypeName.put(item.getName(), item.getEquipmentId()));
        return mapEquipmentTypeName;
    }

    public boolean isUsedEquipment(Long equipmentId) {
        String sql = """
                select count(1) from (
                    select 1 from mat_planning_equipments 
                    where equipment_id = :equipmentId and ifnull(is_deleted, :isDeleted) = :isDeleted
                    
                    union all
                    select 1 from mat_standard_equipments 
                    where equipment_id = :equipmentId and ifnull(is_deleted, :isDeleted) = :isDeleted
                ) t
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("equipmentId", equipmentId);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);

        Integer count = queryForObject(sql, params, Integer.class);

        return count > 0;
    }

    public List<EquipmentsResponse> getEquipmentByNames(List<String> equipmentNames) {
        String sql = """
                SELECT a.equipment_id,
                    a.code, a.name,
                    a.equipment_group_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_group_id = sc.value and sc.category_type = :equipmentGroup) equipmentGroupName,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName
                from mat_equipments a
                where a.is_deleted = :isDeleted
                and lower(a.name) in (:equipmentNames)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentNames", equipmentNames);
        params.put("equipmentGroup", Constants.CATEGORY_TYPE.EQUIPMENT_GROUP);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, EquipmentsResponse.class);
    }


    public List<EquipmentsResponse> getEquipmentByCodes(List<String> equipmentCodes, Long warehouseId, String isIncrease) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.equipment_id,
                    a.code, a.name, a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (!Utils.isNullObject(warehouseId)) {
            sql.append(", we.quantity inventoryQuantity ");
        }

        sql.append(" from mat_equipments a ");
        if (!Utils.isNullObject(warehouseId)) {
            if ("Y".equals(isIncrease)) {
                sql.append(" LEFT ");
            }
            sql.append("""
                     JOIN mat_warehouse_equipments we ON we.equipment_id = a.equipment_id AND we.warehouse_id = :warehouseId AND IFNULL(we.is_deleted, :isDeleted) = :isDeleted
                     AND we.quantity > 0
                     """);
            params.put("warehouseId", warehouseId);
        }
        sql.append("""
                
                where a.is_deleted = :isDeleted
                and lower(a.code) in (:equipmentCodes)
                and a.equipment_group_id in ('VTTH', 'VTTHDV', 'VTTHKT')
                """);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentCodes", equipmentCodes);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, EquipmentsResponse.class);
    }

    public List<EquipmentsResponse> getEquipmentByIds(List<Long> equipmentIds) {
        String sql = """
                SELECT a.equipment_id,
                    a.code, a.name, a.unit_price,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName
                from mat_equipments a
                where a.is_deleted = :isDeleted
                and a.equipment_id in (:equipmentIds)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentIds", equipmentIds);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, EquipmentsResponse.class);
    }

    public List<EquipmentsResponse> getListByType(Long equipmentTypeId) {
        String sql = """
                SELECT a.equipment_id,
                    a.code, a.name, a.unit_price,
                    a.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    a.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE a.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName
                from mat_equipments a
                where a.is_deleted = :isDeleted
                and a.equipment_type_id in (:equipmentTypeId)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("equipmentTypeId", equipmentTypeId);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, EquipmentsResponse.class);
    }
}
