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
 * Lop entity ung voi bang lib_book_editions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lib_book_editions")
public class BookEditionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "book_edition_id")
    private Long bookEditionId;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "published_year")
    private Long publishedYear;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "total_pages")
    private Long totalPages;

    @Column(name = "book_format_id")
    private String bookFormatId;

    @Column(name = "publisher_id")
    private String publisherId;


}
