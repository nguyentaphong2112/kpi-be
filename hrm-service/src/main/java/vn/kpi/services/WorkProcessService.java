/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.*;
import vn.kpi.models.request.WorkProcessRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_work_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface WorkProcessService {

    TableResponseEntity<WorkProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(WorkProcessRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException;
    Long saveData(WorkProcessRequest.SubmitFormV2 dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<WorkProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request);

    ResponseEntity autoUpdateWorkProcess(String fromDate,boolean isSchedule);
}
