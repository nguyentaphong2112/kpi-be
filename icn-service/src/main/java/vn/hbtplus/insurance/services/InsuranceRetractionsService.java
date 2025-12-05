/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.insurance.models.request.InsuranceRetractionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.InsuranceRetractionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang icn_insurance_retractions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface InsuranceRetractionsService {

    TableResponseEntity<InsuranceRetractionsResponse> searchData(InsuranceRetractionsRequest.SearchForm dto);

    TableResponseEntity<InsuranceRetractionsResponse> searchDataPopup(InsuranceRetractionsRequest.SearchForm dto);

    ResponseEntity saveData(InsuranceRetractionsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(InsuranceRetractionsRequest.SearchForm dto) throws BaseAppException;

    BaseResponseEntity<InsuranceRetractionsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(InsuranceRetractionsRequest.SearchForm dto) throws Exception;

    int calculate(List<String> empCodes, Date stringToDate, boolean isScheduled) throws Exception;
    int calculateByListPeriod(List<String> empCodes, List<String> listPeriodDate) throws Exception;
    Date getPeriodForSchedule();
}
