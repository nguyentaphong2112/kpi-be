/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.tax.income.models.request.IncomeItemsRequest;
import vn.hbtplus.tax.income.models.response.IncomeItemsResponse;

import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang pit_income_items
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface IncomeItemsService {

    BaseDataTableDto<IncomeItemsResponse> searchData(IncomeItemsRequest.SearchForm dto);

    Long saveData(IncomeItemsRequest.SubmitForm dto, Long id) throws RecordNotExistsException;

    ResponseEntity deleteData(Long id) throws BaseAppException;

    BaseResponseEntity<IncomeItemsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(IncomeItemsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> downloadTemplate(Long id) throws Exception;

    List<IncomeItemsResponse> getDataBySalaryPeriod(String taxPeriodDate, String isImport);

    void autoCreateForPeriod(Date periodDate);
}
