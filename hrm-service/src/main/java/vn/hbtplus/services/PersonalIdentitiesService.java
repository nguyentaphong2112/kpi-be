/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.PersonalIdentitiesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.PersonalIdentitiesResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang hr_personal_identities
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface PersonalIdentitiesService {

    BaseDataTableDto<PersonalIdentitiesResponse.SearchResult> getPersonalIdentities(Long id, BaseSearchRequest request);

    TableResponseEntity<PersonalIdentitiesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(PersonalIdentitiesRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<PersonalIdentitiesResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate, String identityTypeId) throws Exception;

    ResponseEntity<Object> downloadImportTemplate(String identityTypeId) throws Exception;

}
