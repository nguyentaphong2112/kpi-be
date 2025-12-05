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
 * Lop entity ung voi bang crm_pytago_values
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_pytago_values")
public class PytagoValuesEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "pytago_value_id")    private Long pytagoValueId;    @Column(name = "index_no")    private String indexNo;    @Column(name = "value")    private Long value;    @Column(name = "object_id")    private Long objectId;    @Column(name = "table_name")    private String tableName;

}
