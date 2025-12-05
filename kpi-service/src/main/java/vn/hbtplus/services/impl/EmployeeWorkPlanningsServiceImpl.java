/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.EmployeeWorkPlanningsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.EmployeeWorkPlanningsEntity;
import vn.hbtplus.repositories.entity.ObjectAttributesEntity;
import vn.hbtplus.repositories.entity.OrganizationWorkPlanningsEntity;
import vn.hbtplus.repositories.impl.EmployeeWorkPlanningsRepository;
import vn.hbtplus.repositories.jpa.EmployeeIndicatorsRepositoryJPA;
import vn.hbtplus.repositories.jpa.EmployeeWorkPlanningsRepositoryJPA;
import vn.hbtplus.services.EmployeeWorkPlanningsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang kpi_employee_work_plannings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EmployeeWorkPlanningsServiceImpl implements EmployeeWorkPlanningsService {

    private final EmployeeWorkPlanningsRepository employeeWorkPlanningsRepository;
    private final EmployeeWorkPlanningsRepositoryJPA employeeWorkPlanningsRepositoryJPA;
    private final EmployeeIndicatorsRepositoryJPA employeeIndicatorsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmployeeWorkPlanningsResponse.SearchForm> searchData(EmployeeWorkPlanningsRequest.SearchForm dto) {
        return ResponseUtils.ok(employeeWorkPlanningsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EmployeeWorkPlanningsRequest.SubmitForm dto, Long id) throws BaseAppException {
        EmployeeWorkPlanningsEntity entity;
        if (id != null && id > 0L) {
            entity = employeeWorkPlanningsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeeWorkPlanningsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        employeeWorkPlanningsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getEmployeeWorkPlanningId());
    }

    @Override
    public ResponseEntity saveListData(EmployeeWorkPlanningsRequest.ListData dto) throws BaseAppException {
        List<EmployeeWorkPlanningsEntity> listSave = new ArrayList<>();
        for (EmployeeWorkPlanningsRequest.SubmitForm data : dto.getListData()) {
            EmployeeWorkPlanningsEntity entity;
            entity = new EmployeeWorkPlanningsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            Utils.copyProperties(data, entity);
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            listSave.add(entity);
        }
        employeeWorkPlanningsRepository.insertBatch(EmployeeWorkPlanningsEntity.class, listSave, Utils.getUserNameLogin());
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmployeeWorkPlanningsEntity> optional = employeeWorkPlanningsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeWorkPlanningsEntity.class);
        }
        employeeWorkPlanningsRepository.deActiveObject(EmployeeWorkPlanningsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EmployeeWorkPlanningsResponse.SearchForm> getDataById(Long id) throws RecordNotExistsException {
        Optional<EmployeeWorkPlanningsEntity> optional = employeeWorkPlanningsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeWorkPlanningsEntity.class);
        }
        EmployeeWorkPlanningsResponse.SearchForm dto = new EmployeeWorkPlanningsResponse.SearchForm();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeeWorkPlanningsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = employeeWorkPlanningsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ResponseEntity getDataByEvaluationId(Long id) throws RecordNotExistsException {
        EmployeeWorkPlanningsResponse.WorkPlanningData result = new EmployeeWorkPlanningsResponse.WorkPlanningData();
        result.setListData(employeeWorkPlanningsRepository.getListEmployeeWorkPlanning(id));
        result.setEmpEvaluationIds(employeeIndicatorsRepositoryJPA.findIndicatorIdsByEmployeeEvaluationId(id));
        return ResponseUtils.ok(result);
    }

}
