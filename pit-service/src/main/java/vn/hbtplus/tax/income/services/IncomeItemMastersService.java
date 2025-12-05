/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.tax.income.models.request.IncomeItemMastersRequest;
import vn.hbtplus.tax.income.models.response.IncomeItemMastersResponse;

import java.util.Date;

/**
 * Lop interface service ung voi bang pit_income_item_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface IncomeItemMastersService {

    BaseDataTableDto<IncomeItemMastersResponse> searchData(IncomeItemMastersRequest.SearchForm dto);

    ResponseEntity saveData(IncomeItemMastersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws BaseAppException;

    BaseResponseEntity<IncomeItemMastersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(IncomeItemMastersRequest.SearchForm dto) throws Exception;

    Long importIncome(MultipartFile fileImport, Long incomeItemId, Date stringToDate, String isCalculated) throws Exception;

    void calculateTax(Long id) throws  Exception;

    void lockPeriodById(Long id) throws BaseAppException;

    void unLockPeriodById(Long id) throws BaseAppException;

    ResponseEntity<Object> exportDetailIncomeById(Long incomeItemMasterId, Integer isPreview) throws Exception;

    void undoCalculateTax(Long id) throws BaseAppException;
}
