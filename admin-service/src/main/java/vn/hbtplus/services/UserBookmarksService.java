/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.UserBookmarksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.UserBookmarksResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_user_bookmarks
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface UserBookmarksService {

    TableResponseEntity<UserBookmarksResponse> searchData(UserBookmarksRequest.SearchForm dto);

    ResponseEntity saveData(UserBookmarksRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<UserBookmarksResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(UserBookmarksRequest.SearchForm dto) throws Exception;

    List<UserBookmarksResponse.DetailBean> getDataByUser(String userName, String bookmarkType);
}
