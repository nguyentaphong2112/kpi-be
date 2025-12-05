/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;


/**
 * Lop entity ung voi bang stk_warehouse_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "mat_warehouse_equipments")
public class WarehouseEquipmentsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "warehouse_equipment_id")
    private Long warehouseEquipmentId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "update_price_time")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date updatePriceTime;


}
