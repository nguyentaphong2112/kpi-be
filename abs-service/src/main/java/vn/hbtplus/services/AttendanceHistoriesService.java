/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.AttendanceHistoriesRequest;

/**
 * Lop interface service ung voi bang abs_attendance_histories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface AttendanceHistoriesService {

    TableResponseEntity<AttendanceHistoriesResponse> searchData(AttendanceHistoriesRequest.SearchForm dto);

    TableResponseEntity<AttendanceHistoriesResponse> searchDataByCurrentUser(AttendanceHistoriesRequest.SearchForm dto);

    ResponseEntity saveData(AttendanceHistoriesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<AttendanceHistoriesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(AttendanceHistoriesRequest.SearchForm dto) throws Exception;

    TableResponseEntity<AttendanceHistoriesResponse.AttendanceLogResponse> getLogData(AttendanceHistoriesRequest.SearchForm dto);

    ResponseEntity updateStatusById(AttendanceHistoriesRequest.SubmitForm dto, Long id) throws RecordNotExistsException;

}
