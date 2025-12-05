/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.WorkedHistoriesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.models.response.WorkedHistoriesResponse;
import vn.kpi.repositories.entity.WorkedHistoriesEntity;
import vn.kpi.repositories.impl.WorkedHistoriesRepository;
import vn.kpi.repositories.jpa.WorkedHistoriesRepositoryJPA;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.WorkedHistoriesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_worked_histories
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class WorkedHistoriesServiceImpl implements WorkedHistoriesService {

    private final WorkedHistoriesRepository workedHistoriesRepository;
    private final WorkedHistoriesRepositoryJPA workedHistoriesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WorkedHistoriesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(workedHistoriesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(WorkedHistoriesRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException {
        WorkedHistoriesEntity entity;
        if (id != null && id > 0L) {
            entity = workedHistoriesRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("workedHistoryId and employeeId not matching!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WorkedHistoriesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        workedHistoriesRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getWorkedHistoryId(), dto.getListAttributes(), WorkedHistoriesEntity.class, null);
        return ResponseUtils.ok(entity.getWorkedHistoryId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<WorkedHistoriesEntity> optional = workedHistoriesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkedHistoriesEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("workedHistoryId and employeeId not matching!");
        }
        workedHistoriesRepository.deActiveObject(WorkedHistoriesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WorkedHistoriesResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        WorkedHistoriesResponse.DetailBean dto = workedHistoriesRepository.getDataById(id);
        if (dto == null) {
            throw new RecordNotExistsException(id, WorkedHistoriesEntity.class);
        }
        if (!dto.getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("workedHistoryId and employeeId not matching!");
        }
        dto.setListAttributes(objectAttributesService.getAttributes(id, workedHistoriesRepository.getSQLTableName(WorkedHistoriesEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/qua-trinh-cong-tac-truoc-tuyen-dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = workedHistoriesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "qua-trinh-cong-tac-truoc-tuyen-dung.xlsx");
    }

    @Override
    public BaseDataTableDto<WorkedHistoriesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<WorkedHistoriesResponse.DetailBean> tableDto = workedHistoriesRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getWorkedHistoryId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_worked_histories");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getWorkedHistoryId()));
        });
        return tableDto;
    }

}
