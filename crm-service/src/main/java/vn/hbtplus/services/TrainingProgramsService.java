/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.TrainingProgramsRequest;

/**
 * Lop interface service ung voi bang crm_training_programs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TrainingProgramsService {

    TableResponseEntity<TrainingProgramsResponse.SearchResult> searchData(TrainingProgramsRequest.SearchForm dto);

    ResponseEntity saveData(TrainingProgramsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<TrainingProgramsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(TrainingProgramsRequest.SearchForm dto) throws Exception;

    ListResponseEntity<TrainingProgramsResponse.DataSelected> getListData();

}
