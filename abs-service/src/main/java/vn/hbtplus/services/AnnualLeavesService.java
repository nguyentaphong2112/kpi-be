/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.AnnualLeavesRequest;

import javax.validation.Valid;
import java.util.List;

/**
 * Lop interface service ung voi bang abs_annual_leaves
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface AnnualLeavesService {

    TableResponseEntity<AnnualLeavesResponse.SearchResult> searchData(AnnualLeavesRequest.SearchForm dto);

    ResponseEntity saveData(AnnualLeavesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<AnnualLeavesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(AnnualLeavesRequest.SearchForm dto) throws Exception;

    ResponseEntity calculate(Integer year, List<Long> empIds);
}
