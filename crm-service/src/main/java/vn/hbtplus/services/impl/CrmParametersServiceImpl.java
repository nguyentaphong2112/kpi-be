/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.ParametersRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ParametersEntity;
import vn.hbtplus.repositories.impl.CrmParametersRepository;
import vn.hbtplus.repositories.jpa.ParametersRepositoryJPA;
import vn.hbtplus.services.CrmParametersService;
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
 * Lop impl service ung voi bang icn_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CrmParametersServiceImpl implements CrmParametersService {

    private final CrmParametersRepository parametersRepository;
    private final ParametersRepositoryJPA parametersRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ParametersResponse> searchData(ParametersRequest.SearchForm dto) {
        return ResponseUtils.ok(parametersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ParametersRequest.SubmitForm dto) throws BaseAppException {
        ParametersEntity entity;
        if (dto.getParameterId() != null && dto.getParameterId() > 0L) {
            entity = parametersRepositoryJPA.getById(dto.getParameterId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ParametersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        parametersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getParameterId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ParametersEntity> optional = parametersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ParametersEntity.class);
        }
        parametersRepository.deActiveObject(ParametersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ParametersResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<ParametersEntity> optional = parametersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ParametersEntity.class);
        }
        ParametersResponse dto = new ParametersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ParametersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = parametersRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public <T> T getConfig(Class<T> className, Date date)
            throws InstantiationException, IllegalAccessException, BaseAppException {
        return null;
    }

    @Override
    public <T> T getConfigValue(String configCode, Date date, Class<T> className) {
        return null;
    }

}
