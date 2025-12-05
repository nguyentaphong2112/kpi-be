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
 * Lop entity ung voi bang hr_award_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_award_process")
public class AwardProcessEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "award_process_id")    private Long awardProcessId;    @Column(name = "employee_id")    private Long employeeId;    @Column(name = "award_form_id")    private String awardFormId;    @Column(name = "award_year")    private Long awardYear;    @Column(name = "document_no")    private String documentNo;    @Column(name = "document_signed_date")    @Temporal(javax.persistence.TemporalType.DATE)    private Date documentSignedDate;

}
