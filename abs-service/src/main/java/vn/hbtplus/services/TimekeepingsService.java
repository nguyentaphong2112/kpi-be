/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.AbsTimekeepingDTO;
import vn.hbtplus.models.dto.WorkdayTypeVariableDTO;
import vn.hbtplus.models.request.TimekeepingsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TimekeepingsResponse;

import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang abs_timekeepings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TimekeepingsService {

    TableResponseEntity<TimekeepingsResponse.SearchResult> searchData(String type, TimekeepingsRequest.SearchForm dto);

    ResponseEntity saveData(String type, TimekeepingsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<TimekeepingsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(String type, TimekeepingsRequest.SearchForm dto) throws Exception;



    void autoSetTimekeeping(Date timekeepingDate, List<Long> empCodes);

    void autoSetOverTimekeeping(Date timekeepingDate, List<Long> employeeCodes);
}
