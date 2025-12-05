/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.SalaryReviewsRequest;
import vn.kpi.utils.ExportWorld;

import java.io.IOException;
import java.util.List;

/**
 * Lop interface service ung voi bang hr_salary_reviews
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface SalaryReviewsService {

    TableResponseEntity<SalaryReviewsResponse.SearchResult> searchData(SalaryReviewsRequest.SearchForm dto);

    ResponseEntity saveData(SalaryReviewsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<SalaryReviewsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(SalaryReviewsRequest.SearchForm dto) throws Exception;

    boolean makeList(String periodId) throws IllegalAccessException;

    boolean importData(String periodId, MultipartFile fileImport, List<MultipartFile> fileExtends) throws IOException;

    ExportWorld exportDataById(Long id) throws Exception;

    ResponseEntity<Object> downloadTemplate(String periodId) throws Exception;
}
