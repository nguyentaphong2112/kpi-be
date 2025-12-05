/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;
import org.springframework.data.domain.AuditorAware;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Lop entity ung voi bang sys_dynamic_report_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_dynamic_report_parameters")
@EntityListeners(AuditorAware.class)
public class DynamicReportParametersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "dynamic_report_parameter_id")
    private Long dynamicReportParameterId;

    @Column(name = "dynamic_report_id")
    private Long dynamicReportId;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "append_query")
    private String appendQuery;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "url_api")
    private String urlApi;

    @Column(name = "name")
    private String name;

    @Column(name = "title")
    private String title;

    @Column(name = "is_required")
    private String isRequired;
}
