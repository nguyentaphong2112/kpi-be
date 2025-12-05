/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.bean.WarehouseNotifyBean;
import vn.hbtplus.models.request.WarehousesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehousesResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang stk_warehouses
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface WarehousesService {

    ResponseEntity searchData(WarehousesRequest.SearchForm dto);

    ResponseEntity searchList(WarehousesRequest.SearchForm dto);

    ResponseEntity saveData(WarehousesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<WarehousesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(WarehousesRequest.SearchForm dto) throws Exception;

    ResponseEntity lockOrUnlockWarehouse(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmployeesResponse> getEmpByCode(String code) throws RecordNotExistsException;

    void sendNotification(List<WarehouseNotifyBean> notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES sendToApproveImport);
}
