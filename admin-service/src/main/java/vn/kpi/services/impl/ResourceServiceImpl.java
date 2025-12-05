/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.configs.BaseCachingConfiguration;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.TreeDto;
import vn.kpi.models.request.ResourceRequest;
import vn.kpi.models.response.ResourceResponse;
import vn.kpi.repositories.entity.ResourceEntity;
import vn.kpi.repositories.impl.ResourceRepository;
import vn.kpi.repositories.impl.ScopeRepository;
import vn.kpi.repositories.jpa.ResourceRepositoryJPA;
import vn.kpi.services.ResourceService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang sys_resources
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourcesRepository;
    private final ScopeRepository scopeRepository;
    private final ResourceRepositoryJPA resourcesRepositoryJPA;

    private final CacheManager cacheManager;
    private final RedisServiceImpl redisServiceImpl;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<ResourceResponse.SearchResult> searchData(ResourceRequest.SearchForm dto) {
        return resourcesRepository.searchData(dto);
    }

    @Override
    @Transactional
    public Long saveData(ResourceRequest.SubmitForm dto, Long resourceId) throws BaseAppException {
        if (resourcesRepository.duplicate(ResourceEntity.class, resourceId, "code", dto.getCode())) {
            throw new BaseAppException("ERROR_RESOURCE_CODE_DUPLICATE", I18n.getMessage("error.resource.duplicateCode"));
        }
        ResourceEntity entity;
        if (resourceId != null && resourceId > 0L) {
            entity = resourcesRepositoryJPA.getById(resourceId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ResourceEntity();
            entity.setCreatedTime(new Date());
            entity.setStatus(ResourceEntity.STATUS.ACTIVE);
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        resourcesRepositoryJPA.saveAndFlush(entity);

        //neu khong co scope nao thi set mac dinh 1 scope la view
        if (Utils.isNullOrEmpty(dto.getScopeIds())) {
            dto.setScopeIds(Arrays.asList(scopeRepository.getScopeId(Scope.VIEW)));
        }
        //xoa cac scope khong su dung nua
        resourcesRepository.inActiveScopeNotIn(entity.getResourceId(), dto.getScopeIds());
        //insert scope
        resourcesRepository.activeScopeIn(entity.getResourceId(), dto.getScopeIds());
        //insert scope
        resourcesRepository.insertPermissions(entity.getResourceId(), dto.getScopeIds());

        //update path
        resourcesRepository.updatePathInfo(entity);

        redisServiceImpl.clearCache(BaseCachingConfiguration.ADMIN_MENU);

        return entity.getResourceId();
    }

    @Override
    @Transactional
    public boolean deleteData(Long id) throws RecordNotExistsException {
        Optional<ResourceEntity> optional = resourcesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ResourceEntity.class);
        }
        // check đã sử dụng
        boolean isUsed = resourcesRepository.checkResourceUsed(id);
        if (isUsed) {
            throw new BaseAppException("ERROR_RESOURCE_IS_USED", I18n.getMessage("error.resource.delete.used"));
        }
        resourcesRepository.deActiveObject(ResourceEntity.class, id);
        redisServiceImpl.clearCache(BaseCachingConfiguration.ADMIN_MENU);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse.DetailBean getDataById(Long id) throws RecordNotExistsException {
        Optional<ResourceEntity> optional = resourcesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ResourceEntity.class);
        }
        ResourceResponse.DetailBean dto = new ResourceResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setScopeIds(resourcesRepository.getScopeIdsByResource(id));
        return dto;
    }

    @Override
    public ResponseEntity<Object> exportData(ResourceRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = resourcesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    @Transactional
    public boolean lockById(Long id) throws BaseAppException {
        Optional<ResourceEntity> optional = resourcesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ResourceEntity.class);
        } else if (ResourceEntity.STATUS.INACTIVE.equals(optional.get().getStatus())) {
            throw new BaseAppException("RECORD_LOCKED", "error.record.locked");
        }
        ResourceEntity entity = optional.get();
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setStatus(ResourceEntity.STATUS.INACTIVE);
        resourcesRepositoryJPA.save(entity);
        return true;
    }

    @Override
    @Transactional
    public boolean unlockById(Long id) throws BaseAppException {
        Optional<ResourceEntity> optional = resourcesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ResourceEntity.class);
        } else if (ResourceEntity.STATUS.ACTIVE.equals(optional.get().getStatus())) {
            throw new BaseAppException("RECORD_UNLOCKED", "error.record.unlocked");
        }
        ResourceEntity entity = optional.get();
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setStatus(ResourceEntity.STATUS.ACTIVE);
        resourcesRepositoryJPA.save(entity);
        return true;
    }

    @Override
    public List<TreeDto> getResourceRootNodes() {
        return resourcesRepository.getResourceRootNodes();
    }

    @Override
    public List<TreeDto> getResourceChildNodes(Long nodeId) {
        return resourcesRepository.getChildren(nodeId);
    }

    @Override
    public List<TreeDto> initTree() {
        List<TreeDto> lstMenu = resourcesRepository.getAllResources();
        List<TreeDto> results = new ArrayList<>();
        Map<String, TreeDto> mapResources = new HashMap<>();
        lstMenu.stream().forEach(item -> {
            mapResources.put(item.getNodeId(), item);
        });
        lstMenu.stream().forEach(item -> {
            if (item.getParentId() == null || mapResources.get(item.getParentId()) == null) {
                results.add(item);
            } else {
                TreeDto parent = mapResources.get(item.getParentId());
                parent.addChild(item);
            }
        });

        return results;
    }

    @Override
    public BaseDataTableDto<ResourceResponse.SearchTreeChooseResult> search(ResourceRequest.TreeSearchRequest request) {
        return resourcesRepository.searchTreeChooser(request);
    }
}
