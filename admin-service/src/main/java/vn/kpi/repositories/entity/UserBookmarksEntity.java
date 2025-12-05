/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Lop entity ung voi bang sys_user_bookmarks
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_user_bookmarks")
public class UserBookmarksEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_bookmark_id")
    private Long userBookmarkId;

    @Column(name = "login_name")
    private String loginName;

    @Column(name = "name")
    private String name;

    @Column(name = "bookmark_type")
    private String bookmarkType;

    @Column(name = "options")
    private String options;


}
