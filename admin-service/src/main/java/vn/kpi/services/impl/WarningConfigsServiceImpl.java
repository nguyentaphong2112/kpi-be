/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.Scope;
import vn.kpi.models.dto.CategoryDto;
import vn.kpi.models.request.WarningConfigsRequest;
import vn.kpi.models.response.*;
import vn.kpi.repositories.entity.WarningConfigsEntity;
import vn.kpi.repositories.impl.UtilsRepository;
import vn.kpi.repositories.impl.WarningConfigsRepository;
import vn.kpi.repositories.jpa.WarningConfigsRepositoryJPA;
import vn.kpi.services.AuthorizationService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.WarningConfigsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.ExportExcel;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang sys_warning_configs
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class WarningConfigsServiceImpl implements WarningConfigsService {

    private final WarningConfigsRepository warningConfigsRepository;
    private final WarningConfigsRepositoryJPA warningConfigsRepositoryJPA;
    private final AuthorizationService authorizationService;
    private final ObjectAttributesService objectAttributesService;
    private final UtilsRepository utilsRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WarningConfigsResponse> searchData(WarningConfigsRequest.SearchForm dto) {
        return ResponseUtils.ok(warningConfigsRepository.searchData(dto));
    }

    @Override
    public TableResponseEntity<Object> searchDataPopUp(WarningConfigsRequest.SearchForm dto) {
        List<ObjectAttributesResponse> objectAttributesResponseList =
                objectAttributesService.getAttributes(dto.getWarningConfigId(), warningConfigsRepository.getSQLTableName(WarningConfigsEntity.class));
        String configTableValue = objectAttributesResponseList.stream()
                .filter(attr -> "QUERY_TABLE".equals(attr.getAttributeCode()))
                .map(ObjectAttributesResponse::getAttributeValue)
                .findFirst()
                .orElse(null);
        if (!Utils.isNullOrEmpty(configTableValue)) {
            Map map = new HashMap<>();
            ResponseUtils.ok(utilsRepository.getListPagination(configTableValue, map, dto, Object.class));
        }
        return null;
    }


    @Override
    @Transactional
    public ResponseEntity saveData(WarningConfigsRequest.SubmitForm dto, Long id) throws BaseAppException {
        WarningConfigsEntity entity;
        boolean isDuplicate = warningConfigsRepository.duplicate(WarningConfigsEntity.class, id, "resource", dto.getResource());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_RESOURCE_DUPLICATE", I18n.getMessage("error.resource.code.duplicate"));
        }
        if (id != null && id > 0L) {
            entity = warningConfigsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WarningConfigsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        warningConfigsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getWarningConfigId(), dto.getListAttributes(), WarningConfigsEntity.class, null);
        return ResponseUtils.ok(entity.getWarningConfigId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WarningConfigsEntity> optional = warningConfigsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarningConfigsEntity.class);
        }
        warningConfigsRepository.deActiveObject(WarningConfigsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WarningConfigsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<WarningConfigsEntity> optional = warningConfigsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarningConfigsEntity.class);
        }
        WarningConfigsResponse dto = new WarningConfigsResponse();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, warningConfigsRepository.getSQLTableName(WarningConfigsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(WarningConfigsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = warningConfigsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<WarningConfigsResponse> getListWarning() {
        String userName = Utils.getUserNameLogin();
        List<WarningConfigsResponse> warningConfigsResponses = warningConfigsRepository.getListWarning();
        List<Long> listId = warningConfigsResponses.stream().map(WarningConfigsResponse::getWarningConfigId).toList();
        Map<Long, List<ObjectAttributesResponse>> mapAttribute = objectAttributesService.getListMapAttributes(listId, warningConfigsRepository.getSQLTableName(WarningConfigsEntity.class));
        List<WarningConfigsResponse> results = new ArrayList<>();
        warningConfigsResponses.forEach(item -> {
            if (authorizationService.checkPermission(Scope.VIEW, item.getResource(), userName)) {
                List<ObjectAttributesResponse> attributesResponseList = mapAttribute.get(item.getWarningConfigId());
                if (!Utils.isNullOrEmpty(attributesResponseList)) {
                    String configTableValue = attributesResponseList.stream()
                            .filter(attr -> "CONFIG_TABLE".equals(attr.getAttributeCode()))
                            .map(ObjectAttributesResponse::getAttributeValue)
                            .findFirst()
                            .orElse("");
                    String isShowExcel = attributesResponseList.stream()
                            .filter(attr -> "IS_SHOW_EXCEL".equals(attr.getAttributeCode()))
                            .map(ObjectAttributesResponse::getAttributeValue)
                            .findFirst()
                            .orElse(null);
                    if (!Utils.isNullOrEmpty(configTableValue)) {
                        List<CategoryDto> listColumnTable = Utils.fromJsonList(configTableValue, CategoryDto.class);
                        item.setIsPopup(!listColumnTable.isEmpty());
                        item.setListColumnTable(listColumnTable);
                    }
                    item.setIsShowExcel("Y".equals(isShowExcel));
                }
                results.add(item);
            }
        });
        return results;
    }

}
