/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ObjectAttributesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang lib_object_attributes
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface ObjectAttributesService {

    TableResponseEntity<ObjectAttributesResponse> searchData(ObjectAttributesRequest.SearchForm dto);

    ResponseEntity saveData(ObjectAttributesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ObjectAttributesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ObjectAttributesRequest.SearchForm dto) throws Exception;

    List<ObjectAttributesResponse> getAttributes(Long id, String sqlTableName);

    void saveObjectAttributes(Long objectId, List<AttributeRequestDto> listAttributes, Class className, String functionCode);
}
