/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.request.LogActionsRequest;
import vn.hbtplus.models.response.LogActionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.LogActionsService;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class LogActionsController {
    private final LogActionsService logActionsService;

    @GetMapping(value = "/v1/log-actions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<LogActionsResponse> searchData(LogActionsRequest.SearchForm dto) {
        return logActionsService.searchData(dto);
    }

}
