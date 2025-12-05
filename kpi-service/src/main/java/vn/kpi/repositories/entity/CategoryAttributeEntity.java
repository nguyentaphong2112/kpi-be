/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang sys_category_attributes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_category_attributes")
public class CategoryAttributeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "category_attribute_id")
    private Long categoryAttributeId;

    @Column(name = "attribute_code")
    private String attributeCode;

    @Column(name = "attribute_value")
    private String attributeValue;
    @Column(name = "data_type")
    private String dataType;

    @Column(name = "category_id")
    private Long categoryId;


}
