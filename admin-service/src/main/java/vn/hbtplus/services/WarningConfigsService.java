/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.WarningConfigsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_warning_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface WarningConfigsService {

    TableResponseEntity<WarningConfigsResponse> searchData(WarningConfigsRequest.SearchForm dto);

    TableResponseEntity<Object> searchDataPopUp(WarningConfigsRequest.SearchForm dto);

    ResponseEntity saveData(WarningConfigsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<WarningConfigsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(WarningConfigsRequest.SearchForm dto) throws Exception;

    List<WarningConfigsResponse> getListWarning();
}
