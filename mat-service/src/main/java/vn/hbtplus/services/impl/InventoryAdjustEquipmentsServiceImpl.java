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
import vn.hbtplus.models.request.InventoryAdjustEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.InventoryAdjustEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.InventoryAdjustEquipmentsEntity;
import vn.hbtplus.repositories.impl.InventoryAdjustEquipmentsRepository;
import vn.hbtplus.repositories.jpa.InventoryAdjustEquipmentsRepositoryJPA;
import vn.hbtplus.services.InventoryAdjustEquipmentsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang stk_inventory_adjust_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class InventoryAdjustEquipmentsServiceImpl implements InventoryAdjustEquipmentsService {

    private final InventoryAdjustEquipmentsRepository inventoryAdjustEquipmentsRepository;
    private final InventoryAdjustEquipmentsRepositoryJPA inventoryAdjustEquipmentsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InventoryAdjustEquipmentsResponse> searchData(InventoryAdjustEquipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(inventoryAdjustEquipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(InventoryAdjustEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        InventoryAdjustEquipmentsEntity entity;
        if (dto.getInventoryAdjustEquipmentId() != null && dto.getInventoryAdjustEquipmentId() > 0L) {
            entity = inventoryAdjustEquipmentsRepositoryJPA.getById(dto.getInventoryAdjustEquipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new InventoryAdjustEquipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        inventoryAdjustEquipmentsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getInventoryAdjustEquipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<InventoryAdjustEquipmentsEntity> optional = inventoryAdjustEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InventoryAdjustEquipmentsEntity.class);
        }
        inventoryAdjustEquipmentsRepository.deActiveObject(InventoryAdjustEquipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<InventoryAdjustEquipmentsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<InventoryAdjustEquipmentsEntity> optional = inventoryAdjustEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InventoryAdjustEquipmentsEntity.class);
        }
        InventoryAdjustEquipmentsResponse dto = new InventoryAdjustEquipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(InventoryAdjustEquipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = inventoryAdjustEquipmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
