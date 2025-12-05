/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang PNS_SQL_CONFIGS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pns_sql_configs")
public class SqlConfigsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "sql_config_id")
    private Long sqlConfigId;

    @Column(name = "name")
    private String name;

    @Column(name = "contract_type_ids")
    private String contractTypeIds;

    @Column(name = "sql")
    private String sql;

}
