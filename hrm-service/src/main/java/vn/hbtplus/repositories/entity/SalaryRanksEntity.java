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
 * Lop entity ung voi bang hr_salary_ranks
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_salary_ranks")
public class SalaryRanksEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "salary_rank_id")    private Long salaryRankId;    @Column(name = "code")    private String code;    @Column(name = "name")    private String name;    @Column(name = "salary_type")    private String salaryType;    @Column(name = "order_number")    private Long orderNumber;    @Column(name = "start_date")    @Temporal(javax.persistence.TemporalType.DATE)    private Date startDate;    @Column(name = "end_date")    @Temporal(javax.persistence.TemporalType.DATE)    private Date endDate;

}
