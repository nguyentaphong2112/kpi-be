/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.ShipmentsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ShipmentsEntity;
import vn.hbtplus.repositories.impl.ShipmentsRepository;
import vn.hbtplus.repositories.jpa.ShipmentsRepositoryJPA;
import vn.hbtplus.services.ShipmentsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang crm_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ShipmentsServiceImpl implements ShipmentsService {

    private final ShipmentsRepository shipmentsRepository;
    private final ShipmentsRepositoryJPA shipmentsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ShipmentsResponse> searchData(ShipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(shipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ShipmentsRequest.SubmitForm dto) throws BaseAppException {
        ShipmentsEntity entity;
        if (dto.getShipmentId() != null && dto.getShipmentId() > 0L) {
            entity = shipmentsRepositoryJPA.getById(dto.getShipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ShipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        shipmentsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getShipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ShipmentsEntity> optional = shipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ShipmentsEntity.class);
        }
        shipmentsRepository.deActiveObject(ShipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ShipmentsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<ShipmentsEntity> optional = shipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ShipmentsEntity.class);
        }
        ShipmentsResponse dto = new ShipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ShipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = shipmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
