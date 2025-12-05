/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang hr_jobs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_jobs")
public class JobsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "note")
    private String note;

    @Column(name = "job_type")
    private String jobType;

    public interface JOB_TYPES {
        String CHUC_VU = "CHUC_VU";
        String CONG_VIEC = "CONG_VIEC";
        String HUONG_LUONG = "HUONG_LUONG";
        String VI_TRI_VIEC_LAM = "VI_TRI_VIEC_LAM";
    }

}
