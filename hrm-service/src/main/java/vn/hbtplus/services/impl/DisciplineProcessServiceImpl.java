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
import vn.hbtplus.models.request.DisciplineProcessRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.DisciplineProcessResponse;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.DisciplineProcessEntity;
import vn.hbtplus.repositories.impl.DisciplineProcessRepository;
import vn.hbtplus.repositories.jpa.DisciplineProcessRepositoryJPA;
import vn.hbtplus.services.DisciplineProcessService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_discipline_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class DisciplineProcessServiceImpl implements DisciplineProcessService {

    private final DisciplineProcessRepository disciplineProcessRepository;
    private final DisciplineProcessRepositoryJPA disciplineProcessRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DisciplineProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(disciplineProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(DisciplineProcessRequest.SubmitForm dto, Long employeeId,Long id) throws BaseAppException {
        DisciplineProcessEntity entity;
        if (id != null && id > 0L) {
            entity = disciplineProcessRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            if(!entity.getEmployeeId().equals(employeeId)){
                throw new BaseAppException("disciplineProcessId and employeeId not matching!");
            }
        } else {
            entity = new DisciplineProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        disciplineProcessRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getDisciplineProcessId(), dto.getListAttributes(), DisciplineProcessEntity.class, null);
        return ResponseUtils.ok(entity.getDisciplineProcessId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId,Long id) throws BaseAppException {
        Optional<DisciplineProcessEntity> optional = disciplineProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DisciplineProcessEntity.class);
        }
        if(!optional.get().getEmployeeId().equals(employeeId)){
            throw new BaseAppException("disciplineProcessId and employeeId not matching!");
        }
        disciplineProcessRepository.deActiveObject(DisciplineProcessEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DisciplineProcessResponse.DetailBean> getDataById(Long employeeId,Long id) throws BaseAppException {
        Optional<DisciplineProcessEntity> optional = disciplineProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DisciplineProcessEntity.class);
        }
        if(!optional.get().getEmployeeId().equals(employeeId)){
            throw new BaseAppException("disciplineProcessId and employeeId not matching!");
        }
        DisciplineProcessResponse.DetailBean dto = new DisciplineProcessResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, disciplineProcessRepository.getSQLTableName(DisciplineProcessEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/thong-tin-ky-luat.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = disciplineProcessRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-ky-luat.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<DisciplineProcessResponse.SearchResult> tableDto = disciplineProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getDisciplineProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_discipline_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getDisciplineProcessId()));
        });
        return tableDto;
    }

}
