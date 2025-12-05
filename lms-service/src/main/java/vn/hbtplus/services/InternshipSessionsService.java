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
import vn.hbtplus.models.request.InternshipSessionsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang lms_internship_sessions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface InternshipSessionsService {

    TableResponseEntity<InternshipSessionsResponse.SearchResult> searchData(InternshipSessionsRequest.SearchForm dto);

    ResponseEntity saveData(InternshipSessionsRequest.SubmitForm dto, List<MultipartFile> files, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<InternshipSessionsResponse.Detail> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(InternshipSessionsRequest.SearchForm dto) throws Exception;

    String getTemplateIndicator() throws Exception;

    boolean importData(MultipartFile fileImport) throws Exception;

}
