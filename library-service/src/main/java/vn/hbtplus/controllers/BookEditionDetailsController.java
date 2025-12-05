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
import vn.hbtplus.models.request.BookEditionDetailsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookEditionDetailsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.BookEditionDetailService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class BookEditionDetailsController {
    private final BookEditionDetailService bookEditionDetailsService;

    @PostMapping(value = "/v1/book-edition-details", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody BookEditionDetailsRequest.SubmitForm dto) throws BaseAppException {
        return ResponseUtils.ok(bookEditionDetailsService.saveData(dto.getBookEditionId(),dto.getBookNos()));
    }

    @DeleteMapping(value = "/v1/book-edition-details/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return bookEditionDetailsService.deleteData(id);
    }

    @GetMapping(value = "/v1/book-edition-details/get-info/{bookNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<BookEditionDetailsResponse.EditionDetail> getDataById(@PathVariable String bookNo) throws BaseAppException {
        return ResponseUtils.ok(bookEditionDetailsService.getDataByBookNo(bookNo));
    }

    @GetMapping(value = "/v1/book-edition-details/increment-book-no/{total}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity incrementBookNo(@PathVariable Integer total) throws Exception {
        return ResponseUtils.ok(bookEditionDetailsService.incrementBookNo(total), "DS_Ma_sach.xlsx");
    }

    @GetMapping(value = "/v1/book-edition-details/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<BookEditionDetailsResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return bookEditionDetailsService.getDataById(id);
    }

    @GetMapping(value = "/v1/book-edition-details/edition", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BookEditionDetailsResponse.EditionDetail> getDataByEditionId(BookEditionDetailsRequest.SearchForm dto) throws RecordNotExistsException {
        return bookEditionDetailsService.getDataByEditionId(dto);
    }
}
