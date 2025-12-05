/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import com.jxcell.CellException;
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
import vn.hbtplus.models.AttributeConfigDto;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.EducationCertificatesRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.EducationCertificatesEntity;
import vn.hbtplus.repositories.entity.HealthRecordsEntity;
import vn.hbtplus.repositories.entity.ObjectAttributesEntity;
import vn.hbtplus.repositories.impl.EducationCertificatesRepository;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.jpa.EducationCertificatesRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.EducationCertificatesService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.*;

import java.text.MessageFormat;
import java.util.*;

/**
 * Lop impl service ung voi bang hr_education_certificates
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EducationCertificatesServiceImpl implements EducationCertificatesService {

    private final EducationCertificatesRepository educationCertificatesRepository;
    private final ObjectAttributesService objectAttributesService;
    private final EducationCertificatesRepositoryJPA educationCertificatesRepositoryJPA;
    private final EmployeesRepository employeesRepository;
    private final AttachmentService attachmentService;
    private final FileService fileService;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EducationCertificatesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(educationCertificatesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(EducationCertificatesRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException {
        boolean isDuplicate = educationCertificatesRepository.isDuplicateEduCertificate(dto, employeeId, id);
        if (isDuplicate) {
            throw new BaseAppException("ERROR_EDUCATION_CERTIFICATE_DUPLICATE", I18n.getMessage("error.educationCertificate.validate.duplicate"));
        }

        EducationCertificatesEntity entity;
        if (id != null && id > 0L) {
            entity = educationCertificatesRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("educationCertificateId and employeeId not matching!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EducationCertificatesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        educationCertificatesRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getEducationCertificateId(), dto.getListAttributes(), EducationCertificatesEntity.class, null);
        objectAttributesService.saveObjectAttributes(entity.getEducationCertificateId(), dto.getListAttributes(), EducationCertificatesEntity.class, dto.getCertificateTypeId());
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_CERTIFICATES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_CERTIFICATES_EMP);
        fileService.uploadFiles(files, entity.getEducationCertificateId(), Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_CERTIFICATES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_CERTIFICATES_EMP, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getEducationCertificateId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<EducationCertificatesEntity> optional = educationCertificatesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EducationCertificatesEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("educationCertificateId and employeeId not matching!");
        }
        educationCertificatesRepository.deActiveObject(EducationCertificatesEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_CERTIFICATES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_CERTIFICATES_EMP);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EducationCertificatesResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<EducationCertificatesEntity> optional = educationCertificatesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EducationCertificatesEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("educationCertificateId and employeeId not matching!");
        }
        EducationCertificatesResponse.DetailBean dto = new EducationCertificatesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, educationCertificatesRepository.getSQLTableName(EducationCertificatesEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_CERTIFICATES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_CERTIFICATES_EMP, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/thong-tin-chung-chi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = educationCertificatesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-chung-chi.xlsx");
    }

    @Override
    public BaseDataTableDto<EducationCertificatesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<EducationCertificatesResponse.DetailBean> tableDto = educationCertificatesRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getEducationCertificateId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_education_certificates");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getEducationCertificateId()));
        });
        return tableDto;
    }

    @Override
    public ResponseEntity<Object> downloadTemplate(EducationCertificatesRequest.SubmitForm dto) throws Exception {
        if (dto.getCertificateTypeId() != null) {
            return downloadTemplateTypeSelect(dto);
        } else {
            return downloadTemplateNoTypeSelect(dto);
        }
    }


    private ResponseEntity<Object> downloadTemplateTypeSelect(EducationCertificatesRequest.SubmitForm dto) throws Exception {
        String pathTemplate = "template/import/BM-import-chung-chi-co-chon-VBCC.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);
        List<AttributeConfigDto> attributeConfigs = objectAttributesService.getAttributes(Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_CERTIFICATES, dto.getCertificateTypeId());
        List<BaseCategoryDto> listCertificateType = educationCertificatesRepository.getListCategories(Constant.CATEGORY_CODES.LOAI_CHUNG_CHI);
        Map<String, String> mapCertificateType = new HashMap<>();
        listCertificateType.forEach(item -> {
            mapCertificateType.put(item.getValue(), item.getName());
        });

        List<BaseCategoryDto> listCertificateName = educationCertificatesRepository.getListCategoriesByParent(Constant.CATEGORY_CODES.TEN_CHUNG_CHI, Constant.CATEGORY_CODES.LOAI_CHUNG_CHI, dto.getCertificateTypeId());
        BaseCategoryDto category = educationCertificatesRepository.getCategory(Constant.CATEGORY_CODES.LOAI_CHUNG_CHI, dto.getCertificateTypeId(), BaseCategoryDto.class);
        List<EducationCertificatesResponse.AttributeDto> attributes = educationCertificatesRepository.getAttributeOfCategory(category.getCategoryId());
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (BaseCategoryDto categoryDto : listCertificateName) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        attributes.forEach(attribute -> {
            if (attribute.getAttributeCode().equals("LABEL_TEN_CHUNG_CHI")) {
                try {
                    dynamicExport.setText(String.valueOf(attribute.getAttributeValue()), 3, 2);
                } catch (CellException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        int col = 7;
        for (AttributeConfigDto attributeConfig : attributeConfigs) {
            dynamicExport.setText(attributeConfig.getName(), col++, 2);
            dynamicExport.setCellFormat(3, col, 3, col, ExportExcel.CENTER_FORMAT);
        }
        dynamicExport.replaceText("${certificate_type}", mapCertificateType.get(dto.getCertificateTypeId()));
        dynamicExport.setCellFormat(2, 0, 6, col - 1, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM-import-chung-chi-co-chon-VBCC.xlsx", false);
    }


    private ResponseEntity<Object> downloadTemplateNoTypeSelect(EducationCertificatesRequest.SubmitForm dto) throws Exception {
        String pathTemplate = "template/import/BM-import-chung-chi-khong-chon-VBCC.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);
        List<BaseCategoryDto> listCertificateType = educationCertificatesRepository.getListCategories(Constant.CATEGORY_CODES.LOAI_CHUNG_CHI);
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (BaseCategoryDto categoryDto : listCertificateType) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        int col = 8;
        dynamicExport.setCellFormat(2, 0, 6, col - 1, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM-import-chung-chi-khong-chon-VBCC.xlsx", false);
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity importProcess(MultipartFile file, EducationCertificatesRequest.SubmitForm dto) throws Exception {
        if (dto.getCertificateTypeId() != null) {
            return importProcessTypeSelect(file, dto);
        } else {
            return importProcessNoTypeSelect(file, dto);
        }
    }


    private ResponseEntity importProcessTypeSelect(MultipartFile file, EducationCertificatesRequest.SubmitForm dto) throws Exception {
        List<AttributeConfigDto> attributeConfigs = objectAttributesService.getAttributes(Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_CERTIFICATES, dto.getCertificateTypeId());
        BaseCategoryDto category = educationCertificatesRepository.getCategory(Constant.CATEGORY_CODES.LOAI_CHUNG_CHI, dto.getCertificateTypeId(), BaseCategoryDto.class);
        List<EducationCertificatesResponse.AttributeDto> attributes = educationCertificatesRepository.getAttributeOfCategory(category.getCategoryId());
        List<ImportExcel.ImportConfigBean> columnConfigs = new ArrayList<>();
        columnConfigs.add(new ImportExcel.ImportConfigBean("STT", ImportExcel.STRING, true, 20, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Mã nhân viên", ImportExcel.STRING, false, 20, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Họ và tên", ImportExcel.STRING, false, 50, false));
        String certificateTitle = "Tên chứng chỉ";
        boolean isAlowText = false;
        for (EducationCertificatesResponse.AttributeDto attribute : attributes) {
            if ("LABEL_TEN_CHUNG_CHI".equals(attribute.getAttributeCode())) {
                certificateTitle = String.valueOf(attribute.getAttributeValue());
            }
            if ("CHO_NHAP_TEXT".equals(attribute.getAttributeCode())
                && "Y".equals(attribute.getAttributeValue())) {
                isAlowText = true;
            }
        }
        columnConfigs.add(new ImportExcel.ImportConfigBean(certificateTitle, ImportExcel.STRING, false, 1000, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Nơi cấp", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Ngày cấp", ImportExcel.DATE, false, 10, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Ngày hết hạn", ImportExcel.DATE, true, 10, false));
        for (AttributeConfigDto attributeConfig : attributeConfigs) {
            ImportExcel.ImportConfigBean configBean = new ImportExcel.ImportConfigBean();
            configBean.setValues(attributeConfig.getName(), ImportExcel.STRING, true, 1000, false, 0d, null, "", true, true);
            columnConfigs.add(configBean);
        }
        ImportExcel importExcel = new ImportExcel(columnConfigs.toArray(new ImportExcel.ImportConfigBean[]{}), 10000, 3);


        List<Object[]> dataList = new ArrayList<>();

        Map<String, List<ObjectAttributesEntity>> mapValues = new HashMap<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }

            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);

            Map<String, String> mapCertificateName = new HashMap<>();
            List<BaseCategoryDto> listCertificateName = educationCertificatesRepository.getListCategoriesByParent(Constant.CATEGORY_CODES.TEN_CHUNG_CHI, Constant.CATEGORY_CODES.LOAI_CHUNG_CHI, dto.getCertificateTypeId());
            listCertificateName.forEach(item -> mapCertificateName.put(item.getName().toLowerCase(), item.getValue()));

            List<EducationCertificatesEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;

            for (Object[] obj : dataList) {
                EducationCertificatesEntity entity = new EducationCertificatesEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                int col = 0;
                String employeeCode = (String) obj[1];
                Long employeeId = 0L;
                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Mã nhân viên không tồn tại", employeeCode);
                } else if (!mapEmp.get(employeeCode.toLowerCase()).getFullName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(row, col, MessageFormat.format("Họ tên nhân viên không khớp với {0} - {1}", employeeCode, mapEmp.get(employeeCode.toLowerCase()).getFullName()), (String) obj[2]);
                } else {
                    employeeId = mapEmp.get(employeeCode.toLowerCase()).getEmployeeId();
                    entity.setEmployeeId(employeeId);
                }

                String certificateName = Utils.NVL(((String) obj[3])).trim();
                String certificateId = mapCertificateName.get(certificateName.toLowerCase());
                if (!Utils.isNullOrEmpty(certificateName)
                    && certificateId == null
                    && !isAlowText) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), certificateName);
                } else {
                    entity.setCertificateId(certificateId);
                }

                entity.setCertificateTypeId(dto.getCertificateTypeId());
                entity.setCertificateName(certificateName);
                entity.setIssuedPlace((String) obj[4]);
                entity.setIssuedDate((Date) obj[5]);
                entity.setExpiredDate((Date) obj[6]);

                listInsert.add(entity);

                col = 7;
                for (AttributeConfigDto attributeConfig : attributeConfigs) {
                    ObjectAttributesEntity objectAttributesEntity = new ObjectAttributesEntity();
                    objectAttributesEntity.setAttributeCode(attributeConfig.getCode());
                    objectAttributesEntity.setAttributeValue(obj[col] == null ? null : obj[col].toString());
                    String key = (!mapEmp.isEmpty() && mapEmp.get(employeeCode.toLowerCase()) != null)
                            ? mapEmp.get(employeeCode.toLowerCase()).getEmployeeId() + "-" + certificateId
                            : "defaultKey-" + certificateId;
                    if (mapValues.get(key) == null) {
                        mapValues.put(key, new ArrayList<>());
                    }
                    mapValues.get(key).add(objectAttributesEntity);
                    col++;
                }
                row++;
            }


            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                educationCertificatesRepository.insertBatch(EducationCertificatesEntity.class, listInsert, userName);
                //thuc hien insert attributes
                educationCertificatesRepository.insertValues(mapValues);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }

    private ResponseEntity importProcessNoTypeSelect(MultipartFile file, EducationCertificatesRequest.SubmitForm dto) throws Exception {
        List<ImportExcel.ImportConfigBean> columnConfigs = new ArrayList<>();
        columnConfigs.add(new ImportExcel.ImportConfigBean("STT", ImportExcel.STRING, true, 20, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Mã nhân viên", ImportExcel.STRING, false, 20, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Họ và tên", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Loại chứng chỉ", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Tên chứng chỉ", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Nơi cấp", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Ngày cấp", ImportExcel.DATE, false, 10, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Ngày hết hạn", ImportExcel.DATE, true, 10, false));

        ImportExcel importExcel = new ImportExcel(columnConfigs.toArray(new ImportExcel.ImportConfigBean[]{}), 10000, 2);


        List<Object[]> dataList = new ArrayList<>();

        Map<String, List<ObjectAttributesEntity>> mapValues = new HashMap<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }

            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);

            Map<String, String> mapCertificateType = new HashMap<>();
            List<BaseCategoryDto> listCertificateType = educationCertificatesRepository.getListCategories(Constant.CATEGORY_CODES.LOAI_CHUNG_CHI);
            listCertificateType.forEach(item -> mapCertificateType.put(item.getName().toLowerCase(), item.getValue()));


            List<EducationCertificatesEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;

            for (Object[] obj : dataList) {

                EducationCertificatesEntity entity = new EducationCertificatesEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                String certificateTypeName = Utils.NVL(((String) obj[3])).trim();
                String certificateTypeId = mapCertificateType.get(certificateTypeName.toLowerCase());
                if (!Utils.isNullOrEmpty(certificateTypeName) && certificateTypeId == null) {
                    importExcel.addError(row, 3, I18n.getMessage("error.import.category.invalid"), certificateTypeName);
                } else {
                    entity.setCertificateTypeId(certificateTypeId);
                }

                Map<String, String> mapCertificateName = new HashMap<>();
                List<BaseCategoryDto> listCertificateName = educationCertificatesRepository.getListCategoriesByParent(Constant.CATEGORY_CODES.TEN_CHUNG_CHI, Constant.CATEGORY_CODES.LOAI_CHUNG_CHI, certificateTypeId);
                listCertificateName.forEach(item -> mapCertificateName.put(item.getName().toLowerCase(), item.getValue()));


                int col = 0;
                String employeeCode = (String) obj[1];
                Long employeeId = 0L;
                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Mã nhân viên không tồn tại", employeeCode);
                } else if (!mapEmp.get(employeeCode.toLowerCase()).getFullName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(row, col, MessageFormat.format("Họ tên nhân viên không khớp với {0} - {1}", employeeCode, mapEmp.get(employeeCode.toLowerCase()).getFullName()), (String) obj[2]);
                } else {
                    employeeId = mapEmp.get(employeeCode.toLowerCase()).getEmployeeId();
                    entity.setEmployeeId(employeeId);
                }

                String certificateName = Utils.NVL(((String) obj[4])).trim();
                String certificateId = mapCertificateName.get(certificateName.toLowerCase());
                if (!Utils.isNullOrEmpty(certificateName) && certificateId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), certificateName);
                } else {
                    entity.setCertificateId(certificateId);
                }

                entity.setCertificateName(certificateName);
                entity.setIssuedPlace((String) obj[5]);
                entity.setIssuedDate((Date) obj[6]);
                entity.setExpiredDate((Date) obj[7]);

                listInsert.add(entity);

                row++;
            }


            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                educationCertificatesRepository.insertBatch(EducationCertificatesEntity.class, listInsert, userName);

            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }

}
