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
import vn.hbtplus.models.request.WarehouseEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehouseEquipmentsResponse;
import vn.hbtplus.repositories.entity.WarehouseEquipmentsEntity;
import vn.hbtplus.repositories.impl.WarehouseEquipmentsRepository;
import vn.hbtplus.repositories.impl.WarehousesRepository;
import vn.hbtplus.repositories.jpa.WarehouseEquipmentsRepositoryJPA;
import vn.hbtplus.services.WarehouseEquipmentsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang stk_warehouse_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class WarehouseEquipmentsServiceImpl implements WarehouseEquipmentsService {

    private final WarehouseEquipmentsRepository warehouseEquipmentsRepository;
    private final WarehouseEquipmentsRepositoryJPA warehouseEquipmentsRepositoryJPA;
    private final WarehousesRepository warehousesRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WarehouseEquipmentsResponse> searchData(WarehouseEquipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(warehouseEquipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(WarehouseEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        WarehouseEquipmentsEntity entity;
        if (dto.getWarehouseEquipmentId() != null && dto.getWarehouseEquipmentId() > 0L) {
            entity = warehouseEquipmentsRepositoryJPA.getById(dto.getWarehouseEquipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WarehouseEquipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        if(dto.getUnitPrice() != null && dto.getUnitPrice() > 0L) {
            entity.setUpdatePriceTime(new Date());
        }
        warehouseEquipmentsRepositoryJPA.save(entity);
        //cap nhat don gia hien tai trong lich su
        warehouseEquipmentsRepository.updateHistory(entity.getWarehouseId(), entity.getEquipmentId(), entity.getUnitPrice());
        return ResponseUtils.ok(entity.getWarehouseEquipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WarehouseEquipmentsEntity> optional = warehouseEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarehouseEquipmentsEntity.class);
        }
        warehouseEquipmentsRepository.deActiveObject(WarehouseEquipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WarehouseEquipmentsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<WarehouseEquipmentsEntity> optional = warehouseEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarehouseEquipmentsEntity.class);
        }
        WarehouseEquipmentsResponse dto = new WarehouseEquipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(WarehouseEquipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = warehouseEquipmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    @Transactional
    public void updateWarehouseEquipments(Long warehouseId, Long objectId, String type) {
        //update du lieu vat tu trong kho
        warehousesRepository.updateWarehouseEquipment(warehouseId, objectId, type);
        //update dữ liệu lịch sử
        warehousesRepository.updateHistories(warehouseId, objectId, type);
    }

    @Override
    @Transactional
    public void initHistory(Date periodDate) {
        warehousesRepository.initHistory(periodDate);
    }

}
