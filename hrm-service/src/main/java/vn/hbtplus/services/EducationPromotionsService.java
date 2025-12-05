/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EducationPromotionsRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EducationPromotionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang hr_education_promotions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EducationPromotionsService {

    TableResponseEntity<EducationPromotionsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    ResponseEntity saveData(EducationPromotionsRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long employeeId,Long id) throws BaseAppException;

    BaseResponseEntity<EducationPromotionsResponse.DetailBean> getDataById(Long employeeId,Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request);
}
