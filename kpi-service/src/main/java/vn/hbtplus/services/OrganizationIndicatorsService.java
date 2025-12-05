/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.OrganizationEvaluationsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.OrganizationIndicatorsRequest;
import vn.hbtplus.repositories.entity.OrganizationIndicatorsEntity;

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
