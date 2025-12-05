/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.*;

import lombok.Data;


/**
 * Lop entity ung voi bang kpi_organization_indicators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "kpi_organization_indicators")
public class OrganizationIndicatorsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "organization_indicator_id")
    private Long organizationIndicatorId;

    @Column(name = "indicator_conversion_id")
    private Long indicatorConversionId;

    @Column(name = "indicator_id")
    private Long indicatorId;

    @Column(name = "organization_evaluation_id")
    private Long organizationEvaluationId;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "target")
    private String target;

    @Column(name = "status")
    private String status;

    @Column(name = "old_percent")
    private Double oldPercent;

    @Column(name = "result")
    private String result;

    @Column(name = "result_manage")
    private String resultManage;

    @Column(name = "leader_ids")
    private String leaderIds;

    @Column(name = "collaborator_ids")
    private String collaboratorIds;

    @Column(name = "assignment_note")
    private String assignmentNote;

    @Column(name = "leader_type")
    private String leaderType;

    @Column(name = "collaborator_type")
    private String collaboratorType;

    @Column(name = "self_point")
    private Long selfPoint;

    @Column(name = "manage_point")
    private Long managePoint;

    @Column(name = "status_level1")
    private String statusLevel1;

    @Transient
    private String unitName;


    public interface STATUS {
        String CHO_XAC_NHAN = "CHO_XAC_NHAN";
        String DU_THAO = "DU_THAO";
        String XAC_NHAN = "XAC_NHAN";
        String YEU_CAU_NHAP_LAI = "YEU_CAU_NHAP_LAI";
    }
}
