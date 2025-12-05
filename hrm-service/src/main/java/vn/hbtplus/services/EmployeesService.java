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
import vn.hbtplus.models.dto.EmployeeInfoDto;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.utils.ExportWorld;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Lop interface service ung voi bang hr_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EmployeesService {

    ResponseEntity saveData(EmployeesRequest.SubmitForm dto) throws BaseAppException, InstantiationException, IllegalAccessException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    EmployeesResponse.PersonalInfo getDataById(Long id) throws RecordNotExistsException, ExecutionException, InterruptedException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto<EmployeesResponse.SearchResult> searchBasicInfoEmployee(EmployeesRequest.SearchForm dto);

    BaseDataTableDto<EmployeesResponse.SearchResult> getEmpDataPicker(EmployeesRequest.SearchForm dto);

    BaseResponseEntity<Object> getAvatar(Long employeeId) throws IOException;

    ResponseEntity<Object> uploadAvatar(Long employeeId, MultipartFile fileAvatar) throws IOException;

    ResponseEntity<Object> deleteAvatar(Long employeeId);

    BaseResponseEntity<EmployeeInfoDto> getBasicInfo(Long employeeId) throws RecordNotExistsException;

    BaseResponseEntity<Long> saveBasicInfo(EmployeesRequest.PersonalInfoSubmitForm dto, Long employeeId) throws BaseAppException;

    boolean updatePoliticalInfo(EmployeesRequest.PoliticalInfo dto, Long employeeId);

    EmployeesResponse.PoliticalInfo getPoliticalInfo(Long employeeId);

    Long getEmployeeId(String userEmpCode);

    ExportWorld exportWord(Long employeeId) throws Exception;

    String getNextEmployeeCode() throws InstantiationException, IllegalAccessException;

    BaseDataTableDto<EmployeesResponse.SearchResult> searchEmployeeDirectory(EmployeesRequest.SearchForm dto);



    Long getOrgByOrgLevelManage(String employeeCode, Long orgLevelManage);


}
