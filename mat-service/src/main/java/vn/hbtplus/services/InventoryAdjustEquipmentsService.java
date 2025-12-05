/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.InventoryAdjustEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.InventoryAdjustEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang stk_inventory_adjust_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface InventoryAdjustEquipmentsService {

    TableResponseEntity<InventoryAdjustEquipmentsResponse> searchData(InventoryAdjustEquipmentsRequest.SearchForm dto);

    ResponseEntity saveData(InventoryAdjustEquipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<InventoryAdjustEquipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(InventoryAdjustEquipmentsRequest.SearchForm dto) throws Exception;

}
