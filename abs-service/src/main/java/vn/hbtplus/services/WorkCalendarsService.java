/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.WorkCalendarsDTO;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.WorkCalendarsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang abs_work_calendars
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface WorkCalendarsService {

    TableResponseEntity<WorkCalendarsResponse> searchData(WorkCalendarsRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(WorkCalendarsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<WorkCalendarsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(WorkCalendarsRequest.SearchForm dto) throws Exception;

    List<WorkCalendarsDTO> getActiveWorkCalendars();

}
