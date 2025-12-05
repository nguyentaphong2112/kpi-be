/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.BooksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BooksResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.io.IOException;

/**
 * Lop interface service ung voi bang lib_books
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface BookService {

    TableResponseEntity<BooksResponse.SearchResult> searchData(BooksRequest.SearchForm dto) throws Exception;

    ResponseEntity saveData(BooksRequest.SubmitForm dto, MultipartFile fileAvatar,
                            MultipartFile fileContent, Long bookId) throws Exception;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<BooksResponse.DetailResult> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(BooksRequest.SearchForm dto) throws Exception;

    BaseDataTableDto searchAuthor(BooksRequest.SearchForm dto);

    BaseDataTableDto searchTranslator(BooksRequest.SearchForm dto);

    BaseDataTableDto searchGenre(BooksRequest.SearchForm dto);

    ResponseEntity getAvatar(Long bookId);

    ResponseEntity getBookFile(Long bookId) throws IOException;

    ResponseEntity getByFileId(Long bookId) throws IOException;
}
