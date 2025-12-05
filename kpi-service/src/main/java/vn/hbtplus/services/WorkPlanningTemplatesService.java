/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WorkPlanningTemplatesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.WorkPlanningTemplatesResponse;
import vn.hbtplus.models.response.TableResponseEntity;

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
