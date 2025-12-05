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
import vn.hbtplus.models.request.PositionSalaryProcessRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.PositionSalaryProcessResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_position_salary_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface PositionSalaryProcessService {

    TableResponseEntity<PositionSalaryProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    ResponseEntity saveData(PositionSalaryProcessRequest.SubmitForm dto , List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException;
    ResponseEntity saveData(PositionSalaryProcessRequest.SubmitFormV2 dto , List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<PositionSalaryProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws RecordNotExistsException, BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request);

    ResponseEntity deleteDataV2(Long employeeId, Long id);

    BaseResponseEntity<PositionSalaryProcessResponse.DetailBeanV2> getDataByIdV2(Long employeeId, Long id);
}
