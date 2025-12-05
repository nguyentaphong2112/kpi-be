/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.BookEditionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookEditionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang lib_book_editions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface BookEditionService {

    TableResponseEntity<BookEditionsResponse> searchData(BookEditionsRequest.SearchForm dto);

    ResponseEntity saveData(BookEditionsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<BookEditionsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(BookEditionsRequest.SearchForm dto) throws Exception;

    List<BookEditionsResponse.ChooseBookEdition> getListEditions(Long bookId);
}
