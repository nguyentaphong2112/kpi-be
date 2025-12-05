/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.PositionGroupsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ContractTypesEntity;
import vn.hbtplus.repositories.entity.PositionGroupConfigsEntity;
import vn.hbtplus.repositories.entity.PositionGroupsEntity;
import vn.hbtplus.repositories.impl.PositionGroupsRepository;
import vn.hbtplus.repositories.jpa.PositionGroupConfigsRepositoryJPA;
import vn.hbtplus.repositories.jpa.PositionGroupsRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.PositionGroupsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_position_groups
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PositionGroupsServiceImpl implements PositionGroupsService {

    private final PositionGroupsRepository positionGroupsRepository;
    private final PositionGroupsRepositoryJPA positionGroupsRepositoryJPA;
    private final PositionGroupConfigsRepositoryJPA positionGroupConfigsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PositionGroupsResponse> searchData(PositionGroupsRequest.SearchForm dto) {
        return ResponseUtils.ok(positionGroupsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(PositionGroupsRequest.SubmitForm dto, Long id) throws BaseAppException {

        boolean isDuplicate = positionGroupsRepository.duplicate(PositionGroupsEntity.class, id, "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CONTRACT_DUPLICATE", I18n.getMessage("error.positionGroups.code.duplicate"));
        }
        PositionGroupsEntity entity;
        if (id != null && id > 0L) {
            entity = positionGroupsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new PositionGroupsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        positionGroupsRepositoryJPA.save(entity);

        objectAttributesService.saveObjectAttributes(entity.getPositionGroupId(), dto.getListAttributes(), PositionGroupsEntity.class, null);
        //luu du lieu cau hinh
        List<Long> configIds = new ArrayList<>();
        for (PositionGroupsRequest.ConfigDto item : dto.getConfigs()) {
            if (Utils.isNullOrEmpty(item.getJobIds())) {
                configIds.add(saveConfig(entity.getGroupTypeId(), entity.getPositionGroupId(), item.getOrgTypeId(), item.getOrganizationId(), null));
            } else {
                for (Long jobId : item.getJobIds()) {
                    configIds.add(saveConfig(entity.getGroupTypeId(), entity.getPositionGroupId(), item.getOrgTypeId(), item.getOrganizationId(), jobId));
                }
            }
        }

        positionGroupConfigsRepositoryJPA.inactiveConfigNotIn(entity.getPositionGroupId(), configIds, Utils.getUserNameLogin());

        return ResponseUtils.ok(entity.getPositionGroupId());
    }

    private Long saveConfig(String groupTypeId,
                            Long positionGroupId, String orgTypeId, Long organizationId, Long jobId) throws BaseAppException {
        PositionGroupConfigsEntity configsEntity = positionGroupsRepository.getConfig(groupTypeId, orgTypeId, organizationId, jobId);
        if (configsEntity == null) {
            configsEntity = new PositionGroupConfigsEntity();
            configsEntity.setPositionGroupId(positionGroupId);
            configsEntity.setJobId(jobId);
            configsEntity.setOrgTypeId(orgTypeId);
            configsEntity.setOrganizationId(organizationId);
            configsEntity.setCreatedBy(Utils.getUserNameLogin());
            configsEntity.setCreatedTime(new Date());
        } else {
            if (configsEntity.isDeleted()) {
                configsEntity.setCreatedBy(Utils.getUserNameLogin());
                configsEntity.setCreatedTime(new Date());
                configsEntity.setModifiedBy(null);
                configsEntity.setModifiedTime(null);
                configsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                configsEntity.setPositionGroupId(positionGroupId);
            } else {
                if (!configsEntity.getPositionGroupId().equals(positionGroupId)) {
                    throw new BaseAppException("Dữ liệu đã được cấu hình với nhóm chức danh: " +
                            positionGroupsRepository.get(PositionGroupsEntity.class, configsEntity.getPositionGroupConfigId()).getName());
                }
            }
        }
        positionGroupConfigsRepositoryJPA.saveAndFlush(configsEntity);

        return configsEntity.getPositionGroupConfigId();
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<PositionGroupsEntity> optional = positionGroupsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PositionGroupsEntity.class);
        }
        positionGroupsRepository.deActiveObject(PositionGroupsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PositionGroupsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<PositionGroupsEntity> optional = positionGroupsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PositionGroupsEntity.class);
        }
        PositionGroupsResponse.DetailBean dto = new PositionGroupsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);

        List<PositionGroupsResponse.ConfigDto> listConfigs = positionGroupsRepository.getConfigByPositionGroup(id);
        Map<String, PositionGroupsResponse.ConfigDto> mapConfigs = new HashMap<>();
        listConfigs.stream().forEach(item -> {
            String key = String.join("#", item.getOrgTypeId(), item.getOrganizationId().toString());
            if(mapConfigs.get(key) == null){
                PositionGroupsResponse.ConfigDto configDto = new PositionGroupsResponse.ConfigDto(item);
                dto.getConfigs().add(configDto);
                mapConfigs.put(key, configDto);
            } else {
                mapConfigs.get(key).add(item);
            }
        });

        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(PositionGroupsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = positionGroupsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ListResponseEntity<PositionGroupsResponse.DetailBean> getListData() {
        List<PositionGroupsEntity> listData = positionGroupsRepository.findAll(PositionGroupsEntity.class);
        return ResponseUtils.ok(Utils.mapAll(listData, PositionGroupsResponse.DetailBean.class));
    }

}
