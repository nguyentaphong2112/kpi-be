/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseApproveRequest;
import vn.hbtplus.insurance.models.request.EmployeeChangesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.EmployeeChangesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.insurance.repositories.entity.EmployeeChangesEntity;
import vn.hbtplus.insurance.repositories.impl.EmployeeChangesRepository;
import vn.hbtplus.insurance.repositories.jpa.EmployeeChangesRepositoryJPA;
import vn.hbtplus.insurance.services.EmployeeChangesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang icn_employee_changes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class EmployeeChangesServiceImpl implements EmployeeChangesService {

    private final EmployeeChangesRepository employeeChangesRepository;
    private final EmployeeChangesRepositoryJPA employeeChangesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmployeeChangesResponse> searchData(EmployeeChangesRequest.SearchForm dto) {
        return ResponseUtils.ok(employeeChangesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EmployeeChangesRequest.SubmitForm dto) throws BaseAppException {
        EmployeeChangesEntity entity;
        if (dto.getEmployeeChangeId() != null && dto.getEmployeeChangeId() > 0L) {
            entity = employeeChangesRepositoryJPA.getById(dto.getEmployeeChangeId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeeChangesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setContributionType(dto.getContributionType());
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        employeeChangesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getEmployeeChangeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmployeeChangesEntity> optional = employeeChangesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeChangesEntity.class);
        }
        employeeChangesRepository.deActiveObject(EmployeeChangesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EmployeeChangesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<EmployeeChangesEntity> optional = employeeChangesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeChangesEntity.class);
        }
        EmployeeChangesResponse dto = new EmployeeChangesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeeChangesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/BM_Ra_soat_du_lieu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = employeeChangesRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Ra_soat_du_lieu.xlsx");
    }

    @Override
    public boolean makeList(EmployeeChangesRequest.MakeListForm dto) {
        //Lay danh sach
        List<EmployeeChangesEntity> list = employeeChangesRepository.getListEmployeeChange(dto.getPeriodDate());
        if (Utils.isNullOrEmpty(list)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        employeeChangesRepository.insertOrUpdate(list, Utils.getLastDay(dto.getPeriodDate()));
        return true;
    }

    @Override
    public List<Long> updateStatusById(BaseApproveRequest dto, String status) throws BaseAppException {
        if (dto.getListId() == null || dto.getListId().isEmpty()) {
            throw new BaseAppException(I18n.getMessage("global.badRequest"));
        }

        List<EmployeeChangesEntity> listData = employeeChangesRepository.findByListId(EmployeeChangesEntity.class, dto.getListId());
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        for (EmployeeChangesEntity entity : listData) {
            if (status.equals(entity.getStatus())) {
                throw new BaseAppException(I18n.getMessage("global.badRequest"));
            }
        }
        employeeChangesRepository.updateStatus(dto.getListId(), status);
        return dto.getListId();
    }

    @Override
    public List<Long> updateStatus(EmployeeChangesRequest.SearchForm dto, String status) throws BaseAppException {
        List<EmployeeChangesEntity> listEntity = employeeChangesRepository.getListDataByForm(dto);
        if (listEntity == null || listEntity.isEmpty()) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        List<Long> ids = listEntity.stream()
                .filter(entity -> !status.equals(entity.getStatus()))
                .map(EmployeeChangesEntity::getEmployeeChangeId)
                .collect(Collectors.toList());
        employeeChangesRepository.updateStatus(ids, status);
        return ids;
    }

}
