/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import lombok.Data;


/**
 * Lop entity ung voi bang kpi_indicator_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "kpi_indicator_masters")
public class IndicatorMastersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "indicator_master_id")
    private Long indicatorMasterId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "org_type_id")
    private Long orgTypeId;

    @Column(name = "status_id")
    private String statusId;
    @Column(name = "type")
    private String type;
    @Column(name = "manager_job_id")
    private Long managerJobId;


    public interface STATUS {
        String PHE_DUYET = "PHE_DUYET";
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String DE_NGHI_XOA = "DE_NGHI_XOA";
        String TU_CHOI_PHE_DUYET = "TU_CHOI_PHE_DUYET";
        String HET_HIEU_LUC = "HET_HIEU_LUC";
        String CHO_PHE_DUYET_HIEU_LUC_LAI = "CHO_PHE_DUYET_HIEU_LUC_LAI";
    }
    public interface TYPE {
        String ORG = "ORG";
        String EMP = "EMP";
    }

}
