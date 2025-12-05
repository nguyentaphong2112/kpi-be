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
 * Lop entity ung voi bang sys_categories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_categories")
public class CategoriesEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "category_id")    private Long categoryId;    @Column(name = "category_type")    private String categoryType;    @Column(name = "code")    private String code;    @Column(name = "name")    private String name;    @Column(name = "value")    private String value;    @Column(name = "order_number")    private Long orderNumber;    @Column(name = "note")    private String note;    @Column(name = "parent_id")    private String parentId;    @Column(name = "path_id")    private String pathId;    @Column(name = "path_level")    private Long pathLevel;    @Column(name = "path_order")    private String pathOrder;

}
