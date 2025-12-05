/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.MentoringTrainersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.MentoringTrainersResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.io.IOException;

/**
 * Lop interface service ung voi bang lms_mentoring_trainers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface MentoringTrainersService {

    TableResponseEntity<MentoringTrainersResponse> searchData(MentoringTrainersRequest.SearchForm dto);

    ResponseEntity saveData(MentoringTrainersRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<MentoringTrainersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(MentoringTrainersRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> downloadTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file) throws IOException;

}
