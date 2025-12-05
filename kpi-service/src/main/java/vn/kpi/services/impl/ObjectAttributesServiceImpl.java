/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.AttributeConfigDto;
import vn.kpi.models.AttributeRequestDto;
import vn.kpi.models.request.ObjectAttributesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.ObjectAttributesEntity;
import vn.kpi.repositories.impl.ObjectAttributesRepository;
import vn.kpi.repositories.impl.UtilsRepository;
import vn.kpi.repositories.jpa.ObjectAttributesRepositoryJPA;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.*;

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
                String userName = Utils.getUserNameLogin();
                for (AttributeRequestDto attributeForm : listAttributes) {
                    ObjectAttributesEntity objectAttributesEntity = new ObjectAttributesEntity();
                    objectAttributesEntity.setObjectId(objectId);
                    objectAttributesEntity.setDataType(Utils.NVL(attributeForm.getDataType()).toLowerCase());
                    objectAttributesEntity.setAttributeCode(attributeForm.getAttributeCode());
                    objectAttributesEntity.setAttributeValue(attributeForm.getAttributeValue());
                    objectAttributesEntity.setTableName(tableName);
                    listAttributesEntity.add(objectAttributesEntity);
                }
                objectAttributesRepository.insertBatch(ObjectAttributesEntity.class, listAttributesEntity, userName);
            }
        }
    }

    @Override
    public List<ObjectAttributesResponse> getAttributes(Long id, String sqlTableName) {
        return objectAttributesRepository.getListAttributes(id, sqlTableName);
    }

    @Override
    public String getAttribute(Long id, String attributeCode, String sqlTableName) {
        return objectAttributesRepository.getAttribute(id, attributeCode, sqlTableName);
    }

    @Override
    public Map<Long, List<ObjectAttributesResponse>> getListMapAttributes(List<Long> ids, String sqlTableName) {
        if(Utils.isNullOrEmpty(ids)){
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

}
