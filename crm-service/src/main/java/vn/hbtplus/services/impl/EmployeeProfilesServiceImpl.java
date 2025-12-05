/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.EmployeeProfilesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.EmployeeProfilesEntity;
import vn.hbtplus.repositories.impl.EmployeeProfilesRepository;
import vn.hbtplus.repositories.jpa.EmployeeProfilesRepositoryJPA;
import vn.hbtplus.services.EmployeeProfilesService;
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
 * Lop impl service ung voi bang crm_employee_profiles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class EmployeeProfilesServiceImpl implements EmployeeProfilesService {

    private final EmployeeProfilesRepository employeeProfilesRepository;
    private final EmployeeProfilesRepositoryJPA employeeProfilesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmployeeProfilesResponse> searchData(EmployeeProfilesRequest.SearchForm dto) {
        return ResponseUtils.ok(employeeProfilesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EmployeeProfilesRequest.SubmitForm dto) throws BaseAppException {
        EmployeeProfilesEntity entity;
        if (dto.getEmployeeProfileId() != null && dto.getEmployeeProfileId() > 0L) {
            entity = employeeProfilesRepositoryJPA.getById(dto.getEmployeeProfileId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeeProfilesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        employeeProfilesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getEmployeeProfileId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmployeeProfilesEntity> optional = employeeProfilesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeProfilesEntity.class);
        }
        employeeProfilesRepository.deActiveObject(EmployeeProfilesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EmployeeProfilesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<EmployeeProfilesEntity> optional = employeeProfilesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeProfilesEntity.class);
        }
        EmployeeProfilesResponse dto = new EmployeeProfilesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeeProfilesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = employeeProfilesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
