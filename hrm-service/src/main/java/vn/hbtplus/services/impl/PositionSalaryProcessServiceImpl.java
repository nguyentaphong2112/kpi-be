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
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.PositionSalaryProcessRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.PositionSalaryProcessResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.PositionSalaryProcessEntity;
import vn.hbtplus.repositories.impl.PositionSalaryProcessRepository;
import vn.hbtplus.repositories.jpa.PositionSalaryProcessRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.PositionSalaryProcessService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_position_salary_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PositionSalaryProcessServiceImpl implements PositionSalaryProcessService {

    private final PositionSalaryProcessRepository positionSalaryProcessRepository;
    private final ObjectAttributesService objectAttributesService;
    private final PositionSalaryProcessRepositoryJPA positionSalaryProcessRepositoryJPA;
    private final FileService fileService;
    private final AttachmentService attachmentService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PositionSalaryProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(positionSalaryProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(PositionSalaryProcessRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long positionSalaryProcessId) throws BaseAppException {
//        boolean isConflictProcess = positionSalaryProcessRepository.checkConflictProcess(dto, employeeId, positionSalaryProcessId);
//        if (isConflictProcess) {
//            throw new BaseAppException("ERROR_SALARY_PROCESS_CONFLICT", I18n.getMessage("error.salaryProcess.validate.process"));
//        }

        PositionSalaryProcessEntity entity;
        if (positionSalaryProcessId != null && positionSalaryProcessId > 0L) {
            entity = positionSalaryProcessRepositoryJPA.getById(positionSalaryProcessId);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("positionSalaryProcessId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new PositionSalaryProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        positionSalaryProcessRepositoryJPA.save(entity);
//        positionSalaryProcessRepository.updatePositionSalaryProcess(entity.getEmployeeId(), entity.getStartDate(), entity.getSalaryType());

        objectAttributesService.saveObjectAttributes(entity.getPositionSalaryProcessId(), dto.getListAttributes(), PositionSalaryProcessEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP);
        fileService.uploadFiles(files, entity.getPositionSalaryProcessId(), Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getPositionSalaryProcessId());
    }

    @Override
    public ResponseEntity saveData(PositionSalaryProcessRequest.SubmitFormV2 dto, List<MultipartFile> files, Long employeeId, Long positionSalaryProcessId) throws BaseAppException {
        String userName = Utils.getUserNameLogin();

        //validate conflict
        if (positionSalaryProcessRepository.validateConflict(employeeId, positionSalaryProcessId, dto.getStartDate(), dto.getEndDate())) {
            throw new BaseAppException("ERROR_SALARY_PROCESS_CONFLICT", I18n.getMessage("error.salaryProcess.validate.process"));
        }

        Map<Long, PositionSalaryProcessEntity> mapOldData = new HashMap<>();
        if (positionSalaryProcessId != null && positionSalaryProcessId > 0L) {
            PositionSalaryProcessEntity entity;
            entity = positionSalaryProcessRepositoryJPA.getById(positionSalaryProcessId);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("positionSalaryProcessId and employeeId not match!");
            }
            List<PositionSalaryProcessEntity> entities = positionSalaryProcessRepositoryJPA.findByEmployeeIdAndStartDate(employeeId, entity.getStartDate());
            entities.forEach(item -> mapOldData.put(item.getPositionSalaryProcessId(), item));
        }

        List<Long> listIds = new ArrayList<>();

        for (int i = 0; i < dto.getFormData().size(); i++) {
            PositionSalaryProcessRequest.FormData formData = dto.getFormData().get(i);
            PositionSalaryProcessEntity entity = mapOldData.get(formData.getPositionSalaryProcessId());
            if (entity == null) {
                entity = new PositionSalaryProcessEntity();
                entity.setCreatedTime(new Date());
                entity.setCreatedBy(userName);
            } else {
                entity.setModifiedTime(new Date());
                entity.setModifiedBy(userName);
            }
            entity.setOrderNumber(i + 1);
            Utils.copyProperties(formData, entity);
            entity.setEmployeeId(employeeId);
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            positionSalaryProcessRepositoryJPA.save(entity);
            listIds.add(entity.getPositionSalaryProcessId());
            mapOldData.remove(entity.getPositionSalaryProcessId());
        }

        positionSalaryProcessRepository.deActiveObjectByListId(PositionSalaryProcessEntity.class, new ArrayList<>(mapOldData.keySet()));

        //thuc hien cap nhat end_date
        positionSalaryProcessRepository.updatePositionSalaryProcess(employeeId, dto.getStartDate());

        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP);
        listIds.forEach(id -> {
            objectAttributesService.saveObjectAttributes(id, dto.getListAttributes(), PositionSalaryProcessEntity.class, null);
            fileService.uploadFiles(files, id, Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP, Constant.ATTACHMENT.MODULE);
        });


        return ResponseUtils.ok(listIds);
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<PositionSalaryProcessEntity> optional = positionSalaryProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PositionSalaryProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("positionSalaryProcessId and employeeId not match");
        }
        positionSalaryProcessRepository.deActiveObject(PositionSalaryProcessEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP);
//        positionSalaryProcessRepository.updatePositionSalaryProcess(optional.get().getEmployeeId());

        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PositionSalaryProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<PositionSalaryProcessEntity> optional = positionSalaryProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PositionSalaryProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("positionSalaryProcessId and employeeId not match");
        }
        PositionSalaryProcessResponse.DetailBean dto = new PositionSalaryProcessResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, positionSalaryProcessRepository.getSQLTableName(PositionSalaryProcessEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/qua-trinh-luong-truong.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = positionSalaryProcessRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "qua-trinh-luong-truong.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<PositionSalaryProcessResponse.SearchResult> tableDto = positionSalaryProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getPositionSalaryProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_evaluation_results");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getPositionSalaryProcessId()));
        });
        return tableDto;
    }

    @Override
    @Transactional
    public ResponseEntity deleteDataV2(Long employeeId, Long id) {
        Optional<PositionSalaryProcessEntity> optional = positionSalaryProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PositionSalaryProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("positionSalaryProcessId and employeeId not match");
        }
        Date oldStartDate = optional.get().getStartDate();
        positionSalaryProcessRepository.deActiveObject(PositionSalaryProcessEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP);
//        positionSalaryProcessRepository.updatePositionSalaryProcess(optional.get().getEmployeeId());

        //thuc hien auto set end_date của qua trinh truoc do.
        List<PositionSalaryProcessEntity> entities = positionSalaryProcessRepositoryJPA.findByEmployeeIdAndStartDateAndIsDeleted(employeeId, oldStartDate, "N");
        if(entities.isEmpty()){
            positionSalaryProcessRepository.autoUpdatePreProcessForDelete(employeeId, oldStartDate);
        }
        return ResponseUtils.ok(id);
    }

    @Override
    public BaseResponseEntity<PositionSalaryProcessResponse.DetailBeanV2> getDataByIdV2(Long employeeId, Long id) {
        PositionSalaryProcessResponse.DetailBeanV2 dto = new PositionSalaryProcessResponse.DetailBeanV2();

        List<PositionSalaryProcessResponse.DetailBean> list = positionSalaryProcessRepository.getListForEdit(employeeId, id);


        Utils.copyProperties(list.get(0), dto);
        list.forEach(item ->{
            dto.getFormData().add(Utils.copyProperties(item, new PositionSalaryProcessResponse.FormData()));
        });
        dto.setListAttributes(objectAttributesService.getAttributes(id, positionSalaryProcessRepository.getSQLTableName(PositionSalaryProcessEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_POSIITION_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.POSIITION_SALARY_PROCESS_EMP, id));
        return ResponseUtils.ok(dto);
    }

}
