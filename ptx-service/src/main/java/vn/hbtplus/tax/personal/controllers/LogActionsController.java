/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.LogActionsDTO;
import vn.hbtplus.tax.personal.models.response.LogActionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.services.LogActionsService;

import javax.validation.Valid;

@RestController
@RequestMapping(Constant.REQ_EMP_MAPPING_PREFIX)
@RequiredArgsConstructor
public class LogActionsController {
    private final LogActionsService logActionsService;

    @GetMapping(value = "/v1.0/personal/log-actions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<LogActionsResponse> searchData(@Valid LogActionsDTO dto) {
        return logActionsService.searchData(dto, false);
    }
}

