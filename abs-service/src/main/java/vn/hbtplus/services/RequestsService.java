/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.AbsTimekeepingDTO;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.RequestsRequest;

import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang abs_requests
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface RequestsService {

    TableResponseEntity<RequestsResponse> searchData(RequestsRequest.SearchForm dto);

    ResponseEntity saveData(RequestsRequest.SubmitForm dto , MultipartFile fileRequest, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<RequestsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(RequestsRequest.SearchForm dto) throws Exception;

    List<AbsTimekeepingDTO> getListRequestChange(Date lastRun);
}
