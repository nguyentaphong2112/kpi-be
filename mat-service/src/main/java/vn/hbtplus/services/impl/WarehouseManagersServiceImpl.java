/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WarehouseManagersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehouseManagersResponse;
import vn.hbtplus.repositories.entity.WarehouseManagersEntity;
import vn.hbtplus.repositories.impl.WarehouseManagersRepository;
import vn.hbtplus.repositories.jpa.WarehouseManagersRepositoryJPA;
import vn.hbtplus.services.WarehouseManagersService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang stk_warehouse_managers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class WarehouseManagersServiceImpl implements WarehouseManagersService {

    private final WarehouseManagersRepository warehouseManagersRepository;
    private final WarehouseManagersRepositoryJPA warehouseManagersRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WarehouseManagersResponse> searchData(WarehouseManagersRequest.SearchForm dto) {
        return ResponseUtils.ok(warehouseManagersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(WarehouseManagersRequest.SubmitForm dto) throws BaseAppException {
        WarehouseManagersEntity entity;
        if (dto.getWarehouseManagerId() != null && dto.getWarehouseManagerId() > 0L) {
            entity = warehouseManagersRepositoryJPA.getById(dto.getWarehouseManagerId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WarehouseManagersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        warehouseManagersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getWarehouseManagerId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WarehouseManagersEntity> optional = warehouseManagersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarehouseManagersEntity.class);
        }
        warehouseManagersRepository.deActiveObject(WarehouseManagersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WarehouseManagersResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<WarehouseManagersEntity> optional = warehouseManagersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarehouseManagersEntity.class);
        }
        WarehouseManagersResponse dto = new WarehouseManagersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(WarehouseManagersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = warehouseManagersRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
