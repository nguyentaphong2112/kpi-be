/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.CourseLessonResultsRequest;
import vn.hbtplus.models.request.CourseTraineesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CoursesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang crm_courses
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CoursesService {

    TableResponseEntity<CoursesResponse.SearchResult> searchData(CoursesRequest.SearchForm dto);

    ResponseEntity saveData(CoursesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CoursesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(CoursesRequest.SearchForm dto) throws Exception;

    ListResponseEntity<CoursesResponse.UserDataSelected> getListUserData();

    ListResponseEntity<CoursesResponse.DataSelected> getListData(CoursesRequest.SearchForm dto);

    ResponseEntity saveLessonResult(CoursesRequest.SubmitLessonResult dto) throws BaseAppException;

    ListResponseEntity<CourseLessonResultsRequest.SubmitForm> getListLessonResult(List<Long> listCourseLessonId, Long traineeId);

    String getTemplateTrainee() throws Exception;

    String getTemplate(CoursesRequest.ImportRequest dto) throws Exception;

    List<CourseTraineesRequest.SubmitForm> importDataTrainee(MultipartFile fileImport) throws Exception;

    boolean importData(CoursesRequest.ImportRequest dto) throws Exception;

}
