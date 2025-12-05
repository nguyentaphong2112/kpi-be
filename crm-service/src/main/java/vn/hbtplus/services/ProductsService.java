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
import vn.hbtplus.models.request.ProductsRequest;

import java.io.IOException;

/**
 * Lop interface service ung voi bang crm_products
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ProductsService {

    TableResponseEntity<ProductsResponse> searchData(ProductsRequest.SearchForm dto);

    ResponseEntity saveData(ProductsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ProductsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ProductsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> downloadTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file) throws IOException;

}
