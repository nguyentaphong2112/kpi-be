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
 * Lop entity ung voi bang sys_config_mappings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_config_mappings")
public class ConfigMappingsEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "config_mapping_id")    private Long configMappingId;    @Column(name = "code")    private String code;    @Column(name = "name")    private String name;    @Column(name = "parameter_title")    private String parameterTitle;    @Column(name = "value_title")    private String valueTitle;    @Column(name = "data_type")    private String dataType;

}
