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


/**
 * Lop entity ung voi bang lib_book_edition_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lib_book_edition_details")
public class BookEditionDetailEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "book_edition_detail_id")
    private Long bookEditionDetailId;

    @Column(name = "book_edition_id")
    private Long bookEditionId;

    @Column(name = "book_no")
    private String bookNo;

    @Column(name = "status")
    private String status;

    @Column(name = "note")
    private String note;

    public interface STATUS {
        String HIEN_CO ="HIEN_CO";
        String DANG_MUON ="DANG_MUON";
        String DA_MAT ="DA_MAT";
    }
}
