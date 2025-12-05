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
 * Lop entity ung voi bang icn_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "icn_parameters")
public class ParametersEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "parameter_id")    private Long parameterId;    @Column(name = "start_date")    @Temporal(javax.persistence.TemporalType.DATE)    private Date startDate;    @Column(name = "end_date")    @Temporal(javax.persistence.TemporalType.DATE)    private Date endDate;    @Column(name = "config_group")    private String configGroup;    @Column(name = "config_code")    private String configCode;    @Column(name = "config_name")    private String configName;    @Column(name = "data_type")    private String dataType;    @Column(name = "config_value")    private String configValue;

}
