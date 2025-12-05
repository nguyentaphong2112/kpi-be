/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.PytagoValuesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.PytagoValuesEntity;
import vn.hbtplus.repositories.impl.PytagoValuesRepository;
import vn.hbtplus.repositories.jpa.PytagoValuesRepositoryJPA;
import vn.hbtplus.services.PytagoValuesService;
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
 * Lop impl service ung voi bang crm_pytago_values
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class PytagoValuesServiceImpl implements PytagoValuesService {

    private final PytagoValuesRepository pytagoValuesRepository;
    private final PytagoValuesRepositoryJPA pytagoValuesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PytagoValuesResponse> searchData(PytagoValuesRequest.SearchForm dto) {
        return ResponseUtils.ok(pytagoValuesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(PytagoValuesRequest.SubmitForm dto) throws BaseAppException {
        PytagoValuesEntity entity;
        if (dto.getPytagoValueId() != null && dto.getPytagoValueId() > 0L) {
            entity = pytagoValuesRepositoryJPA.getById(dto.getPytagoValueId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new PytagoValuesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        pytagoValuesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getPytagoValueId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<PytagoValuesEntity> optional = pytagoValuesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PytagoValuesEntity.class);
        }
        pytagoValuesRepository.deActiveObject(PytagoValuesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PytagoValuesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<PytagoValuesEntity> optional = pytagoValuesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PytagoValuesEntity.class);
        }
        PytagoValuesResponse dto = new PytagoValuesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(PytagoValuesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = pytagoValuesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
