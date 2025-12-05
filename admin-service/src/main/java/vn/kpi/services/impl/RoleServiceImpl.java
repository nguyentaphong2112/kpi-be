/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.RoleRequest;
import vn.kpi.models.response.RoleResponse;
import vn.kpi.repositories.entity.RoleEntity;
import vn.kpi.repositories.impl.ResourceRepository;
import vn.kpi.repositories.impl.RoleRepository;
import vn.kpi.repositories.impl.ScopeRepository;
import vn.kpi.repositories.jpa.RoleRepositoryJPA;
import vn.kpi.services.RoleService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Lop impl service ung voi bang sys_roles
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository rolesRepository;
    private final ScopeRepository scopeRepository;
    private final ResourceRepository resourceRepository;
    private final RoleRepositoryJPA rolesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<RoleResponse.SearchResult> searchData(RoleRequest.SearchForm dto) {
        return rolesRepository.searchData(dto);
    }

    @Override
    @Transactional
    public Long saveData(RoleRequest.SubmitForm dto, Long roleId) throws BaseAppException {
        if (resourceRepository.duplicate(RoleEntity.class, roleId, "code", dto.getCode())) {
            throw new BaseAppException("ROLE_CODE_DUPLICATE", "error.role.duplicateCode");
        }
        if (resourceRepository.duplicate(RoleEntity.class, roleId, "code", dto.getName())) {
            throw new BaseAppException("ROLE_CODE_DUPLICATE", "error.role.duplicateName");
        }
        RoleEntity entity;
        if (roleId != null && roleId > 0L) {
            entity = rolesRepositoryJPA.getById(roleId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new RoleEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        rolesRepositoryJPA.save(entity);
        return entity.getRoleId();
    }

    @Override
    @Transactional
    public boolean deleteData(Long id) throws RecordNotExistsException {
        Optional<RoleEntity> optional = rolesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, RoleEntity.class);
        }
        rolesRepository.deActiveObject(RoleEntity.class, id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse.DetailBean getDataById(Long id) throws RecordNotExistsException {
        Optional<RoleEntity> optional = rolesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, RoleEntity.class);
        }
        RoleResponse.DetailBean dto = new RoleResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        return dto;
    }

    @Override
    public ResponseEntity<Object> exportData(RoleRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = rolesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    @Transactional
    public boolean grantPermissions(RoleRequest.GrantPermissionForm dto, Long roleId) throws RecordNotExistsException {
        Optional<RoleEntity> optional = rolesRepositoryJPA.findById(roleId);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(roleId, RoleEntity.class);
        }
        //for danh sach node duoc chon de lay ra danh sach menu & quyen
        List<Long> resourceIds = new ArrayList<>();
        List<Long> permissionIds = new ArrayList<>();
        Set<Long> setPermissions = new HashSet<>();
        if (!Utils.isNullOrEmpty(dto.getNodeIds())) {
            dto.getNodeIds().stream().forEach(node -> {
                if (node.startsWith("R_")) {
                    resourceIds.add(Long.valueOf(node.split("_")[1]));
                } else {
                    permissionIds.add(Long.valueOf(node.split("_")[1]));
                }
            });
            setPermissions.addAll(permissionIds);
            //Lay permission view theo resource
            List<Long> permissionViewIds = resourceRepository.getPermissionViewIds(resourceIds, permissionIds);
            setPermissions.addAll(permissionViewIds);
        }


        //insert du lieu phan quyen moi
        rolesRepository.inactivePermissionNotIn(roleId, new ArrayList<>(setPermissions));
        if (!setPermissions.isEmpty()) {
            rolesRepository.insertRolePermissions(roleId, new ArrayList<>(setPermissions));
        }


        return false;
    }

    @Override
    public List<RoleResponse.TreeDto> initTreePermissions() {
        List<RoleResponse.TreeDto> listResources = rolesRepository.getListResources();
        //Lay danh sach permission
        List<RoleResponse.TreeDto> listPermissions = rolesRepository.getListPermissions();
        Map<String, List<RoleResponse.TreeDto>> mapPermissions = new HashMap<>();
        listPermissions.stream().forEach(item -> {
            if (mapPermissions.get(item.getParentId()) == null) {
                mapPermissions.put(item.getParentId(), new ArrayList<>());
            }
            mapPermissions.get(item.getParentId()).add(item);
        });

        List<RoleResponse.TreeDto> results = new ArrayList<>();
        Map<String, RoleResponse.TreeDto> mapResources = new HashMap<>();
        listResources.stream().forEach(item -> {
            mapResources.put(item.getNodeId(), item);
        });
        listResources.stream().forEach(item -> {
            if (item.getParentId() == null || mapResources.get(item.getParentId()) == null) {
                results.add(item);
            } else {
                RoleResponse.TreeDto parent = mapResources.get(item.getParentId());
                parent.addChild(item);
            }
        });

        listResources.stream().forEach(item -> {
            item.addChild(mapPermissions.get(item.getNodeId()), true);
        });

        return results;

    }

    @Override
    public List<String> getSelectedPermissions(Long roleId) {
        List<String> results = rolesRepository.getSelectedResources(roleId);
        results.addAll(rolesRepository.getSelectedPermissions(roleId));
        return results;
    }

    @Override
    public List<RoleResponse.DetailBean> getListRoles() {
        return rolesRepository.getListRoles();
    }

}
