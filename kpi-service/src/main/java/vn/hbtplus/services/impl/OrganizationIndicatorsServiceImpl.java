/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.OrganizationEvaluationsRequest;
import vn.hbtplus.models.request.OrganizationIndicatorsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.IndicatorConversionDetailEntity;
import vn.hbtplus.repositories.entity.OrganizationEntity;
import vn.hbtplus.repositories.entity.OrganizationIndicatorsEntity;
import vn.hbtplus.repositories.impl.IndicatorConversionsRepository;
import vn.hbtplus.repositories.impl.IndicatorsRepository;
import vn.hbtplus.repositories.impl.OrganizationEvaluationsRepository;
import vn.hbtplus.repositories.impl.OrganizationIndicatorsRepository;
import vn.hbtplus.repositories.jpa.OrganizationIndicatorsRepositoryJPA;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.services.OrganizationIndicatorsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang kpi_organization_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class OrganizationIndicatorsServiceImpl implements OrganizationIndicatorsService {

    private final OrganizationIndicatorsRepository organizationIndicatorsRepository;
    private final OrganizationIndicatorsRepositoryJPA organizationIndicatorsRepositoryJPA;
    private final IndicatorConversionsRepository indicatorConversionsRepository;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OrganizationIndicatorsResponse.SearchResult> searchData(OrganizationIndicatorsRequest.SearchForm dto) {
        return ResponseUtils.ok(organizationIndicatorsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OrganizationIndicatorsRequest.SubmitForm dto, Long id, String adjustReason) throws BaseAppException {
        OrganizationIndicatorsEntity entity;
        if (id != null && id > 0L) {
            entity = organizationIndicatorsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrganizationIndicatorsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        if (entity.getOldPercent() == null && adjustReason != null) {
            dto.setOldPercent(entity.getPercent());
        }
        Utils.copyProperties(dto, entity);
        entity.setTarget(Utils.toJson(dto.getTarget()));
        entity.setStatus(BaseConstants.STATUS.ACTIVE);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        organizationIndicatorsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getOrganizationIndicatorId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OrganizationIndicatorsEntity> optional = organizationIndicatorsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationIndicatorsEntity.class);
        }
        organizationIndicatorsRepository.deActiveObject(OrganizationIndicatorsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OrganizationIndicatorsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException {
        Optional<OrganizationIndicatorsEntity> optional = organizationIndicatorsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationIndicatorsEntity.class);
        }
        OrganizationIndicatorsResponse.SearchResult dto = new OrganizationIndicatorsResponse.SearchResult();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(OrganizationIndicatorsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = organizationIndicatorsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ResponseEntity deleteListData(List<Long> organizationIndicatorId, Long organizationEvaluationId, String adjustReason) throws RecordNotExistsException {
        List<Long> listOrganizationIndicatorId = organizationIndicatorsRepository.getListId(organizationEvaluationId);
        List<Long> filteredList = listOrganizationIndicatorId.stream()
                .filter(id -> !organizationIndicatorId.contains(id))
                .collect(Collectors.toList());
        if (!filteredList.isEmpty() && adjustReason != null) {
            organizationIndicatorsRepository.deActiveByListId(filteredList);
        }
        if (!filteredList.isEmpty() && adjustReason == null) {
            organizationIndicatorsRepository.deActiveObjectByListId(OrganizationIndicatorsEntity.class, filteredList);
        }
        return ResponseUtils.ok(listOrganizationIndicatorId);
    }

    @Override
    public List<OrganizationIndicatorsResponse.OrganizationEvaluation> getDataByEvaluationId(OrganizationEvaluationsRequest.SearchForm dto) throws RecordNotExistsException {
        List<OrganizationIndicatorsResponse.OrganizationEvaluation> dataList = organizationIndicatorsRepository.getDataByEvaluationId(dto);
        if (dto.getOrganizationId() != null && 1L == dto.getOrganizationId()) {
            List<Long> orgPermissionIds = authorizationService.getOrgHasPermission(Scope.APPROVE, Constant.RESOURCES.ORGANIZATION_EVALUATE, Utils.getUserNameLogin());
            List<OrganizationEntity> listOrgEntity = organizationIndicatorsRepository.findByListId(OrganizationEntity.class, orgPermissionIds);
            List<Long> orgTypeIdList = listOrgEntity.stream().map(OrganizationEntity::getOrgTypeId).toList();
            List<EmployeesEntity> listEmp = organizationIndicatorsRepository.findByProperties(EmployeesEntity.class, "employeeCode", Utils.getUserEmpCode());
            Long empId = !Utils.isNullOrEmpty(listEmp) ? listEmp.get(0).getEmployeeId() : null;

            if (Utils.isNullOrEmpty(orgPermissionIds)) {
                dataList = new ArrayList<>();
            } else if (!orgPermissionIds.contains(1L)) {
                Set<Long> leaderIdSetAll = new HashSet<>();
                for (OrganizationIndicatorsResponse.OrganizationEvaluation itemCheck : dataList) {
                    String leaderIdsStr = itemCheck.getLeaderIds();
                    if (leaderIdsStr == null || leaderIdsStr.isEmpty()) continue;

                    String[] leaderIdArr = leaderIdsStr.split(";");
                    for (String idStr : leaderIdArr) {
                        idStr = idStr.trim();
                        if (!idStr.isEmpty()) {
                            leaderIdSetAll.add(Long.valueOf(idStr));
                        }
                    }
                }

                Map<Long, Set<Long>> mapOrgValid = organizationIndicatorsRepository.getOrgValid(leaderIdSetAll, orgPermissionIds);

                List<OrganizationIndicatorsResponse.OrganizationEvaluation> filteredList = new ArrayList<>();
                for (OrganizationIndicatorsResponse.OrganizationEvaluation item : dataList) {
                    String leaderIdsStr = item.getLeaderIds();
                    if (Utils.isNullOrEmpty(leaderIdsStr)) continue;

                    Set<Long> leaderIdSet = new HashSet<>();
                    String[] leaderIdArr = leaderIdsStr.split(";");
                    for (String idStr : leaderIdArr) {
                        idStr = idStr.trim();
                        if (!idStr.isEmpty()) {
                            leaderIdSet.add(Long.valueOf(idStr));
                        }
                    }
                    if ("1".equals(item.getLeaderType())) {
                        for (Long leadId : leaderIdSet) {
                            Set<Long> idsValid = mapOrgValid.get(leadId);
                            if (Utils.isNullOrEmpty(idsValid)) continue;
                            for (Long idValid : idsValid) {
                                if (orgPermissionIds.contains(idValid)) {
                                    filteredList.add(item);
                                    break;
                                }
                            }
                        }
                    } else if ("2".equals(item.getLeaderType())) {
                        for (Long leadId : leaderIdSet) {
                            if (orgTypeIdList.contains(leadId)) {
                                filteredList.add(item);
                                break;
                            }
                        }
                    } else if ("3".equals(item.getLeaderType()) && empId != null) {
                        for (Long leadId : leaderIdSet) {
                            if (Objects.equals(empId, leadId)) {
                                filteredList.add(item);
                                break;
                            }
                        }
                    }
                    dataList = filteredList;
                }
            }
        }


        dataList.forEach(item -> {
            List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionDetailEntity.class, "indicatorConversionId", item.getIndicatorConversionId());
            conversionDetailEntities.forEach(d -> {
                IndicatorConversionsResponse.ConversionDetail tem = new IndicatorConversionsResponse.ConversionDetail();
                tem.setResultId(d.getResultId());
                tem.setExpression(getExpression(d));
                item.getConversions().add(tem);
            });
        });
        return dataList;
    }

    @Override
    public TableResponseEntity<OrganizationEvaluationsResponse.OrgParent> getDataTableByEvaluationId
            (OrganizationEvaluationsRequest.OrgParent data) throws RecordNotExistsException {
        return ResponseUtils.ok(organizationIndicatorsRepository.getDataTableByEvaluationId(data));
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
