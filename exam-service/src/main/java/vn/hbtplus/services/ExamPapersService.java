/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ExamPapersRequest;

/**
 * Lop interface service ung voi bang exm_exam_papers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ExamPapersService {

    TableResponseEntity<ExamPapersResponse> searchData(ExamPapersRequest.SearchForm dto);

    ResponseEntity saveData(ExamPapersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ExamPapersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ExamPapersRequest.SearchForm dto) throws Exception;

}
