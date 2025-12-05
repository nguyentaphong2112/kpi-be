/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.*;

import lombok.Data;

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
@Table(name = "kpi_employee_evaluations")
public class EmployeeEvaluationsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "employee_evaluation_id")
    private Long employeeEvaluationId;

    @Column(name = "evaluation_period_id")
    private Long evaluationPeriodId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "org_concurrent_ids")
    private String orgConcurrentIds;

    @Column(name = "status")
    private String status;

    @Column(name = "adjust_reason")
    private String adjustReason;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedTime;

    @Column(name = "self_total_point")
    private Double selfTotalPoint;

    @Column(name = "manager_total_point")
    private Double managerTotalPoint;

    @Column(name = "final_point")
    private Double finalPoint;

    @Column(name = "result_id")
    private String resultId;

    @Column(name = "final_result_id")
    private String finalResultId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "reason_request")
    private String reasonRequest;

    @Column(name = "reason_manage_request")
    private String reasonManageRequest;

    @Transient
    private String employeeName;


    public interface STATUS {
        String CHO_XET_DUYET = "CHO_XET_DUYET";
        String DU_THAO = "DU_THAO";
        String KHOI_TAO = "KHOI_TAO";
        String TU_CHOI_XET_DUYET = "TU_CHOI_XET_DUYET";
        String TU_CHOI_PHE_DUYET = "TU_CHOI_PHE_DUYET";
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String PHE_DUYET = "PHE_DUYET";
    }

}
