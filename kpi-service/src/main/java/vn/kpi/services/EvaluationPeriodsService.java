/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.EvaluationPeriodsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EvaluationPeriodsResponse;
import vn.kpi.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang kpi_evaluation_periods
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface EvaluationPeriodsService {

    TableResponseEntity<EvaluationPeriodsResponse.SearchResult> searchData(EvaluationPeriodsRequest.SearchForm dto);

    ResponseEntity saveData(EvaluationPeriodsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EvaluationPeriodsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EvaluationPeriodsResponse.MaxYear> getDataByMaxYear() throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EvaluationPeriodsRequest.SearchForm dto) throws Exception;

    boolean initData(Long id);

    ResponseEntity updateStatusById(EvaluationPeriodsRequest.Status dto, Long evaluationPeriodId) throws RecordNotExistsException;
}
