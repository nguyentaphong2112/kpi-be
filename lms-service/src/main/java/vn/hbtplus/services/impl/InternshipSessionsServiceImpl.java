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
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.models.AttributeConfigDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.OrgDto;
import vn.hbtplus.models.request.InternshipSessionsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.InternshipSessionDetailsEntity;
import vn.hbtplus.repositories.entity.InternshipSessionsEntity;
import vn.hbtplus.repositories.impl.InternshipSessionsRepository;
import vn.hbtplus.repositories.jpa.InternshipSessionDetailsRepositoryJPA;
import vn.hbtplus.repositories.jpa.InternshipSessionsRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.InternshipSessionsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.*;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang lms_internship_sessions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class InternshipSessionsServiceImpl implements InternshipSessionsService {

    private final InternshipSessionsRepository internshipSessionsRepository;
    private final InternshipSessionsRepositoryJPA internshipSessionsRepositoryJPA;
    private final InternshipSessionDetailsRepositoryJPA internshipSessionDetailsRepositoryJPA;
    private final FileService fileService;
    private final AttachmentService attachmentService;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InternshipSessionsResponse.SearchResult> searchData(InternshipSessionsRequest.SearchForm dto) {
        return ResponseUtils.ok(internshipSessionsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(InternshipSessionsRequest.SubmitForm dto, List<MultipartFile> files, Long id) throws BaseAppException {
        InternshipSessionsEntity entity;
        if (id != null && id > 0L) {
            entity = internshipSessionsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new InternshipSessionsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        internshipSessionsRepositoryJPA.save(entity);

        //Lay danh sach detail ra
        List<InternshipSessionDetailsEntity> entities = internshipSessionDetailsRepositoryJPA.findByInternshipSessionId(entity.getInternshipSessionId());
        Map<String, InternshipSessionDetailsEntity> mapDetails = new HashMap<>();
        entities.forEach(item -> {
            mapDetails.put(item.getOrganizationId() + "-" + item.getMajorId(), item);
        });

        //Luu thong tin bang detail
        dto.getDetails().forEach(detailRequest -> {
            InternshipSessionDetailsEntity detailsEntity = mapDetails.get(detailRequest.getOrganizationId() + "-" + detailRequest.getMajorId());
            if (detailsEntity != null) {
                detailsEntity.setModifiedTime(new Date());
                detailsEntity.setModifiedBy(Utils.getUserNameLogin());
            } else {
                detailsEntity = new InternshipSessionDetailsEntity();
                detailsEntity.setCreatedTime(new Date());
                detailsEntity.setCreatedBy(Utils.getUserNameLogin());
            }
            detailsEntity.setOrganizationId(detailRequest.getOrganizationId());
            detailsEntity.setMajorId(detailRequest.getMajorId());
            detailsEntity.setNumOfStudents(detailRequest.getNumOfStudents());
            detailsEntity.setInternshipSessionId(entity.getInternshipSessionId());
            detailsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            internshipSessionDetailsRepositoryJPA.save(detailsEntity);
            mapDetails.remove(detailRequest.getOrganizationId() + "-" + detailRequest.getMajorId());
        });

        //check xem can xoa ban ghi nao khong
        if (!mapDetails.isEmpty()) {
            mapDetails.values().forEach(detailEntity -> {
                if (!detailEntity.isDeleted()) {
                    detailEntity.setModifiedTime(new Date());
                    detailEntity.setModifiedBy(Utils.getUserNameLogin());
                    detailEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    internshipSessionDetailsRepositoryJPA.save(detailEntity);
                }
            });
        }

        objectAttributesService.saveObjectAttributes(entity.getInternshipSessionId(), dto.getListAttributes(), InternshipSessionsEntity.class, null);

        //Luu file dinh kem
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.LMS_INTERNSHIP_SESSION, Constant.ATTACHMENT.FILE_TYPES.LMS_INTERNSHIP_SESSION);
        fileService.uploadFiles(files, entity.getInternshipSessionId(), Constant.ATTACHMENT.TABLE_NAMES.LMS_INTERNSHIP_SESSION, Constant.ATTACHMENT.FILE_TYPES.LMS_INTERNSHIP_SESSION, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getInternshipSessionId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<InternshipSessionsEntity> optional = internshipSessionsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InternshipSessionsEntity.class);
        }
        internshipSessionsRepository.deActiveObject(InternshipSessionsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<InternshipSessionsResponse.Detail> getDataById(Long id) throws RecordNotExistsException {
        Optional<InternshipSessionsEntity> optional = internshipSessionsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InternshipSessionsEntity.class);
        }
        InternshipSessionsResponse.Detail dto = new InternshipSessionsResponse.Detail();
        dto.setListAttributes(objectAttributesService.getAttributes(id, Constant.ATTACHMENT.TABLE_NAMES.LMS_INTERNSHIP_SESSION));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.LMS_INTERNSHIP_SESSION, Constant.ATTACHMENT.FILE_TYPES.LMS_INTERNSHIP_SESSION, id));
        dto.setDetails(internshipSessionsRepository.getListDetail(id));
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(InternshipSessionsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_sinh_vien_thuc_tap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = internshipSessionsRepository.getListExport(dto);
        dynamicExport.replaceKeys(listDataExport);
        List<AttributeConfigDto> listAttribute = objectAttributesService.getAttributes(Constant.ATTACHMENT.TABLE_NAMES.LMS_INTERNSHIP_SESSION, null);
        int col = 8;
        int row = 4;
        Map<String, Integer> attributeCode = new HashMap<>();
        for (AttributeConfigDto attribute : listAttribute) {
            dynamicExport.setText(attribute.getName(), col, row);
            dynamicExport.mergeCell(row, col, row + 1, col);
            attributeCode.put(attribute.getCode(), col);
            col++;
        }
        dynamicExport.setCellFormat(row, 8, row + 1, col - 1, ExportExcel.CENTER_FORMAT);
        int rowData = 6;
        for (int i = 0; i < listDataExport.size(); i++) {
            Map<String, Object> dataKpiExport = listDataExport.get(i);
            Object value = dataKpiExport.get("internship_session_id");
            Long id = null;
            if (value instanceof Number) {
                id = ((Number) value).longValue();
            }
            List<ObjectAttributesResponse> attributesResponses = objectAttributesService.getAttributes(id, Constant.ATTACHMENT.TABLE_NAMES.LMS_INTERNSHIP_SESSION);
            for (ObjectAttributesResponse attributesResponse : attributesResponses) {
                dynamicExport.setText(attributesResponse.getAttributeValue(), attributeCode.get(attributesResponse.getAttributeCode()), rowData);
            }
            rowData++;
        }
        dynamicExport.setCellFormat(row, 8, row + listDataExport.size() + 1, col - 1, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_sinh_vien_thuc_tap.xlsx");
    }

    @Override
    public String getTemplateIndicator() throws Exception {
        String importTemplateName = "BM_import_DS_sinh_vien_thuc_tap.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);
        List<CategoryDto> listUniversity = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_INTERN_TRUONG_DAO_TAO);
        List<CategoryDto> listMajor = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_INTERN_CHUYEN_NGANH);
        List<OrgDto> listOrg = internshipSessionsRepository.getListOrg();
        dynamicExport.setActiveSheet(1);
        int row = 0;
        for (OrgDto orgDto : listOrg) {
            dynamicExport.setText(orgDto.getFullName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(2);
        for (CategoryDto categoryDto : listUniversity) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(3);
        for (CategoryDto categoryDto : listMajor) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    public boolean  importData(MultipartFile fileImport) throws Exception {
        String fileConfigName = "BM_import_DS_sinh_vien_thuc_tap.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        List<InternshipSessionsEntity> entityList = new ArrayList<>();
        List<InternshipSessionDetailsEntity> entityDetailList = new ArrayList<>();
        Map<String, String> listUniversity = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_INTERN_TRUONG_DAO_TAO).stream()
                .collect(Collectors.toMap(CategoryDto::getName, CategoryDto::getValue));
        Map<String, String> listMajor = internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_INTERN_CHUYEN_NGANH).stream()
                .collect(Collectors.toMap(CategoryDto::getName, CategoryDto::getValue));
        Map<String, Long> listOrg = internshipSessionsRepository.getListOrg().stream()
                .collect(Collectors.toMap(OrgDto::getFullName, OrgDto::getOrganizationId));
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            int index = 0;
            Map<String, InternshipSessionsEntity> entityMap = new HashMap<>();
            for (Object[] obj : dataList) {
                int col = 1;
                Date startDate = (Date) obj[col++];
                Date endDate = (Date) obj[col++];
                if (startDate.after(endDate)) {
                    importExcel.addError(index, 2, I18n.getMessage("error.rangeDate"), startDate.toLocaleString());
                }
                String universityName = (String) obj[col++];
                String universityId = listUniversity.get(universityName);
                if (universityId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", universityName), universityName);
                }
                String sessionName = (String) obj[col++];
                String orgName = (String) obj[col++];
                Long orgId = listOrg.get(orgName);
                if (orgId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", orgName), orgName);
                }
                String majorName = (String) obj[col++];
                String majorId = listMajor.get(majorName);
                if (majorId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", majorName), majorName);
                }
                Long numberOfStudents = (Long) obj[col];
                String uniqueKey = startDate + "|" + endDate + "|" + universityId + "|" + sessionName;
                InternshipSessionsEntity entity;
                if (!entityMap.containsKey(uniqueKey)) {
                    entity = new InternshipSessionsEntity();
                    entity.setInternshipSessionId(internshipSessionsRepository.getNextId(InternshipSessionsEntity.class));
                    entity.setUniversityId(universityId);
                    entity.setSessionName(sessionName);
                    entity.setStartDate(startDate);
                    entity.setEndDate(endDate);
                    entity.setTotalStudents(numberOfStudents);
                    entityList.add(entity);
                    entityMap.put(uniqueKey, entity);
                } else {
                    entity = entityMap.get(uniqueKey);
                    entity.setTotalStudents(entity.getTotalStudents() + numberOfStudents);
                    for (InternshipSessionsEntity existingEntity : entityList) {
                        if (existingEntity.getInternshipSessionId().equals(entity.getInternshipSessionId())) {
                            existingEntity.setTotalStudents(entity.getTotalStudents());
                            break;
                        }
                    }
                }
                InternshipSessionDetailsEntity detailsEntity = new InternshipSessionDetailsEntity();
                detailsEntity.setInternshipSessionId(entity.getInternshipSessionId());
                detailsEntity.setOrganizationId(orgId);
                detailsEntity.setMajorId(majorId);
                detailsEntity.setNumOfStudents(numberOfStudents);
                entityDetailList.add(detailsEntity);
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                internshipSessionsRepository.insertBatch(InternshipSessionsEntity.class, entityList, userName);
                internshipSessionsRepository.insertBatch(InternshipSessionDetailsEntity.class, entityDetailList, userName);
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
        return true;
    }

}
