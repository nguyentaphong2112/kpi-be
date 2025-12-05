/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.AttributeRequestDto;
import vn.kpi.models.response.*;
import vn.kpi.models.request.ObjectAttributesRequest;

import java.util.List;
import java.util.Map;

/**
 * Lop interface service ung voi bang hr_object_attributes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ObjectAttributesService {

    TableResponseEntity<ObjectAttributesResponse> searchData(ObjectAttributesRequest.SearchForm dto);

    ResponseEntity saveData(ObjectAttributesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ObjectAttributesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ObjectAttributesRequest.SearchForm dto) throws Exception;

    void saveObjectAttributes(Long objectId, List<AttributeRequestDto> listAttributes, Class className, String functionCode);

    List<ObjectAttributesResponse> getAttributes(Long id, String sqlTableName);

    Map<Long, List<ObjectAttributesResponse>> getListMapAttributes(List<Long> id, String sqlTableName);

    List getAttributes(String tableName, String functionCode);
}
