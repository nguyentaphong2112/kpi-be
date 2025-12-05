/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.PoliticalParticipationsRequest;
import vn.kpi.models.response.*;
import vn.kpi.repositories.entity.PoliticalParticipationsEntity;
import vn.kpi.repositories.impl.PoliticalParticipationsRepository;
import vn.kpi.repositories.jpa.PoliticalParticipationsRepositoryJPA;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.PoliticalParticipationsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.ExportExcel;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_participations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PoliticalParticipationsServiceImpl implements PoliticalParticipationsService {

    private final PoliticalParticipationsRepository politicalParticipationsRepository;
    private final PoliticalParticipationsRepositoryJPA politicalParticipationsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PoliticalParticipationsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(politicalParticipationsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(PoliticalParticipationsRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException {
        if (politicalParticipationsRepository.checkExitParticipation(dto, employeeId, id)) {
            throw new BaseAppException("ERROR_PARTICIPATION_CONFIG", I18n.getMessage("error.participation.validate.process"));
        }
        PoliticalParticipationsEntity entity;
        if (id != null && id > 0L) {
            entity = politicalParticipationsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new PoliticalParticipationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setEmployeeId(employeeId);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        politicalParticipationsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getParticipationId(), dto.getListAttributes(), PoliticalParticipationsEntity.class, null);
//        politicalParticipationsRepository.updateParticipation(employeeId, dto);
        return ResponseUtils.ok(entity.getParticipationId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws RecordNotExistsException {
        Optional<PoliticalParticipationsEntity> optional = politicalParticipationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PoliticalParticipationsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("participationId and employeeId not match");
        }
        politicalParticipationsRepository.deActiveObject(PoliticalParticipationsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PoliticalParticipationsResponse.DetailBean> getDataById(Long employeeId, Long id) throws RecordNotExistsException {
        Optional<PoliticalParticipationsEntity> optional = politicalParticipationsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PoliticalParticipationsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("positionSalaryProcessId and employeeId not match");
        }
        PoliticalParticipationsResponse.DetailBean dto = new PoliticalParticipationsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, politicalParticipationsRepository.getSQLTableName(PoliticalParticipationsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/danh-sach-tham-gia.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = politicalParticipationsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-tham-gia.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<PoliticalParticipationsResponse.SearchResult> tableDto = politicalParticipationsRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getParticipationId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_political_participations");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getParticipationId()));
        });
        return tableDto;
    }

}
