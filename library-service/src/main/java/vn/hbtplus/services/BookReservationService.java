/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.BookReservationsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookReservationsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang lib_book_reservations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface BookReservationService {

    TableResponseEntity<BookReservationsResponse> searchData(BookReservationsRequest.SearchForm dto);

    ResponseEntity saveData(BookReservationsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<BookReservationsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(BookReservationsRequest.SearchForm dto) throws Exception;

}
