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
import vn.hbtplus.models.request.ExternalTrainingsRequest;

/**
 * Lop interface service ung voi bang lms_external_trainings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ExternalTrainingsService {

    TableResponseEntity<ExternalTrainingsResponse.SearchResult> searchData(ExternalTrainingsRequest.SearchForm dto);

    ResponseEntity saveData(ExternalTrainingsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ExternalTrainingsResponse.Detail> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ExternalTrainingsRequest.SearchForm dto) throws Exception;

    String getTemplateIndicator() throws Exception;

    boolean importData(MultipartFile fileImport) throws Exception;

}
