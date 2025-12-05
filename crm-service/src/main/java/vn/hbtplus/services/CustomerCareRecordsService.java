/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CustomerCareRecordsRequest;

import java.io.IOException;

/**
 * Lop interface service ung voi bang crm_customer_care_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CustomerCareRecordsService {

    TableResponseEntity<CustomerCareRecordsResponse> searchData(CustomerCareRecordsRequest.SearchForm dto);

    ResponseEntity saveData(CustomerCareRecordsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CustomerCareRecordsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> downloadTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file) throws IOException;

    ResponseEntity<Object> exportData(CustomerCareRecordsRequest.SearchForm dto) throws Exception;

}
