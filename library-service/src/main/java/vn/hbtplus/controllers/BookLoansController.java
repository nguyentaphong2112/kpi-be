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
import vn.hbtplus.models.request.BookLoansRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookLoansResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.BookLoanService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class BookLoansController {
    private final BookLoanService bookLoanService;

    @PostMapping(value = "/v1/book-loans/borrowing/{memberId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveBorrowing(@Valid @RequestBody BookLoansRequest.SubmitForm dto,
                                        @PathVariable Long memberId) throws BaseAppException {
        return ResponseUtils.ok(bookLoanService.saveBorrowing(dto.getBookEditionDetailIds(), memberId));
    }

    @PutMapping(value = "/v1/book-loans/returning", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveReturning(@Valid @RequestBody BookLoansRequest.SubmitForm dto
                                        ) {
        return ResponseUtils.ok(bookLoanService.saveReturning(dto.getBookEditionDetailIds()));
    }

    @DeleteMapping(value = "/v1/book-loans/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return bookLoanService.deleteData(id);
    }

}
