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


/**
 * Lop entity ung voi bang stk_incoming_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "mat_incoming_equipments")
public class IncomingEquipmentsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "incoming_equipment_id")
    private Long incomingEquipmentId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "incoming_shipment_id")
    private Long incomingShipmentId;


}
