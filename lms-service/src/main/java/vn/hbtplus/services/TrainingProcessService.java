/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.TrainingProcessRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TrainingProcessResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang lms_training_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface TrainingProcessService {

    TableResponseEntity<TrainingProcessResponse.SearchResult> searchData(TrainingProcessRequest.SearchForm dto);

    ResponseEntity saveData(TrainingProcessRequest.SubmitForm dto, List<MultipartFile> files, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<TrainingProcessResponse.Detail> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(TrainingProcessRequest.SearchForm dto) throws Exception;

    String getImportTemplate() throws Exception;

    boolean importData(MultipartFile fileImport) throws Exception;

}
