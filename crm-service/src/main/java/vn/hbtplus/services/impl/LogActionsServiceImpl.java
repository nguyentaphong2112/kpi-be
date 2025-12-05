/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.request.LogActionsRequest;
import vn.hbtplus.models.response.LogActionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.LogActionsEntity;
import vn.hbtplus.repositories.impl.LogActionsRepository;
import vn.hbtplus.repositories.jpa.LogActionsRepositoryJPA;
import vn.hbtplus.services.LogActionsService;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * Lop impl service ung voi bang crm_log_actions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class LogActionsServiceImpl implements LogActionsService {

    private final LogActionsRepository logActionsRepository;
    private final LogActionsRepositoryJPA logActionsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<LogActionsResponse> searchData(LogActionsRequest.SearchForm dto) {
        return ResponseUtils.ok(logActionsRepository.searchData(dto));
    }

    @SneakyThrows
    @Override
    @Transactional
    public ResponseEntity saveData(Constant.LOG_ACTION action, Object entityOld, Object entityNew, List<AttributeRequestDto> oldAttributes, List<AttributeRequestDto> newAttributes, Long objId, String objName) throws BaseAppException {
        LogActionsEntity entity = new LogActionsEntity();
        entity.setAction(action.getAction());
        Table table = entityNew.getClass().getAnnotation(Table.class);
        String tableName = table.name().toLowerCase();
        entity.setActionName(I18n.getMessage(action.getName()));
        if (objId == null || objId < 0L) {
            objId = logActionsRepository.getIdColumnValue(entityNew);
        }
        entity.setObjType(tableName);
        entity.setObjId(objId);
        entity.setObjName(I18n.getMessage("logAction." + tableName) + " " + Utils.NVL(objName));

        Pair<StringBuilder, StringBuilder> dataPair = Utils.compareEntity(entityOld, entityNew, oldAttributes, newAttributes);
        entity.setDataBefore(dataPair.getLeft().toString());
        entity.setDataAfter(dataPair.getRight().toString());

        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setCreatedBy(Utils.getUserNameLogin());
        entity.setCreatedTime(new Date());
        if (!Utils.isNullOrEmpty(entity.getDataBefore()) || !Utils.isNullOrEmpty(entity.getDataAfter())) {
            logActionsRepositoryJPA.save(entity);
        }
        return ResponseUtils.ok(entity.getLogActionId());
    }

    @Override
    @SneakyThrows
    public ResponseEntity saveData(Constant.LOG_ACTION action, Object entityOld, Object entityNew, String objName) throws BaseAppException {
        LogActionsEntity entity = new LogActionsEntity();
        entity.setAction(action.getAction());
        Table table = entityNew.getClass().getAnnotation(Table.class);
        String tableName = table.name().toLowerCase();
        entity.setActionName(I18n.getMessage(action.getName()));
        Long objId = logActionsRepository.getIdColumnValue(entityNew);
        entity.setObjType(tableName);
        entity.setObjId(objId);
        entity.setObjName(I18n.getMessage("logAction." + tableName) + " " + Utils.NVL(objName));

        Pair<StringBuilder, StringBuilder> dataPair = Utils.compareEntity(entityOld, entityNew);
        entity.setDataBefore(dataPair.getLeft().toString());
        entity.setDataAfter(dataPair.getRight().toString());

        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setCreatedBy(Utils.getUserNameLogin());
        entity.setCreatedTime(new Date());
        if (!Utils.isNullOrEmpty(entity.getDataBefore()) || !Utils.isNullOrEmpty(entity.getDataAfter())) {
            logActionsRepositoryJPA.save(entity);
        }
        return ResponseUtils.ok(entity.getLogActionId());
    }
}
