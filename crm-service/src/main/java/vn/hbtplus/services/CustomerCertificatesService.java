/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CustomerCertificatesRequest;

/**
 * Lop interface service ung voi bang crm_customer_certificates
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface CustomerCertificatesService {

    TableResponseEntity<CustomerCertificatesResponse.SearchResult> searchData(CustomerCertificatesRequest.SearchForm dto);

    ResponseEntity saveData(CustomerCertificatesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CustomerCertificatesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(CustomerCertificatesRequest.SearchForm dto) throws Exception;

    ResponseEntity updateStatusById(CustomerCertificatesRequest.SubmitForm dto, Long id) throws RecordNotExistsException;

}
