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
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.FamilyRelationshipsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.FamilyRelationshipsEntity;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.impl.FamilyRelationshipsRepository;
import vn.hbtplus.repositories.jpa.FamilyRelationshipsRepositoryJPA;
import vn.hbtplus.services.FamilyRelationshipsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Lop impl service ung voi bang hr_family_relationships
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class FamilyRelationshipsServiceImpl implements FamilyRelationshipsService {

    private final FamilyRelationshipsRepository familyRelationshipsRepository;
    private final ObjectAttributesService objectAttributesService;
    private final FamilyRelationshipsRepositoryJPA familyRelationshipsRepositoryJPA;
    private final EmployeesRepository employeesRepository;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<FamilyRelationshipsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(familyRelationshipsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(FamilyRelationshipsRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException {
        boolean isDuplicate = familyRelationshipsRepository.duplicate(FamilyRelationshipsEntity.class, id, "employeeId", employeeId, "relationTypeId", dto.getRelationTypeId(), "fullName", dto.getFullName());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_FAMILY_RELATION_DUPLICATE", I18n.getMessage("error.familyRelationship.validate.duplicate"));
        }
        FamilyRelationshipsEntity entity;
        if (id != null && id > 0L) {
            entity = familyRelationshipsRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("familyRelationshipId and employeeId not matching!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new FamilyRelationshipsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        String formatDate = BaseConstants.COMMON_DATE_FORMAT;
        if ("MONTH".equalsIgnoreCase(dto.getTypeDateOfBirth())) {
            formatDate = BaseConstants.SHORT_DATE_FORMAT;
        } else if ("YEAR".equalsIgnoreCase(dto.getTypeDateOfBirth())) {
            formatDate = BaseConstants.YEAR_FORMAT;
        }
        entity.setDateOfBirth(Utils.stringToDate(dto.getDateOfBirthStr(), formatDate));
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        familyRelationshipsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getFamilyRelationshipId(), dto.getListAttributes(), FamilyRelationshipsEntity.class, null);
        return ResponseUtils.ok(entity.getFamilyRelationshipId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<FamilyRelationshipsEntity> optional = familyRelationshipsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, FamilyRelationshipsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("familyRelationshipId and employeeId not matching!");
        }
        familyRelationshipsRepository.deActiveObject(FamilyRelationshipsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<FamilyRelationshipsResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        FamilyRelationshipsResponse.DetailBean dto = familyRelationshipsRepository.getDataById(id);
        if (dto == null) {
            throw new RecordNotExistsException(id, FamilyRelationshipsEntity.class);
        }
        if (!dto.getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("familyRelationshipId and employeeId not matching!");
        }
        dto.setListAttributes(objectAttributesService.getAttributes(id, familyRelationshipsRepository.getSQLTableName(FamilyRelationshipsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/thong-tin-than-nhan.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = familyRelationshipsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-than-nhan.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<FamilyRelationshipsResponse.DetailBean> tableDto = familyRelationshipsRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getFamilyRelationshipId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_family_relationships");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getFamilyRelationshipId()));
        });
        return tableDto;
    }

    @Override
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        String pathTemplate = "template/import/BM-import-than-nhan.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);
        List<BaseCategoryDto> listFamilyRelation = familyRelationshipsRepository.getListCategories(Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        List<BaseCategoryDto> listFamilyStatus = familyRelationshipsRepository.getListCategories(Constant.CATEGORY_CODES.TINH_TRANG_TN);
        List<BaseCategoryDto> listPolicy = familyRelationshipsRepository.getListCategories(Constant.CATEGORY_CODES.DOI_TUONG_CHINH_SACH);
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (BaseCategoryDto categoryDto : listFamilyRelation) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(2);
        row = 1;
        for (BaseCategoryDto categoryDto : listFamilyStatus) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(3);
        for (BaseCategoryDto categoryDto : listPolicy) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }

        dynamicExport.setActiveSheet(0);
        dynamicExport.setCellFormat(2, 0, 5, 14, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM-import-than-nhan.xlsx", false);
    }

    ;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity importProcess(MultipartFile file) throws IOException {

        ImportExcel importExcel = new ImportExcel("template/import/BM-import-than-nhan.xml");


        List<Object[]> dataList = new ArrayList<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }
            //Lay du lieu trong DB
            List<FamilyRelationshipsEntity> listOldData = familyRelationshipsRepository.findByProperties(FamilyRelationshipsEntity.class);
            Map<String, FamilyRelationshipsEntity> mapOldData = new HashMap<>();
            listOldData.forEach(record -> {
                String key = record.getEmployeeId() + "_" + record.getFullName() + "_" + record.getRelationTypeId();
                mapOldData.put(key, record);
            });

            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);

            Map<String, String> mapFamilyRelationName = new HashMap<>();
            List<CategoryEntity> listFamilyRelation = familyRelationshipsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
            listFamilyRelation.forEach(item -> mapFamilyRelationName.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapFamilyStatusName = new HashMap<>();
            List<CategoryEntity> listFamilyStatus = familyRelationshipsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.TINH_TRANG_TN);
            listFamilyStatus.forEach(item -> mapFamilyStatusName.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapPolicy = new HashMap<>();
            List<CategoryEntity> listPolicy = familyRelationshipsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.DOI_TUONG_CHINH_SACH);
            listPolicy.forEach(item -> mapPolicy.put(item.getName().toLowerCase(), item.getValue()));

            List<FamilyRelationshipsEntity> listInsert = new ArrayList<>();
            List<FamilyRelationshipsEntity> listUpdate = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;

            for (Object[] obj : dataList) {
                FamilyRelationshipsEntity entity = new FamilyRelationshipsEntity();
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

                String relationName = Utils.NVL(((String) obj[3])).trim();
                String relationId = mapFamilyRelationName.get(relationName.toLowerCase());
                if (!Utils.isNullOrEmpty(relationName) && relationId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), relationName);
                } else {
                    entity.setRelationTypeId(relationId);
                }

                String fullName = (String) obj[4];
                entity.setFullName(fullName);

                String day = (String) obj[5];
                String month = (String) obj[6];
                String year = (String) obj[7];

                String defaultDay = "01";
                String defaultMonth = "01";

                String dateString = "";
                String format;
                String DoBType = "";

                if (year != null && month == null && day == null) {
                    dateString = defaultDay + "/" + defaultMonth + "/" + year;
                    DoBType = "YEAR";

                } else if (year != null && month != null && day == null) {
                    dateString = defaultDay + "/" + month + "/" + year;
                    DoBType = "MONTH";
                } else if (year != null && month != null && day != null) {
                    dateString = day + "/" + month + "/" + year;
                    DoBType = "DATE";
                }

                entity.setDateOfBirth(Utils.stringToDate(dateString));
                entity.setTypeDateOfBirth(DoBType);

                String statusName = Utils.NVL(((String) obj[8])).trim();
                String statusId = mapFamilyStatusName.get(statusName.toLowerCase());
                if (!Utils.isNullOrEmpty(statusName) && statusId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), statusName);
                } else {
                    entity.setRelationStatusId(statusId);
                }

                String policyName = Utils.NVL(((String) obj[9])).trim();
                String policyId = mapPolicy.get(policyName.toLowerCase());
                entity.setPolicyTypeId(policyId);

                String personalId = (String) obj[10];
                entity.setPersonalIdNo(personalId);

                String mobileNumber = (String) obj[11];
                entity.setMobileNumber(mobileNumber);

                String job = (String) obj[12];
                entity.setJob(job);

                String orgName = (String) obj[13];
                entity.setOrganizationAddress(orgName);

                String address = (String) obj[14];
                entity.setCurrentAddress(address);

                String recordKey = employeeId + "_" + fullName + "_" + relationId;

                //check conflict trong file
                for (int nextRow = row + 1; nextRow < dataList.size(); nextRow++) {
                    Object[] obj2 = dataList.get(nextRow);
                    String nextEmployeeCode = (String) obj2[1];
                    Long nextEmployeeId = mapEmp.get(nextEmployeeCode.toLowerCase()).getEmployeeId();
                    String nextFullName2 = (String) obj2[4];
                    String nextRelationName = Utils.NVL(((String) obj[3])).trim();
                    String nextRelationId = mapFamilyRelationName.get(nextRelationName.toLowerCase());
                    String nextRowKey = nextEmployeeId + "_" + nextFullName2 + "_" + nextRelationId;
                    if (recordKey.equals(nextRowKey)) {
                        importExcel.addError(row, col, I18n.getMessage("error.familyRelationship.duplicateInFile"), nextEmployeeCode);
                    }
                }

                if (mapOldData.get(recordKey) != null) {
                    entity.setFamilyRelationshipId(mapOldData.get(recordKey).getFamilyRelationshipId());
                    entity.setModifiedTime(new Date());
                    entity.setModifiedBy(userName);
                    listUpdate.add(entity);
                } else {
                    listInsert.add(entity);
                }

                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                familyRelationshipsRepository.insertBatch(FamilyRelationshipsEntity.class, listInsert, userName);
                familyRelationshipsRepository.updateBatch(FamilyRelationshipsEntity.class, listUpdate, true);

            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }
}

