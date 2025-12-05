/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.ConfigMappingsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_config_mappings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ConfigMappingsService {

    TableResponseEntity<ConfigMappingsResponse> searchData(ConfigMappingsRequest.SearchForm dto);

    ResponseEntity saveData(ConfigMappingsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ConfigMappingsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ConfigMappingsRequest.SearchForm dto) throws Exception;

    List<ConfigMappingsResponse> getListConfigByCodes(String attributeValue);
}
