/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.personal.models.request.LogActionsDTO;
import vn.hbtplus.tax.personal.models.response.LogActionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.repositories.entity.LogActionsEntity;
import vn.hbtplus.tax.personal.repositories.impl.LogActionsRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.jpa.LogActionsRepositoryJPA;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.LogActionsService;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;

/**
 * Lop impl service ung voi bang PTX_LOG_ACTIONS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class LogActionsServiceImpl implements LogActionsService {

    private final LogActionsRepositoryImpl logActionsRepositoryImpl;
    private final LogActionsRepositoryJPA logActionsRepositoryJPA;
    private final CommonUtilsService commonUtilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<LogActionsResponse> searchData(LogActionsDTO dto, boolean isAdmin) {
        if (isAdmin) {
            return ResponseUtils.ok(logActionsRepositoryImpl.searchData(dto));
        } else {
            BaseDataTableDto baseResultSelect = logActionsRepositoryImpl.searchData(dto);
            if (!Utils.isNullOrEmpty(baseResultSelect.getListData())) {
                LogActionsResponse responseDTO = (LogActionsResponse) baseResultSelect.getListData().get(0);
                Long empIdByObjectId = logActionsRepositoryImpl.getEmpIdByObjectId(responseDTO.getObjectType(), responseDTO.getObjectId());
                if (!empIdByObjectId.equals(commonUtilsService.getEmpIdLogin())) {
                    throw new BaseAppException("BAD_REQUEST", "");
                }
            }
            return ResponseUtils.ok(baseResultSelect);
        }
    }

    @Override
    public void saveLog(Long objectId, String objectType, String content, Integer oldStatus, Integer newStatus) {
        LogActionsEntity entity = new LogActionsEntity();
        entity.setObjectId(objectId);
        entity.setObjectType(objectType);
        entity.setCreatedBy(Utils.getUserNameLogin());
        entity.setCreatedTime(new Date());
        String logContent = getLogContent(objectType, content, oldStatus, newStatus);
        entity.setContent(logContent);
        logActionsRepositoryJPA.save(entity);
    }

    @Override
    public LogActionsEntity getLogAction(Long objectId, String objectType, String content, Integer oldStatus, Integer newStatus) {
        LogActionsEntity entity = new LogActionsEntity();
        entity.setObjectId(objectId);
        entity.setObjectType(objectType);
        entity.setCreatedBy(Utils.getUserNameLogin());
        entity.setCreatedTime(new Date());
        String logContent = getLogContent(objectType, content, oldStatus, newStatus);
        entity.setContent(logContent);
        return entity;
    }

    private String getLogContent(String objectType, String content, Integer oldStatus, Integer newStatus){
        String actionName;
        if (Constant.LOG_OBJECT_TYPE.CONFIRM.equals(objectType)) {
            if (oldStatus == null) {
                actionName = I18n.getMessage("taxNumber.actionLog.status.register");
            } else if(oldStatus.equals(Constant.TAX_STATUS.WAITING_APPROVAL) && newStatus.equals(Constant.TAX_STATUS.TAX_APPROVAL)) {
                actionName = I18n.getMessage("confirm.actionLog.status.approve");
            } else if (!oldStatus.equals(newStatus)) {
                actionName = I18n.getMessage("taxNumber.actionLog.status.sendApprove");
            } else {
                actionName = I18n.getMessage("taxNumber.actionLog.status.edit");
            }
        } else if (oldStatus == null && newStatus == null) {
            actionName = I18n.getMessage("taxNumber.actionLog.status.delete");
        } else if (oldStatus == null && (newStatus.equals(Constant.TAX_STATUS.DRAFT) || newStatus.equals(Constant.TAX_STATUS.WAITING_APPROVAL))) {
            actionName = I18n.getMessage("taxNumber.actionLog.status.register");
        } else if (newStatus.equals(Constant.TAX_STATUS.WAITING_APPROVAL)) {
            actionName = I18n.getMessage("taxNumber.actionLog.status.sendApprove");
        } else if (newStatus.equals(Constant.TAX_STATUS.DRAFT)) {
            actionName = I18n.getMessage("taxNumber.actionLog.status.cancel");
        } else {
            actionName = I18n.getMessage("taxNumber.actionLog.status." + newStatus);
        }

        StringBuilder logContent = new StringBuilder();
        if (Utils.isNullOrEmpty(content)) {
            logContent.append(actionName);
        } else {
            logContent.append(actionName).append(", ").append(content);
        }
        return logContent.toString();
    }

}
