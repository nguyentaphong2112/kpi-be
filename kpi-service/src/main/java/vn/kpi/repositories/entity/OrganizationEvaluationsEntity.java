/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.*;

import lombok.Data;

import java.util.Date;


/**
 * Lop entity ung voi bang kpi_organization_evaluations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "kpi_organization_evaluations")
public class OrganizationEvaluationsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "organization_evaluation_id")
    private Long organizationEvaluationId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "evaluation_period_id")
    private Long evaluationPeriodId;

    @Column(name = "status")
    private String status;

    @Column(name = "emp_manager_id")
    private Long empManagerId;

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

    @Column(name = "reason_request")
    private String reasonRequest;

    @Column(name = "reason_manage_request")
    private String reasonManageRequest;

    @Transient
    private String empManagerName;

    @Transient
    private String empManagerCode;

    @Transient
    private String orgName;

    @Transient
    private String orgLevelManage;

    public interface STATUS {
        String CHO_XET_DUYET = "CHO_XET_DUYET";
        String DU_THAO = "DU_THAO";
        String KHOI_TAO = "KHOI_TAO";
        String TU_CHOI_XET_DUYET = "TU_CHOI_XET_DUYET";
        String TU_CHOI_PHE_DUYET = "TU_CHOI_PHE_DUYET";
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String PHE_DUYET = "PHE_DUYET";
        String DANH_GIA = "DANH_GIA";
        String QLTT_DANH_GIA = "QLTT_DANH_GIA";
    }
}
