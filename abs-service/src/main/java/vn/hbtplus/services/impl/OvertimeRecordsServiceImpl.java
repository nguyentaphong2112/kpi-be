/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.models.dto.AbsOvertimeRecordDTO;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.request.OvertimeRecordsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.OvertimeRecordsEntity;
import vn.hbtplus.repositories.impl.OvertimeRecordsRepository;
import vn.hbtplus.repositories.jpa.OvertimeRecordsRepositoryJPA;
import vn.hbtplus.services.OvertimeRecordsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * Lop impl service ung voi bang abs_overtime_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class OvertimeRecordsServiceImpl implements OvertimeRecordsService {

    private final OvertimeRecordsRepository overtimeRecordsRepository;
    private final OvertimeRecordsRepositoryJPA overtimeRecordsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OvertimeRecordsResponse> searchData(OvertimeRecordsRequest.SearchForm dto) {
        return ResponseUtils.ok(overtimeRecordsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OvertimeRecordsRequest.SubmitForm dto, Long id) throws BaseAppException {
        List<Long> savedRequestIds = new ArrayList<>();

        for (AbsOvertimeRecordDTO absRequest : dto.getListRecords()) {
            OvertimeRecordsEntity entity = saveOrUpdateRecord(absRequest, dto.getEmployeeId(),dto.getDateTimekeeping());
            savedRequestIds.add(entity.getOvertimeRecordId());
        }

        return ResponseUtils.ok(savedRequestIds);
    }

    private OvertimeRecordsEntity saveOrUpdateRecord(AbsOvertimeRecordDTO absRecords, Long employeeId,Date dateTimeKeeping) {
        OvertimeRecordsEntity entity;

        if (absRecords.getOvertimeRecordId() != null && absRecords.getOvertimeRecordId() > 0L) {
            entity = overtimeRecordsRepositoryJPA.getById(absRecords.getOvertimeRecordId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OvertimeRecordsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setDateTimekeeping(dateTimeKeeping);
        entity.setEmployeeId(employeeId);
        entity.setStartTime(Utils.stringToDate(absRecords.getStartTime() , "dd/MM/yyyy HH:mm"));
        entity.setEndTime(Utils.stringToDate(absRecords.getEndTime() , "dd/MM/yyyy HH:mm"));
        entity.setTotalHours(getHours(entity.getEndTime(),entity.getStartTime()));
        entity.setOvertimeTypeId(absRecords.getOvertimeTypeId());
        entity.setContent(absRecords.getContent());
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);

        return overtimeRecordsRepositoryJPA.save(entity);
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OvertimeRecordsEntity> optional = overtimeRecordsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OvertimeRecordsEntity.class);
        }
        overtimeRecordsRepository.deActiveObject(OvertimeRecordsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OvertimeRecordsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<OvertimeRecordsEntity> optional = overtimeRecordsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OvertimeRecordsEntity.class);
        }
        OvertimeRecordsResponse dto = new OvertimeRecordsResponse();
        AbsOvertimeRecordDTO record = new AbsOvertimeRecordDTO();
        Utils.copyProperties(optional.get(), dto);
        record.setOvertimeRecordId(id);
        record.setOvertimeTypeId(dto.getOvertimeTypeId());
        record.setStartTime(Utils.formatDate(dto.getStartTime() , "dd/MM/yyyy HH:mm"));
        record.setEndTime(Utils.formatDate(dto.getEndTime() , "dd/MM/yyyy HH:mm"));
        record.setContent(dto.getContent());
        List<AbsOvertimeRecordDTO> listRecords = new ArrayList<>();
        listRecords.add(record);
        dto.setListRecords(listRecords);
        return ResponseUtils.ok(dto);

    }

    @Override
    public ResponseEntity<Object> exportData(OvertimeRecordsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = overtimeRecordsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        String pathTemplate = "template/import/BM-Import-du-lieu-cham-cong-lam-them.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<CategoryDto> overtimeTypes = overtimeRecordsRepository.getOvertimeTypes();
       dynamicExport.setActiveSheet(1);
        int row = 1;
        for (CategoryDto overtimeType : overtimeTypes) {
            int col = 0;
            dynamicExport.setEntry(String.valueOf(row++), col++);
            dynamicExport.setText(overtimeType.getName(), col++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setCellFormat(1, 0, dynamicExport.getLastRow(), 1, ExportExcel.BORDER_FORMAT);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM-Import-du-lieu-cham-cong-lam-them.xlsx", false);
    }

    @Override
    public ResponseEntity<Object> processImport(MultipartFile file) throws Exception{
        ImportExcel importExcel = new ImportExcel("template/import/BM-Import-du-lieu-cham-cong-lam-them.xml");
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        InputStream inputStream = file.getInputStream();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }
            Map<String, EmployeeDto> mapEmp = overtimeRecordsRepository.getMapEmpById(empCodeList);
            List<CategoryDto> overtimeTypes = overtimeRecordsRepository.getOvertimeTypes();
            List<OvertimeRecordsEntity> listInsert = new ArrayList<>();
            List<Long> employeeIds = new ArrayList<>();
            int row = 0;
            int col;
            for (Object[] obj : dataList) {
                col = 1;
                OvertimeRecordsEntity entity = null;
                String employeeCode = (String) obj[col];
                String dateTimekeeping = (String) obj[4];
                String startTime = (String) obj[4] + " " +(String) obj[5];
                String endTime = (String) obj[4] + " " +(String) obj[6];
                String overtimeTypeName = (String) obj[3];
                String overtimeTypeId = overtimeTypes.stream()
                        .filter(dto -> dto.getName().equals(overtimeTypeName))
                        .map(CategoryDto::getValue)
                        .findFirst()
                        .orElse(null);
                Long employeeId = 0L;
                boolean isContained = overtimeTypes.stream()
                        .anyMatch(category -> category.getName().equals((String) obj[3]));

                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Mã nhân viên không tồn tại", employeeCode);
                } else if (!mapEmp.get(employeeCode.toLowerCase()).getFullName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(row, col, MessageFormat.format("Họ tên nhân viên không khớp với {0} - {1}", employeeCode, mapEmp.get(employeeCode.toLowerCase()).getFullName()), (String) obj[2]);
                } else if (!isContained){
                    importExcel.addError(row, col, "Hình thức không tồn tại", employeeCode);
                }
                    else {
                    employeeId = mapEmp.get(employeeCode.toLowerCase()).getEmployeeId();
                    employeeIds.add(employeeId);
                }
                if (entity == null) {
                    entity = new OvertimeRecordsEntity();
                    entity.setCreatedTime(new Date());
                    entity.setCreatedBy(userName);
                    entity.setEmployeeId(employeeId);
                    listInsert.add(entity);
                }
                entity.setOvertimeTypeId(overtimeTypeId);
                entity.setDateTimekeeping(Utils.stringToDate(dateTimekeeping, "dd/MM/yyyy"));
                entity.setStartTime(Utils.stringToDate(startTime, "dd/MM/yyyy HH:mm"));
                entity.setEndTime(Utils.stringToDate(endTime, "dd/MM/yyyy HH:mm"));
                entity.setTotalHours(getHours(entity.getEndTime(),entity.getStartTime()));
                entity.setContent((String) obj[7]);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            }
            overtimeRecordsRepository.insertBatch(OvertimeRecordsEntity.class, listInsert, userName);
        } else {
            throw new ErrorImportException(file, importExcel);
        }

        return ResponseUtils.ok();
    }

    private Double getHours(Date endTime, Date startTime) {
        long time = endTime.getTime() - startTime.getTime();
        return Math.round(time * 1d / 10 / 60 / 60) / 100d;
    }

}
