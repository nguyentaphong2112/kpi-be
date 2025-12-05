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


/**
 * Lop entity ung voi bang lib_books
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lib_books")
public class BookEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "title")
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "genre_id")
    private Long genreId;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "summary")
    private String summary;

    @Column(name = "language_id")
    private String languageId;

    @Column(name = "tags")
    private String tags;

    @Column(name = "type")
    private String type;

    @Column(name = "table_of_contents")
    private String tableOfContents;

}
