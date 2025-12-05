/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.SalaryRanksRequest;

import java.sql.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang hr_salary_ranks
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface SalaryRanksService {

    TableResponseEntity<SalaryRanksResponse.SearchResult> searchData(SalaryRanksRequest.SearchForm dto);

    ResponseEntity saveData(SalaryRanksRequest.SubmitForm dto, Long salaryRankId) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<SalaryRanksResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(SalaryRanksRequest.SearchForm dto) throws Exception;

    List<SalaryRanksResponse> getSalaryRanks(String salaryType, Date startDate, Long empTypeId, boolean isGetAttributes);

    List<SalaryRanksResponse.SalaryGradeDto> getSalaryGrades(Long id);

    List<SalaryRanksResponse> getSalaryRanksByListType(List<String> listSalaryType, boolean isGetAttributes);
}
