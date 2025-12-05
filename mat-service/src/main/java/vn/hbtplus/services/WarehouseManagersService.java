/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WarehouseManagersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehouseManagersResponse;

/**
 * Lop interface service ung voi bang stk_warehouse_managers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface WarehouseManagersService {

    TableResponseEntity<WarehouseManagersResponse> searchData(WarehouseManagersRequest.SearchForm dto);

    ResponseEntity saveData(WarehouseManagersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<WarehouseManagersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(WarehouseManagersRequest.SearchForm dto) throws Exception;

}
