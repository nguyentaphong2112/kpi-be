/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.PositionsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.PositionsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.PositionsService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class PositionsController {
    private final PositionsService positionsService;

    @GetMapping(value = "/v1/positions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PositionsResponse.SearchResult> searchData(PositionsRequest.SearchForm dto) {
        return ResponseUtils.ok(positionsService.searchData(dto));
    }

    @PostMapping(value = "/v1/positions", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Boolean> saveData(@RequestBody @Valid  PositionsRequest.SubmitForm dto) throws BaseAppException {
        return positionsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/positions/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Boolean> updateData(@RequestBody @Valid  PositionsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return positionsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/positions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        return positionsService.deleteData(id);
    }

    @GetMapping(value = "/v1/positions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<PositionsResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return positionsService.getDataById(id);
    }

    @GetMapping(value = "/v1/positions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(PositionsRequest.SearchForm dto) throws Exception {
        return positionsService.exportData(dto);
    }

    @GetMapping(value = "/v1/positions/list/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<PositionsResponse.DetailBean> getListByOrgId(@PathVariable Long organizationId,
                                                                           @RequestParam(required = false) String jobType,
                                                                           @RequestParam(required = false) List<String> listJobType) {
        return ResponseUtils.ok(positionsService.getListByOrgId(organizationId, jobType, listJobType));
    }

}
