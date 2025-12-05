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
import vn.hbtplus.models.dto.WorkCalendarsDTO;
import vn.hbtplus.models.request.WorkCalendarsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WorkCalendarsResponse;
import vn.hbtplus.repositories.entity.WorkCalendarDetailsEntity;
import vn.hbtplus.repositories.entity.WorkCalendarsEntity;
import vn.hbtplus.repositories.impl.WorkCalendarsRepository;
import vn.hbtplus.repositories.jpa.WorkCalendarDetailsRepositoryJPA;
import vn.hbtplus.repositories.jpa.WorkCalendarsRepositoryJPA;
import vn.hbtplus.services.WorkCalendarsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang abs_work_calendars
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class WorkCalendarsServiceImpl implements WorkCalendarsService {

    private final WorkCalendarsRepository workCalendarsRepository;
    private final WorkCalendarsRepositoryJPA workCalendarsRepositoryJPA;
    private final WorkCalendarDetailsRepositoryJPA workCalendarDetailsRepositoryJPA;
    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WorkCalendarsResponse> searchData(WorkCalendarsRequest.SearchForm dto) {
        return ResponseUtils.ok(workCalendarsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(WorkCalendarsRequest.SubmitForm dto, Long id) throws BaseAppException {
        WorkCalendarsEntity entity;
        List<WorkCalendarDetailsEntity> details;
        if (id != null && id > 0L) {
            entity = workCalendarsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
//            details = workCalendarDetailsRepositoryJPA.findByWorkCalendarId(id);
//            if (details != null && !details.isEmpty()) {
//                workCalendarDetailsRepositoryJPA.deleteAll(details);
//            }
            workCalendarsRepository.deleteByProperties(WorkCalendarDetailsEntity.class, "work_calendar_id", id);
            workCalendarsRepositoryJPA.save(entity);
        } else {
            entity = new WorkCalendarsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        workCalendarsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getWorkCalendarId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WorkCalendarsEntity> optional = workCalendarsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkCalendarsEntity.class);
        }
        workCalendarsRepository.deActiveObject(WorkCalendarsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WorkCalendarsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<WorkCalendarsEntity> optional = workCalendarsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkCalendarsEntity.class);
        }
        WorkCalendarsResponse dto = new WorkCalendarsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(WorkCalendarsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/import/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = workCalendarsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<WorkCalendarsDTO> getActiveWorkCalendars() {
        List<WorkCalendarsDTO> workCalendarsDTOList = new ArrayList<>();
        List<WorkCalendarsEntity> workCalendarsEntities = workCalendarsRepositoryJPA.findAll();
        if (workCalendarsEntities == null || workCalendarsEntities.isEmpty()) {
            return workCalendarsDTOList;
        }
        for (WorkCalendarsEntity enity: workCalendarsEntities) {
            WorkCalendarsDTO absWorkCalendarsDTO = Utils.copyProperties(enity, WorkCalendarsDTO.class);
            workCalendarsDTOList.add(absWorkCalendarsDTO);
        }
        return workCalendarsDTOList;
    }
}
