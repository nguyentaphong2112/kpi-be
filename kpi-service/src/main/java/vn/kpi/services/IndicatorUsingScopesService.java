/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.IndicatorUsingScopesRequest;

/**
 * Lop interface service ung voi bang kpi_indicator_using_scopes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface IndicatorUsingScopesService {

    TableResponseEntity<IndicatorUsingScopesResponse> searchData(IndicatorUsingScopesRequest.SearchForm dto);

    ResponseEntity saveData(IndicatorUsingScopesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<IndicatorUsingScopesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(IndicatorUsingScopesRequest.SearchForm dto) throws Exception;

}
