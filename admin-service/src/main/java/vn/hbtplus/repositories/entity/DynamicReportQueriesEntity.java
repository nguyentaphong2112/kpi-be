/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

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
 * Lop entity ung voi bang sys_dynamic_report_queries
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_dynamic_report_queries")
@EntityListeners(AuditorAware.class)
public class DynamicReportQueriesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "dynamic_report_query_id")
    private Long dynamicReportQueryId;

    @Column(name = "dynamic_report_id")
    private Long dynamicReportId;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "sql_query")
    private String sqlQuery;


}
