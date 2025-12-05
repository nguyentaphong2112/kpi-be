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
 * Lop entity ung voi bang stk_warehouse_managers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "mat_warehouse_managers")
public class WarehouseManagersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "warehouse_manager_id")
    private Long warehouseManagerId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "role_id")
    private String roleId;

    @Column(name = "has_approve_import")
    private String hasApproveImport;

    @Column(name = "has_approve_export")
    private String hasApproveExport;

    @Column(name = "has_approve_transfer")
    private String hasApproveTransfer;

    @Column(name = "has_approve_adjustment")
    private String hasApproveAdjustment;


}
