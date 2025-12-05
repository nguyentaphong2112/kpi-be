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
import vn.hbtplus.models.request.FavouriteBooksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.FavouriteBooksResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.FavouriteBookService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class FavouriteBooksController {
    private final FavouriteBookService favouriteBookService;

    @PostMapping(value = "/v1/favourite-books/like/{bookId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity likeBook(@PathVariable Long bookId) throws BaseAppException {
        return ResponseEntity.ok(favouriteBookService.likeBook(bookId));
    }

    @PutMapping(value = "/v1/favourite-books/unlike/{bookId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity unlikeBook(@PathVariable Long bookId) throws BaseAppException {
        return ResponseEntity.ok(favouriteBookService.unlikeBook(bookId));
    }


}
