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
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.request.MentoringTraineesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.MentoringTraineesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.MentoringTraineesEntity;
import vn.hbtplus.repositories.impl.MentoringTraineesRepository;
import vn.hbtplus.repositories.jpa.MentoringTraineesRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.MentoringTraineesService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang lms_mentoring_trainees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class MentoringTraineesServiceImpl implements MentoringTraineesService {

    private final MentoringTraineesRepository mentoringTraineesRepository;
    private final MentoringTraineesRepositoryJPA mentoringTraineesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final FileService fileService;
    private final AttachmentService attachmentService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<MentoringTraineesResponse.SearchResult> searchData(MentoringTraineesRequest.SearchForm dto) {
        return ResponseUtils.ok(mentoringTraineesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(MentoringTraineesRequest.SubmitForm dto, List<MultipartFile> files, Long id) throws BaseAppException {
        MentoringTraineesEntity entity;
        if (id != null && id > 0L) {
            Optional<MentoringTraineesEntity> optional = mentoringTraineesRepositoryJPA.findById(id);
            if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
                throw new RecordNotExistsException(id, MentoringTraineesEntity.class);
            }
            entity = optional.get();
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new MentoringTraineesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        mentoringTraineesRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getMedMentoringTraineeId(), dto.getListAttributes(), MentoringTraineesEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.MENTORING_TRAINEES, Constant.ATTACHMENT.FILE_TYPES.MENTORING_TRAINEES_EMP);
        fileService.uploadFiles(files, entity.getMedMentoringTraineeId(), Constant.ATTACHMENT.TABLE_NAMES.MENTORING_TRAINEES, Constant.ATTACHMENT.FILE_TYPES.MENTORING_TRAINEES_EMP, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getMedMentoringTraineeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<MentoringTraineesEntity> optional = mentoringTraineesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MentoringTraineesEntity.class);
        }
        mentoringTraineesRepository.deActiveObject(MentoringTraineesEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.MENTORING_TRAINEES, Constant.ATTACHMENT.FILE_TYPES.MENTORING_TRAINEES_EMP);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<MentoringTraineesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<MentoringTraineesEntity> optional = mentoringTraineesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MentoringTraineesEntity.class);
        }
        MentoringTraineesResponse.DetailBean dto = new MentoringTraineesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, "lms_mentoring_trainees"));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.MENTORING_TRAINEES, Constant.ATTACHMENT.FILE_TYPES.MENTORING_TRAINEES_EMP, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(MentoringTraineesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/med/BM_Xuat_chi_dao_tuyen_tren.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = mentoringTraineesRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_chi_dao_tuyen_tren.xlsx");
    }

    @Override
    public String getTemplateIndicator() throws Exception {
        String importTemplateName = "BM_import_chi_dao_tuyen_tren.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);
        List<CategoryDto> listProject = mentoringTraineesRepository.getListCategories(Constant.CATEGORY_CODES.CDT_CHUONG_TRINH_DAO_TAO);
        List<CategoryDto> listHospital = mentoringTraineesRepository.getListCategories(Constant.CATEGORY_CODES.CDT_DIA_DIEM_DAO_TAO);
        List<EmployeeDto> listEmployee = mentoringTraineesRepository.getListEmployee();
        dynamicExport.setActiveSheet(1);
        int row = 1;
        for (CategoryDto categoryDto : listProject) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 1;
        dynamicExport.setActiveSheet(2);
        for (CategoryDto categoryDto : listHospital) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 1;
        dynamicExport.setActiveSheet(3);
        for (EmployeeDto employeeDto : listEmployee) {
            dynamicExport.setText(employeeDto.getEmployeeName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    public boolean importData(MultipartFile fileImport) throws Exception {
        String fileConfigName = "BM_import_chi_dao_tuyen_tren.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        List<MentoringTraineesEntity> mentoringTraineesEntityList = new ArrayList<>();
        Map<String, String> listProject = mentoringTraineesRepository.getListCategories(Constant.CATEGORY_CODES.CDT_CHUONG_TRINH_DAO_TAO).stream()
                .collect(Collectors.toMap(CategoryDto::getName, CategoryDto::getValue));
        Map<String, String> listHospital = mentoringTraineesRepository.getListCategories(Constant.CATEGORY_CODES.CDT_DIA_DIEM_DAO_TAO).stream()
                .collect(Collectors.toMap(CategoryDto::getName, CategoryDto::getValue));
        Map<String, Long> listEmployee = mentoringTraineesRepository.getListEmployee().stream()
                .collect(Collectors.toMap(EmployeeDto::getEmployeeCode, EmployeeDto::getEmployeeId));
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            int index = 0;
            for (Object[] obj : dataList) {
                int col = 1;
                String employee = (String) obj[col++];
                String employeeCode = employee.split(" - ")[0];
                Long employeeId = listEmployee.get(employeeCode);
                if(employeeId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.med.employee"), employee);
                }
                Date startDate = (Date) obj[col++];
                Date endDate = (Date) obj[col++];
                if(startDate.after(endDate)) {
                    importExcel.addError(index, 2, I18n.getMessage("error.rangeDate"), startDate.toLocaleString());
                }
                String documentNo = (String) obj[col++];
                String projectName = (String) obj[col++];
                String projectId = listProject.get(projectName);
                String hospitalName = (String) obj[col++];
                String hospitalId = listHospital.get(hospitalName);
                Long totalLessons = (Long) obj[col++];
                String content = (String) obj[col];
                MentoringTraineesEntity entity = new MentoringTraineesEntity();
                entity.setEmployeeId(employeeId);
                entity.setDocumentNo(documentNo);
                entity.setProjectId(projectId);
                entity.setHospitalId(hospitalId);
                entity.setStartDate(startDate);
                entity.setEndDate(endDate);
                entity.setTotalLessons(totalLessons);
                entity.setContent(content);
                mentoringTraineesEntityList.add(entity);
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                mentoringTraineesRepository.insertBatch(MentoringTraineesEntity.class, mentoringTraineesEntityList, userName);
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
        return true;
    }

}
