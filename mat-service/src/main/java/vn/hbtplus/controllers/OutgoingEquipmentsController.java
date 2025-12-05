/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.OutgoingEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.OutgoingEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.OutgoingEquipmentsService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.OUTGOING)
public class OutgoingEquipmentsController {
    private final OutgoingEquipmentsService outgoingEquipmentsService;

    @GetMapping(value = "/v1/outgoing-equipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OutgoingEquipmentsResponse> searchData(OutgoingEquipmentsRequest.SearchForm dto) {
        return outgoingEquipmentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/outgoing-equipments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody OutgoingEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        return outgoingEquipmentsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/outgoing-equipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return outgoingEquipmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/outgoing-equipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OutgoingEquipmentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return outgoingEquipmentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/outgoing-equipments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OutgoingEquipmentsRequest.SearchForm dto) throws Exception {
        return outgoingEquipmentsService.exportData(dto);
    }

}
