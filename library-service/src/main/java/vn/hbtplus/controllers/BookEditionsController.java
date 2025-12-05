/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.BookEditionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookEditionsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.BookEditionService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class BookEditionsController {
    private final BookEditionService bookEditionsService;

    @GetMapping(value = "/v1/book-editions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BookEditionsResponse> searchData(BookEditionsRequest.SearchForm dto) {
        return bookEditionsService.searchData(dto);
    }

    @PostMapping(value = "/v1/book-editions", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  BookEditionsRequest.SubmitForm dto) throws BaseAppException {
        return bookEditionsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/book-editions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return bookEditionsService.deleteData(id);
    }

    @GetMapping(value = "/v1/book-editions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<BookEditionsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return bookEditionsService.getDataById(id);
    }

    @GetMapping(value = "/v1/book-editions/list/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<BookEditionsResponse.ChooseBookEdition> getListEditions(@PathVariable Long bookId)  throws RecordNotExistsException {
        return ResponseUtils.ok(bookEditionsService.getListEditions(bookId));
    }

    @GetMapping(value = "/v1/book-editions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(BookEditionsRequest.SearchForm dto) throws Exception {
        return bookEditionsService.exportData(dto);
    }

}
