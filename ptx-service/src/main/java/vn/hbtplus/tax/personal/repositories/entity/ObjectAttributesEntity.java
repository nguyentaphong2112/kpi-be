/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;


/**
 * Lop entity ung voi bang hr_object_attributes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "med_object_attributes")
public class ObjectAttributesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "object_attribute_id")
    private Long objectAttributeId;

    @Column(name = "attribute_code")
    private String attributeCode;

    @Column(name = "attribute_value")
    private String attributeValue;

    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "data_type")
    private String dataType;

}
