/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.UserBookmarksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.UserBookmarksResponse;
import vn.hbtplus.services.UserBookmarksService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.USER_BOOKMARKS)
public class UserBookmarksController {
    private final UserBookmarksService userBookmarksService;

    @GetMapping(value = "/v1/user-bookmarks", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<UserBookmarksResponse> searchData(UserBookmarksRequest.SearchForm dto) {
        return userBookmarksService.searchData(dto);
    }

    @PostMapping(value = "/v1/user-bookmarks", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity saveData(@RequestBody @Valid UserBookmarksRequest.SubmitForm dto) throws BaseAppException {
        return userBookmarksService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/user-bookmarks/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity updateData(@RequestBody @Valid UserBookmarksRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return userBookmarksService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/user-bookmarks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteData(@RequestBody @PathVariable Long id) throws RecordNotExistsException {
        return userBookmarksService.deleteData(id);
    }

    @GetMapping(value = "/v1/user-bookmarks/by-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<UserBookmarksResponse.DetailBean> getDataByUser(@RequestParam String bookmarkType) throws RecordNotExistsException {
        String userName = Utils.getUserNameLogin();
        return ResponseUtils.ok(userBookmarksService.getDataByUser(userName, bookmarkType));
    }

    @GetMapping(value = "/v1/user-bookmarks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<UserBookmarksResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return userBookmarksService.getDataById(id);
    }

    @GetMapping(value = "/v1/user-bookmarks/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(UserBookmarksRequest.SearchForm dto) throws Exception {
        return userBookmarksService.exportData(dto);
    }

}
