/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.request.AttendanceHistoriesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.AttendanceHistoriesEntity;
import vn.hbtplus.repositories.impl.AttendanceHistoriesRepository;
import vn.hbtplus.repositories.jpa.AttendanceHistoriesRepositoryJPA;
import vn.hbtplus.services.AttendanceHistoriesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.EmployeesService;
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
 * Lop impl service ung voi bang abs_attendance_histories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class AttendanceHistoriesServiceImpl implements AttendanceHistoriesService {

    private final AttendanceHistoriesRepository attendanceHistoriesRepository;
    private final AttendanceHistoriesRepositoryJPA attendanceHistoriesRepositoryJPA;
    private final EmployeesService employeesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<AttendanceHistoriesResponse> searchData(AttendanceHistoriesRequest.SearchForm dto) {
        return ResponseUtils.ok(attendanceHistoriesRepository.searchData(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<AttendanceHistoriesResponse> searchDataByCurrentUser(AttendanceHistoriesRequest.SearchForm dto) {
        return ResponseUtils.ok(attendanceHistoriesRepository.searchDataByCurrentUser(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(AttendanceHistoriesRequest.SubmitForm dto, Long id) throws BaseAppException {
        AttendanceHistoriesEntity entity;
        if (id != null && id > 0L) {
            entity = attendanceHistoriesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new AttendanceHistoriesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        if(dto.getStatusId() == null){
            entity.setStatusId(Constant.ATTENDANCE_HISTORY_STATUS.CHO_PHE_DUYET);
        }
        entity.setEmployeeId(employeesService.getEmployeeId(Utils.getUserEmpCode()));
        entity.setIsValid("N");
        attendanceHistoriesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getAttendanceHistoryId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<AttendanceHistoriesEntity> optional = attendanceHistoriesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AttendanceHistoriesEntity.class);
        }
        attendanceHistoriesRepository.deActiveObject(AttendanceHistoriesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<AttendanceHistoriesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<AttendanceHistoriesEntity> optional = attendanceHistoriesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AttendanceHistoriesEntity.class);
        }
        AttendanceHistoriesResponse dto = new AttendanceHistoriesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(AttendanceHistoriesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = attendanceHistoriesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public TableResponseEntity<AttendanceHistoriesResponse.AttendanceLogResponse> getLogData(AttendanceHistoriesRequest.SearchForm dto){
        return ResponseUtils.ok(attendanceHistoriesRepository.getLogData(dto));
    }

    @Override
    public ResponseEntity updateStatusById(AttendanceHistoriesRequest.SubmitForm dto, Long id) throws RecordNotExistsException{
        Optional<AttendanceHistoriesEntity> optional = attendanceHistoriesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AttendanceHistoriesEntity.class);
        }
        AttendanceHistoriesEntity entity = optional.get();
        String userName = Utils.getUserNameLogin();
        if (AttendanceHistoriesEntity.STATUS.PHE_DUYET.equalsIgnoreCase(dto.getStatusId())) {
            if (AttendanceHistoriesEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatusId())
            ) {
                entity.setStatusId(dto.getStatusId());
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(userName);
            entity.setApprovedNote(dto.getApprovedNote());
            attendanceHistoriesRepositoryJPA.save(entity);
        } else {
            entity.setStatusId(dto.getStatusId());
            entity.setApprovedNote(dto.getApprovedNote());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(userName);
            attendanceHistoriesRepositoryJPA.save(entity);
        }
        return ResponseUtils.ok(id);
    }


}
