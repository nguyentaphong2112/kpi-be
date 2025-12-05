/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.annotations.UserLogActivity;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.JobsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.JobsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.JobsService;
import vn.hbtplus.utils.ResponseUtils;

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
