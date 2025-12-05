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
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EducationCertificatesRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.EducationDegreesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_education_degrees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface EducationDegreesService {

    TableResponseEntity<EducationDegreesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    List<EducationDegreesResponse.DetailBean> searchDataByType(String type);

    BaseResponseEntity<Long> saveData(EducationDegreesRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<EducationDegreesResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto<EducationDegreesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request);

    ResponseEntity<Object> downloadTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file, EducationDegreesRequest.SubmitForm dto) throws Exception;
}
