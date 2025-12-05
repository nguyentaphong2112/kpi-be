/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.*;

import java.io.IOException;

/**
 * Lop interface service ung voi bang crm_partners
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface PartnersService {

    TableResponseEntity<PartnersResponse.SearchResult> searchData(PartnersRequest.SearchForm dto);

    ResponseEntity saveData(PartnersRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<PartnersResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(PartnersRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportCard(PartnersRequest.PrintCard dto) throws Exception;

    ResponseEntity<Object> exportTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file) throws IOException;

    ListResponseEntity<PartnersResponse.DetailBean> getListData();


}
