/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ContractTypesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_contract_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractTypesService {

    TableResponseEntity<ContractTypesResponse.SearchResult> searchData(ContractTypesRequest.SearchForm dto);

    ResponseEntity saveData(ContractTypesRequest.SubmitForm dto, Long contractTypeId) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ContractTypesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ContractTypesRequest.SearchForm dto) throws Exception;

    List<ContractTypesResponse.DetailBean> getListData(String classifyCode, Long empTypeId, boolean isGetAttribute);
}
