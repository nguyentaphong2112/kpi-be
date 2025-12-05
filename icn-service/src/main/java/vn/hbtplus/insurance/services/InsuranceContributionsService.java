/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseApproveRequest;
import vn.hbtplus.insurance.models.InsuranceContributionsDto;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang icn_insurance_contributions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface InsuranceContributionsService {

    TableResponseEntity<InsuranceContributionsResponse> searchData(InsuranceContributionsRequest.SearchForm dto);

    ResponseEntity saveData(InsuranceContributionsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException, BaseAppException;

    BaseResponseEntity<InsuranceContributionsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(InsuranceContributionsRequest.SearchForm dto) throws Exception;

    int calculate(List<String> empCodes, Date stringToDate) throws Exception;

    List<Long> updateStatusById(BaseApproveRequest dto, String status) throws BaseAppException;

    List<Long> updateStatus(InsuranceContributionsRequest.SearchForm dto, String status) throws BaseAppException;


    List<InsuranceContributionsDto> calculateContributions(List<String> empCodes, Date periodDate, boolean isRetro) throws Exception;


    int retroByIds(Date periodDate, List<Long> ids) throws BaseAppException;

    int switchType(String type, List<Long> ids, String reason) throws BaseAppException, Exception;

    void retroMedical(Date periodDate, InsuranceContributionsRequest.RetroMedicalForm retroMedicalForm) throws BaseAppException, InstantiationException, IllegalAccessException;

    ResponseEntity<Object> downloadTemplateRetroMedical(Date periodDate) throws Exception;

    void importRetroMedical(Date lastDay, MultipartFile fileImport) throws Exception;

    void validateBeforeCalculate(Date periodDate) throws BaseAppException;
}
