/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ReasonTypesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang abs_reason_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ReasonTypesService {

    TableResponseEntity<ReasonTypesResponse> searchData(ReasonTypesRequest.SearchForm dto);

    BaseResponseEntity saveData(ReasonTypesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ReasonTypesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ReasonTypesRequest.SearchForm dto) throws Exception;

    List<ReasonTypesResponse> getAllReasonLeaves();

    ListResponseEntity<ReasonTypesResponse> getList(boolean isGetAttributes) throws RecordNotExistsException;


}
