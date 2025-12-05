/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.PositionGroupsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_position_groups
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface PositionGroupsService {

    TableResponseEntity<PositionGroupsResponse> searchData(PositionGroupsRequest.SearchForm dto);

    ResponseEntity saveData(PositionGroupsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<PositionGroupsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(PositionGroupsRequest.SearchForm dto) throws Exception;

    ListResponseEntity<PositionGroupsResponse.DetailBean> getListData();
}
