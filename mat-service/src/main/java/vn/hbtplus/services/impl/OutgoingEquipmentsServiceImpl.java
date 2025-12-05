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
import vn.hbtplus.models.request.OutgoingEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.OutgoingEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.OutgoingEquipmentsEntity;
import vn.hbtplus.repositories.impl.OutgoingEquipmentsRepository;
import vn.hbtplus.repositories.jpa.OutgoingEquipmentsRepositoryJPA;
import vn.hbtplus.services.OutgoingEquipmentsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang stk_outgoing_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class OutgoingEquipmentsServiceImpl implements OutgoingEquipmentsService {

    private final OutgoingEquipmentsRepository outgoingEquipmentsRepository;
    private final OutgoingEquipmentsRepositoryJPA outgoingEquipmentsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OutgoingEquipmentsResponse> searchData(OutgoingEquipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(outgoingEquipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OutgoingEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        OutgoingEquipmentsEntity entity;
        if (dto.getOutgoingEquipmentId() != null && dto.getOutgoingEquipmentId() > 0L) {
            entity = outgoingEquipmentsRepositoryJPA.getById(dto.getOutgoingEquipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OutgoingEquipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        outgoingEquipmentsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getOutgoingEquipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OutgoingEquipmentsEntity> optional = outgoingEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OutgoingEquipmentsEntity.class);
        }
        outgoingEquipmentsRepository.deActiveObject(OutgoingEquipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OutgoingEquipmentsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<OutgoingEquipmentsEntity> optional = outgoingEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OutgoingEquipmentsEntity.class);
        }
        OutgoingEquipmentsResponse dto = new OutgoingEquipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(OutgoingEquipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = outgoingEquipmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }


}
