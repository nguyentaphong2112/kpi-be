/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.request.LogActionsRequest;
import vn.hbtplus.models.response.LogActionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang crm_log_actions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface LogActionsService {

    TableResponseEntity<LogActionsResponse> searchData(LogActionsRequest.SearchForm dto);

    ResponseEntity saveData(Constant.LOG_ACTION action, Object entityOld, Object entityNew, List<AttributeRequestDto> oldAttributes, List<AttributeRequestDto> newAttributes, Long objId, String objName) throws BaseAppException;

    ResponseEntity saveData(Constant.LOG_ACTION action, Object entityOld, Object entityNew, String objName) throws BaseAppException;
}
