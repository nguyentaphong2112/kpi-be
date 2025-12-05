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
import javax.persistence.TemporalType;
import java.util.Date;


/**
 * Lop entity ung voi bang kpi_employee_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "kpi_approval_histories")
public class ApprovalHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "approval_history_id")
    private Long approvalHistoryId;
    @Column(name = "object_id")
    private Long objectId;
    @Column(name = "table_name")
    private String tableName;
    @Column(name = "approval_by")
    private String approvalBy;

    @Column(name = "approval_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvalTime;
    @Column(name = "comments")
    private String comments;
    @Column(name = "approval_level")
    private Long approvalLevel;
    @Column(name = "status")
    private String status;

    public interface STATUS {
        String WAITING = "WAITING";
        String OK = "OK";
        String NOT_OK = "NOT_OK";
    }
    public interface TABLE_NAMES {
        String KPI_EMPLOYEE_EVALUATIONS = "kpi_employee_evaluations";
        String KPI_ORGANIZATION_EVALUATIONS = "kpi_organization_evaluations";
    }
}
