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
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.FamilyRelationshipsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.FamilyRelationshipsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.io.IOException;

/**
 * Lop interface service ung voi bang hr_family_relationships
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface FamilyRelationshipsService {

    TableResponseEntity<FamilyRelationshipsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    ResponseEntity saveData(FamilyRelationshipsRequest.SubmitForm dto,Long employeeId, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long employeeId,Long id) throws BaseAppException;

    BaseResponseEntity<FamilyRelationshipsResponse.DetailBean> getDataById(Long employeeId,Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request);

    ResponseEntity<Object> downloadTemplate() throws Exception;

    ResponseEntity importProcess( MultipartFile file) throws IOException;
}
