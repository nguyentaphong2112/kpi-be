/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang icn_employee_changes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "icn_employee_changes")
public class EmployeeChangesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "employee_change_id")
    private Long employeeChangeId;

    @Column(name = "period_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date periodDate;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "change_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date changeDate;

    @Column(name = "change_type")
    private String changeType;

    @Column(name = "contribution_type")
    private String contributionType;

    @Column(name = "reason")
    private String reason;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "status")
    private String status;


    public interface CONTRIBUTION_TYPES {
        String KO_TRICH_NOP = "KO_TRICH_NOP";
        String TRICH_NOP = "TRICH_NOP";
        String CHECK_CONG = "CHECK_CONG";

    }
    public interface STATUS {
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String PHE_DUYET = "PHE_DUYET";
    }

}
