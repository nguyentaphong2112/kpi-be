/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.QuestionsRequest;

/**
 * Lop interface service ung voi bang exm_questions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface QuestionsService {

    TableResponseEntity<QuestionsResponse> searchData(QuestionsRequest.SearchForm dto);

    ResponseEntity saveData(QuestionsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<QuestionsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(QuestionsRequest.SearchForm dto) throws Exception;

}
