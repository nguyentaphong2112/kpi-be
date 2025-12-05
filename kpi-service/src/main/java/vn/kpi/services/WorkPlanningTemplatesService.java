/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.WorkPlanningTemplatesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.WorkPlanningTemplatesResponse;
import vn.kpi.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang kpi_WorkPlanningTemplates
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface WorkPlanningTemplatesService {

    TableResponseEntity<WorkPlanningTemplatesResponse.SearchResult> searchData(WorkPlanningTemplatesRequest.SearchForm dto);

    ResponseEntity saveData(WorkPlanningTemplatesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<WorkPlanningTemplatesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(Long id, Long periodId, List<Long> organizationIds) throws Exception;

    ResponseEntity getList(WorkPlanningTemplatesRequest.SearchForm dto);

}
