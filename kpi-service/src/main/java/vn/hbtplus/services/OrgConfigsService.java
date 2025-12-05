/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.OrgConfigsRequest;

/**
 * Lop interface service ung voi bang kpi_org_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface OrgConfigsService {

    TableResponseEntity<OrgConfigsResponse> searchData(OrgConfigsRequest.SearchForm dto);

    ResponseEntity saveData(OrgConfigsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OrgConfigsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OrgConfigsRequest.SearchForm dto) throws Exception;

}
