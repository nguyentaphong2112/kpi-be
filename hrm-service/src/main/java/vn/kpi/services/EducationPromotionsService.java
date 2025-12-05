/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EducationPromotionsRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EducationPromotionsResponse;
import vn.kpi.models.response.TableResponseEntity;

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
