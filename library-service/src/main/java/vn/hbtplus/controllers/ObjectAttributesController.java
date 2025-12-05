/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ObjectAttributesRequest;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class ObjectAttributesController {
    private final ObjectAttributesService objectAttributesService;

    @GetMapping(value = "/v1/object-attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ObjectAttributesResponse> searchData(ObjectAttributesRequest.SearchForm dto) {
        return objectAttributesService.searchData(dto);
    }

    @PostMapping(value = "/v1/object-attributes", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  ObjectAttributesRequest.SubmitForm dto) throws BaseAppException {
        return objectAttributesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/object-attributes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return objectAttributesService.deleteData(id);
    }

    @GetMapping(value = "/v1/object-attributes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ObjectAttributesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return objectAttributesService.getDataById(id);
    }

    @GetMapping(value = "/v1/object-attributes/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ObjectAttributesRequest.SearchForm dto) throws Exception {
        return objectAttributesService.exportData(dto);
    }

}
