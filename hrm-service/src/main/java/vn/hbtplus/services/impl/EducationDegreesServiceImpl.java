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
import vn.hbtplus.models.request.EducationDegreesRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.EducationCertificatesEntity;
import vn.hbtplus.repositories.entity.EducationDegreesEntity;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.impl.EducationDegreesRepository;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.jpa.EducationDegreesRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.EducationDegreesService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.*;

import java.text.MessageFormat;
import java.util.*;

/**
 * Lop impl service ung voi bang hr_education_degrees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EducationDegreesServiceImpl implements EducationDegreesService {

    private final EducationDegreesRepository educationDegreesRepository;
    private final ObjectAttributesService objectAttributesService;
    private final EducationDegreesRepositoryJPA educationDegreesRepositoryJPA;
    private final EmployeesRepository employeesRepository;
    private final FileService fileService;
    private final AttachmentService attachmentService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EducationDegreesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(educationDegreesRepository.searchData(dto));
    }

    @Override
    public List<EducationDegreesResponse.DetailBean> searchDataByType(String type) {
        return educationDegreesRepository.getListByType(type);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(EducationDegreesRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException {
        boolean isDuplicate = educationDegreesRepository.isDuplicateEduDegree(dto, employeeId, id);
        if (isDuplicate) {
            throw new BaseAppException("ERROR_EDUCATION_DEGREE_DUPLICATE", I18n.getMessage("error.educationDegree.validate.duplicate"));
        }

        EducationDegreesEntity entity;
        boolean updateIsHighest = false;
        if (id != null && id > 0L) {
            entity = educationDegreesRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("educationDegreeId and employeeId not matching!");
            }
            if (entity.getIsHighest().equalsIgnoreCase(BaseConstants.COMMON.YES)
                    && dto.getIsHighest().equalsIgnoreCase(BaseConstants.COMMON.NO)
            ) {
                //truong hop can set 1 ban ghi ve isHighest = Y
                updateIsHighest = true;
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EducationDegreesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
            //neu chua co giay to nao thi set is_main = Y
            if (educationDegreesRepository.findByProperties(EducationDegreesEntity.class, "employeeId", employeeId, "isDeleted", BaseConstants.STATUS.NOT_DELETED, "isHighest", BaseConstants.COMMON.YES).isEmpty()) {
                dto.setIsHighest(BaseConstants.COMMON.YES);
            }
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        educationDegreesRepositoryJPA.saveAndFlush(entity);
        objectAttributesService.saveObjectAttributes(entity.getEducationDegreeId(), dto.getListAttributes(), EducationDegreesEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_DEGREES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_DEGREES_EMP);
        fileService.uploadFiles(files, entity.getEducationDegreeId(), Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_DEGREES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_DEGREES_EMP, Constant.ATTACHMENT.MODULE);

        if (!Utils.isNullOrEmpty(dto.getIsHighest()) && BaseConstants.COMMON.YES.equalsIgnoreCase(dto.getIsHighest())) {
            educationDegreesRepository.updateCancelIsHighest(entity.getEmployeeId(), entity.getEducationDegreeId());
        }
        if (updateIsHighest) {
            educationDegreesRepository.autoUpdateIsHighest(employeeId);
        }
        return ResponseUtils.ok(entity.getEducationDegreeId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<EducationDegreesEntity> optional = educationDegreesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EducationDegreesEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("educationDegreeId and employeeId not matching!");
        }
        educationDegreesRepository.deActiveObject(EducationDegreesEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_DEGREES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_DEGREES_EMP);

        if (optional.get().getIsHighest().equalsIgnoreCase(BaseConstants.COMMON.YES)) {
            educationDegreesRepository.autoUpdateIsHighest(employeeId);
        }
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EducationDegreesResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<EducationDegreesEntity> optional = educationDegreesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EducationDegreesEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("educationDegreeId and employeeId not matching!");
        }
        EducationDegreesResponse.DetailBean dto = new EducationDegreesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, educationDegreesRepository.getSQLTableName(EducationDegreesEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_EDUCATION_DEGREES, Constant.ATTACHMENT.FILE_TYPES.EDUCATION_DEGREES_EMP, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/thong-tin-bang-cap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = educationDegreesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-bang-cap.xlsx");
    }

    @Override
    public BaseDataTableDto<EducationDegreesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<EducationDegreesResponse.DetailBean> tableDto = educationDegreesRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getEducationDegreeId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_education_degrees");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getEducationDegreeId()));
        });
        return tableDto;
    }


    @Override
    public ResponseEntity<Object> downloadTemplate() throws Exception{
        String pathTemplate = "template/import/BM-import-bang-cap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);
        List<CategoryEntity> listMajorLevel = educationDegreesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO);
        List<CategoryEntity> listTrainingMethod = educationDegreesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO);
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (CategoryEntity categoryDto : listMajorLevel) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1,row++);
            dynamicExport.increaseRow();
        }

        dynamicExport.setActiveSheet(2);
        for (CategoryEntity categoryDto : listTrainingMethod) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1,row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);

        dynamicExport.setCellFormat(2, 0, 6, 7, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM-import-bang-cap.xlsx", false);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity importProcess(MultipartFile file, EducationDegreesRequest.SubmitForm dto) throws Exception{
        ImportExcel importExcel = new ImportExcel("template/import/BM-import-bang-cap.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

            Map<String, String> mapMajorLevel = new HashMap<>();
            List<CategoryEntity> listMajorLevel = educationDegreesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO);
            listMajorLevel.forEach(item -> mapMajorLevel.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapTrainingMethod = new HashMap<>();
            List<CategoryEntity> listTrainingMethod = educationDegreesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO);
            listTrainingMethod.forEach(item -> mapTrainingMethod.put(item.getName().toLowerCase(), item.getValue()));

            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }

            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);

            List<EducationDegreesEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;

            for (Object[] obj : dataList) {
                EducationDegreesEntity entity = new EducationDegreesEntity();
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

                String majorLevelName = Utils.NVL(((String) obj[3])).trim();
                String majorLevelId = mapMajorLevel.get(majorLevelName.toLowerCase());
                if (!Utils.isNullOrEmpty(majorLevelName) && majorLevelId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), majorLevelName);
                } else {
                    entity.setMajorLevelId(majorLevelId);
                }

                entity.setMajorLevelName(majorLevelName);
                entity.setMajorName((String) obj[4]);
                entity.setTrainingSchoolName((String) obj[5]);

                String trainingMethodName = Utils.NVL(((String) obj[6])).trim();
                String trainingMethodId = mapTrainingMethod.get(trainingMethodName.toLowerCase());
                if (!Utils.isNullOrEmpty(trainingMethodName) && trainingMethodId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), trainingMethodName);
                } else {
                    entity.setTrainingMethodId(trainingMethodId);
                }
                entity.setGraduatedYear(Integer.parseInt((String) obj[7]));
                if (educationDegreesRepository.findByProperties(EducationDegreesEntity.class, "employeeId", entity.getEmployeeId(), "isDeleted", BaseConstants.STATUS.NOT_DELETED, "isHighest", BaseConstants.COMMON.YES).isEmpty()) {
                    entity.setIsHighest(BaseConstants.COMMON.YES);
                }
                listInsert.add(entity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                educationDegreesRepository.insertBatch(EducationDegreesEntity.class, listInsert, userName);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }

}
