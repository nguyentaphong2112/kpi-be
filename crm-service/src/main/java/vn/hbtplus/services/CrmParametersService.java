/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ParametersRequest;

import java.util.Date;

/**
 * Lop interface service ung voi bang icn_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CrmParametersService {

    TableResponseEntity<ParametersResponse> searchData(ParametersRequest.SearchForm dto);

    ResponseEntity saveData(ParametersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ParametersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ParametersRequest.SearchForm dto) throws Exception;

    <T> T getConfig(Class<T> className, Date date) throws InstantiationException, IllegalAccessException;

    <T> T getConfigValue(String configCode, Date date, Class<T> className);

}
