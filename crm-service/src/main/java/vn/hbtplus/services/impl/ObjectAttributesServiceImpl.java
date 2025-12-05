/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hbtplus.models.AttributeConfigDto;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.repositories.entity.ObjectAttributesEntity;
import vn.hbtplus.repositories.impl.ObjectAttributesRepository;
import vn.hbtplus.repositories.impl.UtilsRepository;
import vn.hbtplus.repositories.jpa.ObjectAttributesRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop impl service ung voi bang hr_object_attributes
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class ObjectAttributesServiceImpl implements ObjectAttributesService {

    private final ObjectAttributesRepository objectAttributesRepository;
    private final UtilsRepository utilsRepository;
    private final ObjectAttributesRepositoryJPA objectAttributesRepositoryJPA;

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
                String userName = Utils.getUserNameLogin();
                for (AttributeRequestDto attributeForm : listAttributes) {
                    ObjectAttributesEntity objectAttributesEntity = new ObjectAttributesEntity();
                    objectAttributesEntity.setObjectId(objectId);
                    objectAttributesEntity.setDataType(Utils.NVL(attributeForm.getDataType()).toLowerCase());
                    objectAttributesEntity.setAttributeCode(attributeForm.getAttributeCode());
                    objectAttributesEntity.setAttributeValue(attributeForm.getAttributeValue());
                    objectAttributesEntity.setAttributeName(attributeForm.getAttributeName());
                    objectAttributesEntity.setTableName(tableName);
                    listAttributesEntity.add(objectAttributesEntity);
                }
                objectAttributesRepository.insertBatch(ObjectAttributesEntity.class, listAttributesEntity, userName);
            }
        }
    }

    @Override
    public List<ObjectAttributesResponse> getAttributes(Long id, String sqlTableName) {
        return objectAttributesRepository.getListAttributes(Arrays.asList(id), sqlTableName);
    }

    @Override
    public Map<Long, List<ObjectAttributesResponse>> getListMapAttributes(List<Long> ids, String sqlTableName) {
        if (Utils.isNullOrEmpty(ids)) {
            return new HashMap<>();
        }
        List<ObjectAttributesResponse> listObjs = objectAttributesRepository.getListAttributes(ids, sqlTableName);
        Map<Long, List<ObjectAttributesResponse>> mapResults = new HashMap<>();
        listObjs.forEach(item -> {
            if (mapResults.get(item.getObjectId()) == null) {
                mapResults.put(item.getObjectId(), new ArrayList<>());
            }
            mapResults.get(item.getObjectId()).add(item);
        });
        return mapResults;
    }

    @Override
    public void saveObjectAttribute(Long objectId, String tableName, String attributeCode, String attributeValue, String dataType) {
        ObjectAttributesEntity objectAttributesEntity = new ObjectAttributesEntity();
        objectAttributesEntity.setObjectId(objectId);
        objectAttributesEntity.setDataType(Utils.NVL(dataType).toLowerCase());
        objectAttributesEntity.setAttributeCode(attributeCode);
        objectAttributesEntity.setAttributeValue(attributeValue);
        objectAttributesEntity.setTableName(tableName);
        objectAttributesEntity.setCreatedBy(Utils.getUserNameLogin());
        objectAttributesEntity.setCreatedTime(new Date());
        objectAttributesRepositoryJPA.save(objectAttributesEntity);
    }

}
