/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import com.jxcell.CellException;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.DutySchedulesRequest;

import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang abs_duty_schedules
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface DutySchedulesService {

    TableResponseEntity<DutySchedulesResponse.SearchResult> searchData(DutySchedulesRequest.SearchForm dto);

    TableResponseEntity<DutySchedulesResponse.SearchResultMonth> searchDataMonth(DutySchedulesRequest.SearchForm dto);

    ResponseEntity saveData(DutySchedulesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity saveDataMonth(DutySchedulesRequest.SubmitFormMonth dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<DutySchedulesResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException;

    List<DutySchedulesResponse.DetailBean> getListData(DutySchedulesRequest.SearchForm dto);

    List<DutySchedulesResponse.DetailBean> getListCopyData(DutySchedulesRequest.SearchForm dto);

    ResponseEntity<Object> exportData(DutySchedulesRequest.ReportForm dto) throws Exception;

    List<DutySchedulesResponse.ListBoxBean> getWeeks();

    ResponseEntity<Object> exportDataTotal(DutySchedulesRequest.ReportForm dto) throws Exception;
}
