/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CourseLessonsRequest;
import vn.hbtplus.repositories.entity.CourseLessonsEntity;

/**
 * Lop interface service ung voi bang crm_course_lessons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CourseLessonsService {

    TableResponseEntity<CourseLessonsResponse.SearchResult> searchData(CourseLessonsRequest.SearchForm dto);

    ResponseEntity saveData(CourseLessonsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CourseLessonsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException;

    ListResponseEntity<CourseLessonsEntity> getDataByCourseId(Long id) throws RecordNotExistsException;

    ListResponseEntity<CourseLessonsResponse.Selected> getDataByCourseListId(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(CourseLessonsRequest.SearchForm dto) throws Exception;

}
