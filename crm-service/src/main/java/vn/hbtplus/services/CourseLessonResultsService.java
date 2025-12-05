/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CourseLessonResultsRequest;

/**
 * Lop interface service ung voi bang crm_course_lesson_results
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CourseLessonResultsService {

    TableResponseEntity<CourseLessonResultsResponse> searchData(CourseLessonResultsRequest.SearchForm dto);

    ResponseEntity saveData(CourseLessonResultsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CourseLessonResultsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(CourseLessonResultsRequest.SearchForm dto) throws Exception;

}
