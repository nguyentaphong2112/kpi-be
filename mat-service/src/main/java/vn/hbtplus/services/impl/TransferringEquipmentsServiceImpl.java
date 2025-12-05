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
import vn.hbtplus.models.request.TransferringEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TransferringEquipmentsResponse;
import vn.hbtplus.repositories.entity.TransferringEquipmentsEntity;
import vn.hbtplus.repositories.impl.TransferringEquipmentsRepository;
import vn.hbtplus.repositories.jpa.TransferringEquipmentsRepositoryJPA;
import vn.hbtplus.services.TransferringEquipmentsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang stk_transferring_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class TransferringEquipmentsServiceImpl implements TransferringEquipmentsService {

    private final TransferringEquipmentsRepository transferringEquipmentsRepository;
    private final TransferringEquipmentsRepositoryJPA transferringEquipmentsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TransferringEquipmentsResponse> searchData(TransferringEquipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(transferringEquipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(TransferringEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        TransferringEquipmentsEntity entity;
        if (dto.getTransferringEquipmentId() != null && dto.getTransferringEquipmentId() > 0L) {
            entity = transferringEquipmentsRepositoryJPA.getById(dto.getTransferringEquipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new TransferringEquipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        transferringEquipmentsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getTransferringEquipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<TransferringEquipmentsEntity> optional = transferringEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TransferringEquipmentsEntity.class);
        }
        transferringEquipmentsRepository.deActiveObject(TransferringEquipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TransferringEquipmentsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<TransferringEquipmentsEntity> optional = transferringEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TransferringEquipmentsEntity.class);
        }
        TransferringEquipmentsResponse dto = new TransferringEquipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(TransferringEquipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = transferringEquipmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
