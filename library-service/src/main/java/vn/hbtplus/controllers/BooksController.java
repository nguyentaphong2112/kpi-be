/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.TableResponse;
import vn.hbtplus.models.request.BooksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BooksResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.BookService;
import vn.hbtplus.utils.ResponseUtils;

import java.io.IOException;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class BooksController {
    private final BookService bookService;

    @GetMapping(value = "/v1/books/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BooksResponse.SearchResult> searchData(BooksRequest.SearchForm dto) throws Exception {
        return bookService.searchData(dto);
    }

    @GetMapping(value = "/v1/books/search-author", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BooksResponse.AuthorResult> searchAuthor(BooksRequest.SearchForm dto) {
        return ResponseUtils.ok(bookService.searchAuthor(dto));
    }

    @GetMapping(value = "/v1/books/search-translator", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BooksResponse.TranslatorResult> searchTranslator(BooksRequest.SearchForm dto) {
        return ResponseUtils.ok(bookService.searchTranslator(dto));
    }

    @GetMapping(value = "/v1/books/search-genre", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BooksResponse.GenreResult> searchGenre(BooksRequest.SearchForm dto) {
        return ResponseUtils.ok(bookService.searchGenre(dto));
    }

    @GetMapping(value = "/v1/books/avatar/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getAvatar(@PathVariable Long bookId) {
        return bookService.getAvatar(bookId);
    }

    @GetMapping(value = "/v1/books/file/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getBookFile(@PathVariable Long bookId) throws IOException {
        return bookService.getBookFile(bookId);
    }

    @GetMapping(value = "/v1/books/download/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getByFileId(@PathVariable Long fileId) throws IOException {
        return bookService.getByFileId(fileId);
    }

    @PostMapping(value = "/v1/books", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@RequestPart(value = "data") BooksRequest.SubmitForm dto,
                                   @RequestPart(value = "fileAvatar", required = false) MultipartFile fileAvatar,
                                   @RequestPart(value = "fileContent", required = false) MultipartFile fileContent) throws Exception {
        return bookService.saveData(dto, fileAvatar, fileContent, null);
    }

    @PutMapping(value = "/v1/books/{bookId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@RequestPart(value = "data") BooksRequest.SubmitForm dto,
                                   @RequestPart(value = "fileAvatar", required = false) MultipartFile fileAvatar,
                                   @RequestPart(value = "fileContent", required = false) MultipartFile fileContent,
                                   @PathVariable Long bookId
    ) throws Exception {
        return bookService.saveData(dto, fileAvatar, fileContent, bookId);
    }

    @DeleteMapping(value = "/v1/books/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return bookService.deleteData(id);
    }

    @GetMapping(value = "/v1/books/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<BooksResponse.DetailResult> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return bookService.getDataById(id);
    }

    @GetMapping(value = "/v1/books/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(BooksRequest.SearchForm dto) throws Exception {
        return bookService.exportData(dto);
    }

}
