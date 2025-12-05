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
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.EvaluationResultsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EvaluationResultsResponse;
import vn.kpi.models.response.TableResponseEntity;

import java.io.IOException;
import java.util.List;

/**
 * Lop interface service ung voi bang hr_evaluation_results
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EvaluationResultsService {

    TableResponseEntity<EvaluationResultsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    ResponseEntity saveData(EvaluationResultsRequest.SubmitForm dto, Long employeeId, Long evaluationResultId) throws BaseAppException;

    ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<EvaluationResultsResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request);

    List<EvaluationResultsResponse.EvaluationPeriods> getListEvaluationPeriods(Integer year, String evaluationType);

    ResponseEntity<Object> downloadImportTemplate(Long periodId) throws Exception;

    ResponseEntity<Object> processImport(MultipartFile file, Long periodId, boolean isForceUpdate) throws IOException;
}
