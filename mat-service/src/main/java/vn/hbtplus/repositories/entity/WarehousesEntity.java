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
 * Lop entity ung voi bang stk_warehouses
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "mat_warehouses")
public class WarehousesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "address")
    private String address;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "type")
    private String type;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "note")
    private String note;

    @Column(name = "path_id")
    private String pathId;

    @Column(name = "path_order")
    private String pathOrder;

    @Column(name = "path_level")
    private Integer pathLevel;

    @Column(name = "order_number")
    private Integer orderNumber;

}
