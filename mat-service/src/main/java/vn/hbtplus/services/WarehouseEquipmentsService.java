/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WarehouseEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehouseEquipmentsResponse;

import java.util.Date;

/**
 * Lop interface service ung voi bang stk_warehouse_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface WarehouseEquipmentsService {

    TableResponseEntity<WarehouseEquipmentsResponse> searchData(WarehouseEquipmentsRequest.SearchForm dto);

    ResponseEntity saveData(WarehouseEquipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<WarehouseEquipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(WarehouseEquipmentsRequest.SearchForm dto) throws Exception;

    void updateWarehouseEquipments(Long warehouseId, Long objectId, String type);

    void initHistory(Date periodDate);
}
