/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang abs_requests
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "abs_requests")
public class RequestsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "status")
    private String status;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "reason_type_id")
    private Long reasonTypeId;

    @Column(name = "note")
    private String note;

    @Column(name = "reason")
    private String reason;

    @Column(name = "request_no")
    private String requestNo;

    public interface STATUS {
        String DU_THAO = "DU_THAO";
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String DA_PHE_DUYET = "DA_PHE_DUYET";
        String DA_HUY = "DA_HUY";
        String DA_TU_CHOI = "DA_TU_CHOI";
    }

}
