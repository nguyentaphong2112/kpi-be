/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.response.*;
import vn.hbtplus.tax.personal.models.response.ObjectAttributesResponse;

import java.util.List;
import java.util.Map;

/**
 * Lop interface service ung voi bang hr_object_attributes
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface ObjectAttributesService {


    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ObjectAttributesResponse> getDataById(Long id) throws RecordNotExistsException;


    void saveObjectAttributes(Long objectId, List<AttributeRequestDto> listAttributes, Class className, String functionCode);

    List<ObjectAttributesResponse> getAttributes(Long id, String sqlTableName);

    Map<Long, List<ObjectAttributesResponse>> getListMapAttributes(List<Long> id, String sqlTableName);
}
