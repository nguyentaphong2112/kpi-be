/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.DocumentTypesRequest;

/**
 * Lop interface service ung voi bang hr_document_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface DocumentTypesService {

    TableResponseEntity<DocumentTypesResponse.SearchResult> searchData(DocumentTypesRequest.SearchForm dto);

    ResponseEntity saveData(DocumentTypesRequest.SubmitForm dto, Long documentTypeId) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<DocumentTypesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(DocumentTypesRequest.SearchForm dto) throws Exception;

    ListResponseEntity<DocumentTypesResponse.DetailBean> getList();
}
