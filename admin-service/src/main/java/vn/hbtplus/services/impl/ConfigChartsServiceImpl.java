/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.request.ConfigChartsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ConfigChartsEntity;
import vn.hbtplus.repositories.entity.WarningConfigsEntity;
import vn.hbtplus.repositories.impl.ConfigChartsRepository;
import vn.hbtplus.repositories.jpa.ConfigChartsRepositoryJPA;
import vn.hbtplus.services.ConfigChartsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang sys_config_charts
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ConfigChartsServiceImpl implements ConfigChartsService {

    private final ConfigChartsRepository configChartsRepository;
    private final ConfigChartsRepositoryJPA configChartsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ConfigChartsResponse> searchData(ConfigChartsRequest.SearchForm dto) {
        return ResponseUtils.ok(configChartsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ConfigChartsRequest.SubmitForm dto,Long id) throws BaseAppException {
        ConfigChartsEntity entity;
        boolean isDuplicate =  configChartsRepository.duplicate(ConfigChartsEntity.class , id , "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CODE_DUPLICATE", I18n.getMessage("error.configChart.duplicateCode"));
        }
        if (id != null && id > 0L) {
            entity = configChartsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ConfigChartsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        configChartsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getConfigChartId(), dto.getListAttributes(), ConfigChartsEntity.class, null);
        return ResponseUtils.ok(entity.getConfigChartId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ConfigChartsEntity> optional = configChartsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigChartsEntity.class);
        }
        configChartsRepository.deActiveObject(ConfigChartsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ConfigChartsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<ConfigChartsEntity> optional = configChartsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigChartsEntity.class);
        }
        ConfigChartsResponse dto = new ConfigChartsResponse();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, Constant.ATTACHMENT.TABLE_NAMES.CONFIG_CHARTS));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ConfigChartsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = configChartsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<ConfigChartsResponse> getListCharts() {
        return configChartsRepository.getListCharts();
    }

}
