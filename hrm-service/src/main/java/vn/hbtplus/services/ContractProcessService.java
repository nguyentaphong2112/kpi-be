/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ContractProcessRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_contract_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractProcessService {

    TableResponseEntity<ContractProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(ContractProcessRequest.SubmitForm dto, List<MultipartFile> files,Long employeeId,  Long id) throws BaseAppException;

    BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws RecordNotExistsException;

    BaseResponseEntity<ContractProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request);

    void autoUpdateEmpType();
}
