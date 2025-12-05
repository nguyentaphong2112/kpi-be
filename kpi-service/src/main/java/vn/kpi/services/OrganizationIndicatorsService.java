/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.OrganizationEvaluationsRequest;
import vn.kpi.models.response.*;
import vn.kpi.models.request.OrganizationIndicatorsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang kpi_organization_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface OrganizationIndicatorsService {

    TableResponseEntity<OrganizationIndicatorsResponse.SearchResult> searchData(OrganizationIndicatorsRequest.SearchForm dto);

    ResponseEntity saveData(OrganizationIndicatorsRequest.SubmitForm dto, Long id, String adjustReason) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OrganizationIndicatorsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OrganizationIndicatorsRequest.SearchForm dto) throws Exception;

    ResponseEntity deleteListData(List<Long> organizationIndicatorId, Long organizationEvaluationId, String adjustReason) throws RecordNotExistsException;

    List<OrganizationIndicatorsResponse.OrganizationEvaluation> getDataByEvaluationId(OrganizationEvaluationsRequest.SearchForm dto) throws RecordNotExistsException;

    TableResponseEntity<OrganizationEvaluationsResponse.OrgParent> getDataTableByEvaluationId(OrganizationEvaluationsRequest.OrgParent data) throws RecordNotExistsException;

}
