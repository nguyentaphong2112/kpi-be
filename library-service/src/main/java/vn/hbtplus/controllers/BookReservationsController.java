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
import vn.hbtplus.models.request.BookReservationsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookReservationsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.BookReservationService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class BookReservationsController {
    private final BookReservationService bookReservationService;

    @GetMapping(value = "/v1/book-reservations", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BookReservationsResponse> searchData(BookReservationsRequest.SearchForm dto) {
        return bookReservationService.searchData(dto);
    }

    @PostMapping(value = "/v1/book-reservations", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  BookReservationsRequest.SubmitForm dto) throws BaseAppException {
        return bookReservationService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/book-reservations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return bookReservationService.deleteData(id);
    }

    @GetMapping(value = "/v1/book-reservations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<BookReservationsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return bookReservationService.getDataById(id);
    }

    @GetMapping(value = "/v1/book-reservations/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(BookReservationsRequest.SearchForm dto) throws Exception {
        return bookReservationService.exportData(dto);
    }

}
