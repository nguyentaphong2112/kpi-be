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
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.OrgDto;
import vn.hbtplus.models.request.IndicatorsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.IndicatorConversionDetailEntity;
import vn.hbtplus.repositories.entity.IndicatorsEntity;
import vn.hbtplus.repositories.entity.ObjectRelationsEntity;
import vn.hbtplus.repositories.impl.IndicatorConversionsRepository;
import vn.hbtplus.repositories.impl.IndicatorsRepository;
import vn.hbtplus.repositories.jpa.IndicatorsRepositoryJPA;
import vn.hbtplus.services.IndicatorsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.ObjectRelationsService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.*;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang kpi_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class IndicatorsServiceImpl implements IndicatorsService {

    private final IndicatorsRepository indicatorsRepository;
    private final IndicatorsRepositoryJPA indicatorsRepositoryJPA;
    private final IndicatorConversionsRepository indicatorConversionsRepository;
    private final ObjectRelationsService objectRelationsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<IndicatorsResponse.SearchResult> searchData(IndicatorsRequest.SearchForm dto) {
        return ResponseUtils.ok(indicatorsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(IndicatorsRequest.SubmitForm dto, Long id) throws BaseAppException {
//        boolean isDuplicate = indicatorsRepository.isDuplicate(dto.getName(), id);
//        if (isDuplicate) {
//            throw new BaseAppException("ERROR_INDICATOR_DUPLICATE", I18n.getMessage("error.indicator.name.duplicate"));
//        }
        IndicatorsEntity entity;
        if (id != null && id > 0L) {
            entity = indicatorsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new IndicatorsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        indicatorsRepositoryJPA.save(entity);
//        saveOrgScope(dto.getOrgIds(), entity.getIndicatorId());
//        saveIndicatorRelate(dto.getIndicatorIds(), entity.getIndicatorId());
        return ResponseUtils.ok(entity.getIndicatorId());
    }

    private void saveOrgScope(List<Long> orgIds, Long indicatorId) {
        objectRelationsService.inactiveReferIdNotIn(indicatorId, orgIds, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS, ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        objectRelationsService.saveObjectRelations(indicatorId, orgIds, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS, ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
    }

    private void saveIndicatorRelate(List<Long> indicatorIds, Long indicatorId) {
        objectRelationsService.inactiveReferIdNotIn(indicatorId, indicatorIds, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN);
        objectRelationsService.saveObjectRelations(indicatorId, indicatorIds, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN);
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<IndicatorsEntity> optional = indicatorsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorsEntity.class);
        }
        indicatorsRepository.deActiveObject(IndicatorsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IndicatorsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException {
        Optional<IndicatorsEntity> optional = indicatorsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorsEntity.class);
        }
        IndicatorsResponse.SearchResult dto = new IndicatorsResponse.SearchResult();
        Utils.copyProperties(optional.get(), dto);
//        dto.setOrgIds(indicatorsRepository.getRelativeIds(id, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS, ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG));
//        dto.setIndicatorIds(indicatorsRepository.getRelativeIds(id, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(IndicatorsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_chi_so_KPI.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = indicatorsRepository.getListExport(dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_chi_so_KPI.xlsx");
    }

    @Override
    public BaseDataTableDto<IndicatorsResponse.SearchResult> getIndicatorPicker(Long organizationId, IndicatorsRequest.SearchForm dto) {
        return indicatorsRepository.getIndicatorPicker(organizationId, dto);
    }

    @Override
    public List<IndicatorsResponse.DetailList> getList(Long organizationId) {
        List<IndicatorsResponse.DetailList> result = indicatorsRepository.getList(organizationId);
        result.forEach(item -> {
            List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionDetailEntity.class, "indicatorConversionId", item.getIndicatorConversionId());
            conversionDetailEntities.forEach(d -> {
                IndicatorConversionsResponse.ConversionDetail tem = new IndicatorConversionsResponse.ConversionDetail();
                tem.setResultId(d.getResultId());
                tem.setExpression(getExpression(d));
                item.getConversions().add(tem);
            });
        });
        return result;
    }

    @Override
    public List<IndicatorsResponse.DetailList> getListEmployee(Long employeeId) {
        List<IndicatorsResponse.DetailList> result = indicatorsRepository.getListEmployee(employeeId);
        if (result.size() == 0) {
            result = indicatorsRepository.getListEmployee2(employeeId);
        }
        result.forEach(item -> {
            List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionDetailEntity.class, "indicatorConversionId", item.getIndicatorConversionId());
            conversionDetailEntities.forEach(d -> {
                IndicatorConversionsResponse.ConversionDetail tem = new IndicatorConversionsResponse.ConversionDetail();
                tem.setResultId(d.getResultId());
                tem.setExpression(getExpression(d));
                item.getConversions().add(tem);
            });
        });
        return result;
    }

    @Override
    public boolean importData(MultipartFile fileImport) throws Exception {
        String fileConfigName = "BM_import_chi_so_kpi.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        Date curDate = new Date();
        String userName = Utils.getUserNameLogin();
        List<IndicatorsEntity> indicatorsEntityList = new ArrayList<>();
        Map<String, String> listUnit = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.DON_VI_TINH).stream()
                .collect(Collectors.toMap(CategoryDto::getName, CategoryDto::getValue, (existing, replacement) -> replacement));
        Map<String, String> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI).stream()
                .collect(Collectors.toMap(CategoryDto::getName, CategoryDto::getValue, (existing, replacement) -> replacement));
        Map<String, String> listPeriodType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.CHU_KY).stream()
                .collect(Collectors.toMap(CategoryDto::getName, CategoryDto::getValue, (existing, replacement) -> replacement));
        Map<String, Long> listOrg = indicatorsRepository.getListOrg().stream()
                .collect(Collectors.toMap(OrgDto::getFullName, OrgDto::getOrganizationId));
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            int index = 0;
            for (Object[] obj : dataList) {
                int col = 1;
                String name = (String) obj[col++];
                if (name.length() > 255) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicator.name.maxlength"), name);
                }
//                boolean isDuplicate = indicatorsRepository.isDuplicate(name, null);
//                if (isDuplicate) {
//                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicator.name.duplicate"), name);
//                }
                String unitName = (String) obj[col++];
                String unitId = listUnit.get(unitName);
                if (Utils.isNullOrEmpty(unitId)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicator.unitId.error"), unitName);
                }
                String periodTypeName = (String) obj[col++];
                String periodType = listPeriodType.get(periodTypeName);
                if (Utils.isNullOrEmpty(periodType)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicator.periodType.error"), periodTypeName);
                }
                String significance = (String) obj[col++];
                String measurement = (String) obj[col++];
                String systemInfo = (String) obj[col++];
                String typeName = (String) obj[col++];
                String type = listType.get(typeName);
                if (Utils.isNullOrEmpty(type)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicator.type.error"), typeName);
                }
                String orgName = (String) obj[col++];
                Long orgId = listOrg.get(orgName);
                if (orgId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.kpi.import.invalid", orgName), orgName);
                }
                String note = (String) obj[col];
                IndicatorsEntity indicatorsEntity = new IndicatorsEntity();
                indicatorsEntity.setIndicatorId(indicatorsRepository.getNextId(IndicatorsEntity.class));
                indicatorsEntity.setName(name);
                indicatorsEntity.setUnitId(unitId);
                indicatorsEntity.setPeriodType(periodType);
                indicatorsEntity.setSignificance(significance);
                indicatorsEntity.setMeasurement(measurement);
                indicatorsEntity.setSystemInfo(systemInfo);
                indicatorsEntity.setType(type);
                indicatorsEntity.setNote(note);
                indicatorsEntity.setOrgIds(List.of(orgId));
                indicatorsEntity.setCreatedTime(curDate);
                indicatorsEntity.setCreatedBy(userName);
                indicatorsEntityList.add(indicatorsEntity);
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                indicatorsRepository.insertBatch(IndicatorsEntity.class, indicatorsEntityList, userName);
                for (IndicatorsEntity entity : indicatorsEntityList) {
                    saveOrgScope(entity.getOrgIds(), entity.getIndicatorId());
                }
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
        return true;
    }

    @Override
    public String getTemplateIndicator() throws Exception {
        String importTemplateName = "BM_import_chi_so_kpi.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);
        List<CategoryDto> listUnit = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.DON_VI_TINH);
        List<CategoryDto> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI);
        List<CategoryDto> listPeriodType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.CHU_KY);
        List<OrgDto> listOrg = indicatorsRepository.getListOrg();
        dynamicExport.setActiveSheet(1);
        int row = 0;
        for (CategoryDto categoryDto : listUnit) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(2);
        for (CategoryDto categoryDto : listType) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(3);
        for (CategoryDto categoryDto : listPeriodType) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 0;
        dynamicExport.setActiveSheet(4);
        for (OrgDto orgDto : listOrg) {
            dynamicExport.setText(orgDto.getFullName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    public String getMappingValue(String parameter, String configCode, Date configDate) {
        return indicatorsRepository.getMappingValue(parameter, configCode, configDate);
    }


    private String getExpression(IndicatorConversionDetailEntity conversionDetailEntity) {
        StringBuilder result = new StringBuilder();
        if (!Utils.isNullOrEmpty(conversionDetailEntity.getMinComparison())) {
            result.append(getComparison(conversionDetailEntity.getMinComparison()));
            result.append(" ");
            result.append(conversionDetailEntity.getMinValue());
        }
        if (!Utils.isNullOrEmpty(conversionDetailEntity.getMaxComparison())) {
            if (!result.isEmpty()) {
                result.append(" và ");
            }
            result.append(getComparison(conversionDetailEntity.getMaxComparison()));
            result.append(" ");
            result.append(conversionDetailEntity.getMaxValue());
        }
        return result.toString();
    }

    private String getComparison(String value) {
        switch (value) {
            case "EQUAL":
                return "=";
            case "GREATER_THAN":
                return ">";
            case "GREATER_THAN_EQUAL":
                return ">=";
            case "LESS_THAN":
                return "<";
            case "LESS_THAN_EQUAL":
                return "<=";
        }
        return null;
    }

}
