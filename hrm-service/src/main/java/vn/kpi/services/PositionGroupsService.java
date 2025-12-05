/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.PositionGroupsRequest;

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
