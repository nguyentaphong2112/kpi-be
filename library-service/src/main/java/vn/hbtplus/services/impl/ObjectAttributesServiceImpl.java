/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.AttributeConfigDto;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.request.ObjectAttributesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ObjectAttributesEntity;
import vn.hbtplus.repositories.impl.ObjectAttributesRepository;
import vn.hbtplus.repositories.impl.UtilsRepository;
import vn.hbtplus.repositories.jpa.ObjectAttributesRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang lib_object_attributes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ObjectAttributesServiceImpl implements ObjectAttributesService {

    private final ObjectAttributesRepository objectAttributesRepository;
    private final UtilsRepository utilsRepository;
    private final ObjectAttributesRepositoryJPA objectAttributesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ObjectAttributesResponse> searchData(ObjectAttributesRequest.SearchForm dto) {
        return ResponseUtils.ok(objectAttributesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ObjectAttributesRequest.SubmitForm dto) throws BaseAppException {
        ObjectAttributesEntity entity;
        if (dto.getObjectAttributeId() != null && dto.getObjectAttributeId() > 0L) {
            entity = objectAttributesRepositoryJPA.getById(dto.getObjectAttributeId());
        } else {
            entity = new ObjectAttributesEntity();
        }
        Utils.copyProperties(dto, entity);
        objectAttributesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getObjectAttributeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ObjectAttributesEntity> optional = objectAttributesRepositoryJPA.findById(id);
        if (optional.isEmpty()) {
            throw new RecordNotExistsException(id, ObjectAttributesEntity.class);
        }
        objectAttributesRepository.deActiveObject(ObjectAttributesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ObjectAttributesResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<ObjectAttributesEntity> optional = objectAttributesRepositoryJPA.findById(id);
        if (optional.isEmpty()) {
            throw new RecordNotExistsException(id, ObjectAttributesEntity.class);
        }
        ObjectAttributesResponse dto = new ObjectAttributesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ObjectAttributesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = objectAttributesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public void saveObjectAttributes(Long objectId, List<AttributeRequestDto> listAttributes, Class className, String functionCode) {
        String tableName = objectAttributesRepository.getSQLTableName(className);
        List<AttributeConfigDto> configAttributes = utilsRepository.getAttributes(tableName, functionCode);
        if (!Utils.isNullOrEmpty(configAttributes)) {
            List<String> attributeCodes = new ArrayList<>();
            configAttributes.forEach(configDto -> {
                attributeCodes.add(configDto.getCode());
            });
            objectAttributesRepository.deleteAttributes(objectId, tableName, attributeCodes);
            if (!Utils.isNullOrEmpty(listAttributes)) {
                List<ObjectAttributesEntity> listAttributesEntity = new ArrayList<>();
                for (AttributeRequestDto attributeForm : listAttributes) {
                    ObjectAttributesEntity objectAttributesEntity = new ObjectAttributesEntity();
                    objectAttributesEntity.setObjectId(objectId);
                    objectAttributesEntity.setDataType(Utils.NVL(attributeForm.getDataType()).toLowerCase());
                    objectAttributesEntity.setAttributeCode(attributeForm.getAttributeCode());
                    objectAttributesEntity.setAttributeValue(attributeForm.getAttributeValue());
                    objectAttributesEntity.setTableName(tableName);
                    listAttributesEntity.add(objectAttributesEntity);
                }
                objectAttributesRepository.insertBatch(ObjectAttributesEntity.class, listAttributesEntity, Utils.getUserNameLogin());
            }
        }
    }

    @Override
    public List<ObjectAttributesResponse> getAttributes(Long id, String sqlTableName) {
        return objectAttributesRepository.getListAttributes(id, sqlTableName);
    }

}
