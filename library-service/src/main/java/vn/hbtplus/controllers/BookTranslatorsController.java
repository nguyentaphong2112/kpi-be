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
import vn.hbtplus.models.request.BookTranslatorsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookTranslatorsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.BookTranslatorService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class BookTranslatorsController {
    private final BookTranslatorService bookTranslatorService;

    @GetMapping(value = "/v1/book-translators", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BookTranslatorsResponse> searchData(BookTranslatorsRequest.SearchForm dto) {
        return bookTranslatorService.searchData(dto);
    }

    @PostMapping(value = "/v1/book-translators", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  BookTranslatorsRequest.SubmitForm dto) throws BaseAppException {
        return bookTranslatorService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/book-translators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return bookTranslatorService.deleteData(id);
    }

    @GetMapping(value = "/v1/book-translators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<BookTranslatorsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return bookTranslatorService.getDataById(id);
    }

    @GetMapping(value = "/v1/book-translators/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(BookTranslatorsRequest.SearchForm dto) throws Exception {
        return bookTranslatorService.exportData(dto);
    }

}
