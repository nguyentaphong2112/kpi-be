/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.OrganizationsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.OrganizationsResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Lop interface service ung voi bang hr_organizations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface OrganizationsService {

    BaseDataTableDto<OrganizationsResponse.SearchResult> searchData(OrganizationsRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(OrganizationsRequest.SubmitForm dto, Long organizationId) throws BaseAppException;

    BaseResponseEntity<Long> deleteData(Long id) throws BaseAppException;

    BaseResponseEntity<OrganizationsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OrganizationsRequest.SearchForm dto) throws Exception;

    BaseDataTableDto<EmployeesResponse.SearchResult> searchListPayroll(OrganizationsRequest.SearchForm dto);

    List getHierarchy(Long orgId);

    BaseResponseEntity<Object> getChart(String chartType, Long organizationId) throws ExecutionException, InterruptedException;

    BaseResponseEntity<Object> getChartLaborStructure(Long organizationId);

    ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate) throws IOException;
}
