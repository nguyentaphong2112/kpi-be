/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.WorkPlanningTemplatesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.WorkPlanningTemplatesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.WorkPlanningTemplatesService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.WORK_PLANNING_TEMPLATE)
public class WorkPlanningTemplatesController {
    private final WorkPlanningTemplatesService workPlanningTemplatesService;

    @GetMapping(value = "/v1/work-planning-templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<WorkPlanningTemplatesResponse.SearchResult> searchData(WorkPlanningTemplatesRequest.SearchForm dto) {
        return workPlanningTemplatesService.searchData(dto);
    }

    @PostMapping(value = "/v1/work-planning-templates", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody WorkPlanningTemplatesRequest.SubmitForm dto) throws BaseAppException {
        return workPlanningTemplatesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/work-planning-templates/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody WorkPlanningTemplatesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return workPlanningTemplatesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/work-planning-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return workPlanningTemplatesService.deleteData(id);
    }

    @GetMapping(value = "/v1/work-planning-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<WorkPlanningTemplatesResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return workPlanningTemplatesService.getDataById(id);
    }

    @GetMapping(value = "/v1/work-planning-templates/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataById(@PathVariable Long id,
                                                 @RequestParam(required = false) Long periodId,
                                                 @RequestParam(required = false) List<Long> organizationIds
    ) throws Exception {
        return workPlanningTemplatesService.exportData(id, periodId, organizationIds);
    }

    @GetMapping(value = "/v1/work-planning-templates/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getList(WorkPlanningTemplatesRequest.SearchForm dto) {
        return workPlanningTemplatesService.getList(dto);
    }

}
