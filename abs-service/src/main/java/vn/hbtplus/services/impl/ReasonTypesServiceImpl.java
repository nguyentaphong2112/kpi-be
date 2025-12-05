/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.ReasonTypesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ReasonTypesEntity;
import vn.hbtplus.repositories.impl.ReasonTypesRepository;
import vn.hbtplus.repositories.jpa.ReasonTypesRepositoryJPA;
import vn.hbtplus.repositories.entity.ReasonTypesEntity;
import vn.hbtplus.services.ReasonTypesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.*;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang abs_reason_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ReasonTypesServiceImpl implements ReasonTypesService {

    private final ReasonTypesRepository reasonTypesRepository;
    private final ReasonTypesRepositoryJPA reasonTypesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ReasonTypesResponse> searchData(ReasonTypesRequest.SearchForm dto) {
        return ResponseUtils.ok(reasonTypesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(ReasonTypesRequest.SubmitForm dto, Long id) throws BaseAppException {
        ReasonTypesEntity entity;
        boolean isDuplicate = reasonTypesRepository.duplicate(ReasonTypesEntity.class, id, "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_REASONTYPES_DUPLICATE", I18n.getMessage("error.reasonTypes.code.duplicate"));
        }

        if (id != null && id > 0L) {
            entity = reasonTypesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ReasonTypesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        reasonTypesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getReasonTypeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ReasonTypesEntity> optional = reasonTypesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ReasonTypesEntity.class);
        }
        reasonTypesRepository.deActiveObject(ReasonTypesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ReasonTypesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<ReasonTypesEntity> optional = reasonTypesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ReasonTypesEntity.class);
        }
        ReasonTypesResponse dto = new ReasonTypesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ReasonTypesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = reasonTypesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }


    @Override
    public List<ReasonTypesResponse> getAllReasonLeaves() {
        return Utils.mapAll(reasonTypesRepositoryJPA.getListReasonLeavesEntitys(BaseConstants.STATUS.NOT_DELETED), ReasonTypesResponse.class);
    }

    @Override
    public ListResponseEntity<ReasonTypesResponse> getList(boolean isGetAttributes) {
        List<ReasonTypesResponse> results = reasonTypesRepository.getListResponseType();
        if (!isGetAttributes || results.isEmpty()) {
            return ResponseUtils.ok(results);
        }

        return ResponseUtils.ok(results);
    }
}
