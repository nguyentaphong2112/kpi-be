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
import vn.hbtplus.models.dto.OrgDto;
import vn.hbtplus.models.request.ExternalTrainingsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ExternalTrainingsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ExternalTrainingsEntity;
import vn.hbtplus.repositories.entity.TrainingProcessBudgetsEntity;
import vn.hbtplus.repositories.entity.TrainingProcessEntity;
import vn.hbtplus.repositories.impl.ExternalTrainingsRepository;
import vn.hbtplus.repositories.impl.InternshipSessionsRepository;
import vn.hbtplus.repositories.impl.TrainingProcessRepository;
import vn.hbtplus.repositories.jpa.ExternalTrainingsRepositoryJPA;
import vn.hbtplus.services.ExternalTrainingsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang lms_external_trainings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class ExternalTrainingsServiceImpl implements ExternalTrainingsService {

    private final ExternalTrainingsRepository externalTrainingsRepository;
    private final ExternalTrainingsRepositoryJPA externalTrainingsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final InternshipSessionsRepository internshipSessionsRepository;
    private final TrainingProcessRepository trainingProcessRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ExternalTrainingsResponse.SearchResult> searchData(ExternalTrainingsRequest.SearchForm dto) {
        return ResponseUtils.ok(externalTrainingsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ExternalTrainingsRequest.SubmitForm dto, Long id) throws BaseAppException {
        ExternalTrainingsEntity entity;
        if (id != null && id > 0L) {
            entity = externalTrainingsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ExternalTrainingsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        externalTrainingsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getExternalTrainingId(), dto.getListAttributes(), ExternalTrainingsEntity.class, null);
        return ResponseUtils.ok(entity.getExternalTrainingId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ExternalTrainingsEntity> optional = externalTrainingsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ExternalTrainingsEntity.class);
        }
        externalTrainingsRepository.deActiveObject(ExternalTrainingsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ExternalTrainingsResponse.Detail> getDataById(Long id) throws RecordNotExistsException {
        Optional<ExternalTrainingsEntity> optional = externalTrainingsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ExternalTrainingsEntity.class);
        }
        ExternalTrainingsResponse.Detail dto = new ExternalTrainingsResponse.Detail();
        dto.setListAttributes(objectAttributesService.getAttributes(id, Constant.ATTACHMENT.TABLE_NAMES.LMS_EXTERNAL_TRAININGS));
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ExternalTrainingsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_dao_tao_ngoai_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = externalTrainingsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_dao_tao_ngoai_vien.xlsx");
    }

    @Override
    public String getTemplateIndicator() throws Exception {
        String importTemplateName = "BM_import_DS_dao_tao_ngoai_vien.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);
        List<CategoryDto> listType = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_DOI_TUONG_NGOAI);
        List<CategoryDto> listGender = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.GIOI_TINH);
        List<CategoryDto> listTrainningType = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_HINH_THUC_DAO_TAO);
        List<CategoryDto> listTrainingMajor = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_CHUYEN_NGANH_NGOAI_VIEN);
        List<OrgDto> listOrg = internshipSessionsRepository.getListOrg();
        List<EmployeeDto> listEmployee = trainingProcessRepository.getListEmployee();
        List<CategoryDto> listTuitionFeeStatus = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_TINH_TRANG_HOC_PHI);
        dynamicExport.setActiveSheet(1);
        int row = 0;
        for (CategoryDto categoryDto : listType) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(2);
        for (CategoryDto categoryDto : listGender) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(3);
        for (CategoryDto categoryDto : listTrainningType) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(4);
        for (CategoryDto categoryDto : listTrainingMajor) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(5);
        for (OrgDto orgDto : listOrg) {
            dynamicExport.setText(orgDto.getFullName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(6);
        for (EmployeeDto employeeDto : listEmployee) {
            dynamicExport.setText(employeeDto.getEmployeeName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(7);
        for (CategoryDto categoryDto : listTuitionFeeStatus) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    public boolean importData(MultipartFile fileImport) throws Exception {
        String fileConfigName = "BM_import_DS_dao_tao_ngoai_vien.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        List<ExternalTrainingsEntity> entityList = new ArrayList<>();
        Map<String, String> listType = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_DOI_TUONG_NGOAI).stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue));
        Map<String, String> listGender = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.GIOI_TINH).stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue));
        Map<String, String> listTrainningType = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_HINH_THUC_DAO_TAO).stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue));
        Map<String, String> listTrainingMajor = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_CHUYEN_NGANH_NGOAI_VIEN).stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue));
        List<OrgDto> listOrg = internshipSessionsRepository.getListOrg();

        Map<String, Long> mapOrgs = new HashMap<>();
        listOrg.forEach(orgDto -> {
            mapOrgs.put(orgDto.getFullName().toLowerCase(), orgDto.getOrganizationId());
            mapOrgs.put(orgDto.getName().toLowerCase(), orgDto.getOrganizationId());
        });
        Map<String, Long> listEmployee = trainingProcessRepository.getListEmployee().stream()
                .collect(Collectors.toMap(EmployeeDto::getEmployeeCode, EmployeeDto::getEmployeeId));
        Map<String, String> listTuitionFeeStatus = internshipSessionsRepository
                .getListCategories(Constant.CATEGORY_CODES.LMS_TINH_TRANG_HOC_PHI)
                .stream()
                .collect(Collectors.toMap(
                        c -> c.getName().toLowerCase(),   // key: name viết thường
                        CategoryDto::getValue,
                        (oldValue, newValue) -> newValue, // xử lý khi trùng key
                        LinkedHashMap::new                // giữ thứ tự
                ));
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            int index = 0;
            for (Object[] obj : dataList) {
                int col = 1;
                String typeName = (String) obj[col++];
                String typeId = listType.get(typeName.toLowerCase().trim());
                if (typeId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", typeName), typeName);
                }
                String fullName = (String) obj[col++];
                String genderName = (String) obj[col++];
                String genderId = listGender.get(genderName.toLowerCase().trim());
                if (genderId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", genderName), genderName);
                }
                Long yearOfBirth = (Long) obj[col++];
                String mobileNumber = (String) obj[col++];
                String personalIdNo = (String) obj[col++];
                String address = (String) obj[col++];
                String organizationAddress = (String) obj[col++];
                Date startDate = (Date) obj[col++];
                Date endDate = (Date) obj[col++];
                if (endDate != null && startDate.after(endDate)) {
                    importExcel.addError(index, 2, I18n.getMessage("error.rangeDate"), startDate.toLocaleString());
                }
                String trainningTypeName = (String) obj[col++];
                String trainningTypeId = listTrainningType.get(trainningTypeName.toLowerCase().trim());
                if (trainningTypeId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", trainningTypeName), trainningTypeName);
                }
                String trainingMajorName = (String) obj[col++];
                String trainingMajorId = listTrainingMajor.get(trainingMajorName.toLowerCase().trim());
                if (trainingMajorId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", trainingMajorName), trainingMajorName);
                }
                String content = (String) obj[col++];
                String orgName = (String) obj[col++];
                Long orgId = mapOrgs.get(orgName.toLowerCase());
                if (orgId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", orgName), orgName);
                }
                String mentor = (String) obj[col++];
                Long mentorId = null;
                if (!Utils.isNullOrEmpty(mentor)) {
                    String mentorCode = mentor.split(" - ")[0];
                    mentorId = listEmployee.get(mentorCode);
                    if (mentorId == null) {
                        importExcel.addError(index, 1, I18n.getMessage("error.lms.mentor"), mentor);
                    }
                }
                String admissionResults = (String) obj[col++];
                String graduatedResults = (String) obj[col++];
                String certificateNo = (String) obj[col++];
                Date certificateDate = (Date) obj[col++];
                Long numberOfLessons = (Long) obj[col++];
                String tuitionFeeStatusName = (String) obj[col];
                String tuitionFeeStatusId = listTuitionFeeStatus.get(tuitionFeeStatusName.toLowerCase().trim());
                if (tuitionFeeStatusId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", tuitionFeeStatusName), tuitionFeeStatusName);
                }
                ExternalTrainingsEntity entity = new ExternalTrainingsEntity();
                entity.setTypeId(typeId);
                entity.setFullName(fullName);
                entity.setGenderId(genderId);
                entity.setYearOfBirth(yearOfBirth.toString());
                entity.setMobileNumber(mobileNumber);
                entity.setPersonalIdNo(personalIdNo);
                entity.setAddress(address);
                entity.setOrganizationAddress(organizationAddress);
                entity.setStartDate(startDate);
                entity.setEndDate(endDate);
                entity.setTrainningTypeId(trainningTypeId);
                entity.setTrainingMajorId(trainingMajorId);
                entity.setContent(content);
                entity.setOrganizationId(orgId);
                entity.setMentorId(mentorId);
                entity.setAdmissionResults(admissionResults);
                entity.setGraduatedResults(graduatedResults);
                entity.setCertificateNo(certificateNo);
                entity.setCertificateDate(certificateDate);
                entity.setNumberOfLessons(numberOfLessons);
                entity.setTuitionFeeStatusId(tuitionFeeStatusId);
                entityList.add(entity);
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                internshipSessionsRepository.insertBatch(ExternalTrainingsEntity.class, entityList, userName);
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
        return true;
    }

}
