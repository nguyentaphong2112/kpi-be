/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.BookLoansRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookLoansResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang lib_book_loans
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface BookLoanService {

    TableResponseEntity<BookLoansResponse> searchData(BookLoansRequest.SearchForm dto);


    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<BookLoansResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(BookLoansRequest.SearchForm dto) throws Exception;

    boolean saveBorrowing(List<Long> ids, Long memberId) throws BaseAppException;
    boolean saveReturning(List<Long> ids) ;
}
