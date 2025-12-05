/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.JobsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.JobsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_jobs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface JobsService {

    TableResponseEntity<JobsResponse.SearchResult> searchData(JobsRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(JobsRequest.SubmitForm dto, Long id) throws BaseAppException;

    BaseResponseEntity<Long> deleteData(Long id) throws BaseAppException;

    BaseResponseEntity<JobsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(JobsRequest.SearchForm dto) throws Exception;

    List<JobsResponse.DetailBean> getListJobs(List<String> jobType, Long organizationId);
}
