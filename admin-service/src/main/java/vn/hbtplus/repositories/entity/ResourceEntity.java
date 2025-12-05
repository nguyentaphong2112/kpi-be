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
 * Lop entity ung voi bang sys_resources
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_resources")
public class ResourceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;
    @Column(name = "icon")
    private String icon;
    @Column(name = "status")
    private String status;

    @Column(name = "url")
    private String url;

    @Column(name = "is_menu")
    private String isMenu;
    @Column(name = "path_id")
    private String pathId;
    @Column(name = "path_level")
    private Integer pathLevel;
    @Column(name = "order_number")
    private Long orderNumber;

    public interface STATUS {
        String ACTIVE = "ACTIVE";
        String INACTIVE = "INACTIVE";
    }
}
