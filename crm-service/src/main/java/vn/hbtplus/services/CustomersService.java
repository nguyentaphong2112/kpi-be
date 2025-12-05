/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.CustomerCareRecordsRequest;
import vn.hbtplus.models.request.CustomersRequest;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.*;

import java.io.IOException;

/**
 * Lop interface service ung voi bang crm_customers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CustomersService {

    TableResponseEntity<CustomersResponse.SearchResult> searchData(CustomersRequest.SearchForm dto);

    ResponseEntity saveData(CustomersRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CustomersResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(CustomersRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportCard(PartnersRequest.PrintCard dto) throws Exception;

    ResponseEntity<Object> exportTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file) throws IOException;

    ResponseEntity<Object> customerCare(CustomerCareRecordsRequest.SubmitForm dto);
    ListResponseEntity<CustomersResponse.DataSelected> getListData();

    ResponseEntity<Object> addCourse(CustomersRequest.CourseForm dto);

    TableResponseEntity<CustomersResponse.SearchResult> getListPageable(CustomersRequest.SearchForm dto);
}
