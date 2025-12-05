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


/**
 * Lop entity ung voi bang lib_book_loans
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "lib_book_loans")
public class BookLoanEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "book_loan_id")
    private Long bookLoanId;

    @Column(name = "book_edition_detail_id")
    private Long bookEditionDetailId;
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "borrowed_date")
    private Date borrowedDate;
    @Column(name = "return_date")
    private Date returnDate;

}
