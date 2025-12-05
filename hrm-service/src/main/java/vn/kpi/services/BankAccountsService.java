/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.BankAccountsRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BankAccountsResponse;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang hr_bank_accounts
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface BankAccountsService {

    TableResponseEntity<BankAccountsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(BankAccountsRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<BankAccountsResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto<BankAccountsResponse.SearchResult> getBankAccounts(Long id, BaseSearchRequest request);

    ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate) throws Exception;

    ResponseEntity<Object> downloadImportTemplate() throws Exception;

}
