/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.IndicatorUsingScopesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.IndicatorUsingScopesEntity;
import vn.hbtplus.repositories.impl.IndicatorUsingScopesRepository;
import vn.hbtplus.repositories.jpa.IndicatorUsingScopesRepositoryJPA;
import vn.hbtplus.services.IndicatorUsingScopesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang kpi_indicator_using_scopes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class IndicatorUsingScopesServiceImpl implements IndicatorUsingScopesService {

    private final IndicatorUsingScopesRepository indicatorUsingScopesRepository;
    private final IndicatorUsingScopesRepositoryJPA indicatorUsingScopesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<IndicatorUsingScopesResponse> searchData(IndicatorUsingScopesRequest.SearchForm dto) {
        return ResponseUtils.ok(indicatorUsingScopesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(IndicatorUsingScopesRequest.SubmitForm dto) throws BaseAppException {
//        KpiIndicatorUsingScopesEntity entity;
//        if (dto.getId() != null && dto.getId() > 0L) {
//            entity = kpiIndicatorUsingScopesRepositoryJPA.getById(dto.getId());
//            entity.setModifiedTime(new Date());
//            entity.setModifiedBy(Utils.getUserNameLogin());
//        } else {
//            entity = new KpiIndicatorUsingScopesEntity();
//            entity.setCreatedTime(new Date());
//            entity.setCreatedBy(Utils.getUserNameLogin());
//        }
//        Utils.copyProperties(dto, entity);
//        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
//        kpiIndicatorUsingScopesRepositoryJPA.save(entity);
//        return ResponseUtils.ok(entity.getId());
        return  null;
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<IndicatorUsingScopesEntity> optional = indicatorUsingScopesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorUsingScopesEntity.class);
        }
        indicatorUsingScopesRepository.deActiveObject(IndicatorUsingScopesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IndicatorUsingScopesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<IndicatorUsingScopesEntity> optional = indicatorUsingScopesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorUsingScopesEntity.class);
        }
        IndicatorUsingScopesResponse dto = new IndicatorUsingScopesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(IndicatorUsingScopesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = indicatorUsingScopesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
