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
 * Lop entity ung voi bang fpn_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "mat_equipments")
public class EquipmentsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "name")
    private String name;

    @Column(name = "equipment_group_id")
    private String equipmentGroupId;

    @Column(name = "equipment_type_id")
    private String equipmentTypeId;

    @Column(name = "equipment_unit_id")
    private String equipmentUnitId;

    @Column(name = "warning_days")
    private Long warningDays;

    @Column(name = "is_serial_checking")
    private String isSerialChecking;

    @Column(name = "serial_no")
    private String serialNo;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "note")
    private String note;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "location")
    private String location;

}
