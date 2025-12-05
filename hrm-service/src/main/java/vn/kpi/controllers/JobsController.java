/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.annotations.UserLogActivity;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.JobsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.JobsResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.JobsService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.JOB)
public class JobsController {
    private final JobsService jobsService;

    @GetMapping(value = "/v1/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<JobsResponse.SearchResult> searchData(JobsRequest.SearchForm dto) {
        return jobsService.searchData(dto);
    }

    @PostMapping(value = "/v1/jobs", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid  JobsRequest.SubmitForm dto) throws BaseAppException {
        return jobsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/jobs/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    @UserLogActivity
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid  JobsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return jobsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/jobs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        return jobsService.deleteData(id);
    }

    @GetMapping(value = "/v1/jobs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<JobsResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return jobsService.getDataById(id);
    }

    @GetMapping(value = "/v1/jobs/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(JobsRequest.SearchForm dto) throws Exception {
        return jobsService.exportData(dto);
    }

    @GetMapping(value = "/v1/jobs/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<JobsResponse.DetailBean> getListJobs(@RequestParam(required = false) List<String> jobType,
                                                                   @RequestParam(required = false) Long organizationId) {
        return ResponseUtils.ok(jobsService.getListJobs(jobType, organizationId));
    }

}
