/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.WorkdayTypesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ReasonTypesEntity;
import vn.hbtplus.repositories.entity.WorkdayTypesEntity;
import vn.hbtplus.repositories.impl.WorkdayTypesRepository;
import vn.hbtplus.repositories.jpa.WorkdayTypesRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.WorkdayTypesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.I18n;
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
 * Lop impl service ung voi bang abs_workday_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class WorkdayTypesServiceImpl implements WorkdayTypesService {

    private final WorkdayTypesRepository workdayTypesRepository;
    private final WorkdayTypesRepositoryJPA workdayTypesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WorkdayTypesResponse> searchData(WorkdayTypesRequest.SearchForm dto) {
        return ResponseUtils.ok(workdayTypesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity saveData(WorkdayTypesRequest.SubmitForm dto , Long id) throws BaseAppException {
        WorkdayTypesEntity entity;
        boolean isDuplicate = workdayTypesRepository.duplicate(WorkdayTypesEntity.class, id, "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_WORKDAYTYPES_DUPLICATE", I18n.getMessage("error.workdayTypes.code.duplicate"));
        }
        if (id != null && id > 0L) {
            entity = workdayTypesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WorkdayTypesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        workdayTypesRepositoryJPA.save(entity);

        objectAttributesService.saveObjectAttributes(entity.getWorkdayTypeId(), dto.getListAttributes(), WorkdayTypesEntity.class, null);
        return ResponseUtils.ok(entity.getWorkdayTypeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WorkdayTypesEntity> optional = workdayTypesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkdayTypesEntity.class);
        }
        workdayTypesRepository.deActiveObject(WorkdayTypesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WorkdayTypesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<WorkdayTypesEntity> optional = workdayTypesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkdayTypesEntity.class);
        }
        WorkdayTypesResponse dto = new WorkdayTypesResponse();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, workdayTypesRepository.getSQLTableName(WorkdayTypesEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(WorkdayTypesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = workdayTypesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<WorkdayTypesResponse> getList() {
        return workdayTypesRepository.getList();
    }

}
