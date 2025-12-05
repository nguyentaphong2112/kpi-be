/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.WarningConfigsRequest;

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
