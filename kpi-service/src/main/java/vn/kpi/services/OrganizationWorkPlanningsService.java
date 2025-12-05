/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.OrganizationWorkPlanningsRequest;
import vn.kpi.repositories.entity.OrganizationWorkPlanningsEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang kpi_organization_work_plannings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface OrganizationWorkPlanningsService {

    TableResponseEntity<OrganizationWorkPlanningsResponse> searchData(OrganizationWorkPlanningsRequest.SearchForm dto);

    ResponseEntity saveData(OrganizationWorkPlanningsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OrganizationWorkPlanningsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ListResponseEntity<OrganizationWorkPlanningsEntity> getDataByEvaluationId(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OrganizationWorkPlanningsRequest.SearchForm dto) throws Exception;


    List<OrganizationWorkPlanningsEntity> getOrgPlanning(Long periodId, List<Long> organizationIds);
}
