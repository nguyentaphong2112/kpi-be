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
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.AllowanceProcessRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.AllowanceProcessResponse;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.AllowanceProcessEntity;
import vn.hbtplus.repositories.impl.AllowanceProcessRepository;
import vn.hbtplus.repositories.jpa.AllowanceProcessRepositoryJPA;
import vn.hbtplus.services.AllowanceProcessService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_allowance_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class AllowanceProcessServiceImpl implements AllowanceProcessService {

    private final AllowanceProcessRepository allowanceProcessRepository;
    private final AllowanceProcessRepositoryJPA allowanceProcessRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<AllowanceProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(allowanceProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(AllowanceProcessRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException {
        boolean isConflictProcess = allowanceProcessRepository.isConflictProcess(dto, employeeId, id);
        if (isConflictProcess) {
            throw new BaseAppException("ERROR_CONFLICT_ALLOWANCE", I18n.getMessage("allowanceProcess.validate.conflict"));
        }
        AllowanceProcessEntity entity;
        if (id != null && id > 0L) {
            entity = allowanceProcessRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("allowanceProcessId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());

        } else {
            entity = new AllowanceProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        allowanceProcessRepositoryJPA.saveAndFlush(entity);
        objectAttributesService.saveObjectAttributes(entity.getAllowanceProcessId(), dto.getListAttributes(), AllowanceProcessEntity.class, null);
        allowanceProcessRepository.autoUpdateToDate(entity.getEmployeeId(), entity.getAllowanceTypeId());
        return ResponseUtils.ok(entity.getAllowanceProcessId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<AllowanceProcessEntity> optional = allowanceProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AllowanceProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("allowanceProcessId and employeeId not match!");
        }
        allowanceProcessRepository.deActiveObject(AllowanceProcessEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<AllowanceProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        AllowanceProcessResponse.DetailBean data = allowanceProcessRepository.getDataById(id);
        if (data == null) {
            throw new RecordNotExistsException(id, AllowanceProcessEntity.class);
        }
        if (!data.getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("allowanceProcessId and employeeId not match!");
        }
        data.setListAttributes(objectAttributesService.getAttributes(id, allowanceProcessRepository.getSQLTableName(AllowanceProcessEntity.class)));
        return ResponseUtils.ok(data);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/dien-bien-phu-cap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = allowanceProcessRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "dien-bien-phu-cap.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<AllowanceProcessResponse.SearchResult> tableDto = allowanceProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getAllowanceProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_education_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getAllowanceProcessId()));
        });
        return tableDto;
    }

}
