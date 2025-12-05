/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.ConfigMappingsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ConfigMappingsEntity;
import vn.hbtplus.repositories.impl.ConfigMappingsRepository;
import vn.hbtplus.repositories.jpa.ConfigMappingsRepositoryJPA;
import vn.hbtplus.services.ConfigMappingsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang sys_config_mappings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ConfigMappingsServiceImpl implements ConfigMappingsService {

    private final ConfigMappingsRepository configMappingsRepository;
    private final ConfigMappingsRepositoryJPA configMappingsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ConfigMappingsResponse> searchData(ConfigMappingsRequest.SearchForm dto) {
        return ResponseUtils.ok(configMappingsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ConfigMappingsRequest.SubmitForm dto, Long id) throws BaseAppException {
        ConfigMappingsEntity entity;
        if (id != null && id > 0L) {
            entity = configMappingsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ConfigMappingsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        configMappingsRepositoryJPA.save(entity);

        return ResponseUtils.ok(entity.getConfigMappingId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ConfigMappingsEntity> optional = configMappingsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigMappingsEntity.class);
        }
        configMappingsRepository.deActiveObject(ConfigMappingsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ConfigMappingsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<ConfigMappingsEntity> optional = configMappingsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigMappingsEntity.class);
        }
        ConfigMappingsResponse dto = new ConfigMappingsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ConfigMappingsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = configMappingsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<ConfigMappingsResponse> getListConfigByCodes(String attributeValue) {
        return configMappingsRepository.getListConfigByCodes(List.of(attributeValue.replace(" ", "").split(",")));
    }

}
