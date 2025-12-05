/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang lms_research_project_members
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lms_research_project_members")
public class ResearchProjectMembersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "research_project_member_id")
    private Long researchProjectMemberId;

    @Column(name = "research_project_id")
    private Long researchProjectId;

    @Column(name = "role_id")
    private String roleId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "note")
    private String note;

    @Column(name = "type")
    private String type;

    public interface TYPES {
        String THAM_GIA_DE_TAI = "THAM_GIA_DE_TAI";
        String PHE_DUYET_DE_TAI = "PHE_DUYET_DE_TAI";
        String CHO_PHEP_THUC_HIEN = "CHO_PHEP_THUC_HIEN";
        String NGHIEM_THU_DE_TAI = "NGHIEM_THU_DE_TAI";
        String DANH_GIA_DE_TAI = "DANH_GIA_DE_TAI";
    }

}
