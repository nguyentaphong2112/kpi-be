/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.HealthRecordsRequest;

import java.io.IOException;

/**
 * Lop interface service ung voi bang hr_health_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface HealthRecordsService {

    TableResponseEntity<HealthRecordsResponse> searchData(HealthRecordsRequest.SearchForm dto);

    ResponseEntity saveData(HealthRecordsRequest.SubmitForm dto,Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<HealthRecordsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(HealthRecordsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> downloadTemplate(String periodId) throws Exception;

    ResponseEntity importProcess(String periodId,MultipartFile file) throws IOException;
}
