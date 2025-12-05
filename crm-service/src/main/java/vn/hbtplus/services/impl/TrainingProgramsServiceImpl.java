/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.TrainingProgramsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.TrainingProgramsEntity;
import vn.hbtplus.repositories.impl.TrainingProgramsRepository;
import vn.hbtplus.repositories.jpa.TrainingProgramsRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.TrainingProgramsService;
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
 * Lop impl service ung voi bang crm_training_programs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class TrainingProgramsServiceImpl implements TrainingProgramsService {

    private final TrainingProgramsRepository trainingProgramsRepository;
    private final TrainingProgramsRepositoryJPA trainingProgramsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TrainingProgramsResponse.SearchResult> searchData(TrainingProgramsRequest.SearchForm dto) {
        return ResponseUtils.ok(trainingProgramsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(TrainingProgramsRequest.SubmitForm dto, Long id) throws BaseAppException {
        TrainingProgramsEntity entity;
        if (id != null && id > 0L) {
            entity = trainingProgramsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new TrainingProgramsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        trainingProgramsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getTrainingProgramId(), dto.getListAttributes(), TrainingProgramsEntity.class, null);
        return ResponseUtils.ok(entity.getTrainingProgramId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<TrainingProgramsEntity> optional = trainingProgramsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TrainingProgramsEntity.class);
        }
        trainingProgramsRepository.deActiveObject(TrainingProgramsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TrainingProgramsResponse.DetailBean> getDataById(Long id)  throws RecordNotExistsException {
        Optional<TrainingProgramsEntity> optional = trainingProgramsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TrainingProgramsEntity.class);
        }
        TrainingProgramsResponse.DetailBean dto = new TrainingProgramsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, trainingProgramsRepository.getSQLTableName(TrainingProgramsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(TrainingProgramsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = trainingProgramsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ListResponseEntity<TrainingProgramsResponse.DataSelected> getListData() {
        return ResponseUtils.ok(trainingProgramsRepository.getListData());
    }

}
