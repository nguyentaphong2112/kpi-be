package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.models.bean.StkEquipmentBean;
import vn.hbtplus.repositories.BaseRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RStkWarehouseRepository extends BaseRepository {

    public List<StkEquipmentBean> getHistoryByEquipmentType(Long warehouseId, Date periodDate) {
        String sql = """
                select
                	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                	sum(T.quantity) as quantity,
                	sum(T.quantity * ifnull(we.unit_price, eq.unit_price)) as amountMoney
                from
                (select
                	h.equipment_id,
                	h.quantity,
                	h.unit_price
                from mat_warehouse_equipment_histories h
                where h.is_deleted = 'N'
                and h.warehouse_id = :warehouseId
                and h.period_date = :periodDate) T, mat_equipments eq
                left join mat_warehouse_equipments we on we.equipment_id = eq.equipment_id and we.warehouse_id = :warehouseId and we.is_deleted = 'N'
                where T.equipment_id = eq.equipment_id
                group by eq.equipment_type_id,
                	eq.equipment_unit_id
                order by eq.equipment_type_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("periodDate", periodDate);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getHistoryByOrganization(Long organizationId, Long warehouseId, Date periodDate) {
        String sql = """
                select
                    sh.department_id,
                    (select name from hr_organizations where organization_id = sh.department_id) as department_name,
                    sh.warehouse_id,
                    sh.name as warehouse_name,
                	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                	eq.equipment_id,
                	eq.name as equipment_name,
                	eq.code as equipment_code,
                	h.quantity as quantity,
                	h.quantity * ifnull(we.unit_price,eq.unit_price) as amountMoney
                from mat_equipments eq,
                    mat_warehouses sh,
                    mat_warehouse_equipment_histories h
                    left join mat_warehouse_equipments we on we.equipment_id = h.equipment_id and we.warehouse_id = h.warehouse_id and we.is_deleted = 'N'
                where h.is_deleted = 'N'
                and sh.is_deleted = 'N'
                and h.quantity > 0
                and h.period_date = :periodDate
                and sh.warehouse_id = h.warehouse_id
                and h.equipment_id = eq.equipment_id
                """;
        Map<String, Object> params = new HashMap<>();
        if (warehouseId != null) {
            sql += " and sh.warehouse_id = :warehouseId";
            params.put("warehouseId", warehouseId);
        }
        if (organizationId != null) {
            sql += " and sh.department_id = :organizationId";
            params.put("organizationId", organizationId);
        }
        params.put("periodDate", periodDate);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getHistoryByEquipment(Long warehouseId, Long equipmentId, Date periodDate) {
        String sql = """
                select
                	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                	eq.equipment_id,
                	eq.name as equipment_name,
                	eq.code as equipment_code,
                	h.quantity as quantity,
                	h.quantity * ifnull(we.unit_price,eq.unit_price) as amountMoney
                from mat_equipments eq,
                    mat_warehouse_equipment_histories h
                    left join mat_warehouse_equipments we on we.equipment_id = h.equipment_id and we.warehouse_id = h.warehouse_id and we.is_deleted = 'N'
                where h.is_deleted = 'N'
                and h.warehouse_id = :warehouseId
                and h.period_date = :periodDate
                and h.equipment_id = eq.equipment_id
                """;
        Map<String, Object> params = new HashMap<>();
        if (equipmentId != null) {
            sql += " and eq.equipment_id = :equipmentId";
            params.put("equipmentId", equipmentId);
        }
        sql += " order by eq.equipment_type_id, eq.equipment_id";
        params.put("warehouseId", warehouseId);
        params.put("periodDate", periodDate);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getIncomingByEquipmentType(Long warehouseId, Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return new ArrayList<>();
        }
        String sql = """
                select
                	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                	sum(T.quantity) as quantity,
                	sum(T.quantity * ifnull(we.unit_price, eq.unit_price)) as amountMoney
                from (select
                	a.equipment_id,
                	a.quantity,
                	a.unit_price
                from mat_incoming_shipments am, mat_incoming_equipments a
                where am.incoming_date between :startDate and :endDate 
                and am.status_id = 'PHE_DUYET'
                and am.is_deleted = 'N'
                and a.is_deleted = 'N'
                and a.incoming_shipment_id = am.incoming_shipment_id
                and am.warehouse_id = :warehouseId
                union all 
                select
                   a.equipment_id,
                   ifnull(a.quantity,0) - ifnull(a.inventory_quantity,0)  AS quantity,
                   a.unit_price
                  from mat_inventory_adjustments am, mat_inventory_adjust_equipments a
                  where am.end_date between :startDate and :endDate
                  and am.status_id = 'PHE_DUYET'
                  and am.is_deleted = 'N'
                  and a.is_deleted = 'N'
                  and am.type = 'TANG'
                  and a.inventory_ajustment_id = am.inventory_adjustment_id
                  and am.warehouse_id = :warehouseId
                ) T , mat_equipments eq
                left join mat_warehouse_equipments we on we.equipment_id = eq.equipment_id and we.warehouse_id = :warehouseId and we.is_deleted = 'N'
                where T.equipment_id = eq.equipment_id
                group by eq.equipment_type_id, eq.equipment_unit_id
                order by eq.equipment_type_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getIncomingByOrganization(Long organizationId, Long warehouseId, Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder("""
                   WITH v_equipments as (
                   	select
                   		am.warehouse_id,
                   		a.equipment_id,
                   		am.incoming_date,
                   		a.quantity,
                   		a.unit_price
                   	from
                   		mat_incoming_shipments am,
                   		mat_incoming_equipments a
                   	 where am.is_deleted = 'N'
                   		and a.is_deleted = 'N'
                   		and a.quantity > 0
                   		and am.status_id = 'PHE_DUYET'
                   		and a.incoming_shipment_id = am.incoming_shipment_id
                   		and am.incoming_date between :startDate and :endDate
                   	union all
                   	select
                   		am.warehouse_id,
                   		a.equipment_id,
                   		am.end_date as incoming_date,
                   		ifnull(a.quantity,0) - ifnull(a.inventory_quantity,0) quantity,
                   		a.unit_price
                   	from
                   		mat_inventory_adjustments am,
                   		mat_inventory_adjust_equipments a
                   	 where am.is_deleted = 'N'
                   		and a.is_deleted = 'N'
                   		and a.quantity > 0
                   		and am.type = 'TANG'
                   		and am.status_id = 'PHE_DUYET'
                   		and am.inventory_adjustment_id = a.inventory_ajustment_id
                   		and am.end_date between :startDate and :endDate
                   )
                   select
                   	sh.department_id,
                   	(select name from hr_organizations where organization_id = sh.department_id) as department_name,
                   	sh.warehouse_id,
                   	sh.name as warehouse_name,
                    eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                   	eq.code as equipment_code,
                   	eq.name as equipment_name,
                   	a.equipment_id,
                   	sum(a.quantity) as quantity,
                   	sum(a.quantity * ifnull(we.unit_price, eq.unit_price) ) as amount_money
                    from 
                   		mat_warehouses sh,
                   		mat_equipments eq,
                        v_equipments a
                        left join mat_warehouse_equipments we on we.equipment_id = a.equipment_id and we.warehouse_id = a.warehouse_id and we.is_deleted = 'N'
                    where  a.warehouse_id = sh.warehouse_id
                    and a.equipment_id = eq.equipment_id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        if (organizationId != null) {
            params.put("organizationId", organizationId);
            sql.append(" and sh.department_id = :organizationId");
        }
        if (warehouseId != null) {
            params.put("warehouseId", warehouseId);
            sql.append(" and sh.warehouse_id = :warehouseId");
        }
        sql.append(" group by sh.department_id," +
                   "sh.warehouse_id, sh.name," +
                   "eq.equipment_type_id," +
                   "eq.equipment_unit_id," +
                   "a.equipment_id,eq.code, eq.name");

        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getOutgoingByOrganization(Long organizationId, Long warehouseId, Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder("""
                   WITH v_equipments as (
                   	select
                   		am.warehouse_id,
                   		a.equipment_id,
                   		am.outgoing_date,
                   		a.quantity,
                   		a.unit_price
                   	from
                   		mat_outgoing_shipments am,
                   		mat_outgoing_equipments a
                   	 where am.is_deleted = 'N'
                   		and a.is_deleted = 'N'
                   		and a.quantity > 0
                   		and am.status_id = 'PHE_DUYET'
                   		and a.outgoing_shipment_id = am.outgoing_shipment_id
                   		and am.outgoing_date between :startDate and :endDate
                   	union all
                   	select
                   		am.warehouse_id,
                   		a.equipment_id,
                   		am.end_date as outgoing_date,
                   		ifnull(a.inventory_quantity,0) - ifnull(a.quantity,0) quantity,
                   		a.unit_price
                   	from
                   		mat_inventory_adjustments am,
                   		mat_inventory_adjust_equipments a
                   	 where am.is_deleted = 'N'
                   		and a.is_deleted = 'N'
                   		and a.quantity > 0
                   		and am.type = 'GIAM'
                   		and am.status_id = 'PHE_DUYET'
                   		and am.inventory_adjustment_id = a.inventory_ajustment_id
                   		and am.end_date between :startDate and :endDate
                   )
                   select
                   	sh.department_id,
                   	(select name from hr_organizations where organization_id = sh.department_id) as department_name,
                   	sh.warehouse_id,
                   	sh.name as warehouse_name,
                   	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                   	eq.code as equipment_code,
                   	eq.name as equipment_name,
                   	a.equipment_id,
                   	sum(a.quantity) as quantity,
                   	sum(a.quantity * ifnull(we.unit_price, eq.unit_price)) as amount_money
                    from 
                   		mat_warehouses sh,
                   		mat_equipments eq,
                        v_equipments a
                        left join mat_warehouse_equipments we on we.equipment_id = a.equipment_id and we.warehouse_id = a.warehouse_id and we.is_deleted = 'N'
                    where  a.warehouse_id = sh.warehouse_id
                    and a.equipment_id = eq.equipment_id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        if (organizationId != null) {
            params.put("organizationId", organizationId);
            sql.append(" and sh.department_id = :organizationId");
        }
        if (warehouseId != null) {
            params.put("warehouseId", warehouseId);
            sql.append(" and sh.warehouse_id = :warehouseId");
        }
        sql.append(" group by sh.department_id," +
                   "sh.warehouse_id, sh.name," +
                   "eq.equipment_type_id," +
                   "eq.equipment_unit_id," +
                   "a.equipment_id,eq.code, eq.name");

        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql.toString(), params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getIncomingByEquipment(Long warehouseId, Long equipmentId, Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return new ArrayList<>();
        }
        String sql = """
                select
                	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                	eq.equipment_id,
                	eq.name as equipment_name,
                	eq.code as equipment_code,
                	sum(T.quantity) as quantity,
                	sum(T.quantity * ifnull(we.unit_price, eq.unit_price)) as amountMoney
                from (select
                   a.equipment_id,
                   a.quantity,
                   a.unit_price
                  from mat_incoming_shipments am, mat_incoming_equipments a
                  where am.incoming_date between :startDate and :endDate
                  and am.status_id = 'PHE_DUYET'
                  and am.is_deleted = 'N'
                  and a.is_deleted = 'N'
                  and a.incoming_shipment_id = am.incoming_shipment_id
                  and am.warehouse_id = :warehouseId
                  {FILTER_EQUIPMENT}
                  UNION all
                  select
                   a.equipment_id,
                   ifnull(a.quantity,0) - ifnull(a.inventory_quantity,0)  AS quantity,
                   a.unit_price
                  from mat_inventory_adjustments am, mat_inventory_adjust_equipments a
                  where am.end_date between :startDate and :endDate
                  and am.status_id = 'PHE_DUYET'
                  and am.is_deleted = 'N'
                  and a.is_deleted = 'N'
                  and am.type = 'TANG'
                  and a.inventory_ajustment_id = am.inventory_adjustment_id
                  and am.warehouse_id = :warehouseId
                  {FILTER_EQUIPMENT}
                ) T , mat_equipments eq
                left join mat_warehouse_equipments we on we.equipment_id = eq.equipment_id and we.warehouse_id = :warehouseId and we.is_deleted = 'N'
                where T.equipment_id = eq.equipment_id
                group by eq.equipment_type_id, eq.equipment_unit_id, eq.equipment_id, eq.name 
                order by eq.equipment_type_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        if (equipmentId != null) {
            sql = sql.replace("{FILTER_EQUIPMENT}", " and a.equipment_id = :equipmentId ");
            params.put("equipmentId", equipmentId);
        } else {
            sql = sql.replace("{FILTER_EQUIPMENT}","");
        }

        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getOutgoingByEquipmentType(Long warehouseId, Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return new ArrayList<>();
        }
        String sql = """
                select
                	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                	sum(T.quantity) as quantity,
                	sum(T.quantity * ifnull(we.unit_price, eq.unit_price)) as amountMoney
                from (select
                	a.equipment_id,
                	a.quantity,
                	a.unit_price
                from mat_outgoing_shipments am, mat_outgoing_equipments a 
                where am.outgoing_date between :startDate and :endDate 
                and am.status_id = 'PHE_DUYET'
                and am.is_deleted = 'N'
                and a.is_deleted = 'N'
                and a.outgoing_shipment_id = am.outgoing_shipment_id
                and am.warehouse_id = :warehouseId
                union all 
                select
                   a.equipment_id,
                   ifnull(a.inventory_quantity,0) - ifnull(a.quantity,0)  AS quantity,
                   a.unit_price
                  from mat_inventory_adjustments am, mat_inventory_adjust_equipments a
                  where am.end_date between :startDate and :endDate
                  and am.status_id = 'PHE_DUYET'
                  and am.is_deleted = 'N'
                  and a.is_deleted = 'N'
                  and am.type = 'GIAM'
                  and a.inventory_ajustment_id = am.inventory_adjustment_id
                  and am.warehouse_id = :warehouseId
                ) T , mat_equipments eq
                left join mat_warehouse_equipments we on we.equipment_id = eq.equipment_id and we.warehouse_id = :warehouseId and we.is_deleted = 'N'
                where T.equipment_id = eq.equipment_id
                group by eq.equipment_type_id, eq.equipment_unit_id
                order by eq.equipment_type_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getOutgoingByEquipment(Long warehouseId, Long equipmentId, Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return new ArrayList<>();
        }
        String sql = """
                select
                	eq.equipment_type_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_type_id = sc.value and sc.category_type = :equipmentType) equipmentTypeName,
                    eq.equipment_unit_id,
                    (SELECT sc.name FROM sys_categories sc WHERE eq.equipment_unit_id = sc.value and sc.category_type = :equipmentUnit) equipmentUnitName,
                	eq.equipment_id,
                	eq.name as equipment_name,
                	eq.code as equipment_code,
                	sum(T.quantity) as quantity,
                	sum(T.quantity * ifnull(we.unit_price, eq.unit_price)) as amountMoney
                from (
                   select
                       a.equipment_id,
                       a.quantity,
                       a.unit_price
                    from mat_outgoing_shipments am, mat_outgoing_equipments a
                    where am.outgoing_date between :startDate and :endDate
                    and am.status_id = 'PHE_DUYET'
                    and am.is_deleted = 'N'
                    and a.is_deleted = 'N'
                    and a.outgoing_shipment_id = am.outgoing_shipment_id
                    and am.warehouse_id = :warehouseId
                    {FILTER_EQUIPMENT}
                    UNION all
                    select
                       a.equipment_id,
                       ifnull(a.inventory_quantity,0) - ifnull(a.quantity,0) AS quantity,
                       a.unit_price
                    from mat_inventory_adjustments am, mat_inventory_adjust_equipments a
                    where am.end_date between :startDate and :endDate
                    and am.status_id = 'PHE_DUYET'
                    and am.is_deleted = 'N'
                    and a.is_deleted = 'N'
                    and am.type = 'GIAM'
                    and a.inventory_ajustment_id = am.inventory_adjustment_id
                    and am.warehouse_id = :warehouseId
                    {FILTER_EQUIPMENT}
                ) T , mat_equipments eq
                left join mat_warehouse_equipments we on we.equipment_id = eq.equipment_id and we.warehouse_id = :warehouseId and we.is_deleted = 'N'
                where T.equipment_id = eq.equipment_id
                group by eq.equipment_type_id, eq.equipment_unit_id, eq.equipment_id, eq.name
                order by eq.equipment_type_id, eq.equipment_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        if (equipmentId != null) {
            sql = sql.replace("{FILTER_EQUIPMENT}", " and a.equipment_id = :equipmentId ");
            params.put("equipmentId", equipmentId);
        } else {
            sql = sql.replace("{FILTER_EQUIPMENT}","");
        }
        params.put("equipmentUnit", Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        params.put("equipmentType", Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getListIncomingShipment(Long warehouseId, Long equipmentId, Date fromDate, Date toDate) {
        String sql = """
                select
                    a.incoming_date as pickingDate,
                    eq.quantity,  
                    a.picking_no,
                    a.approved_time,
                    'Y' as isIncoming,
                    type as type,
                    (case when type = 'NHAP_MOI' then 'Nhập mới'
                        when type = 'DIEU_CHUYEN' then 'Điều chuyển'
                        else '' end) type_name
                from mat_incoming_shipments a,
                mat_incoming_equipments eq
                where a.is_deleted = 'N'
                and eq.is_deleted = 'N'
                and a.status_id = 'PHE_DUYET'
                and a.warehouse_id = :warehouseId
                and eq.equipment_id = :equipmentId
                and a.incoming_shipment_id = eq.incoming_shipment_id
                and a.incoming_date between :startDate and :endDate
                union all 
                select
                		a.end_date as pickingDate,
                		ifnull(eq.quantity,0) - ifnull(eq.inventory_quantity,0) quantity, 
                		a.inventory_adjustment_no picking_no,
                		a.approved_time,
                		'Y' as isIncoming,
                		type as type,
                		'Kiểm kê tăng hàng hóa' type_name
                from mat_inventory_adjustments a,
                mat_inventory_adjust_equipments eq
                where a.is_deleted = 'N'
                and eq.is_deleted = 'N'
                and a.status_id = 'PHE_DUYET'
                and a.inventory_adjustment_id = eq.inventory_ajustment_id
                and a.end_date BETWEEN :startDate and :endDate
                and a.type = 'TANG'
                and a.warehouse_id = :warehouseId
                and eq.equipment_id = :equipmentId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("equipmentId", equipmentId);
        params.put("startDate", fromDate);
        params.put("endDate", toDate);
        return getListData(sql, params, StkEquipmentBean.class);
    }

    public List<StkEquipmentBean> getListOutgoingShipment(Long warehouseId, Long equipmentId, Date fromDate, Date toDate) {
        String sql = """
                select
                    a.outgoing_date as pickingDate,
                    eq.quantity,  
                    a.picking_no,
                    a.approved_time,
                    'N' as isIncoming,
                    type as type,
                    (case when type = 'SU_DUNG' then 'Xuất kho sử dụng'
                        when type = 'DIEU_CHUYEN' then 'Điều chuyển'
                        else '' end) type_name
                from mat_outgoing_shipments a,
                mat_outgoing_equipments eq
                where a.is_deleted = 'N'
                and eq.is_deleted = 'N'
                and a.status_id = 'PHE_DUYET'
                and a.warehouse_id = :warehouseId
                and eq.equipment_id = :equipmentId
                and a.outgoing_shipment_id = eq.outgoing_shipment_id
                and a.outgoing_date between :startDate and :endDate
                union all 
                select
                		a.end_date as pickingDate,
                		ifnull(eq.inventory_quantity,0) - ifnull(eq.quantity,0) quantity, 
                		a.inventory_adjustment_no picking_no,
                		a.approved_time,
                		'N' as isIncoming,
                		type as type,
                		'Kiểm kê giảm hàng hóa' type_name
                from mat_inventory_adjustments a,
                mat_inventory_adjust_equipments eq
                where a.is_deleted = 'N'
                and eq.is_deleted = 'N'
                and a.status_id = 'PHE_DUYET'
                and a.inventory_adjustment_id = eq.inventory_ajustment_id
                and a.end_date BETWEEN :startDate and :endDate
                and a.type = 'GIAM'
                and a.warehouse_id = :warehouseId
                and eq.equipment_id = :equipmentId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        params.put("equipmentId", equipmentId);
        params.put("startDate", fromDate);
        params.put("endDate", toDate);
        return getListData(sql, params, StkEquipmentBean.class);
    }
}
