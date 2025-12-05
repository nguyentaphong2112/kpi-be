/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.BookTranslatorsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookTranslatorsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang lib_book_translators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface BookTranslatorService {

    TableResponseEntity<BookTranslatorsResponse> searchData(BookTranslatorsRequest.SearchForm dto);

    ResponseEntity saveData(BookTranslatorsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<BookTranslatorsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(BookTranslatorsRequest.SearchForm dto) throws Exception;

}
