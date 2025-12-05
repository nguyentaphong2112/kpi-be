/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.EmployeeIndicatorsRequest;
import vn.hbtplus.models.request.OrganizationIndicatorsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeeIndicatorsResponse;
import vn.hbtplus.models.response.IndicatorConversionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.EmployeeIndicatorsEntity;
import vn.hbtplus.repositories.entity.IndicatorConversionDetailEntity;
import vn.hbtplus.repositories.impl.EmployeeIndicatorsRepository;
import vn.hbtplus.repositories.impl.IndicatorConversionsRepository;
import vn.hbtplus.repositories.impl.IndicatorsRepository;
import vn.hbtplus.repositories.jpa.EmployeeIndicatorsRepositoryJPA;
import vn.hbtplus.services.EmployeeIndicatorsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang kpi_employee_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EmployeeIndicatorsServiceImpl implements EmployeeIndicatorsService {

    private final EmployeeIndicatorsRepository employeeIndicatorsRepository;
    private final EmployeeIndicatorsRepositoryJPA employeeIndicatorsRepositoryJPA;
    private final IndicatorConversionsRepository indicatorConversionsRepository;
    private final IndicatorsRepository indicatorsRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmployeeIndicatorsResponse.SearchResult> searchData(EmployeeIndicatorsRequest.SearchForm dto) {
        return ResponseUtils.ok(employeeIndicatorsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EmployeeIndicatorsRequest.SubmitForm dto, Long id, String adjustReason) throws BaseAppException {
        EmployeeIndicatorsEntity entity;
        if (id != null && id > 0L) {
            entity = employeeIndicatorsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeeIndicatorsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        if (entity.getOldPercent() == null && adjustReason != null) {
            dto.setOldPercent(entity.getPercent());
        }
        Utils.copyProperties(dto, entity);
        entity.setStatus(BaseConstants.STATUS.ACTIVE);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        employeeIndicatorsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getEmployeeIndicatorId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmployeeIndicatorsEntity> optional = employeeIndicatorsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeIndicatorsEntity.class);
        }
        employeeIndicatorsRepository.deActiveObject(EmployeeIndicatorsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EmployeeIndicatorsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException {
        Optional<EmployeeIndicatorsEntity> optional = employeeIndicatorsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeIndicatorsEntity.class);
        }
        EmployeeIndicatorsResponse.SearchResult dto = new EmployeeIndicatorsResponse.SearchResult();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeeIndicatorsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = employeeIndicatorsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ResponseEntity deleteListData(List<Long> employeeIndicatorId, Long employeeEvaluationId, String adjustReason) throws RecordNotExistsException {
        List<Long> listEmployeeIndicatorId = employeeIndicatorsRepository.getListId(employeeEvaluationId);
        List<Long> filteredList = listEmployeeIndicatorId.stream()
                .filter(id -> !employeeIndicatorId.contains(id))
                .toList();
        if (!filteredList.isEmpty() && adjustReason != null) {
            employeeIndicatorsRepository.deActiveByListId(filteredList);
        }
        if (!filteredList.isEmpty() && adjustReason == null) {
            employeeIndicatorsRepository.deActiveObjectByListId(EmployeeIndicatorsEntity.class, filteredList);
        }
        return ResponseUtils.ok(listEmployeeIndicatorId);
    }

    @Override
    public List<EmployeeIndicatorsResponse.EmployeeEvaluation> getDataByEvaluationId(Long id, boolean isGetAll) throws RecordNotExistsException {
        List<EmployeeIndicatorsResponse.EmployeeEvaluation> dataList = employeeIndicatorsRepository.getDataByEvaluationId(id, isGetAll);
//        Map<String, String> listCategory = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.DKY_MUC_TIEU_DON_VI).stream()
//                .collect(Collectors.toMap(CategoryDto::getValue, CategoryDto::getName));
        dataList.forEach(item -> {
            List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionDetailEntity.class, "indicatorConversionId", item.getIndicatorConversionId());
            Map<String, String> targetMap = new HashMap<>();
            if (item.getTargetStr() != null) {
                String target1 = null;
                String target2 = null;
                String target3 = null;
                OrganizationIndicatorsRequest.Target target = Utils.fromJson(item.getTargetStr(), OrganizationIndicatorsRequest.Target.class);
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
        });
        return dataList;
    }


    private String getExpression(IndicatorConversionDetailEntity conversionDetailEntity, Map<String, String> targetMap) {
        StringBuilder result = new StringBuilder();

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
