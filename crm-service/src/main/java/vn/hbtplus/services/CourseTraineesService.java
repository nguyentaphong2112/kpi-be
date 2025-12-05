/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CourseTraineesRequest;

/**
 * Lop interface service ung voi bang crm_course_trainees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CourseTraineesService {

    TableResponseEntity<CourseTraineesResponse> searchData(CourseTraineesRequest.SearchForm dto);

    ResponseEntity saveData(CourseTraineesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CourseTraineesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(CourseTraineesRequest.SearchForm dto) throws Exception;

}
