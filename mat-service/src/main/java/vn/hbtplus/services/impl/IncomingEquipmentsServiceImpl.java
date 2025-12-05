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
import vn.hbtplus.models.request.IncomingEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.IncomingEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.IncomingEquipmentsEntity;
import vn.hbtplus.repositories.impl.IncomingEquipmentsRepository;
import vn.hbtplus.repositories.jpa.IncomingEquipmentsRepositoryJPA;
import vn.hbtplus.services.IncomingEquipmentsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang stk_incoming_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class IncomingEquipmentsServiceImpl implements IncomingEquipmentsService {

    private final IncomingEquipmentsRepository incomingEquipmentsRepository;
    private final IncomingEquipmentsRepositoryJPA incomingEquipmentsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<IncomingEquipmentsResponse> searchData(IncomingEquipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(incomingEquipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(IncomingEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        IncomingEquipmentsEntity entity;
        if (dto.getIncomingEquipmentId() != null && dto.getIncomingEquipmentId() > 0L) {
            entity = incomingEquipmentsRepositoryJPA.getById(dto.getIncomingEquipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new IncomingEquipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        incomingEquipmentsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getIncomingEquipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<IncomingEquipmentsEntity> optional = incomingEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomingEquipmentsEntity.class);
        }
        incomingEquipmentsRepository.deActiveObject(IncomingEquipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IncomingEquipmentsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<IncomingEquipmentsEntity> optional = incomingEquipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomingEquipmentsEntity.class);
        }
        IncomingEquipmentsResponse dto = new IncomingEquipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(IncomingEquipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = incomingEquipmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
