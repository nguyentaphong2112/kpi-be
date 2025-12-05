/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.ErrorImportException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.feigns.ReportFeignClient;
import vn.kpi.models.AttributeConfigDto;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.dto.BaseCategoryDto;
import vn.kpi.models.request.HealthRecordsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.HealthRecordsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.CategoryEntity;
import vn.kpi.repositories.entity.HealthRecordsEntity;
import vn.kpi.repositories.entity.ObjectAttributesEntity;
import vn.kpi.repositories.impl.EmployeesRepository;
import vn.kpi.repositories.impl.HealthRecordsRepository;
import vn.kpi.repositories.jpa.HealthRecordsRepositoryJPA;
import vn.kpi.services.HealthRecordsService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.UtilsService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ImportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_health_records
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class HealthRecordsServiceImpl implements HealthRecordsService {

    private final HealthRecordsRepository healthRecordsRepository;
    private final HealthRecordsRepositoryJPA healthRecordsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final EmployeesRepository employeesRepository;
    private final UtilsService utilsService;
    private final ReportFeignClient reportFeignClient;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<HealthRecordsResponse> searchData(HealthRecordsRequest.SearchForm dto) {
        return ResponseUtils.ok(healthRecordsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(HealthRecordsRequest.SubmitForm dto, Long id) throws BaseAppException {

        boolean duplicate = healthRecordsRepository.duplicate(HealthRecordsEntity.class, id, "employeeId", dto.getEmployeeId(), "examinationPeriodId", dto.getExaminationPeriodId());
        if (duplicate) {
            throw new BaseAppException("SAVE_HEALTH_RECORD_DUPLICATE", I18n.getMessage("error.healthRecord.duplicateRecord"));
        }

        HealthRecordsEntity entity;
        if (id != null && id > 0L) {
            entity = healthRecordsRepositoryJPA.getById(dto.getHealthRecordId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new HealthRecordsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        healthRecordsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getHealthRecordId(), dto.getListAttributes(), HealthRecordsEntity.class, null);
        return ResponseUtils.ok(entity.getHealthRecordId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<HealthRecordsEntity> optional = healthRecordsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, HealthRecordsEntity.class);
        }
        healthRecordsRepository.deActiveObject(HealthRecordsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<HealthRecordsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<HealthRecordsEntity> optional = healthRecordsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, HealthRecordsEntity.class);
        }
        HealthRecordsResponse dto = new HealthRecordsResponse();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, healthRecordsRepository.getSQLTableName(HealthRecordsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(HealthRecordsRequest.SearchForm dto) throws Exception {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(httpServletRequest), Constant.REPORT_CONFIG_CODES.EXPORT_DANH_SACH_KHAM_CHUA_BENH);
        ExportExcel dynamicExport;
        List<Map<String, Object>> listDataExport;
        if (response != null && response.getData() != null) {
            ReportConfigDto reportConfigDto = response.getData();
            dynamicExport = utilsService.initExportExcelByFileId(reportConfigDto.getAttachmentFileList().get(0).getFileId(), 2);
            listDataExport = healthRecordsRepository.getListExport(reportConfigDto.getQueryResponseList().get(0).getSqlQuery(), dto);
        } else {
            String pathTemplate = "template/export/BM_export_ket_qua_kham_chua_benh.xlsx";
            dynamicExport = new ExportExcel(pathTemplate, 2, true);
            listDataExport = healthRecordsRepository.getListExport(null, dto);
        }
        //set period id
        if (dto.getExaminationPeriodId() != null) {
            BaseCategoryDto categoryDto = healthRecordsRepository.getCategory(Constant.CATEGORY_CODES.HRM_DOT_KHAM_SUC_KHOE, dto.getExaminationPeriodId(), BaseCategoryDto.class);
            dynamicExport.replaceText(String.format("${%s}", "ky_kham_chua_benh"), categoryDto.getName());
        } else {
            dynamicExport.replaceText(String.format("${%s}", "ky_kham_chua_benh"), "");
        }

        dynamicExport.replaceKeys(listDataExport);

        return ResponseUtils.ok(dynamicExport, "Danh_sach_ket_qua_kham_chua_benh.xlsx");


    }

    @Override
    public ResponseEntity<Object> downloadTemplate(String periodId) throws Exception {
        String pathTemplate = "template/import/BM_import_ket_qua_kham_chua_benh.xlsx";
        ExportExcel dynamicExport = utilsService.initExportExcel("BM_IMPORT_KHAM_CHUA_BENH", pathTemplate, 5);

        List<BaseCategoryDto> listExaminationPeriod = healthRecordsRepository.getListCategories(Constant.CATEGORY_CODES.HRM_DOT_KHAM_SUC_KHOE);
        List<BaseCategoryDto> listResult = healthRecordsRepository.getListCategories(Constant.CATEGORY_CODES.HRM_XEP_LOAI_SUC_KHOE);
        List<BaseCategoryDto> listDisease = healthRecordsRepository.getListCategories(Constant.CATEGORY_CODES.HRM_BENH_GAP_PHAI);
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (BaseCategoryDto categoryDto : listResult) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(2);
        row = 1;
        for (BaseCategoryDto categoryDto : listDisease) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        for (BaseCategoryDto categoryDto : listExaminationPeriod) {
            if (categoryDto.getValue().equals(periodId)) {
                dynamicExport.replaceText("${examination_period}", categoryDto.getName());
            }
        }
        return ResponseUtils.ok(dynamicExport, "BM_import_ket_qua_kham_chua_benh.xlsx", false);
    }

    @Override
    @Transactional
    public ResponseEntity importProcess(String periodId, MultipartFile file) throws IOException {
        List<AttributeConfigDto> attributeConfigs = objectAttributesService.getAttributes(healthRecordsRepository.getSQLTableName(HealthRecordsEntity.class), null);
        List<ImportExcel.ImportConfigBean> columnConfigs = new ArrayList<>();
//        ImportExcel importExcel = new ImportExcel("template/import/BM_import_ket_qua_kham_chua_benh.xml");
        columnConfigs.add(new ImportExcel.ImportConfigBean("STT", ImportExcel.STRING, true, 20, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Mã nhân viên", ImportExcel.STRING, false, 20, true));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Họ và tên", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Ngày khám", ImportExcel.DATE, false, 10, false));
        for (AttributeConfigDto dto : attributeConfigs) {
            ImportExcel.ImportConfigBean configBean = new ImportExcel.ImportConfigBean();
            configBean.setValues(dto.getName(), dto.getDataType().equalsIgnoreCase("string") ? ImportExcel.STRING : ImportExcel.DOUBLE,
                    !dto.isRequired(),
                    500, false, 0d, null, "", true, true);
            columnConfigs.add(configBean);
        }
        columnConfigs.add(new ImportExcel.ImportConfigBean("Bệnh gặp phải", ImportExcel.STRING, true, 500, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Kết quả xếp loại", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Kết luận", ImportExcel.STRING, false, 50, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean("Mã bệnh nhân", ImportExcel.STRING, true, 20, true));
        ImportExcel importExcel = new ImportExcel(columnConfigs.toArray(new ImportExcel.ImportConfigBean[]{}), 10000, 4);


        List<Object[]> dataList = new ArrayList<>();

        Map<Long, List<ObjectAttributesEntity>> mapValues = new HashMap<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> empCodeList = new ArrayList<>();
            dataList.forEach(data -> {
                empCodeList.add(data[1].toString().toUpperCase());
            });
            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);

            Map<String, String> mapResultName = new HashMap<>();
            List<CategoryEntity> listResult = healthRecordsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HRM_XEP_LOAI_SUC_KHOE);
            listResult.forEach(item -> mapResultName.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapDiseaseName = new HashMap<>();
            List<CategoryEntity> listDisease = healthRecordsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HRM_BENH_GAP_PHAI);
            listDisease.forEach(item -> mapDiseaseName.put(item.getName().toLowerCase(), item.getValue()));

            List<HealthRecordsEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;

            for (Object[] obj : dataList) {
                HealthRecordsEntity entity = new HealthRecordsEntity();
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
                col = 3;
                entity.setExaminationPeriodId(periodId);
                Date examinationDate = (Date) obj[col];
                entity.setExaminationDate(examinationDate);
                col++;
                for (AttributeConfigDto attributeConfig : attributeConfigs) {
                    ObjectAttributesEntity objectAttributesEntity = new ObjectAttributesEntity();
                    objectAttributesEntity.setAttributeCode(attributeConfig.getCode());
                    objectAttributesEntity.setAttributeValue(obj[col] == null ? null : obj[col].toString());
                    if (!mapValues.containsKey(employeeId)) {
                        mapValues.put(employeeId, new ArrayList<>());
                    }
                    mapValues.get(employeeId).add(objectAttributesEntity);
                    col++;
                }
                String[] diseaseName = Utils.NVL(((String) obj[col])).split(",");
                List<String> diseaseIdsList = new ArrayList<>();
                for (String disease : diseaseName) {
                    disease = disease.trim();
                    String diseaseId = mapDiseaseName.get(disease.toLowerCase());
                    if (!Utils.isNullOrEmpty(disease) && diseaseId == null) {
                        importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), disease);
                    } else {
                        diseaseIdsList.add(diseaseId);
                    }
                }
                col++;
                String diseaseIds = String.join(",", diseaseIdsList);
                entity.setDiseaseIds(diseaseIds);
                //xep loai suc khoe
                String result = (String) obj[col];
                if (!mapResultName.containsKey(result.toLowerCase())) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), result);
                } else {
                    entity.setResultId(mapResultName.get(result.toLowerCase()));
                }

                entity.setDoctorConclusion((String) obj[col++]);
                entity.setPatientId((String) obj[col]);
                listInsert.add(entity);
                row++;
            }


            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                //thuc hien xoa du lieu cu neu nhu da ton tai
                healthRecordsRepository.deleteOldData(empCodeList, periodId);

                healthRecordsRepository.insertBatch(HealthRecordsEntity.class, listInsert, userName);
                //thuc hien insert attributes
                healthRecordsRepository.insertValues(mapValues, periodId);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }

}
