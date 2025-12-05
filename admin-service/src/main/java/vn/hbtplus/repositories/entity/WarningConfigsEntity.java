/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Lop entity ung voi bang sys_warning_configs
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "sys_warning_configs")
public class WarningConfigsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "warning_config_id")
    private Long warningConfigId;

    @Column(name = "title")
    private String title;

    @Column(name = "resource")
    private String resource;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "icon")
    private String icon;

    @Column(name = "api_uri")
    private String apiUri;

    @Column(name = "url_view_detail")
    private String urlViewDetail;

    @Column(name = "sql_query")
    private String sqlQuery;

    @Column(name = "is_must_positive")
    private String isMustPositive;

    @Column(name = "order_number")
    private Long orderNumber;


}
