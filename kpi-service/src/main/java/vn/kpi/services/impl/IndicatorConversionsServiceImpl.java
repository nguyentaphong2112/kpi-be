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
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.OrgDto;
import vn.kpi.models.request.IndicatorConversionsRequest;
import vn.kpi.models.request.OrganizationIndicatorsRequest;
import vn.kpi.models.response.*;
import vn.kpi.repositories.entity.CategoryEntity;
import vn.kpi.repositories.entity.IndicatorConversionDetailEntity;
import vn.kpi.repositories.entity.IndicatorConversionsEntity;
import vn.kpi.repositories.entity.IndicatorMastersEntity;
import vn.kpi.repositories.impl.IndicatorConversionsRepository;
import vn.kpi.repositories.impl.IndicatorMastersRepository;
import vn.kpi.repositories.impl.IndicatorsRepository;
import vn.kpi.repositories.jpa.IndicatorConversionDetailsRepositoryJPA;
import vn.kpi.repositories.jpa.IndicatorConversionsRepositoryJPA;
import vn.kpi.repositories.jpa.IndicatorMastersRepositoryJPA;
import vn.kpi.services.IndicatorConversionsService;
import vn.kpi.utils.*;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang kpi_indicator_conversions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class IndicatorConversionsServiceImpl implements IndicatorConversionsService {

    private final IndicatorConversionsRepository indicatorConversionsRepository;
    private final IndicatorConversionsRepositoryJPA indicatorConversionsRepositoryJPA;
    private final IndicatorConversionDetailsRepositoryJPA indicatorConversionDetailsRepositoryJPA;
    private final IndicatorsRepository indicatorsRepository;
    private final IndicatorMastersRepository indicatorMastersRepository;
    private final IndicatorMastersRepositoryJPA indicatorMastersRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<IndicatorConversionsResponse.SearchResult> searchData(IndicatorConversionsRequest.SearchForm dto) {
        return ResponseUtils.ok(indicatorConversionsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(IndicatorConversionsRequest.SubmitForm dto, Long id) throws BaseAppException {
        Map<Long, IndicatorConversionsEntity> listDataCheck = indicatorConversionsRepository.getDataDup(dto.getIndicatorMasterId(), id);
        if (listDataCheck.get(dto.getIndicatorId()) != null) {
            if (dto.getIndicatorId() != null && dto.getIndicatorId() > 0) {
                throw new BaseAppException("ERROR_INDICATOR_CONVERSION_DUPLICATE", I18n.getMessage("error.indicatorConversion.indicator.duplicate"));
            } else {
                return ResponseUtils.ok(listDataCheck.get(dto.getIndicatorId()).getIndicatorConversionId());
            }
        }
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        IndicatorConversionsEntity entity;
        if (id != null && id > 0L) {
            entity = indicatorConversionsRepositoryJPA.getById(id);
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userName);
        } else {
            entity = new IndicatorConversionsEntity();
            entity.setCreatedTime(currentDate);
            entity.setCreatedBy(userName);
        }
        Utils.copyProperties(dto, entity);
        entity.setStatus(IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET);
        entity.setIsFocusReduction(IndicatorConversionsEntity.FOCUS_REDUCTION.N);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        indicatorConversionsRepositoryJPA.save(entity);

        IndicatorMastersEntity entityMaster = indicatorMastersRepository.get(IndicatorMastersEntity.class, entity.getIndicatorMasterId());
        entityMaster.setModifiedTime(new Date());
        entityMaster.setModifiedBy(Utils.getUserNameLogin());
        entityMaster.setStatusId(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET);
        indicatorMastersRepositoryJPA.save(entityMaster);
        indicatorConversionDetailsRepositoryJPA.deleteByIndicatorConversionId(entity.getIndicatorConversionId());
        List<IndicatorConversionDetailEntity> listInsertData = new ArrayList<>();
        if (!Utils.isNullObject(dto.getConversions())) {
            dto.getConversions().forEach(item -> {
                IndicatorConversionDetailEntity conversionDetail = new IndicatorConversionDetailEntity();
                conversionDetail.setIndicatorConversionId(entity.getIndicatorConversionId());
                conversionDetail.setResultId(item.getResultId());
                conversionDetail.setMinValue(item.getMinValue());
                conversionDetail.setMaxValue(item.getMaxValue());
                conversionDetail.setMaxComparison(item.getMaxComparison());
                conversionDetail.setMinComparison(item.getMinComparison());
                conversionDetail.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                conversionDetail.setNote(item.getNote());
                listInsertData.add(conversionDetail);
            });
        }

        indicatorMastersRepository.insertBatch(IndicatorConversionDetailEntity.class, listInsertData, userName);
        return ResponseUtils.ok(entity.getIndicatorConversionId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<IndicatorConversionsEntity> optional = indicatorConversionsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorConversionsEntity.class);
        }
        IndicatorConversionsEntity entity = optional.get();
        if (IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatus())) {
            indicatorConversionsRepository.deActiveObject(IndicatorConversionsEntity.class, id);
        } else {
            entity.setStatus(IndicatorConversionsEntity.STATUS.DE_NGHI_XOA);
            entity.setModifiedBy(Utils.getUserNameLogin());
            entity.setModifiedTime(new Date());
            indicatorConversionsRepositoryJPA.save(entity);
        }
        indicatorMastersRepository.updateStatusByConversions(entity.getIndicatorMasterId());
        return ResponseUtils.ok(id);
    }


    @Override
    public ResponseEntity updateStatusById(Long id, IndicatorConversionsRequest.SubmitForm dto) throws RecordNotExistsException {
        Optional<IndicatorConversionsEntity> optional = indicatorConversionsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorConversionsEntity.class);
        }
        IndicatorConversionsEntity entity = optional.get();
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        if (IndicatorConversionsEntity.STATUS.PHE_DUYET.equalsIgnoreCase(dto.getStatus())) {
            if (IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatus())
                    || IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI.equalsIgnoreCase(entity.getStatus())
            ) {
                entity.setStatus(dto.getStatus());
            } else if (IndicatorConversionsEntity.STATUS.DE_NGHI_XOA.equalsIgnoreCase(entity.getStatus())) {
                entity.setStatus(IndicatorConversionsEntity.STATUS.HET_HIEU_LUC);
            }
        } else {
            entity.setStatus(dto.getStatus());
        }
        indicatorConversionsRepositoryJPA.save(entity);
        indicatorMastersRepository.updateStatusByConversions(entity.getIndicatorMasterId());

        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IndicatorConversionsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        IndicatorConversionsResponse.DetailBean dto = indicatorConversionsRepository.getById(id);
        List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.getDetails(id);
        conversionDetailEntities.forEach(d -> {
            IndicatorConversionsResponse.ConversionDetail tem = new IndicatorConversionsResponse.ConversionDetail();
            Utils.copyProperties(d, tem);
            tem.setExpression(getExpression(d, new HashMap<>()));
            dto.getConversions().add(tem);
        });
        return ResponseUtils.ok(dto);
    }


    @Override
    public IndicatorConversionsResponse.Indicators getListConversion(Long indicatorMasterId) {
        IndicatorConversionsResponse.Indicators result = indicatorConversionsRepository.getIndicatorsByMasterId(indicatorMasterId);
        return result;
    }

    @Override
    public TableResponseEntity<IndicatorConversionsResponse.Indicator> getListConversionTable(IndicatorConversionsRequest.SearchForm dto) {
        BaseDataTableDto<IndicatorConversionsResponse.Indicator> listData = indicatorConversionsRepository.getListIndicator(dto.getIndicatorMasterId(), dto);
//        Map<String, String> listCategory = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.DKY_MUC_TIEU_DON_VI).stream()
//                .collect(Collectors.toMap(CategoryDto::getValue, CategoryDto::getName));
        listData.getListData().forEach(item -> {
            List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionDetailEntity.class, "indicatorConversionId", item.getIndicatorConversionId());
            conversionDetailEntities.forEach(d -> {
                IndicatorConversionsResponse.ConversionDetail tem = new IndicatorConversionsResponse.ConversionDetail();
                tem.setResultId(d.getResultId());
                tem.setExpression(getExpression(d, new HashMap<>()));
                item.getConversions().add(tem);
            });
        });
        return ResponseUtils.ok(listData);
    }

    @Override
    public TableResponseEntity<IndicatorConversionsResponse.Indicator> getListIndicatorConversion(IndicatorConversionsRequest.SearchForm dto) {
        BaseDataTableDto data = indicatorConversionsRepository.getListIndicatorConversion(dto);
        List<IndicatorConversionsResponse.Indicator> result = data.getListData();
//        Map<String, String> listCategory = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.DKY_MUC_TIEU_DON_VI).stream()
//                .collect(Collectors.toMap(CategoryDto::getValue, CategoryDto::getName));
        result.forEach(item -> {
            List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionDetailEntity.class, "indicatorConversionId", item.getIndicatorConversionId());
            if (dto.getIsEmp()) {
                Map<String, String> targetMap = new HashMap<>();
                if (item.getTarget() != null) {
                    String target1 = null;
                    String target2 = null;
                    String target3 = null;
                    OrganizationIndicatorsRequest.Target target = Utils.fromJson(item.getTarget(), OrganizationIndicatorsRequest.Target.class);
                    if (target != null) {
                        if (target.getM1() != null) target1 = getValueTarget(target.getM1());
                        if (target.getM2() != null) target2 = getValueTarget(target.getM2());
                        if (target.getM3() != null) target3 = getValueTarget(target.getM3());
                    }
                    targetMap.put("mức ngưỡng", target1);
                    targetMap.put("mức cơ bản", target2);
                    targetMap.put("mức đẩy mạnh", target3);
                }
                conversionDetailEntities.forEach(d -> {
                    IndicatorConversionsResponse.ConversionDetail tem = new IndicatorConversionsResponse.ConversionDetail();
                    tem.setResultId(d.getResultId());
                    tem.setExpression(getExpression(d, targetMap));
                    item.getConversions().add(tem);
                });
            } else {
                conversionDetailEntities.forEach(d -> {
                    IndicatorConversionsResponse.ConversionDetail tem = new IndicatorConversionsResponse.ConversionDetail();
                    tem.setResultId(d.getResultId());
                    tem.setExpression(getExpression(d, new HashMap<>()));
                    item.getConversions().add(tem);
                });
            }
        });
        data.setListData(result);
        return ResponseUtils.ok(data);
    }

    @Override
    public List<IndicatorConversionsResponse.Organization> getListOrganization(Long organizationId, Long orgTypeId) {
        return indicatorConversionsRepository.getListOrganization(organizationId, orgTypeId);
    }

    @Override
    public String getTemplateIndicator(Long indicatorMasterId, Long orgId) throws Exception {
        String pathTemplate = "template/import/BM_import_ngan_hang_chi_so.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        List<IndicatorsResponse.DataList> indicatorList = indicatorConversionsRepository.getIndicators(orgId);
        IndicatorConversionsResponse.Indicators data = indicatorConversionsRepository.getJobName(indicatorMasterId);
        dynamicExport.setText(data.getOrganizationName(), 1, 0);
        if (!Utils.isNullOrEmpty(data.getJobName()) && "4".equals(data.getKpiLevel())) {
            dynamicExport.setText("DANH MỤC CHỈ SỐ ĐÁNH GIÁ HIỆU SUẤT LÀM VIỆC CỦA VỊ TRÍ VIỆC LÀM (KPI cấp 4)", 0, 1);
            dynamicExport.setText("Tên vị trí việc làm: " + data.getJobName(), 0, 3);
        } else {
            dynamicExport.setText("HỆ THỐNG THANG ĐO TƯƠNG ỨNG VỚI CÁC CHỈ SỐ ĐÁNH GIÁ HIỆU SUẤT LÀM VIỆC CỦA ĐƠN VỊ CẤU THÀNH ĐƠN VỊ THUỘC TRƯỜNG", 0, 1);
        }
        String importTemplateName = removeAccent(String.join(" ", Utils.NVL(data.getKpiLevelName()), Utils.NVL(data.getJobName().toUpperCase()), Utils.NVL(data.getOrganizationName()))) + ".xlsx";
        String fileName = Utils.getFilePathExportDefault(importTemplateName);
        dynamicExport.setActiveSheet(1);
        int row = 0;
        for (IndicatorsResponse.DataList indicator : indicatorList) {
            dynamicExport.setText(indicator.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    private static String removeAccent(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(normalized).replaceAll("");

        noAccent = noAccent.replaceAll("đ", "d");
        noAccent = noAccent.replaceAll("Đ", "D");
        return noAccent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importData(MultipartFile fileImport, Long indicatorMasterId) throws Exception {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_ngan_hang_chi_so.xml");
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        List<IndicatorConversionsEntity> indicatorConversionsEntityList = new ArrayList<>();
        Optional<IndicatorMastersEntity> optional = indicatorMastersRepositoryJPA.findById(indicatorMasterId);
        Map<Long, IndicatorConversionsEntity> listDataCheck = indicatorConversionsRepository.getDataDup(indicatorMasterId, null);
        Map<String, String> requiredList = IndicatorConversionsEntity.REQUIRED_LIST_MAP;
        Map<String, String> conversionTypeMap = IndicatorConversionsEntity.CONVERSION_VALUE_MAP;
        Map<String, IndicatorsResponse.DataList> indicatorList = indicatorConversionsRepository.getIndicators(optional.get().getOrganizationId()).stream()
                .collect(Collectors.toMap(
                        IndicatorsResponse.DataList::getName,
                        Function.identity(),
                        (existing, replacement) -> replacement)
                );

        Map<String, String> mapConversionType = indicatorsRepository.getMapData("name", "value", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.KPI_DKY_MUC_TIEU_DON_VI);

        List<String> conversionList = new ArrayList<>();
        List<String> indicatorNameList = new ArrayList<>();
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            int index = 0;
            for (Object[] obj : dataList) {
                int col = 1;
                String name = (String) obj[col++];
                IndicatorsResponse.DataList indicatorData = indicatorList.get(name.trim());
                Long indicatorId = indicatorData != null ? indicatorData.getIndicatorId() : null;
                if (indicatorId == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.indicatorConversion.indicator.error"), name);
                } else {
                    boolean isDuplicate2 = indicatorNameList.contains(name.trim());
                    IndicatorConversionsEntity isDuplicateEntity = listDataCheck.get(indicatorId);
                    if (isDuplicateEntity != null || isDuplicate2) {
                        importExcel.addError(index, 1, I18n.getMessage("error.indicatorConversion.indicator.duplicate"), name);
                    }
                    indicatorNameList.add(name.trim());
                }
                String conversionType = obj[7] != null ? conversionTypeMap.get(((String) obj[7]).trim().toLowerCase()) : "CA_NHAN";
                if (conversionType == null) {
                    importExcel.addError(index, 7, I18n.getMessage("error.import.category.invalid"), (String) obj[7]);
                }
                String ratingType = indicatorData != null ? indicatorData.getRatingType() : "";
                boolean isOrg = IndicatorConversionsEntity.CONVERSION_TYPE.DON_VI.equals(conversionType);
                String five = (String) obj[col++];
                if (!"SELECT".equals(ratingType) && checkComparison(five, isOrg, mapConversionType)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicatorConversion.error"), five);
                }
                String four = (String) obj[col++];
                if (!"SELECT".equals(ratingType) && checkComparison(four, isOrg, mapConversionType)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicatorConversion.error"), four);
                }
                String three = (String) obj[col++];
                if (!"SELECT".equals(ratingType) && checkComparison(three, isOrg, mapConversionType)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicatorConversion.error"), three);
                }
                String two = (String) obj[col++];
                if (!"SELECT".equals(ratingType) && checkComparison(two, isOrg, mapConversionType)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicatorConversion.error"), two);
                }
                String one = (String) obj[col++];
                if (!"SELECT".equals(ratingType) && checkComparison(one, isOrg, mapConversionType)) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.indicatorConversion.error"), one);
                }
                conversionList.add(five != null ? five.trim() : "");
                conversionList.add(four != null ? four.trim() : "");
                conversionList.add(three != null ? three.trim() : "");
                conversionList.add(two != null ? two.trim() : "");
                conversionList.add(one != null ? one.trim() : "");
                col++;
                String isRequired = obj[col] != null ? requiredList.get(((String) obj[col]).trim()) : "N";
                if (isRequired == null) {
                    importExcel.addError(index, col, I18n.getMessage("error.import.category.invalid"), (String) obj[col]);
                }
                col++;
                String note = (String) obj[col];
                IndicatorConversionsEntity entity = new IndicatorConversionsEntity();
                entity.setIndicatorId(indicatorId);
                entity.setNote(note);
                entity.setIsRequired(isRequired);
                entity.setConversionType(conversionType);
                entity.setIndicatorMasterId(indicatorMasterId);
                entity.setStatus(IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET);
                entity.setConversionType(IndicatorConversionsEntity.CONVERSION_TYPE.CA_NHAN);
                entity.setIsFocusReduction(IndicatorConversionsEntity.FOCUS_REDUCTION.N);
                indicatorConversionsEntityList.add(entity);
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                indicatorConversionsRepository.insertBatch(IndicatorConversionsEntity.class, indicatorConversionsEntityList, userName);
                int i = 0;
                for (IndicatorConversionsEntity entity : indicatorConversionsEntityList) {
                    boolean isOrg = IndicatorConversionsEntity.CONVERSION_TYPE.DON_VI.equals(entity.getConversionType());
                    saveDetail(conversionList.get(i++), "5", entity.getIndicatorConversionId(), isOrg, mapConversionType);
                    saveDetail(conversionList.get(i++), "4", entity.getIndicatorConversionId(), isOrg, mapConversionType);
                    saveDetail(conversionList.get(i++), "3", entity.getIndicatorConversionId(), isOrg, mapConversionType);
                    saveDetail(conversionList.get(i++), "2", entity.getIndicatorConversionId(), isOrg, mapConversionType);
                    saveDetail(conversionList.get(i++), "1", entity.getIndicatorConversionId(), isOrg, mapConversionType);
                }
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
        return true;
    }

    @Override
    public Integer getPoint(Long indicatorConversionId, String value) {
        //Lay thong tin cau hinh
//        List<>
        return null;
    }

    @Override
    public ResponseEntity<Object> exportData(IndicatorConversionsRequest.SearchForm dto) throws Exception {
        return null;
    }

    @Override
    public ListResponseEntity<OrgDto> getOrgList(Long employeeId) {
        return ResponseUtils.ok(indicatorConversionsRepository.getOrgList(employeeId));
    }

    private String getExpression(IndicatorConversionDetailEntity conversionDetailEntity, Map<String, String> targetMap) {
        StringBuilder result = new StringBuilder("");
        if (!Utils.isNullOrEmpty(conversionDetailEntity.getMinComparison())) {
            result.append(getComparison(conversionDetailEntity.getMinComparison()));
            result.append(" ");
            result.append(!targetMap.isEmpty() && !Utils.isNullOrEmpty(targetMap.get(conversionDetailEntity.getMinValue().toLowerCase().trim()))
                    ? targetMap.get(conversionDetailEntity.getMinValue().toLowerCase().trim()) : conversionDetailEntity.getMinValue());
        }
        if (!Utils.isNullOrEmpty(conversionDetailEntity.getMaxComparison())) {
            if (!result.isEmpty()) {
                result.append(" và ");
            }
            result.append(getComparison(conversionDetailEntity.getMaxComparison()));
            result.append(" ");
            result.append(!targetMap.isEmpty() && !Utils.isNullOrEmpty(targetMap.get(conversionDetailEntity.getMaxValue().toLowerCase().trim()))
                    ? targetMap.get(conversionDetailEntity.getMaxValue().toLowerCase().trim()) : conversionDetailEntity.getMaxValue());
        }
        return result.toString();
    }

    private String getComparison(String value) {
        switch (value) {
            case "EQUAL":
                return "";
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

    private String getComparison2(String value) {
        switch (value) {
            case "=":
                return "EQUAL";
            case ">":
                return "GREATER_THAN";
            case ">=":
                return "GREATER_THAN_EQUAL";
            case "<":
                return "LESS_THAN";
            case "<=":
                return "LESS_THAN_EQUAL";
        }
        return null;
    }


    private boolean checkComparison(String comparison, boolean isOrg, Map<String, String> mapData) {
        String[] comparisonList;
        if (!Utils.isNullOrEmpty(comparison)) {
            comparisonList = comparison.split(" - ");
            Pattern pattern = Pattern.compile("(>=|<=|>|<|=|>= |<= |> |< |= )(.+)");
            for (String comparisonItem : comparisonList) {
                Matcher matcher = pattern.matcher(comparisonItem.trim());
                if (matcher.find()) {
                    try {
                        if (isOrg) {
                            if (mapData.get(matcher.group(2).trim().toLowerCase()) == null) {
                                return true;
                            }
                        } else {
                            Double.parseDouble(matcher.group(2));
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                } else {
                    try {
                        if (isOrg) {
                            if (mapData.get(comparisonItem.trim().toLowerCase()) == null) {
                                return true;
                            }
                        } else {
                            Double.parseDouble(comparisonItem.trim());
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void saveDetail(String comparison, String resultId, Long indicatorConversionId, boolean isOrg, Map<String, String> mapConversionType) {
        IndicatorConversionDetailEntity conversionDetail = new IndicatorConversionDetailEntity();
        String[] comparisonList = new String[]{};
        if (!Utils.isNullOrEmpty(comparison)) {
            comparisonList = comparison.split(" - ");
//            Pattern pattern = Pattern.compile("(>=|<=|>|<|=|>= |<= |> |< |= )(\\d+(\\.\\d+)?)");
            Pattern pattern = Pattern.compile("(>=|<=|>|<|=|>= |<= |> |< |= )(.+)");
            for (String comparisonItem : comparisonList) {
                Matcher matcher = pattern.matcher(comparisonItem.trim());
                if (matcher.find()) {
                    if (matcher.group(1).trim().equals(">") || matcher.group(1).trim().equals(">=")) {
                        conversionDetail.setMinComparison(getComparison2(matcher.group(1).trim()));
                        if (isOrg) {
                            conversionDetail.setMinValue(mapConversionType.get(matcher.group(2).trim().toLowerCase()).trim());
                        } else {
                            conversionDetail.setMinValue(matcher.group(2));
                        }
                    } else {
                        conversionDetail.setMaxComparison(getComparison2(matcher.group(1).trim()));
                        if (isOrg) {
                            conversionDetail.setMaxValue(mapConversionType.get(matcher.group(2).trim().toLowerCase()).trim());
                        } else {
                            conversionDetail.setMaxValue(matcher.group(2));
                        }
                    }
                } else {
                    if (!Utils.isNullOrEmpty(comparisonItem.trim())) {
                        conversionDetail.setMaxComparison("EQUAL");
                        if (isOrg) {
                            conversionDetail.setMinValue(mapConversionType.get(comparisonItem.trim().toLowerCase()).trim());
                        } else {
                            conversionDetail.setMinValue(comparisonItem.trim());
                        }
                    }
                }
            }
            conversionDetail.setResultId(resultId);
            conversionDetail.setIndicatorConversionId(indicatorConversionId);
            conversionDetail.setCreatedBy(Utils.getUserNameLogin());
            conversionDetail.setCreatedTime(new Date());
            indicatorConversionDetailsRepositoryJPA.save(conversionDetail);
        }
    }


    private String getValueTarget(String comparison) {
        if (!Utils.isNullOrEmpty(comparison)) {
//            Pattern pattern = Pattern.compile("(>=|<=|>|<|=|>= |<= |> |< |= )(\\d+(\\.\\d+)?)");
            Pattern pattern = Pattern.compile("(>=|<=|>|<|=|>= |<= |> |< |= )(.+)");

            Matcher matcher = pattern.matcher(comparison.trim());
            if (matcher.find()) {
                return matcher.group(2).trim();
            } else {
                return comparison.trim();
            }
        }
        return null;
    }
}
