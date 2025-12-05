/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.response.*;
import vn.kpi.models.request.PositionsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang hr_positions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface PositionsService {

    BaseDataTableDto<PositionsResponse.SearchResult> searchData(PositionsRequest.SearchForm dto);

    BaseResponseEntity<Boolean> saveData(PositionsRequest.SubmitForm dto, Long id) throws BaseAppException;

    BaseResponseEntity<Long> deleteData(Long id) throws BaseAppException;

    BaseResponseEntity<PositionsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(PositionsRequest.SearchForm dto) throws Exception;

    List<PositionsResponse.DetailBean> getListByOrgId(Long organizationId, String jobType, List<String> listJobType);
}
