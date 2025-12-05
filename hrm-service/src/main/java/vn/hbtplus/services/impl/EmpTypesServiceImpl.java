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
import vn.hbtplus.models.request.EmpTypesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.DocumentTypesEntity;
import vn.hbtplus.repositories.entity.EmpTypesEntity;
import vn.hbtplus.repositories.impl.EmpTypesRepository;
import vn.hbtplus.repositories.jpa.EmpTypesRepositoryJPA;
import vn.hbtplus.services.EmpTypesService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_emp_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EmpTypesServiceImpl implements EmpTypesService {

    private final EmpTypesRepository empTypesRepository;
    private final EmpTypesRepositoryJPA empTypesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmpTypesResponse.SearchResult> searchData(EmpTypesRequest.SearchForm dto) {
        return ResponseUtils.ok(empTypesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EmpTypesRequest.SubmitForm dto, Long empTypeId) throws BaseAppException {

        boolean isDuplicate = empTypesRepository.duplicate(DocumentTypesEntity.class, empTypeId, "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_EMP_DUPLICATE", I18n.getMessage("error.empTypes.code.duplicate"));
        }

        isDuplicate = empTypesRepository.duplicate(DocumentTypesEntity.class, empTypeId, "name", dto.getName());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_EMP_DUPLICATE", I18n.getMessage("error.empTypes.name.duplicate"));
        }

        EmpTypesEntity entity;
        if (empTypeId != null && empTypeId > 0L) {
            entity = empTypesRepositoryJPA.getById(empTypeId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmpTypesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        empTypesRepositoryJPA.save(entity);
        empTypesRepositoryJPA.flush();

        objectAttributesService.saveObjectAttributes(entity.getEmpTypeId(), dto.getListAttributes(), EmpTypesEntity.class, null);

        return ResponseUtils.ok(entity.getEmpTypeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmpTypesEntity> optional = empTypesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmpTypesEntity.class);
        }
        empTypesRepository.deActiveObject(EmpTypesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EmpTypesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<EmpTypesEntity> optional = empTypesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmpTypesEntity.class);
        }
        EmpTypesResponse.DetailBean dto = new EmpTypesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, empTypesRepository.getSQLTableName(EmpTypesEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ListResponseEntity<EmpTypesResponse.DetailBean> getList(boolean isGetAttributes) {
        List<EmpTypesResponse.DetailBean> results = empTypesRepository.getListEmpType();
        if (!isGetAttributes || results.isEmpty()) {
            return ResponseUtils.ok(results);
        }

        List<Long> ids = results.stream().map(EmpTypesResponse.DetailBean::getEmpTypeId).toList();
        Map<Long, List<ObjectAttributesResponse>> maps = objectAttributesService.getListMapAttributes(ids, "hr_emp_types");
        results.forEach(item -> item.setListAttributes(maps.get(item.getEmpTypeId())));
        return ResponseUtils.ok(results);
    }

    @Override
    public ResponseEntity<Object> exportData(EmpTypesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = empTypesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
