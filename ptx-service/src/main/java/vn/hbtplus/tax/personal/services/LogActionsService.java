/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services;

import vn.hbtplus.tax.personal.models.request.LogActionsDTO;
import vn.hbtplus.tax.personal.models.response.LogActionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.repositories.entity.LogActionsEntity;

/**
 * Lop interface service ung voi bang PTX_LOG_ACTIONS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface LogActionsService {

    TableResponseEntity<LogActionsResponse> searchData(LogActionsDTO dto, boolean isAdmin);

    void saveLog(Long objectId, String objectType, String content, Integer oldStatus, Integer newStatus);

    LogActionsEntity getLogAction(Long objectId, String objectType, String content, Integer oldStatus, Integer newStatus);


}
