/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.request.OrganizationEvaluationsRequest;
import vn.hbtplus.models.request.OrganizationIndicatorsRequest;
import vn.hbtplus.models.request.OrganizationWorkPlanningsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.*;
import vn.hbtplus.repositories.jpa.*;
import vn.hbtplus.services.*;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang kpi_organization_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class OrganizationEvaluationsServiceImpl implements OrganizationEvaluationsService {

    private final OrganizationEvaluationsRepository organizationEvaluationsRepository;
    private final OrganizationEvaluationsRepositoryJPA organizationEvaluationsRepositoryJPA;
    private final OrganizationIndicatorsService organizationIndicatorsService;
    private final OrganizationWorkPlanningsService organizationWorkPlanningsService;
    private final EmployeeEvaluationsRepository employeeEvaluationsRepository;
    private final EmployeeIndicatorsRepositoryJPA employeeIndicatorsRepositoryJPA;
    private final PermissionFeignClient permissionFeignClient;
    private final ApprovalHistoryRepositoryJPA approvalHistoryRepositoryJPA;
    private final HttpServletRequest request;
    private final EmployeeEvaluationsRepositoryJPA employeeEvaluationsRepositoryJPA;
    private final IndicatorMastersRepository indicatorMastersRepository;
    private final OrgConfigsRepository orgConfigsRepository;
    private final AuthorizationService authorizationService;
    private final IndicatorsService indicatorsService;
    private final ObjectAttributesService objectAttributesService;
    private final UtilsService utilsService;
    private final OrganizationIndicatorsRepositoryJPA organizationIndicatorsRepositoryJPA;
    private final OrganizationWorkPlanningsRepositoryJPA organizationWorkPlanningsRepositoryJPA;
    private final EmployeeEvaluationsServiceImpl employeeEvaluationsServiceImpl;
    private final IndicatorsRepository indicatorsRepository;
    private final OrganizationIndicatorsRepository organizationIndicatorsRepository;
    private final ParameterRepositoryImpl parameterRepositoryImpl;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OrganizationEvaluationsResponse.SearchResult> searchData(OrganizationEvaluationsRequest.SearchForm dto) {
        if (!Utils.isNullOrEmpty(dto.getGroupCodes())) {
            List<CategoryDto> listCategory = organizationEvaluationsRepository.getCategoryByListCode(dto.getGroupCodes());

            List<String> orgIds = listCategory.stream()
                    .filter(c -> !Utils.isNullOrEmpty(c.getAttributeValue()))
                    .flatMap(c -> Arrays.stream(c.getAttributeValue().split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()).distinct().toList();
            dto.setOrgIdList(orgIds);
        }
        return ResponseUtils.ok(organizationEvaluationsRepository.searchData(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity saveData(OrganizationEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        OrganizationEvaluationsEntity entity = organizationEvaluationsRepositoryJPA.getById(dto.getOrganizationEvaluationId());
//        String kpiConfig = organizationEvaluationsRepository.getKPIConfig(entity.getOrganizationId(), entity.getEvaluationPeriodId());
//        Integer minKPI = (Objects.isNull(kpiConfig) ||"null".equals(kpiConfig.split("-")[0])) ? null : Integer.parseInt(kpiConfig.split("-")[0]);
//        Integer maxKPI = (Objects.isNull(kpiConfig) || "null".equals(kpiConfig.split("-")[1])) ? null : Integer.parseInt(kpiConfig.split("-")[1]);

        int orgKPISize = dto.getOrganizationIndicatorList().size();
//        if (minKPI != null && maxKPI != null
//            && maxKPI.equals(minKPI) && !minKPI.equals(orgKPISize)) {
//            throw new BaseAppException(String.format("Đơn vị bắt buộc đăng ký %d chỉ số!", minKPI));
//        } else if (minKPI != null && minKPI > orgKPISize) {
//            throw new BaseAppException(String.format("Đơn vị bắt buộc đăng ký tối thiểu %d chỉ số!", minKPI));
//        } else if (maxKPI != null && maxKPI < orgKPISize) {
//            throw new BaseAppException(String.format("Đơn vị chỉ được đăng ký tối đa %d chỉ số!", maxKPI));
//        }
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<Long> listId = dto.getOrganizationIndicatorList().stream()
                .map(OrganizationIndicatorsRequest.SubmitForm::getOrganizationIndicatorId)
                .collect(Collectors.toList());
        entity.setAdjustReason(dto.getAdjustReason());
        entity.setEmpManagerId(dto.getEmpManagerId());
        entity.setSelfTotalPoint(dto.getSelfTotalPoint());
        entity.setManagerTotalPoint(dto.getManagerTotalPoint());
        String resultId = null;
        if (dto.getManagerTotalPoint() != null) {
            if (dto.getManagerTotalPoint() >= 4.2) {
                resultId = "A1";
            } else if (dto.getManagerTotalPoint() >= 3.4) {
                resultId = "A2";
            } else if (dto.getManagerTotalPoint() >= 2.6) {
                resultId = "A3";
            } else {
                resultId = "B";
            }
        }
        entity.setResultId(resultId);
        entity.setModifiedTime(currentDate);
        entity.setModifiedBy(userName);
        if (Constant.STATUS.KHOI_TAO.equalsIgnoreCase(entity.getStatus()) || Constant.STATUS.YC_NHAP_LAI.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        } else if (Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) && !Utils.isNullOrEmpty(dto.getAdjustReason())) {
            entity.setStatus(Constant.STATUS.CHO_PHE_DUYET);
        } else if ((Constant.STATUS.CHO_QLTT_DANH_GIA_LAI.equalsIgnoreCase(entity.getStatus()) || Constant.STATUS.CHO_QLTT_DANH_GIA.equalsIgnoreCase(entity.getStatus())) && "Y".equalsIgnoreCase(dto.getIsEvaluateManage())) {
            entity.setStatus(Constant.STATUS.QLTT_DANH_GIA);
        } else if ((Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || Constant.STATUS.YC_DANH_GIA_LAI.equalsIgnoreCase(entity.getStatus())) && "Y".equals(dto.getIsEvaluate())) {
            entity.setStatus(Constant.STATUS.DANH_GIA);
        } else if (OrganizationEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || OrganizationEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        }
        organizationEvaluationsRepositoryJPA.save(entity);
        organizationIndicatorsService.deleteListData(listId, dto.getOrganizationEvaluationId(), dto.getAdjustReason());
        //Lay ra config trong so toi thieu
        List<OrganizationIndicatorsEntity> listIndicatorAdd = new ArrayList<>();
        List<OrganizationIndicatorsEntity> listIndicatorUpdate = new ArrayList<>();
//        String configKpiPercent = indicatorsService.getMappingValue(String.valueOf(orgKPISize), "KPI_DON_VI_TRONG_SO_TOI_THIEU", new Date());
//        Integer kpiMinPercent = Utils.isNullOrEmpty(configKpiPercent) ? null : Integer.parseInt(configKpiPercent);
//        Integer khdvMinPercent = (Objects.isNull(kpiConfig) || "null".equals(kpiConfig.split("-")[2])) ? null : Integer.parseInt(kpiConfig.split("-")[2]);
//        String idKPIDonvi = parameterRepositoryImpl.getConfigValue("ID_KPI_KHCT_DON_VI", new Date(), String.class);
        for (OrganizationIndicatorsRequest.SubmitForm it : dto.getOrganizationIndicatorList()) {
//            if (kpiMinPercent != null && kpiMinPercent > it.getPercent()) {
//                throw new BaseAppException(String.format("Trọng số kpi tối thiểu phải bằng %s", kpiMinPercent));
//            }
//            if (khdvMinPercent != null && khdvMinPercent > it.getPercent()
//                && Utils.stringToListLong(idKPIDonvi, ",").contains(it.getIndicatorId())) {
//                throw new BaseAppException(String.format("Trọng số kpi kế hoạch công tác đơn vị tối thiểu phải bằng %s", khdvMinPercent));
//            }
            OrganizationIndicatorsEntity entityIndicator;
            boolean isUpdate = it.getOrganizationIndicatorId() != null && it.getOrganizationIndicatorId() > 0L;
            if (isUpdate) {
                entityIndicator = organizationIndicatorsRepositoryJPA.getById(it.getOrganizationIndicatorId());
                entityIndicator.setModifiedTime(currentDate);
                entityIndicator.setModifiedBy(userName);
            } else {
                entityIndicator = new OrganizationIndicatorsEntity();
            }
            if (entityIndicator.getOldPercent() == null && dto.getAdjustReason() != null) {
                it.setOldPercent(entityIndicator.getPercent());
            }
            Utils.copyProperties(it, entityIndicator);
//            entityIndicator.setTarget(Utils.toJson(it.getTarget()));
            entityIndicator.setStatus(BaseConstants.STATUS.ACTIVE);
            entityIndicator.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            if (isUpdate) {
                listIndicatorUpdate.add(entityIndicator);
            } else {
                entityIndicator.setStatusLevel1(OrganizationIndicatorsEntity.STATUS.DU_THAO);
                listIndicatorAdd.add(entityIndicator);
            }
        }

        organizationEvaluationsRepository.insertBatch(OrganizationIndicatorsEntity.class, listIndicatorAdd, userName);
        organizationEvaluationsRepository.updateBatch(OrganizationIndicatorsEntity.class, listIndicatorUpdate, true);
//        List<OrganizationWorkPlanningsEntity> listWorkPlanningAdd = new ArrayList<>();
//        List<OrganizationWorkPlanningsEntity> listWorkPlanningUpdate = new ArrayList<>();
//        for (OrganizationWorkPlanningsRequest.SubmitForm workPlanning : dto.getWorkPlanningList()) {
//            OrganizationWorkPlanningsEntity workPlanningEntity;
//            boolean isUpdate = workPlanning.getOrganizationWorkPlanningId() != null && workPlanning.getOrganizationWorkPlanningId() > 0L;
//            if (isUpdate) {
//                workPlanningEntity = organizationWorkPlanningsRepositoryJPA.getById(workPlanning.getOrganizationWorkPlanningId());
//                workPlanningEntity.setModifiedTime(currentDate);
//                workPlanningEntity.setModifiedBy(userName);
//            } else {
//                workPlanningEntity = new OrganizationWorkPlanningsEntity();
//            }
//            Utils.copyProperties(workPlanning, workPlanningEntity);
//            workPlanningEntity.setContent(workPlanningEntity.getContent());
//            workPlanningEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
//            if (isUpdate) {
//                listWorkPlanningUpdate.add(workPlanningEntity);
//            } else {
//                listWorkPlanningAdd.add(workPlanningEntity);
//            }
//        }
//
//        organizationEvaluationsRepository.insertBatch(OrganizationWorkPlanningsEntity.class, listWorkPlanningAdd, userName);
//        organizationEvaluationsRepository.updateBatch(OrganizationWorkPlanningsEntity.class, listWorkPlanningUpdate, true);
        return ResponseUtils.ok(dto.getOrganizationEvaluationId());
    }

    @Override
    public ResponseEntity saveEmpManager(OrganizationEvaluationsRequest.SubmitForm dto, Long id) throws BaseAppException {
        OrganizationEvaluationsEntity entity;
        if (id != null && id > 0L) {
            entity = organizationEvaluationsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            entity.setEmpManagerId(dto.getEmpManagerId());
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            organizationEvaluationsRepositoryJPA.save(entity);
        }
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OrganizationEvaluationsEntity> optional = organizationEvaluationsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationEvaluationsEntity.class);
        }
        organizationEvaluationsRepository.deActiveObject(OrganizationEvaluationsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(organizationEvaluationsRepository.getDataById(id));
    }

    @Override
    public ResponseEntity<Object> exportData(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_giao_nhan_KPI_don_vi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = organizationEvaluationsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_giao_nhan_KPI_don_vi.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportDataEvaluation(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_danh_gia_KPI_don_vi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = organizationEvaluationsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_danh_gia_KPI_don_vi.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportOrgSummary(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_tong_hop_xep_loai_KPI_don_vi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);
        List<CategoryDto> listCategory = new ArrayList<>();
        if (!Utils.isNullOrEmpty(dto.getGroupCodes())) {
            listCategory = organizationEvaluationsRepository.getCategoryByListCode(dto.getGroupCodes());

            List<String> orgIds = listCategory.stream()
                    .filter(c -> !Utils.isNullOrEmpty(c.getAttributeValue()))
                    .flatMap(c -> Arrays.stream(c.getAttributeValue().split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()).distinct().toList();
            dto.setOrgIdList(orgIds);
        }

        List<Map<String, Object>> listDataExport = organizationEvaluationsRepository.getExportOrgSummary(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        int stt = 1;
        if ("2".equals(dto.getType())) {
            dynamicExport.deleteRange(4, 0, 4, 8);
            for (CategoryDto it : listCategory) {
                dynamicExport.setText(it.getName(), 1);
                dynamicExport.setCellFormat(1, 1, ExportExcel.BOLD_FORMAT);
                dynamicExport.increaseRow();
                for (Map<String, Object> item : listDataExport) {
                    List<String> listOrgId = Arrays.stream(it.getAttributeValue().split(",")).toList();
                    String orgId = Utils.getStringFromMap(item, "organization_id");

                    if (listOrgId.contains(orgId)) {
                        dynamicExport.setText(stt + "", 0);
                        dynamicExport.setText(Utils.getStringFromMap(item, "organizationName"), 1);
                        dynamicExport.setText(Utils.getStringFromMap(item, "self_total_point"), 2);
                        dynamicExport.setText(Utils.getStringFromMap(item, "manager_total_point"), 3);
                        dynamicExport.setText(Utils.getStringFromMap(item, "manager_grade"), 4);
                        dynamicExport.setText(Utils.getStringFromMap(item, "result_id"), 5);
                        dynamicExport.setText(Utils.getStringFromMap(item, "final_point"), 6);
                        dynamicExport.setText(Utils.getStringFromMap(item, "final_result_id"), 7);
                        dynamicExport.setText(Utils.getStringFromMap(item, "statusName"), 8);
                        dynamicExport.increaseRow();
                        stt++;
                    }
                }
            }
            dynamicExport.setCellFormat(4, 0, 2 + stt + listCategory.size(), 8, ExportExcel.BORDER_FORMAT);
        } else if ("3".equals(dto.getType())) {
            Map<String, String> mapOrgType = organizationEvaluationsRepository.getMapData("value", "name", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.HR_LOAI_HINH_DON_VI);
            dynamicExport.deleteRange(4, 0, 4, 8);
            for (String it : dto.getOrgTypeIdList()) {
                dynamicExport.setText(mapOrgType.get(it), 1);
                dynamicExport.setCellFormat(1, 1, ExportExcel.BOLD_FORMAT);
                dynamicExport.increaseRow();
                for (Map<String, Object> item : listDataExport) {
                    String orgTypeId = Utils.getStringFromMap(item, "org_type_id");

                    if (it.equals(orgTypeId)) {
                        dynamicExport.setText(stt + "", 0);
                        dynamicExport.setText(Utils.getStringFromMap(item, "organizationName"), 1);
                        dynamicExport.setText(Utils.getStringFromMap(item, "self_total_point"), 2);
                        dynamicExport.setText(Utils.getStringFromMap(item, "manager_total_point"), 3);
                        dynamicExport.setText(Utils.getStringFromMap(item, "manager_grade"), 4);
                        dynamicExport.setText(Utils.getStringFromMap(item, "result_id"), 5);
                        dynamicExport.setText(Utils.getStringFromMap(item, "final_point"), 6);
                        dynamicExport.setText(Utils.getStringFromMap(item, "final_result_id"), 7);
                        dynamicExport.setText(Utils.getStringFromMap(item, "statusName"), 8);
                        dynamicExport.increaseRow();
                        stt++;
                    }
                }
            }
            dynamicExport.setCellFormat(4, 0, 2 + stt + dto.getOrgTypeIdList().size(), 8, ExportExcel.BORDER_FORMAT);
        } else {
            dynamicExport.replaceKeys(listDataExport);
        }
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tong_hop_xep_loai_KPI_don_vi.xlsx");
    }

    @Override
    public ResponseEntity updateOrgSummary(OrganizationEvaluationsRequest.OrgSummarySubmitForm dto, Long id) throws BaseAppException {
        OrganizationEvaluationsEntity entity;
        if (id != null && id > 0L) {
            entity = organizationEvaluationsRepositoryJPA.getById(id);
            entity.setFinalPoint(dto.getFinalPoint());
            entity.setFinalResultId(dto.getFinalResultId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrganizationEvaluationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        organizationEvaluationsRepositoryJPA.save(entity);

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity managerUpdateOrgSummary(OrganizationEvaluationsRequest.OrgSummarySubmitForm dto, Long id) throws BaseAppException {
        OrganizationEvaluationsEntity entity;
        if (id != null && id > 0L) {
            entity = organizationEvaluationsRepositoryJPA.getById(id);
            entity.setResultId(dto.getResultId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrganizationEvaluationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        organizationEvaluationsRepositoryJPA.save(entity);

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> exportDataById(Long id) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_KPI_don_vi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<String> resultIdList = Arrays.asList("1", "2", "3", "4", "5");
        List<String> resultIdData = new ArrayList<>();

        List<Map<String, Object>> listDataOrgExport = organizationEvaluationsRepository.getExportOrgData(id);
        List<Map<String, Object>> listDataKpiExport = organizationEvaluationsRepository.getExportKpiData(id);
        List<Map<String, Object>> listDataEmpWorkPlanningExport = organizationEvaluationsRepository.getExportEmpWorkPlanningData(id);
        Map<String, String> mapUnit = organizationEvaluationsRepository.getMapData("value", "name", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH);

        Date currentDate = new Date();
        dynamicExport.replaceText("${day}", Utils.formatDate(currentDate, "dd"));
        dynamicExport.replaceText("${month}", Utils.formatDate(currentDate, "MM"));
        dynamicExport.replaceText("${year}", Utils.formatDate(currentDate, "YYYY"));

        String orgLevelManage = Utils.getStringFromMap(listDataOrgExport.get(0), "org_level_manage").trim();
        String orgTypeId = Utils.getStringFromMap(listDataOrgExport.get(0), "org_type_id").trim();
        dynamicExport.replaceText("${job1}", "3".equals(orgLevelManage) ? "TRƯỞNG ĐƠN VỊ" : "HIỆU TRƯỞNG");
        dynamicExport.replaceText("${job2}", (("2".equals(orgLevelManage) && "4".equals(orgTypeId)) || List.of("5", "10").contains(orgTypeId)) ? "CHỦ TỊCH/TỔ TRƯỞNG CÔNG ĐOÀN" : ("3".equals(orgLevelManage) ? "TỔ TRƯỞNG CÔNG ĐOÀN" : "CHỦ TỊCH CÔNG ĐOÀN KHOA"));
        dynamicExport.replaceText("${job3}", List.of("5", "10").contains(orgTypeId) ? "GIÁM ĐỐC TRUNG TÂM" :
                ("3".equals(orgLevelManage) ? "TRƯỞNG ĐƠN VỊ CẤU THÀNH" : ("2".equals(orgLevelManage) && "4".equals(orgTypeId) ? "TRƯỞNG PHÒNG" : "TRƯỞNG KHOA")));

//        dynamicExport.setText("KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + listDataOrgExport.get(0).get("evaluationPeriodYear"), 0, 3);
//        dynamicExport.setText(listDataOrgExport.get(0).get("orgName").toString(), 0, 4);
        String yearStr = objectAttributesService.getAttribute(Long.parseLong(listDataOrgExport.get(0).get("evaluation_period_id").toString()), "NAM_HOC", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
//        String yearStr = (Long.parseLong(listDataOrgExport.get(0).get("evaluationPeriodYear").toString()) - 1) + "-" + listDataOrgExport.get(0).get("evaluationPeriodYear");
        dynamicExport.setText(List.of(Constant.STATUS.QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA).contains(Utils.getStringFromMap(listDataOrgExport.get(0), "status").trim()) ?
                "TỔNG KẾT CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr :
                "KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr, 0, 3);
        dynamicExport.replaceKeys(listDataOrgExport.get(0));

        if ("1".equals(Utils.getStringFromMap(listDataOrgExport.get(0), "organization_id").trim())) {
            List<CategoryDto> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI);
            setAttributes(Constant.CATEGORY_TYPES.PHAN_LOAI, listType, Utils.getStringFromMap(listDataOrgExport.get(0), "evaluation_period_id"));


            Set<String> existingTypes = listDataKpiExport.stream()
                    .map(item -> Utils.getStringFromMap(item, "type"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Map<String, Object>> filteredData = new ArrayList<>();
            Set<String> categoriesToShow = findAllCategoriesToShow(existingTypes, listType);

            buildTreeWithKpiData(listType, categoriesToShow, listDataKpiExport, filteredData);
            listDataKpiExport.clear();
            listDataKpiExport.addAll(filteredData);
        }

        for (int i = 0; i < listDataKpiExport.size(); i++) {
            Map<String, Object> dataKpiExport = listDataKpiExport.get(i);
            if (!"1".equals(Utils.getStringFromMap(listDataOrgExport.get(0), "organization_id").trim())) {
                dataKpiExport.put("stt_kpi", i + 1);
            }
            boolean isSelect = "SELECT".equals(Utils.getStringFromMap(dataKpiExport, "rating_type").trim()) && !"DON_VI".equals(Utils.getStringFromMap(dataKpiExport, "conversion_type").trim());
            String percent = convertLong(dataKpiExport.get("percent").toString());
            if (!Utils.isNullOrEmpty(percent)) {
                percent += "%";
                dataKpiExport.put("percent", percent);
            }

            String target1 = "-";
            String target2 = "-";
            String target3 = "-";
            if (dataKpiExport.get("target") != null) {
                String targetStr = dataKpiExport.get("target").toString();
//                while (targetStr.startsWith("\"") && targetStr.endsWith("\"")) {
//                    targetStr = targetStr.substring(1, targetStr.length() - 1);
//                }
//                targetStr = targetStr.replace("\\\"", "\"");
                OrganizationIndicatorsRequest.Target target = Utils.fromJson(targetStr, OrganizationIndicatorsRequest.Target.class);
                if (target != null) {
                    if (target.getM1() != null) target1 = target.getM1();
                    if (target.getM2() != null) target2 = target.getM2();
                    if (target.getM3() != null) target3 = target.getM3();
                }
            }
            dataKpiExport.put("m1", target1);
            dataKpiExport.put("m2", target2);
            dataKpiExport.put("m3", target3);
            String expressionList = (String) dataKpiExport.get("expressionList");
            if (expressionList != null) {
                String[] expressions = expressionList.split("; ");
                for (int j = 0; j < expressions.length; j++) {
                    String[] conditionAndValue = expressions[j].split("#");

                    if (conditionAndValue.length == 2) {
                        String condition = extractCondition(conditionAndValue[0], isSelect);
                        String value = conditionAndValue[1].trim();
                        resultIdData.add(value);
                        dataKpiExport.put(value, condition);
                    }
                }
                List<String> missingIds = new ArrayList<>();
                for (String resultId : resultIdList) {
                    if (!resultIdData.contains(resultId)) {
                        missingIds.add(resultId);
                    }
                }
                for (String missingId : missingIds) {
                    dataKpiExport.put(missingId, "-");
                }
            } else {
                for (String resultId : resultIdList) {
                    dataKpiExport.put(resultId, "-");
                }
            }
            listDataKpiExport.set(i, dataKpiExport);
            resultIdData = new ArrayList<>();
        }

        if (Utils.isNullOrEmpty(listDataKpiExport)) {
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("stt_kpi", "");
            mapData.put("kpiName", "");
            mapData.put("unitName", "");
            mapData.put("percent", "");
            mapData.put("target1", "");
            mapData.put("target2", "");
            mapData.put("target3", "");
            mapData.put("5", "");
            mapData.put("4", "");
            mapData.put("3", "");
            mapData.put("2", "");
            mapData.put("1", "");
            mapData.put("note", "");
            listDataKpiExport.add(mapData);
        }
        dynamicExport.replaceKeys(listDataKpiExport);
        dynamicExport.setLastRow(18 + listDataKpiExport.size());
        if (Utils.isNullOrEmpty(listDataEmpWorkPlanningExport)) {
            dynamicExport.setText("III. KẾ HOẠCH CÔNG TÁC", 0);
            dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
            dynamicExport.mergeCell(0, 5);
        }
        for (int j = 0; j < listDataEmpWorkPlanningExport.size(); j++) {
            String content = (String) listDataEmpWorkPlanningExport.get(j).get("content");
//            String year = listDataEmpWorkPlanningExport.get(j).get("evaluationPeriodYear").toString();
            List<EmployeeWorkPlanningsResponse.Content> contentList = Utils.fromJsonList(content, EmployeeWorkPlanningsResponse.Content.class);
            dynamicExport.setText("III. KẾ HOẠCH CÔNG TÁC NĂM HỌC " + yearStr, 0);
            dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
            dynamicExport.mergeCell(0, 5);
            dynamicExport.setLastRow(dynamicExport.getLastRow() + 3);
            contentList = contentList.stream()
                    .filter(Objects::nonNull)
                    .filter(data -> data.getKey() != null || data.getParam() != null)
                    .collect(Collectors.toList());
            for (int i = 0; i < contentList.size(); i++) {
                if (contentList.get(i) != null) {
                    EmployeeWorkPlanningsResponse.Content contentDTO = contentList.get(i);
                    boolean checkDescendant = checkDescendant(contentList, contentDTO);
                    int columnIndex = 0;
                    if ("3".equals(contentDTO.getLevel())) {
                        dynamicExport.setEntry("", columnIndex++);
                        dynamicExport.setText(getKeyByIndex(contentDTO.getKey()) + " " + contentDTO.getParam(), columnIndex++);
                    } else if ("4".equals(contentDTO.getLevel())) {
                        dynamicExport.setEntry("", columnIndex++);
                        dynamicExport.setText("+ " + contentDTO.getParam(), columnIndex++);
                    } else if ("5".equals(contentDTO.getLevel())) {
                        dynamicExport.setEntry("", columnIndex++);
                        dynamicExport.setText("- " + contentDTO.getParam(), columnIndex++);
                    } else {
                        dynamicExport.setEntry(contentDTO.getKey(), columnIndex++);
                        dynamicExport.setText(contentDTO.getParam(), columnIndex++);
                    }
                    dynamicExport.setText(mapUnit.get(Utils.isNullOrEmpty(contentDTO.getUnit()) ? "" : contentDTO.getUnit()), columnIndex++);
                    dynamicExport.setText(contentDTO.getStepOne(), columnIndex++);
                    dynamicExport.setText(contentDTO.getStepTwo(), columnIndex++);
                    dynamicExport.setText(contentDTO.getFullYear(), columnIndex++);
                    dynamicExport.setText(contentDTO.getNote(), columnIndex);
                    dynamicExport.mergeCell(columnIndex, columnIndex + 6);
                    if (Utils.isNullOrEmpty(contentDTO.getParentKey())) {
                        dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
                    }
                    if ("2".equals(contentDTO.getLevel()) && checkDescendant) {
                        dynamicExport.setCellFormat(0, 1, ExportExcel.NORMAL_ITALIC);
                        dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
                    }
                    if ("3".equals(contentDTO.getLevel()) && checkDescendant) {
                        dynamicExport.setCellFormat(1, 1, ExportExcel.NORMAL_ITALIC);
                        dynamicExport.setCellFormat(1, 1, ExportExcel.BOLD_FORMAT);
                    }
                    dynamicExport.increaseRow();
                }
            }
            dynamicExport.setCellFormat(dynamicExport.getLastRow() - contentList.size(), 0, dynamicExport.getLastRow() - 1, 12, ExportExcel.BORDER_FORMAT);
        }
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.copyRange(dynamicExport.getLastRow() + 1, 1, dynamicExport.getLastRow() + 2, 6, 6, 12, 7, 17);
        dynamicExport.deleteRange(0, 12, 8, 17);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_KPI_don_vi.xlsx");
    }

    private void buildTreeWithKpiData(List<CategoryDto> allCategories,
                                      Set<String> categoriesToShow,
                                      List<Map<String, Object>> listData,
                                      List<Map<String, Object>> result) {
        Map<String, List<CategoryDto>> filteredChildrenMap = new HashMap<>();

        for (CategoryDto category : allCategories) {
            String parentId = category.getAttributes() != null ?
                    Utils.getStringFromMap(category.getAttributes(), "PARENT_ID") : null;
            if (!Utils.isNullOrEmpty(parentId) && categoriesToShow.contains(category.getValue())) {
                filteredChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
            }
        }

        List<CategoryDto> rootCategories = allCategories.stream()
                .filter(c -> categoriesToShow.contains(c.getValue()))
                .filter(c -> {
                    String parentId = c.getAttributes() != null ?
                            Utils.getStringFromMap(c.getAttributes(), "PARENT_ID") : null;
                    return Utils.isNullOrEmpty(parentId) || !categoriesToShow.contains(parentId);
                })
                .collect(Collectors.toList());

        rootCategories.sort(Comparator.comparing(c -> Long.parseLong(c.getValue())));

        int rootIndex = 1;
        for (CategoryDto rootCategory : rootCategories) {
            addKpiCategoryToResult(rootCategory, filteredChildrenMap, listData, result,
                    1, null, rootIndex++);
        }
    }


    private double addKpiCategoryToResult(CategoryDto category,
                                          Map<String, List<CategoryDto>> childrenMap,
                                          List<Map<String, Object>> listData,
                                          List<Map<String, Object>> result,
                                          int currentLevel,
                                          String parentKey,
                                          int index) {
        String categoryValue = category.getValue();
        String currentKey = generateKeyByLevel(currentLevel, index, parentKey);


        Map<String, Object> header = new HashMap<>();
        header.put("kpiName", category.getName());
        header.put("stt_kpi", currentKey);
        header.put("unitName", "");
        header.put("note", "");
        header.put("self_point", "");
        header.put("manage_point", "");
        header.put("percent", 0.0);
        result.add(header);


        List<Map<String, Object>> listDataChild = listData.stream()
                .filter(item -> categoryValue.equals(Utils.getStringFromMap(item, "type"))).toList();
        List<CategoryDto> children = childrenMap.getOrDefault(categoryValue, Collections.emptyList());
        List<Object> allChildItems = new ArrayList<>(listDataChild);
        allChildItems.addAll(children);

//        boolean hasAnyChildren = !listDataChild.isEmpty() || !children.isEmpty();
//        header.put("isChildren", hasAnyChildren);

        double totalPercent = 0.0;
        int childIndex = 1;

        for (Object item : allChildItems) {
            String childKey = generateKeyByLevel(currentLevel + 1, childIndex++, currentKey);

            if (item instanceof Map) {
                Map<String, Object> dataItem = (Map<String, Object>) item;
                dataItem.put("stt_kpi", childKey);
                result.add(dataItem);
                Object percentObj = dataItem.get("percent");
                double percent = percentObj != null ? Double.parseDouble(percentObj.toString()) : 0.0;
                totalPercent += percent;
            } else if (item instanceof CategoryDto childCategory) {
                double childPercent = addKpiCategoryToResult(
                        childCategory, childrenMap, listData, result,
                        currentLevel + 1, currentKey, childIndex - 1
                );
                totalPercent += childPercent;
            }
        }

        header.put("percent", totalPercent);
        return totalPercent;
    }

    public static String extractCondition(String input, boolean isSelect) {
        if (isSelect) {
            Pattern pattern = Pattern.compile("=\\s*(.+)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1).trim();
            } else {
                return "";
            }
        } else {
            return input.trim();
        }
    }

    @Override
    public ResponseEntity<Object> exportEvaluateById(Long id) throws Exception {
        ExportExcel dynamicExport = handleExportEvaluation(id);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_danh_gia_KPI_don_vi.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportAllEmp(Long id) throws Exception {
        Map<Long, List<Map<String, Object>>> mapIndicatorData = new HashMap<>();
        Map<Long, List<Map<String, Object>>> mapWorkPlanningData = new HashMap<>();
        Map<Long, EmployeeDto> empDataMap;
        Map<String, String> mapUnit;
        Map<Long, EmployeeEvaluationsResponse.EmpBean> empBeanMap = employeeEvaluationsRepository.getMapDataByOrg(id);
        List<Long> empEvaluationIdList = new ArrayList<>(empBeanMap.keySet());
        ExecutorService executor = Executors.newFixedThreadPool(20);

        try {
            CompletableFuture<Map<Long, EmployeeDto>> futureEmpDataMap =
                    CompletableFuture.supplyAsync(() -> employeeEvaluationsRepository.getMapEmpByIds(empEvaluationIdList), executor);

            CompletableFuture<Map<String, String>> futureMapUnit =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getMapData(
                            "value", "name", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH), executor);

            List<CompletableFuture<?>> futures = new ArrayList<>();

            for (Long empEvaluationId : empEvaluationIdList) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    List<Map<String, Object>> kpiData = employeeEvaluationsRepository.getExportKpiData(empEvaluationId);
                    List<Map<String, Object>> workPlanningData = employeeEvaluationsRepository.getExportEmpWorkPlanningData(empEvaluationId);

                    mapIndicatorData.put(empEvaluationId, kpiData);
                    mapWorkPlanningData.put(empEvaluationId, workPlanningData);
                }, executor);

                futures.add(future);
            }
            List<CompletableFuture<?>> allFutures = new ArrayList<>();
            allFutures.add(futureEmpDataMap);
            allFutures.add(futureMapUnit);
            allFutures.addAll(futures);

            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

            empDataMap = futureEmpDataMap.join();
            mapUnit = futureMapUnit.join();
        } finally {
            executor.shutdown();
        }

        ExportExcel dynamicExport = handleExportEvaluation(id);
        int i = 0;
        dynamicExport.insertSheets(1, empBeanMap.size());
        for (Map.Entry<Long, EmployeeEvaluationsResponse.EmpBean> entry : empBeanMap.entrySet()) {
            i++;
            EmployeeEvaluationsResponse.EmpBean empData = entry.getValue();
            Long empEvaluationId = entry.getKey();
            ExportExcel dynamicExportEmp =
                    employeeEvaluationsServiceImpl.handleExportEvaluation(empData, mapIndicatorData.get(empEvaluationId), mapUnit);
            dynamicExport.setActiveSheet(i);
            dynamicExportEmp.setSheetName(sanitizeSheetName(empData.getEmployeeName()));
            dynamicExport.copySheetFromBook(0, i, dynamicExportEmp);
        }
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_danh_gia_KPI_nhan_vien_cua_don_vi.xlsx", false);
    }

    @Override
    public ResponseEntity<Object> exportAggregateData(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_tong_ket_KPI_don_vi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataOrgExport = new ArrayList<>();
        Map<Long, String> xepLoai = OrganizationEvaluationsResponse.getXepLoaiString();
        if (!Utils.isNullOrEmpty(dto.getGroupCode())) {
            List<CategoryDto> listCategory = organizationEvaluationsRepository.getCategoryByListCode(List.of(dto.getGroupCode()));

            List<String> orgIds = listCategory.stream()
                    .filter(c -> !Utils.isNullOrEmpty(c.getAttributeValue()))
                    .flatMap(c -> Arrays.stream(c.getAttributeValue().split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()).distinct().toList();
            dto.setOrgIdList(orgIds);
//            if (!List.of("NGD1", "NGD2", "NBM1", "NBM2").contains(dto.getGroupCode())) {
//                dynamicExport.mergeCell(0, 0, 0, 21);
//                dynamicExport.setCellFormat(0, 0, 0, 21, ExportExcel.CENTER_FORMAT);
//                dynamicExport.deleteRange(2, 22, 4, 39);
//            } else {
//                dynamicExport.mergeCell(0, 0, 0, 39);
//                dynamicExport.setCellFormat(0, 0, 0, 39, ExportExcel.CENTER_FORMAT);
//            }
            Collections.sort(dto.getTableColumns());
            int col = 8;
            int colAdd = 43;
            if (dto.getTableColumns().size() > 1) {
                dynamicExport.copyRange(2, col, 4, col + 1, 2, colAdd, 4, colAdd + 1);
                col += 2;
            }
            for (Long column : dto.getTableColumns()) {
                int columnIndex = column.intValue();
                dynamicExport.copyRange(2, col, 4, col + 1, 2, colAdd + 2 * columnIndex, 4, colAdd + 2 * columnIndex + 1);
                col += 2;
            }
            dynamicExport.mergeCell(0, 0, 0, col - 1);
            dynamicExport.setCellFormat(0, 0, 0, col - 1, ExportExcel.CENTER_FORMAT);
        }
        ExecutorService executor = Executors.newFixedThreadPool(16);

        try {
            Map<Long, CompletableFuture<?>> futureMap = new HashMap<>();
            futureMap.put(0L, CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getInitial(dto), executor));
            for (Long columnId : dto.getTableColumns()) {
                switch (columnId.intValue()) {
                    case 1 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_DAY_MANH,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTyLeVuotDayManh(dto), executor));

                    case 2 -> futureMap.put(Constant.XepLoaiConstant.TONG_TRONG_SO_VUOT_DAY_MANH,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTongTrongSoVuotDayManh(dto), executor));

                    case 3 -> futureMap.put(Constant.XepLoaiConstant.MUC_VUOT_SO_VOI_DAY_MANH,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getMucVuotVuotDayManh(dto), executor));

                    case 4 -> futureMap.put(Constant.XepLoaiConstant.KHCT_DON_VI,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getKHCTDonVi(dto), executor));

                    case 5 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_KHCT,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTyLeVuotMucKHCT(dto), executor));

                    case 6 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_KHONG_HOAN_THANH_KHCT,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTyLeKhongHoanThanhKHCT(dto), executor));

                    case 7 -> futureMap.put(Constant.XepLoaiConstant.TONG_SO_GIO_GIANG,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTongSoGioGiang(dto), executor));

                    case 8 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_CBQT,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getVuotMuc(dto, 445L), executor));

                    case 9 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_CBTC,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getVuotMuc(dto, 446L), executor));

                    case 10 -> futureMap.put(Constant.XepLoaiConstant.PHAT_TRIEN_DAO_TAO,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto,
                                    List.of(2300L, 718L, 563L, 463L, 200L, 168L, 94L)), executor));

                    case 11 -> futureMap.put(Constant.XepLoaiConstant.KIEM_DINH_DAI_HOC,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(2992L)), executor));

                    case 12 -> futureMap.put(Constant.XepLoaiConstant.KIEM_DINH_THAC_SI,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(294L)), executor));

                    case 13 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_TOT_NGHIEP_DUNG_HAN,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(1768L)), executor));

                    case 14 -> futureMap.put(Constant.XepLoaiConstant.SO_GIANG_VIEN_DAT_NN,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(451L)), executor));

                    case 15 -> futureMap.put(Constant.XepLoaiConstant.SO_GIANG_VIEN_BAO_VE_LATS,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getSoGiangVienBaoVeLATS(dto), executor));
                }
            }


            CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0])).join();


            listDataOrgExport = (List<Map<String, Object>>) futureMap.get(0L).get();

            if (Utils.isNullOrEmpty(listDataOrgExport)) {
                throw new BaseAppException(I18n.getMessage("global.notFound"));
            }

            for (Map<String, Object> dataOrgExport : listDataOrgExport) {
                Long organizationEvaluationId = ((Integer) dataOrgExport.get("organization_evaluation_id")).longValue();

                for (Long columnId : dto.getTableColumns()) {
                    if (!futureMap.containsKey(columnId)) continue;

                    @SuppressWarnings("unchecked")
                    Map<Long, Map<String, Object>> dataMap = (Map<Long, Map<String, Object>>) futureMap.get(columnId).get();

                    String fieldName = xepLoai.get(columnId);
                    String value = Utils.getStringFromMap(dataMap.get(organizationEvaluationId), "data");
                    Long rank = (Long) dataMap.get(organizationEvaluationId).get("xep_loai");
                    dataOrgExport.put(fieldName.substring(9), value);
                    dataOrgExport.put(fieldName, rank);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }


        if (dto.getTableColumns().size() > 1) {
            for (Map<String, Object> dataOrgExport : listDataOrgExport) {
                double average = 0d;
                for (Long column : dto.getTableColumns()) {
                    average += (Long) dataOrgExport.get(xepLoai.get(column));
                }
                average = average / dto.getTableColumns().size();
                average = Utils.round(average, 2);
                dataOrgExport.put("average", average);
            }
            listDataOrgExport.sort((m1, m2) -> {
                Double avg1 = (Double) m1.get("average");
                Double avg2 = (Double) m2.get("average");
                return avg1.compareTo(avg2);
            });

            int rank = 1;
            int index = 1;
            Double prevAverage = null;

            for (Map<String, Object> dataOrgExport : listDataOrgExport) {
                Double currentAverage = (Double) dataOrgExport.get("average");

                if (prevAverage != null && !currentAverage.equals(prevAverage)) {
                    rank = index;
                }

                dataOrgExport.put("rank", rank);
                prevAverage = currentAverage;
                index++;
            }
        }
        dynamicExport.replaceKeys(listDataOrgExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tong_ket_KPI_don_vi.xlsx", false);
    }

    @Override
    public ResponseEntity<Object> exportAggregateKHCTSchool() throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_tong_hop_thuc_hien_KHCT.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<OrganizationEvaluationsResponse.Content> phongQLKHDN;
        List<OrganizationEvaluationsResponse.Content> phongQLDT;
        List<OrganizationEvaluationsResponse.Content> vienDTSDH;
        List<OrganizationEvaluationsResponse.Content> vienDTQT;
        List<OrganizationEvaluationsResponse.Content> phongTCNS;
        List<OrganizationEvaluationsResponse.Content> phongKHTC;
        List<OrganizationEvaluationsResponse.Content> phongKTDBCL;
        List<OrganizationEvaluationsResponse.Content> phongTTTS;
        List<OrganizationEvaluationsResponse.Content> phongQTCSVC;
        List<OrganizationIndicatorsEntity> indicatorPhongQLDT;
        List<OrganizationIndicatorsEntity> indicatorPhongKTDBCL;
        List<OrganizationIndicatorsEntity> indicatorTTCNTT;
        List<OrganizationIndicatorsEntity> indicatorPhongQLKHDN;
        List<OrganizationIndicatorsEntity> indicatorVienDTQT;
        List<OrganizationIndicatorsEntity> indicatorPhongCTSV;
        Map<String, String> mapUnit;
        Map<String, Object> listMap = new TreeMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            CompletableFuture<Map<String, String>> futureMapUnit =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getMapData("value", "name", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH), executor);
            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futurePhongQLKHDN =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(512L,
                            List.of("Tạp chí thuộc danh mục Wos/Scopus", "Tỷ lệ bài báo thuộc tạp chí trong danh mục Wos hoặc Scopus/GV",
                                    "Tạp chí trong nước từ 1,0 điểm trở lên", "Tỷ lệ bài báo thuộc tạp chí trong nước từ 1,0 điểm trở lên/GV", "Tạp chí trong nước 0,75 điểm",
                                    "Tỷ lệ bài báo thuộc tạp chí trong nước 0,75 điểm/GV", "Báo cáo đăng toàn văn bằng tiếng Anh trong kỷ Hội thảo khoa học quốc tế có ISBN tổ chức trong nước",
                                    "Đề tài cấp Nhà nước", "Đề tài cấp Bộ và tương đương", "Đề tài cấp Trường", "Dự án RD", "Giáo trình", "Sách chuyên khảo", "Sách tham khảo", "Chương sách được chỉ mục Scopus", "Đề tài NCKH sinh viên",
                                    "Tỷ lệ sinh viên tham gia NCKH/tổng số SV", "Hội thảo khoa học quốc tế", "Hội thảo khoa học quốc gia", "Hội thảo khoa học cấp Trường"), null), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futurePhongQLDT =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(510L,
                            List.of("- Giờ giảng lý thuyết", "- Giờ hướng dẫn thảo luận", "- Giờ khác", "- Giờ thực tập ngoài doanh nghiệp", "- Giờ báo cáo thực tế",
                                    "- Xây dựng học liệu đào tạo trực tuyến", "- Xây dựng học liệu đào tạo từ xa", "- Tỷ lệ SV tốt nghiệp đúng hạn",
                                    "- Tỷ lệ SV tốt nghiệp trong thời gian không chậm quá 2 năm so với kế hoạch học tập chuẩn", "- CTĐT định hướng chuyên sâu nghề nghiệp quốc tế", "- CTĐT cử nhân song bằng quốc tế",
                                    "Đại học đào tạo từ xa"), null), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futureVienDTSDH =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(575L,
                            List.of("- Thạc sĩ", "- Tiến sĩ", "- Giờ giảng lý thuyết", "- Giờ giảng hướng dẫn thảo luận", "- Giờ khác",
                                    "Phối hợp với các khoa/viện rà soát, đánh giá, cập nhật giữa chu kỳ các chương trình đào tạo trình độ thạc sĩ theo định hướng ứng dụng năm 2024; kiểm định các CTĐT trình độ thạc sĩ"), null), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futureVienDTQT =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(570L,
                            List.of("- Giờ giảng lý thuyết", "- Giờ hướng dẫn thảo luận và thực hành", "- Giờ khác"), List.of("1.1")), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futurePhongTCNS =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(509L,
                            List.of("- Tổng số GV", "- Tỷ lệ TS/GV", "- Tỷ lệ GS,PGS/GV", "- Tỷ lệ GV GD chuyên môn bằng ngoại ngữ"), null), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futurePhongKHTC =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(514L,
                            List.of("- Quyết định thu học phí CQ", "- Quyết định thu học phí Sau đại học", "- Quyết định thu học phí ĐTQT", "- Quyết định thu học phí người học là người nước ngoài"), null), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futurePhongKTDBCL =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(520L,
                            List.of("Kiểm định chất lượng chương trình đào tạo theo tiêu chuẩn AUN-QA"), null), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futurePhongTTTS =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(513L,
                            List.of("Tuyển sinh trình độ đại học"), null), executor);

            CompletableFuture<List<OrganizationEvaluationsResponse.Content>> futurePhongQTCSVC =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getDataByOrganizationEvaluationId(516L,
                            List.of("Xây dựng dự án Giảng đường trung tâm đảm bảo theo tiến độ được phê duyệt"), null), executor);


            CompletableFuture<List<OrganizationIndicatorsEntity>> futureIndicatorPhongQLDT =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getIndicatorByOrganizationEvaluationId(510L,
                            List.of(1762L)), executor);


            CompletableFuture<List<OrganizationIndicatorsEntity>> futureIndicatorPhongKTDBCL =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getIndicatorByOrganizationEvaluationId(520L,
                            List.of(294L, 295L, 296L, 297L)), executor);


            CompletableFuture<List<OrganizationIndicatorsEntity>> futureIndicatorTTCNTT =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getIndicatorByOrganizationEvaluationId(581L,
                            List.of(1303L, 2917L, 1310L)), executor);

            CompletableFuture<List<OrganizationIndicatorsEntity>> futureIndicatorPhongQLKHDN =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getIndicatorByOrganizationEvaluationId(512L,
                            List.of(287L)), executor);

            CompletableFuture<List<OrganizationIndicatorsEntity>> futureIndicatorVienDTQT =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getIndicatorByOrganizationEvaluationId(570L,
                            List.of(288L)), executor);
            CompletableFuture<List<OrganizationIndicatorsEntity>> futureIndicatorPhongCTSV =
                    CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getIndicatorByOrganizationEvaluationId(511L,
                            List.of(288L)), executor);

            List<CompletableFuture<?>> allFutures = new ArrayList<>();
            allFutures.add(futurePhongQLKHDN);
            allFutures.add(futurePhongQLDT);
            allFutures.add(futureVienDTSDH);
            allFutures.add(futureVienDTQT);
            allFutures.add(futurePhongTCNS);
            allFutures.add(futurePhongKHTC);
            allFutures.add(futurePhongKTDBCL);
            allFutures.add(futurePhongTTTS);
            allFutures.add(futurePhongQTCSVC);
            allFutures.add(futureIndicatorPhongQLDT);
            allFutures.add(futureIndicatorPhongKTDBCL);
            allFutures.add(futureIndicatorTTCNTT);
            allFutures.add(futureIndicatorPhongQLKHDN);
            allFutures.add(futureIndicatorVienDTQT);
            allFutures.add(futureIndicatorPhongCTSV);
            allFutures.add(futureMapUnit);

            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

            mapUnit = futureMapUnit.join();
            phongQLKHDN = futurePhongQLKHDN.join();
            phongQLDT = futurePhongQLDT.join();
            vienDTSDH = futureVienDTSDH.join();
            vienDTQT = futureVienDTQT.join();
            phongTCNS = futurePhongTCNS.join();
            phongKHTC = futurePhongKHTC.join();
            phongKTDBCL = futurePhongKTDBCL.join();
            phongTTTS = futurePhongTTTS.join();
            phongQTCSVC = futurePhongQTCSVC.join();
            indicatorPhongQLDT = futureIndicatorPhongQLDT.join();
            indicatorPhongKTDBCL = futureIndicatorPhongKTDBCL.join();
            indicatorTTCNTT = futureIndicatorTTCNTT.join();
            indicatorPhongQLKHDN = futureIndicatorPhongQLKHDN.join();
            indicatorVienDTQT = futureIndicatorVienDTQT.join();
            indicatorPhongCTSV = futureIndicatorPhongCTSV.join();
        } finally {
            executor.shutdown();
        }

        getMapData(listMap, phongQLKHDN, mapUnit);
        getMapData(listMap, phongQLDT, mapUnit);
        getMapData(listMap, vienDTSDH, mapUnit);
        getMapData(listMap, vienDTQT, mapUnit);
        getMapData(listMap, phongTCNS, mapUnit);
        getMapData(listMap, phongKHTC, mapUnit);
        getMapData(listMap, phongKTDBCL, mapUnit);
        getMapData(listMap, phongTTTS, mapUnit);
        getMapData(listMap, phongQTCSVC, mapUnit);
        getMapDataIndicator(listMap, indicatorPhongQLDT);
        getMapDataIndicator(listMap, indicatorPhongKTDBCL);
        getMapDataIndicator(listMap, indicatorTTCNTT);
        getMapDataIndicator(listMap, indicatorPhongQLKHDN);
        indicatorVienDTQT.addAll(indicatorPhongCTSV);
        getMapTotalIndicator(listMap, indicatorVienDTQT);
        dynamicExport.replaceKeys(listMap);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tong_ket_KPI_don_vi.xlsx", false);
    }

    @Override
    public ResponseEntity<Object> exportAggregateKHCTSchoolLevel1() throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_tong_hop_muc_tieu_cap_truong.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        Map<String, Object> listMap = new TreeMap<>();
        List<OrganizationIndicatorsEntity> indicatorList = organizationEvaluationsRepository.getIndicatorByOrganizationEvaluationId(255L, null);
        getMapDataIndicator(listMap, indicatorList);
        dynamicExport.replaceKeys(listMap);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_tong_hop_muc_tieu_cap_truong.xlsx", false);
    }

    @Override
    public ResponseEntity<Object> exportAggregateKHCTSchoolInvalid(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_don_vi_khong_thoa_man.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<CategoryDto> listCategory = organizationEvaluationsRepository.getCategoryByListCode(null);

        List<String> orgIds = listCategory.stream()
                .filter(c -> !Utils.isNullOrEmpty(c.getAttributeValue()))
                .flatMap(c -> Arrays.stream(c.getAttributeValue().split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty()).distinct().toList();
        dto.setOrgIds(orgIds);
        List<Map<String, Object>> listMap = organizationEvaluationsRepository.getExportKpiOrgInvalid(dto);
        if (Utils.isNullOrEmpty(listMap)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listMap);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_don_vi_khong_thoa_man.xlsx", false);
    }

    public void getMapData(Map<String, Object> listMap, List<OrganizationEvaluationsResponse.Content> data, Map<String, String> mapUnit) {
        for (OrganizationEvaluationsResponse.Content it : data) {
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getKeyValue() + "unit", mapUnit.get(it.getUnit()));
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getKeyValue() + "stepOne", checkString(it.getStepOne()));
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getKeyValue() + "stepTwo", checkString(it.getStepTwo()));
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getKeyValue() + "fullYear", checkString(it.getFullYear()));
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getKeyValue() + "resultManage", checkString(it.getResultManage()));
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getKeyValue() + "managePoint", checkString(it.getManagePoint()));
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getKeyValue() + "note", checkString(it.getNote()));
        }
    }

    public void getMapDataIndicator(Map<String, Object> listMap, List<OrganizationIndicatorsEntity> data) {
        for (OrganizationIndicatorsEntity it : data) {
            OrganizationIndicatorsRequest.Target target = Utils.fromJson(it.getTarget(), OrganizationIndicatorsRequest.Target.class);
            String m3 = getValueTarget(target.getM3());
            String m2 = getValueTarget(target.getM2());
            String m1 = getValueTarget(target.getM1());
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getIndicatorId() + "unit", it.getUnitName());
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getIndicatorId() + "fullYear", m3);
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getIndicatorId() + "m3", m3);
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getIndicatorId() + "m2", m2);
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getIndicatorId() + "m1", m1);
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getIndicatorId() + "resultManage", Utils.isNullOrEmpty(it.getResultManage()) ? "0" : it.getResultManage());
            listMap.put(it.getOrganizationEvaluationId() + "#" + it.getIndicatorId() + "managePoint", !Utils.isNullOrEmpty(m3) ?
                    (Utils.round((Double.parseDouble(Utils.isNullOrEmpty(it.getResultManage()) ? "0" : it.getResultManage()) / Double.parseDouble(m3)), 2)) : null);
        }
    }

    public void getMapTotalIndicator(Map<String, Object> listMap, List<OrganizationIndicatorsEntity> data) {
        Double fullYear = 0D;
        Double resultManage = 0D;
        Double managePoint = 0D;
        for (OrganizationIndicatorsEntity it : data) {
            OrganizationIndicatorsRequest.Target target = Utils.fromJson(it.getTarget(), OrganizationIndicatorsRequest.Target.class);
            String m3 = getValueTarget(target.getM3());
            fullYear += Double.parseDouble(m3 != null ? m3 : "0");
            resultManage += Double.parseDouble(it.getResultManage() != null ? it.getResultManage() : "0");
            managePoint += !Utils.isNullOrEmpty(m3) ?
                    (Utils.round((Double.parseDouble(Utils.isNullOrEmpty(it.getResultManage()) ? "0" : it.getResultManage()) / Double.parseDouble(m3)), 2)) : 0;
        }
        listMap.put("totalUnit", data.get(0).getUnitName());
        listMap.put("totalFullYear", fullYear);
        listMap.put("totalResultManage", resultManage);
        listMap.put("totalManagePoint", managePoint);
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
        return "0";
    }

    public String checkString(String value) {
        if ("null".equalsIgnoreCase(value)) {
            return "";
        }
        if (value != null && !value.isEmpty()) {
            return value.replaceAll("^\"+|\"+$", "");
        }
        return "";
    }

    @Override
    public TableResponseEntity<Map<String, Object>> searchAggregateData(OrganizationEvaluationsRequest.SearchForm dto) {
        if (!Utils.isNullOrEmpty(dto.getGroupCode())) {
            List<CategoryDto> listCategory = organizationEvaluationsRepository.getCategoryByListCode(List.of(dto.getGroupCode()));

            List<String> orgIds = listCategory.stream()
                    .filter(c -> !Utils.isNullOrEmpty(c.getAttributeValue()))
                    .flatMap(c -> Arrays.stream(c.getAttributeValue().split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()).distinct().toList();
            dto.setOrgIdList(orgIds);
        }
        List<Map<String, Object>> listData = new ArrayList<>();
        Map<Long, String> xepLoai = OrganizationEvaluationsResponse.getXepLoaiString();
        ExecutorService executor = Executors.newFixedThreadPool(16);

        try {
            Map<Long, CompletableFuture<?>> futureMap = new HashMap<>();
            futureMap.put(0L, CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getInitial(dto), executor));
            for (Long columnId : dto.getTableColumns()) {
                switch (columnId.intValue()) {
                    case 1 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_DAY_MANH,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTyLeVuotDayManh(dto), executor));

                    case 2 -> futureMap.put(Constant.XepLoaiConstant.TONG_TRONG_SO_VUOT_DAY_MANH,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTongTrongSoVuotDayManh(dto), executor));

                    case 3 -> futureMap.put(Constant.XepLoaiConstant.MUC_VUOT_SO_VOI_DAY_MANH,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getMucVuotVuotDayManh(dto), executor));

                    case 4 -> futureMap.put(Constant.XepLoaiConstant.KHCT_DON_VI,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getKHCTDonVi(dto), executor));

                    case 5 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_KHCT,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTyLeVuotMucKHCT(dto), executor));

                    case 6 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_KHONG_HOAN_THANH_KHCT,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTyLeKhongHoanThanhKHCT(dto), executor));

                    case 7 -> futureMap.put(Constant.XepLoaiConstant.TONG_SO_GIO_GIANG,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getTongSoGioGiang(dto), executor));

                    case 8 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_CBQT,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getVuotMuc(dto, 445L), executor));

                    case 9 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_CBTC,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getVuotMuc(dto, 446L), executor));

                    case 10 -> futureMap.put(Constant.XepLoaiConstant.PHAT_TRIEN_DAO_TAO,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto,
                                    List.of(2300L, 718L, 563L, 463L, 200L, 168L, 94L)), executor));

                    case 11 -> futureMap.put(Constant.XepLoaiConstant.KIEM_DINH_DAI_HOC,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(2992L)), executor));

                    case 12 -> futureMap.put(Constant.XepLoaiConstant.KIEM_DINH_THAC_SI,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(294L)), executor));

                    case 13 -> futureMap.put(Constant.XepLoaiConstant.TY_LE_TOT_NGHIEP_DUNG_HAN,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(1768L)), executor));

                    case 14 -> futureMap.put(Constant.XepLoaiConstant.SO_GIANG_VIEN_DAT_NN,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getGeneralData(dto, List.of(451L)), executor));

                    case 15 -> futureMap.put(Constant.XepLoaiConstant.SO_GIANG_VIEN_BAO_VE_LATS,
                            CompletableFuture.supplyAsync(() -> organizationEvaluationsRepository.getSoGiangVienBaoVeLATS(dto), executor));
                }
            }


            CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0])).join();


            listData = (List<Map<String, Object>>) futureMap.get(0L).get();


            for (Map<String, Object> dataOrgExport : listData) {
                Long organizationEvaluationId = ((Integer) dataOrgExport.get("organization_evaluation_id")).longValue();

                for (Long columnId : dto.getTableColumns()) {
                    if (!futureMap.containsKey(columnId)) continue;

                    @SuppressWarnings("unchecked")
                    Map<Long, Map<String, Object>> dataMap = (Map<Long, Map<String, Object>>) futureMap.get(columnId).get();

                    String fieldName = xepLoai.get(columnId);
                    String value = Utils.getStringFromMap(dataMap.get(organizationEvaluationId), "data");
                    Long rank = (Long) dataMap.get(organizationEvaluationId).get("xep_loai");
                    dataOrgExport.put(fieldName.substring(9), value);
                    dataOrgExport.put(fieldName, rank);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }


        if (dto.getTableColumns().size() > 1) {
            for (Map<String, Object> data : listData) {
                double average = 0d;
                for (Long column : dto.getTableColumns()) {
                    average += Utils.NVL(Long.parseLong(Utils.getStringFromMap(data, xepLoai.get(column))));
                }
                average = average / dto.getTableColumns().size();

                average = Utils.round(average, 2);
                data.put("average", average);
            }
            listData.sort(Comparator.comparing(
                    m -> ((Double) m.get("average"))
            ));
            int rank = dto.getStartRecord() + 1;
            int index = dto.getStartRecord() + 1;
            Double prevAverage = null;

            for (Map<String, Object> data : listData) {
                Double currentAverage = (Double) data.get("average");

                if (prevAverage != null && !currentAverage.equals(prevAverage)) {
                    rank = index;
                }

                data.put("rank", rank);
                prevAverage = currentAverage;
                index++;
            }
        }

        int fromIndex = Math.min(dto.getStartRecord(), listData.size());
        int toIndex = Math.min(dto.getStartRecord() + dto.getPageSize(), listData.size());
        List<Map<String, Object>> pagedList = listData.subList(fromIndex, toIndex);

        BaseDataTableDto<Map<String, Object>> result = new BaseDataTableDto<>();
        result.setListData(pagedList);
        result.setTotal(Integer.valueOf(listData.size()).longValue());
        result.setPageIndex(dto.getStartRecord() / dto.getPageSize() + 1);
        result.setPageSize(dto.getPageSize());
        return ResponseUtils.ok(result);
    }

    public ExportExcel handleExportEvaluation(Long id) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_danh_gia_KPI_don_vi.xlsx";
        List<Map<String, Object>> listDataOrgExport = organizationEvaluationsRepository.getExportOrgData(id);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<String> resultIdList = Arrays.asList("1", "2", "3", "4", "5");
        List<String> resultIdData = new ArrayList<>();

        List<Map<String, Object>> listDataKpiExport = organizationEvaluationsRepository.getExportKpiData(id);
        List<Map<String, Object>> listDataEmpWorkPlanningExport = organizationEvaluationsRepository.getExportEmpWorkPlanningData(id);
        Map<String, String> mapUnit = organizationEvaluationsRepository.getMapData("value", "name", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH);

        Date currentDate = new Date();
        String yearStr = objectAttributesService.getAttribute(Long.parseLong(listDataOrgExport.get(0).get("evaluation_period_id").toString()), "NAM_HOC", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
//        String yearStr = (Long.parseLong(listDataOrgExport.get(0).get("evaluationPeriodYear").toString()) - 1) + "-" + listDataOrgExport.get(0).get("evaluationPeriodYear");
        dynamicExport.replaceText("${day}", Utils.formatDate(currentDate, "dd"));
        dynamicExport.replaceText("${month}", Utils.formatDate(currentDate, "MM"));
        dynamicExport.replaceText("${year}", Utils.formatDate(currentDate, "YYYY"));
        dynamicExport.replaceText("${self_total_point}", Utils.getStringFromMap(listDataOrgExport.get(0), "self_total_point").trim());
        dynamicExport.replaceText("${manager_total_point}", Utils.getStringFromMap(listDataOrgExport.get(0), "manager_total_point").trim());
        String orgLevelManage = Utils.getStringFromMap(listDataOrgExport.get(0), "org_level_manage").trim();
        String orgTypeId = Utils.getStringFromMap(listDataOrgExport.get(0), "org_type_id").trim();
        dynamicExport.replaceText("${job1}", "3".equals(orgLevelManage) ? "TRƯỞNG ĐƠN VỊ" : "HIỆU TRƯỞNG");
        dynamicExport.replaceText("${job2}", "3".equals(orgLevelManage) ? "TỔ TRƯỞNG CÔNG ĐOÀN" : ("2".equals(orgLevelManage) && "4".equals(orgTypeId) ? "CHỦ TỊCH/TỔ TRƯỞNG CÔNG ĐOÀN" : "CHỦ TỊCH CÔNG ĐOÀN KHOA"));
        dynamicExport.replaceText("${job3}", "3".equals(orgLevelManage) ? "TRƯỞNG ĐƠN VỊ CẤU THÀNH" : ("2".equals(orgLevelManage) && "4".equals(orgTypeId) ? "TRƯỞNG PHÒNG" : "TRƯỞNG KHOA"));
//        dynamicExport.setText("KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + listDataOrgExport.get(0).get("evaluationPeriodYear"), 0, 3);
//        dynamicExport.setText("Đơn vị: " + listDataOrgExport.get(0).get("orgName"), 0, 4);
        dynamicExport.setText(List.of(Constant.STATUS.QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA).contains(Utils.getStringFromMap(listDataOrgExport.get(0), "status").trim()) ?
                "TỔNG KẾT CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr : "KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr, 0, 3);
        dynamicExport.replaceKeys(listDataOrgExport.get(0));
        dynamicExport.setSheetName(sanitizeSheetName(Utils.getStringFromMap(listDataOrgExport.get(0), "orgName").trim()));

        if ("1".equals(Utils.getStringFromMap(listDataOrgExport.get(0), "organization_id").trim())) {
            List<CategoryDto> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI);
            setAttributes(Constant.CATEGORY_TYPES.PHAN_LOAI, listType, Utils.getStringFromMap(listDataOrgExport.get(0), "evaluation_period_id"));


            Set<String> existingTypes = listDataKpiExport.stream()
                    .map(item -> Utils.getStringFromMap(item, "type"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Map<String, Object>> filteredData = new ArrayList<>();
            Set<String> categoriesToShow = findAllCategoriesToShow(existingTypes, listType);

            buildTreeWithKpiData(listType, categoriesToShow, listDataKpiExport, filteredData);
            listDataKpiExport.clear();
            listDataKpiExport.addAll(filteredData);
        }

        for (int i = 0; i < listDataKpiExport.size(); i++) {
            Map<String, Object> dataKpiExport = listDataKpiExport.get(i);
            if (!"1".equals(Utils.getStringFromMap(listDataOrgExport.get(0), "organization_id").trim())) {
                dataKpiExport.put("stt_kpi", i + 1);
            }
            boolean isSelect = "SELECT".equals(Utils.getStringFromMap(dataKpiExport, "rating_type").trim()) && !"DON_VI".equals(Utils.getStringFromMap(dataKpiExport, "conversion_type").trim());
            String percent = convertLong(dataKpiExport.get("percent").toString());
            if (!Utils.isNullOrEmpty(percent)) {
                percent += "%";
                dataKpiExport.put("percent", percent);
            }
            if (dataKpiExport.get("result") != null) {
                dataKpiExport.put("result", convertLong(dataKpiExport.get("result").toString()));
            } else {
                dataKpiExport.put("result", "");
            }

            if (dataKpiExport.get("result_manage") != null) {
                dataKpiExport.put("result_manage", convertLong(dataKpiExport.get("result_manage").toString()));
            } else {
                dataKpiExport.put("result_manage", "");
            }
            String target1 = "-";
            String target2 = "-";
            String target3 = "-";
            if (dataKpiExport.get("target") != null) {
                if (dataKpiExport.get("target") != null) {
                    String targetStr = dataKpiExport.get("target").toString();
//                while (targetStr.startsWith("\"") && targetStr.endsWith("\"")) {
//                    targetStr = targetStr.substring(1, targetStr.length() - 1);
//                }
//                targetStr = targetStr.replace("\\\"", "\"");
                    OrganizationIndicatorsRequest.Target target = Utils.fromJson(targetStr, OrganizationIndicatorsRequest.Target.class);
                    if (target != null) {
                        if (target.getM1() != null) target1 = target.getM1();
                        if (target.getM2() != null) target2 = target.getM2();
                        if (target.getM3() != null) target3 = target.getM3();
                    }
                }
            }
            dataKpiExport.put("m1", target1);
            dataKpiExport.put("m2", target2);
            dataKpiExport.put("m3", target3);
            String expressionList = (String) dataKpiExport.get("expressionList");
            if (expressionList != null) {
                String[] expressions = expressionList.split("; ");
                for (int j = 0; j < expressions.length; j++) {
                    String[] conditionAndValue = expressions[j].split("#");

                    if (conditionAndValue.length == 2) {
                        String condition = extractCondition(conditionAndValue[0], isSelect);
                        String value = conditionAndValue[1].trim();
                        resultIdData.add(value);
                        dataKpiExport.put(value, condition);
                    }
                }
                List<String> missingIds = new ArrayList<>();
                for (String resultId : resultIdList) {
                    if (!resultIdData.contains(resultId)) {
                        missingIds.add(resultId);
                    }
                }
                for (String missingId : missingIds) {
                    dataKpiExport.put(missingId, "-");
                }
            } else {
                for (String resultId : resultIdList) {
                    dataKpiExport.put(resultId, "-");
                }
            }
            listDataKpiExport.set(i, dataKpiExport);
            resultIdData = new ArrayList<>();
        }

        if (Utils.isNullOrEmpty(listDataKpiExport)) {
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("stt_kpi", "");
            mapData.put("kpiName", "");
            mapData.put("unitName", "");
            mapData.put("percent", "");
            mapData.put("target1", "");
            mapData.put("target2", "");
            mapData.put("target3", "");
            mapData.put("result", "");
            mapData.put("result_manage", "");
            mapData.put("self_point", "");
            mapData.put("manage_point", "");
            mapData.put("5", "");
            mapData.put("4", "");
            mapData.put("3", "");
            mapData.put("2", "");
            mapData.put("1", "");
            mapData.put("note", "");
            listDataKpiExport.add(mapData);
        }
        dynamicExport.replaceKeys(listDataKpiExport);
        dynamicExport.setLastRow(19 + listDataKpiExport.size());
        if (Utils.isNullOrEmpty(listDataEmpWorkPlanningExport)) {
            dynamicExport.setText("III. KẾ HOẠCH CÔNG TÁC", 0);
            dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
            dynamicExport.mergeCell(0, 5);
        }
        for (int j = 0; j < listDataEmpWorkPlanningExport.size(); j++) {
            String content = (String) listDataEmpWorkPlanningExport.get(j).get("content");
//            String year = listDataEmpWorkPlanningExport.get(j).get("evaluationPeriodYear").toString();
            List<EmployeeWorkPlanningsResponse.Content> contentList = Utils.fromJsonList(content, EmployeeWorkPlanningsResponse.Content.class);
            dynamicExport.setText("III. KẾ HOẠCH CÔNG TÁC NĂM HỌC " + yearStr, 0);
            dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
            dynamicExport.mergeCell(0, 5);
            dynamicExport.setLastRow(dynamicExport.getLastRow() + 3);
            contentList = contentList.stream()
                    .filter(Objects::nonNull)
                    .filter(data -> data.getKey() != null || data.getParam() != null)
                    .collect(Collectors.toList());
            for (int i = 0; i < contentList.size(); i++) {
                if (contentList.get(i) != null) {
                    EmployeeWorkPlanningsResponse.Content contentDTO = contentList.get(i);
                    boolean checkDescendant = checkDescendant(contentList, contentDTO);
                    int columnIndex = 0;
                    if ("3".equals(contentDTO.getLevel())) {
                        dynamicExport.setEntry("", columnIndex++);
                        dynamicExport.setText(getKeyByIndex(contentDTO.getKey()) + " " + contentDTO.getParam(), columnIndex++);
                    } else if ("4".equals(contentDTO.getLevel())) {
                        dynamicExport.setEntry("", columnIndex++);
                        dynamicExport.setText("+ " + contentDTO.getParam(), columnIndex++);
                    } else if ("5".equals(contentDTO.getLevel())) {
                        dynamicExport.setEntry("", columnIndex++);
                        dynamicExport.setText("- " + contentDTO.getParam(), columnIndex++);
                    } else {
                        dynamicExport.setEntry(contentDTO.getKey(), columnIndex++);
                        dynamicExport.setText(contentDTO.getParam(), columnIndex++);
                    }
                    dynamicExport.setText(mapUnit.get(Utils.isNullOrEmpty(contentDTO.getUnit()) ? "" : contentDTO.getUnit()), columnIndex++);
                    dynamicExport.setText(contentDTO.getStepOne(), columnIndex++);
                    dynamicExport.setText(contentDTO.getStepTwo(), columnIndex++);
                    dynamicExport.setText(contentDTO.getFullYear(), columnIndex++);
                    dynamicExport.setCellFormat(columnIndex, columnIndex, ExportExcel.ALIGN_LEFT);
                    dynamicExport.setText(contentDTO.getResult(), columnIndex++);
                    dynamicExport.setCellFormat(columnIndex, columnIndex, ExportExcel.ALIGN_RIGHT);
                    dynamicExport.setText(contentDTO.getSelfPoint(), columnIndex++);
                    dynamicExport.setCellFormat(columnIndex, columnIndex, ExportExcel.ALIGN_LEFT);
                    dynamicExport.setText(contentDTO.getResultManage(), columnIndex++);
                    dynamicExport.setCellFormat(columnIndex, columnIndex, ExportExcel.ALIGN_RIGHT);
                    dynamicExport.setText(contentDTO.getManagePoint(), columnIndex++);
                    dynamicExport.setCellFormat(columnIndex, columnIndex, ExportExcel.ALIGN_LEFT);
                    dynamicExport.setText(contentDTO.getNote(), columnIndex);
                    dynamicExport.mergeCell(columnIndex, columnIndex + 6);
                    if (Utils.isNullOrEmpty(contentDTO.getParentKey())) {
                        dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
                    }
                    if ("2".equals(contentDTO.getLevel()) && checkDescendant) {
                        dynamicExport.setCellFormat(0, 1, ExportExcel.NORMAL_ITALIC);
                        dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
                    }
                    if ("3".equals(contentDTO.getLevel()) && checkDescendant) {
                        dynamicExport.setCellFormat(1, 1, ExportExcel.NORMAL_ITALIC);
                        dynamicExport.setCellFormat(1, 1, ExportExcel.BOLD_FORMAT);
                    }
                    dynamicExport.increaseRow();
                }
            }
            dynamicExport.setCellFormat(dynamicExport.getLastRow() - contentList.size(), 0, dynamicExport.getLastRow() - 1, 16, ExportExcel.BORDER_FORMAT);
        }
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.copyRange(dynamicExport.getLastRow() + 1, 1, dynamicExport.getLastRow() + 2, 6, 6, 16, 7, 21);
        dynamicExport.deleteRange(0, 16, 8, 21);
        return dynamicExport;
    }

    public static String sanitizeSheetName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Sheet1";
        }

        name = name.replaceAll("[\\\\/:*?\\[\\]]", "");

        if (name.length() > 31) {
            name = name.substring(0, 31);
        }

        if (name.equalsIgnoreCase("History")) {
            name = "Sheet_History";
        }

        return name;
    }

    private boolean checkDescendant(List<EmployeeWorkPlanningsResponse.Content> contentList, EmployeeWorkPlanningsResponse.Content contentDTO) {
        for (int i = 0; i < contentList.size(); i++) {
            if (contentDTO.getKey().equals(contentList.get(i).getParentKey())) {
                return true;
            }
        }
        return false;
    }

    private String getKeyByIndex(String key) {
        String characters = "abcdefghijklmnopqrstuvwxyz";
        String[] arrIndex = key.split("\\.");
        int index = Integer.parseInt(arrIndex[arrIndex.length - 1]);
        return characters.substring(index - 1, index) + ")";
    }

    @Override
    public ResponseEntity getIndicatorById(Long id) throws BaseAppException {
        Optional<OrganizationEvaluationsEntity> optional = organizationEvaluationsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationEvaluationsEntity.class);
        }
        OrganizationEvaluationsRequest.SearchForm dto = new OrganizationEvaluationsRequest.SearchForm();
        dto.setOrganizationEvaluationId(id);
        OrganizationEvaluationsResponse.DetailBean result = new OrganizationEvaluationsResponse.DetailBean();
        List<OrganizationIndicatorsResponse.OrganizationEvaluation> listData = organizationIndicatorsService.getDataByEvaluationId(dto);
        if (optional.get().getOrganizationId() != null && 1L == optional.get().getOrganizationId()) {
            List<CategoryDto> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI);
            setAttributes(Constant.CATEGORY_TYPES.PHAN_LOAI, listType, optional.get().getEvaluationPeriodId().toString());
            Set<String> existingTypes = listData.stream()
                    .map(OrganizationIndicatorsResponse.OrganizationEvaluation::getType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<OrganizationIndicatorsResponse.OrganizationEvaluation> filteredData = new ArrayList<>();
            Set<String> categoriesToShow = findAllCategoriesToShow(existingTypes, listType);

            buildTreeWithData(listType, categoriesToShow, listData, filteredData);
            result.setListData(filteredData);
        } else {
            result.setListData(listData);
        }
        result.setAdjustReason(optional.get().getAdjustReason());
        return ResponseUtils.ok(result);
    }

    @Override
    public ResponseEntity getIndicatorByIdLevel1(OrganizationEvaluationsRequest.SearchForm dto) throws BaseAppException {
        OrganizationEvaluationsResponse.DetailBean result = new OrganizationEvaluationsResponse.DetailBean();
        List<OrganizationIndicatorsResponse.OrganizationEvaluation> listData = organizationIndicatorsService.getDataByEvaluationId(dto);
        List<CategoryDto> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI);
        setAttributes(Constant.CATEGORY_TYPES.PHAN_LOAI, listType, dto.getEvaluationPeriodId().toString());

        Set<String> existingTypes = listData.stream()
                .map(OrganizationIndicatorsResponse.OrganizationEvaluation::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OrganizationIndicatorsResponse.OrganizationEvaluation> filteredData = new ArrayList<>();
        Set<String> categoriesToShow = findAllCategoriesToShow(existingTypes, listType);

        buildTreeWithData(listType, categoriesToShow, listData, filteredData);
        result.setListData(filteredData);
        return ResponseUtils.ok(result);
    }

    private Set<String> findAllCategoriesToShow(Set<String> existingTypes, List<CategoryDto> allCategories) {

        Map<String, CategoryDto> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(CategoryDto::getValue, Function.identity()));

        Set<String> categoriesToShow = new HashSet<>();
        Queue<String> queue = new LinkedList<>(existingTypes);

        while (!queue.isEmpty()) {
            String currentValue = queue.poll();

            if (categoriesToShow.contains(currentValue)) {
                continue;
            }

            categoriesToShow.add(currentValue);

            CategoryDto currentCategory = categoryMap.get(currentValue);
            if (currentCategory != null) {
                String parentId = currentCategory.getAttributes() != null
                        ? Utils.getStringFromMap(currentCategory.getAttributes(), "PARENT_ID")
                        : null;

                if (!Utils.isNullOrEmpty(parentId) && categoryMap.containsKey(parentId)) {
                    queue.add(parentId);
                } else if (Utils.isNullOrEmpty(parentId)) {
                    queue.add(currentCategory.getValue());
                }
            }
        }

        return categoriesToShow;
    }

    private void buildTreeWithData(List<CategoryDto> allCategories,
                                   Set<String> categoriesToShow,
                                   List<OrganizationIndicatorsResponse.OrganizationEvaluation> listData,
                                   List<OrganizationIndicatorsResponse.OrganizationEvaluation> result) {
        Map<String, List<CategoryDto>> filteredChildrenMap = new HashMap<>();

        for (CategoryDto category : allCategories) {
            String parentId = category.getAttributes() != null ?
                    Utils.getStringFromMap(category.getAttributes(), "PARENT_ID") : null;
            if (!Utils.isNullOrEmpty(parentId) && categoriesToShow.contains(category.getValue())) {
                filteredChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
            }
        }

        List<CategoryDto> rootCategories = allCategories.stream()
                .filter(c -> categoriesToShow.contains(c.getValue()))
                .filter(c -> {
                    String parentId = c.getAttributes() != null ?
                            Utils.getStringFromMap(c.getAttributes(), "PARENT_ID") : null;
                    return Utils.isNullOrEmpty(parentId) || !categoriesToShow.contains(parentId);
                })
                .collect(Collectors.toList());

        rootCategories.sort(Comparator.comparing(c -> Long.parseLong(c.getValue())));

        int rootIndex = 1;
        for (CategoryDto rootCategory : rootCategories) {
            addCategoryToResultAndGetPercent(rootCategory, filteredChildrenMap, listData, result,
                    1, null, rootIndex++);
        }
    }

    private double addCategoryToResultAndGetPercent(CategoryDto category,
                                                    Map<String, List<CategoryDto>> childrenMap,
                                                    List<OrganizationIndicatorsResponse.OrganizationEvaluation> listData,
                                                    List<OrganizationIndicatorsResponse.OrganizationEvaluation> result,
                                                    int currentLevel,
                                                    String parentKey,
                                                    int index) {

        String categoryValue = category.getValue();
        String currentKey = generateKeyByLevel(currentLevel, index, parentKey);


        OrganizationIndicatorsResponse.OrganizationEvaluation header = new OrganizationIndicatorsResponse.OrganizationEvaluation();
        header.setIndicatorName(category.getName());
        header.setKey(currentKey);
        header.setPercent(0.0);
        header.setLevel(currentLevel);
        result.add(header);

        List<OrganizationIndicatorsResponse.OrganizationEvaluation> listDataChild = listData.stream()
                .filter(item -> categoryValue.equals(item.getType()))
                .toList();
        List<CategoryDto> children = childrenMap.getOrDefault(categoryValue, Collections.emptyList());
        List<Object> allChildItems = new ArrayList<>(listDataChild);
        allChildItems.addAll(children);

        boolean hasAnyChildren = !listDataChild.isEmpty() || !children.isEmpty();
        header.setIsChildren(hasAnyChildren);

        double totalPercent = 0.0;
        int childIndex = 1;

        for (Object item : allChildItems) {
            String childKey = generateKeyByLevel(currentLevel + 1, childIndex, currentKey);

            if (item instanceof OrganizationIndicatorsResponse.OrganizationEvaluation dataItem) {
                dataItem.setLevel(currentLevel + 1);
                dataItem.setKey(childKey);
                result.add(dataItem);
                totalPercent += Utils.NVL(dataItem.getPercent());
            } else if (item instanceof CategoryDto childCategory) {
                double childPercent = addCategoryToResultAndGetPercent(
                        childCategory, childrenMap, listData, result,
                        currentLevel + 1, currentKey, childIndex
                );
                totalPercent += childPercent;
            }

            childIndex++;
        }


        header.setPercent(totalPercent);

        return totalPercent;
    }

    private String generateKeyByLevel(int level, int index, String parentKey) {
        switch (level) {
            case 1:
                return String.valueOf((char) ('A' + index - 1));
            case 2:
                return parentKey + (index);
            case 3:
                return String.valueOf(index);
            default:
                return parentKey + "." + index;
        }
    }

    private void setAttributes(String categoryType, List<CategoryDto> listCategory, String evaluationPeriodId) {
        List<CategoryAttributeEntity> listAttributes = indicatorsRepository.getAllAttributeByCategoryType(categoryType);
        Map<Long, Map<String, Object>> mapAttributes = new HashMap<>();
        listAttributes.forEach(item -> {
            mapAttributes.computeIfAbsent(item.getCategoryId(), k -> new HashMap<>());
            mapAttributes.get(item.getCategoryId()).put(item.getAttributeCode(), item.getAttributeValue());
        });
        List<CategoryDto> filteredList = listCategory.stream()
                .filter(dto -> {
                    String periodStr = Utils.getStringFromMap(mapAttributes.get(dto.getCategoryId()), "PERIOD_ID");
                    return evaluationPeriodId.equals(periodStr);
                }).toList();

        filteredList.forEach(dto -> dto.setAttributes(mapAttributes.get(dto.getCategoryId())));

        listCategory.clear();
        listCategory.addAll(filteredList);

    }

    @Override
    public ListResponseEntity<OrganizationWorkPlanningsEntity> getWorkPlanningById(Long id) throws BaseAppException {
        return organizationWorkPlanningsService.getDataByEvaluationId(id);
    }


    @Override
    public ResponseEntity saveIndicatorData(OrganizationEvaluationsRequest.IndicatorSubmitForm dto, Long id) throws BaseAppException {
        //Validate so luong kpi
        OrganizationEvaluationsEntity entity = organizationEvaluationsRepositoryJPA.getById(id);
        Integer minTotalKpi = orgConfigsRepository.getMinTotalKpi(entity.getOrganizationId(),
                orgConfigsRepository.get(EvaluationPeriodsEntity.class, entity.getEvaluationPeriodId()).getYear());
        if (minTotalKpi != null && minTotalKpi.intValue() > dto.getOrganizationIndicatorList().size()) {
            throw new BaseAppException(String.format("Đơn vị cần đăng ký tối thiểu %d chỉ số!", minTotalKpi));
        }

        List<Long> listId = dto.getOrganizationIndicatorList().stream()
                .map(it -> it.getOrganizationIndicatorId())
                .collect(Collectors.toList());
        entity.setAdjustReason(dto.getAdjustReason());
        entity.setEmpManagerId(dto.getEmpManagerId());
        entity.setSelfTotalPoint(dto.getSelfTotalPoint());
        entity.setManagerTotalPoint(dto.getManagerTotalPoint());
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        if ("KHOI_TAO".equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus("DU_THAO");
        } else if ("PHE_DUYET".equalsIgnoreCase(entity.getStatus()) && !Utils.isNullOrEmpty(dto.getAdjustReason())) {
            entity.setStatus(Constant.STATUS.CHO_PHE_DUYET);
        } else if (Constant.STATUS.DANH_GIA.equalsIgnoreCase(entity.getStatus()) && "Y".equalsIgnoreCase(dto.getIsEvaluateManage())) {
            entity.setStatus(Constant.STATUS.QLTT_DANH_GIA);
        } else if (Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) && "Y".equals(dto.getIsEvaluate())) {
            entity.setStatus(Constant.STATUS.DANH_GIA);
        } else if (OrganizationEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || OrganizationEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        }
        organizationEvaluationsRepositoryJPA.save(entity);
        organizationIndicatorsService.deleteListData(listId, id, dto.getAdjustReason());
        for (OrganizationIndicatorsRequest.SubmitForm it : dto.getOrganizationIndicatorList()) {
            organizationIndicatorsService.saveData(it, it.getOrganizationIndicatorId(), dto.getAdjustReason());
        }
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity saveWorkPlanningData(OrganizationWorkPlanningsRequest.SubmitForm dto) throws BaseAppException {
        OrganizationEvaluationsEntity entity;
        entity = organizationEvaluationsRepositoryJPA.getById(dto.getOrganizationEvaluationId());
        entity.setEmpManagerId(dto.getEmpManagerId());
        if (Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) && "Y".equals(dto.getIsEvaluate())) {
            entity.setStatus(Constant.STATUS.DANH_GIA);
        } else if ("KHOI_TAO".equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        } else if ("PHE_DUYET".equalsIgnoreCase(entity.getStatus()) && !Utils.isNullOrEmpty(dto.getAdjustReason())) {
            entity.setStatus(Constant.STATUS.CHO_PHE_DUYET);
        } else if (OrganizationEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || OrganizationEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        }
        entity.setAdjustReason(dto.getAdjustReason());
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        organizationEvaluationsRepositoryJPA.save(entity);
        return organizationWorkPlanningsService.saveData(dto, dto.getOrganizationWorkPlanningId());
    }

    @Override
    public ResponseEntity updateStatusById(OrganizationWorkPlanningsRequest.Status dto, Long id) throws RecordNotExistsException {
        Optional<OrganizationEvaluationsEntity> optional = organizationEvaluationsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationEvaluationsEntity.class);
        }
        optional.get().setStatus(dto.getStatus());
        if (!Utils.isNullOrEmpty(dto.getReason())) {
            optional.get().setReason(dto.getReason());
        }
        if (!Utils.isNullOrEmpty(dto.getReasonRequest())) {
            optional.get().setReasonRequest(dto.getReasonRequest());
        }
        optional.get().setModifiedTime(new Date());
        optional.get().setModifiedBy(Utils.getUserNameLogin());
        organizationEvaluationsRepositoryJPA.save(optional.get());
//        if (!Utils.isNullOrEmpty(dto.getContent())) {
//            ObjectAttributesRequest.SubmitForm data = new ObjectAttributesRequest.SubmitForm();
//            data.setAttributeCode(Constant.STATUS.DE_NGHI_DIEU_CHINH);
//            data.setDataType("string");
//            data.setTableName("kpi_organization_evaluations");
//            data.setAttributeValue(dto.getContent());
//            data.setObjectId(id);
//            objectAttributesService.saveData(data);
//        }
//        if (dto.getStatus().trim().equals(Constant.STATUS.PHE_DUYET)) {
//            if (optional.get().getEmpManagerId() != null && optional.get().getEmpManagerId() > 0) {
//                List<EmployeeEvaluationsEntity> listEmpEvaluation =
//                        organizationEvaluationsRepository.findByProperties(EmployeeEvaluationsEntity.class, "employeeId", optional.get().getEmpManagerId(), "evaluationPeriodId", optional.get().getEvaluationPeriodId());
//                if (!Utils.isNullOrEmpty(listEmpEvaluation)) {
//                    List<OrganizationIndicatorsEntity> listOrgIndicator =
//                            organizationEvaluationsRepository.findByProperties(OrganizationIndicatorsEntity.class, "organizationEvaluationId", id);
//                    List<EmployeeIndicatorsEntity> listEmpIndicator =
//                            organizationEvaluationsRepository.findByProperties(EmployeeIndicatorsEntity.class, "employeeEvaluationId", listEmpEvaluation.get(0).getEmployeeEvaluationId());
//                    for (OrganizationIndicatorsEntity orgIndicator : listOrgIndicator) {
//                        boolean found = false;
//                        List<Map<String, String>> targetList = fromJsonListToMap(orgIndicator.getTarget());
//                        Map<String, String> lastMap = targetList.get(0);
//                        String lastValue = null;
//                        for (Map.Entry<String, String> entry : lastMap.entrySet()) {
//                            lastValue = entry.getValue();
//                        }
//                        for (EmployeeIndicatorsEntity empIndicator : listEmpIndicator) {
//                            if (orgIndicator.getIndicatorConversionId().equals(empIndicator.getIndicatorConversionId())) {
//                                empIndicator.setPercent(orgIndicator.getPercent());
//                                empIndicator.setTarget(lastValue);
//                                empIndicator.setModifiedTime(new Date());
//                                empIndicator.setModifiedBy(Utils.getUserNameLogin());
//                                employeeIndicatorsRepositoryJPA.save(empIndicator);
//                                found = true;
//                                break;
//                            }
//                        }
//                        if (!found) {
//                            EmployeeIndicatorsEntity newEmpIndicator = new EmployeeIndicatorsEntity();
//                            newEmpIndicator.setIndicatorConversionId(orgIndicator.getIndicatorConversionId());
//                            newEmpIndicator.setEmployeeEvaluationId(listEmpEvaluation.get(0).getEmployeeEvaluationId());
//                            newEmpIndicator.setTarget(lastValue);
//                            newEmpIndicator.setStatus(BaseConstants.STATUS.ACTIVE);
//                            newEmpIndicator.setIndicatorId(orgIndicator.getIndicatorId());
//                            newEmpIndicator.setPercent(orgIndicator.getPercent());
//                            newEmpIndicator.setCreatedTime(new Date());
//                            newEmpIndicator.setCreatedBy(Utils.getUserNameLogin());
//                            employeeIndicatorsRepositoryJPA.save(newEmpIndicator);
//                        }
//                    }
//                }
//            }
//        }
        return ResponseUtils.ok(id);
    }

    @Override
    public boolean review(String type, OrganizationEvaluationsRequest.Review reviewRequest) {
        String userName = Utils.getUserNameLogin();
        HttpHeaders httpHeaders = Utils.getRequestHeader(request);
        reviewRequest.getIds().forEach(id -> {
            OrganizationEvaluationsEntity entity = organizationEvaluationsRepositoryJPA.getById(id);
            //validate status
            if (!OrganizationEvaluationsEntity.STATUS.CHO_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
                throw new BaseAppException("status is invalid!");
            }
            ApprovalHistoryEntity approvalHistoryEntity = approvalHistoryRepositoryJPA.getWaitingApproval(ApprovalHistoryEntity.TABLE_NAMES.KPI_ORGANIZATION_EVALUATIONS, id);
            if (approvalHistoryEntity == null) {
                throw new BaseAppException("not exists record for approval");
            }

            List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(Scope.REVIEW, Constant.RESOURCES.ORGANIZATION_EVALUATION, userName);
            boolean hasPermission = false;
            for (PermissionDataDto item : permissionDataDtos) {
                if (item.getOrgIds() != null &&
                    organizationEvaluationsRepository.checkOrg(item.getOrgIds(), approvalHistoryEntity.getApprovalLevel())) {
                    hasPermission = true;
                    break;
                }
            }

            if (!hasPermission) {
                throw new BaseAppException("User not has permission for approval");
            }

            approvalHistoryEntity.setStatus(type.toUpperCase());
            approvalHistoryEntity.setApprovalBy(userName);
            approvalHistoryEntity.setApprovalTime(new Date());
            approvalHistoryEntity.setComments(reviewRequest.getComment());
            approvalHistoryRepositoryJPA.save(approvalHistoryEntity);

            if (ApprovalHistoryEntity.STATUS.OK.equalsIgnoreCase(type)) {
                //thuc hien chuyen tiep sang ban ghi xet duyet tiep theo
                List<Long> orgIds = permissionFeignClient.getGrantedDomain(httpHeaders, Scope.REVIEW, Constant.RESOURCES.ORGANIZATION_EVALUATION, approvalHistoryEntity.getApprovalLevel()).getData();
                if (Utils.isNullOrEmpty(orgIds)
                    || (orgIds.size() == 1 && orgIds.get(0).equals(approvalHistoryEntity.getApprovalLevel()))) {
                    //het cap xet duyet
                    entity.setStatus(OrganizationEvaluationsEntity.STATUS.CHO_PHE_DUYET);
                    entity.setModifiedBy(userName);
                    entity.setModifiedTime(new Date());
                    organizationEvaluationsRepositoryJPA.save(entity);
                } else {
                    //Tao moi cap xét duyệt mới
                    ApprovalHistoryEntity newEntity = new ApprovalHistoryEntity();
                    newEntity.setCreatedBy(userName);
                    newEntity.setCreatedTime(new Date());
                    newEntity.setStatus(ApprovalHistoryEntity.STATUS.WAITING);
                    if (orgIds.get(orgIds.size() - 1).equals(approvalHistoryEntity.getApprovalLevel())) {
                        newEntity.setApprovalLevel(orgIds.get(orgIds.size() - 2));
                    } else {
                        newEntity.setApprovalLevel(orgIds.get(orgIds.size() - 1));
                    }
                    newEntity.setTableName(approvalHistoryEntity.getTableName());
                    newEntity.setObjectId(id);
                    newEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    approvalHistoryRepositoryJPA.save(newEntity);
                }
            } else {
                entity.setStatus(OrganizationEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET);
                entity.setModifiedBy(userName);
                entity.setModifiedTime(new Date());
                organizationEvaluationsRepositoryJPA.save(entity);
            }
        });


        return true;
    }

    @Override
    public boolean sendForApproval(Long id) {
        OrganizationEvaluationsEntity entity = organizationEvaluationsRepositoryJPA.getById(id);
        HttpHeaders httpHeaders = Utils.getRequestHeader(request);
        String userName = Utils.getUserNameLogin();
        //validate status
        if (!OrganizationEvaluationsEntity.STATUS.DU_THAO.equalsIgnoreCase(entity.getStatus())) {
            throw new BaseAppException("status is invalid!");
        }
//        OrganizationEvaluationsEntity entityLevel1 = organizationEvaluationsRepository.getOrgEvaluationLevel1(entity.getEvaluationPeriodId());
//        OrganizationEvaluationsRequest.SearchForm dto = new OrganizationEvaluationsRequest.SearchForm();
//        dto.setOrganizationEvaluationId(entityLevel1.getOrganizationEvaluationId());
//        List<OrganizationIndicatorsResponse.OrganizationEvaluation> listData = organizationIndicatorsService.getDataByEvaluationId(dto);
//        Integer minTotalKpi = orgConfigsRepository.getMinTotalKpi(entity.getOrganizationId(),
//                orgConfigsRepository.get(EvaluationPeriodsEntity.class, entity.getEvaluationPeriodId()).getYear());
//        if (minTotalKpi != null && minTotalKpi > listData.size()) {
//            throw new BaseAppException(String.format("Đơn vị cần đăng ký tối thiểu %d chỉ số!", minTotalKpi));
//        } else if (listData.stream().anyMatch(el -> !OrganizationIndicatorsEntity.STATUS.XAC_NHAN.equals(el.getStatusLevel1()))) {
//            throw new BaseAppException("Có chỉ số chưa ở trạng thái Xác nhận.");
//        }

        //update tat ca cac ban ghi cho xet duyet
//        approvalHistoryRepositoryJPA.inactiveOldData(ApprovalHistoryEntity.TABLE_NAMES.KPI_ORGANIZATION_EVALUATIONS, id);
//        List<Long> orgIds = permissionFeignClient.getGrantedDomain(httpHeaders, Scope.REVIEW, Constant.RESOURCES.ORGANIZATION_EVALUATION, entity.getOrganizationId()).getData();
//        if (Utils.isNullOrEmpty(orgIds)) {
        //het cap xet duyet
        entity.setStatus(OrganizationEvaluationsEntity.STATUS.CHO_PHE_DUYET);
        entity.setModifiedBy(userName);
        entity.setModifiedTime(new Date());
        organizationEvaluationsRepositoryJPA.save(entity);
//        } else {
//            //het cap xet duyet
//            entity.setStatus(OrganizationEvaluationsEntity.STATUS.CHO_XET_DUYET);
//            entity.setModifiedBy(userName);
//            entity.setModifiedTime(new Date());
//            organizationEvaluationsRepositoryJPA.save(entity);
//            //Tao moi cap xét duyệt mới
//            ApprovalHistoryEntity newEntity = new ApprovalHistoryEntity();
//            newEntity.setCreatedBy(userName);
//            newEntity.setCreatedTime(new Date());
//            newEntity.setStatus(ApprovalHistoryEntity.STATUS.WAITING);
//            newEntity.setApprovalLevel(orgIds.get(orgIds.size() - 1));
//            newEntity.setTableName(ApprovalHistoryEntity.TABLE_NAMES.KPI_ORGANIZATION_EVALUATIONS);
//            newEntity.setObjectId(id);
//            newEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
//            approvalHistoryRepositoryJPA.save(newEntity);
//        }

        return true;
    }

    @Override
    @Transactional
    public boolean approve(String type, OrganizationEvaluationsRequest.Review reviewRequest) {
        String userName = Utils.getUserNameLogin();
        HttpHeaders httpHeaders = Utils.getRequestHeader(request);
        reviewRequest.getIds().forEach(id -> {
            OrganizationEvaluationsEntity entity = organizationEvaluationsRepositoryJPA.getById(id);
            //validate status
            if (!OrganizationEvaluationsEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatus())) {
                throw new BaseAppException("status is invalid!");
            }
            //chi duoc phe duyet kpi cua don vi cap duoi
            //Lay quyen phe duyet cua user
            List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.APPROVE, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, Utils.getUserNameLogin());
            if (!organizationEvaluationsRepository.checkApprove(entity.getOrganizationId(), orgIds)) {
                throw new BaseAppException("Bạn không được phê duyệt KPI đơn vị này!");
            }
            //check xem kpi cua don vi cap tren da duoc phe duyet chua
            Long orgParentId = orgConfigsRepository.get(OrganizationEntity.class, entity.getOrganizationId()).getParentId();
            if (orgParentId != null) {
                String orgParentNotApproved = organizationEvaluationsRepository.getParentNotApproved(
                        orgParentId, entity.getEvaluationPeriodId());
                if (!Utils.isNullOrEmpty(orgParentNotApproved)) {
                    throw new BaseAppException(String.format("Kpi đơn vị cấp trên: %s chưa được phê duyệt!", orgParentNotApproved));
                }
            }

            if (ApprovalHistoryEntity.STATUS.OK.equalsIgnoreCase(type)) {
                //thuc hien chuyen tiep sang ban ghi xet duyet tiep theo
                entity.setStatus(OrganizationEvaluationsEntity.STATUS.PHE_DUYET);
            } else {
                entity.setStatus(OrganizationEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET);
            }
            entity.setApprovedBy(userName);
            entity.setApprovedTime(new Date());
            organizationEvaluationsRepositoryJPA.save(entity);

            //neu la phe duyet kpi cap don vi
            //--> thuc hien approve sang kpi cua lanh dao don vi
            if (OrganizationEvaluationsEntity.STATUS.PHE_DUYET.equals(entity.getStatus())) {
                Long empManagerId = entity.getEmpManagerId();
                List<EmployeeEvaluationsEntity> data = employeeEvaluationsRepository.findByProperties(EmployeeEvaluationsEntity.class, "evaluationPeriodId", entity.getEvaluationPeriodId(), "employeeId", empManagerId);
                EmployeeEvaluationsEntity employeeEvaluationsEntity;
                if (!Utils.isNullOrEmpty(data)) {
                    employeeEvaluationsEntity =
                            employeeEvaluationsRepository.findByProperties(EmployeeEvaluationsEntity.class, "evaluationPeriodId", entity.getEvaluationPeriodId(), "employeeId", empManagerId).get(0);
                    employeeEvaluationsEntity.setModifiedBy(userName);
                    employeeEvaluationsEntity.setModifiedTime(new Date());
                } else {
                    employeeEvaluationsEntity = new EmployeeEvaluationsEntity();
                    employeeEvaluationsEntity.setEvaluationPeriodId(entity.getEvaluationPeriodId());
                    employeeEvaluationsEntity.setEmployeeId(empManagerId);
                    employeeEvaluationsEntity.setCreatedBy(userName);
                    employeeEvaluationsEntity.setCreatedTime(new Date());
                }
                employeeEvaluationsEntity.setStatus(EmployeeEvaluationsEntity.STATUS.DU_THAO);
                employeeEvaluationsRepositoryJPA.save(employeeEvaluationsEntity);

                List<EmployeeIndicatorsEntity> employeeIndicatorsEntities =
                        employeeEvaluationsRepository.findByProperties(EmployeeIndicatorsEntity.class, "employeeEvaluationId", employeeEvaluationsEntity.getEmployeeEvaluationId());
                List<OrganizationIndicatorsEntity> organizationIndicatorsEntities =
                        employeeEvaluationsRepository.findByProperties(OrganizationIndicatorsEntity.class, "organizationEvaluationId", entity.getOrganizationEvaluationId());
                Map<Long, OrganizationIndicatorsEntity> mapOrgIndicatorsEntity = organizationIndicatorsEntities.stream().collect(Collectors.toMap(
                        item -> item.getIndicatorId(), item -> item, (existing, replacement) -> replacement
                ));
//                boolean existKHCTCN = false;
//                ParameterEntity parameterEntity = organizationEvaluationsRepository.getParameter("ID_KHCTCN", "PARAMETER_KPI");
//                Long idCN = parameterEntity != null ? Long.parseLong(parameterEntity.getConfigValue()) : IndicatorsEntity.ID_KHCTCN;
//                for (EmployeeIndicatorsEntity item : employeeIndicatorsEntities) {
//                    if (!item.getIndicatorId().equals(idCN)) {
//                        OrganizationIndicatorsEntity organizationIndicatorsEntity = mapOrgIndicatorsEntity.get(item.getIndicatorId());
//                        if (organizationIndicatorsEntity == null || organizationIndicatorsEntity.getStatus().equals(BaseConstants.STATUS.INACTIVE)) {
//                            item.setStatus(BaseConstants.STATUS.INACTIVE);
//                            item.setModifiedBy(userName);
//                            item.setModifiedTime(new Date());
//                            employeeIndicatorsRepositoryJPA.save(item);
//                        }
//                    } else {
//                        existKHCTCN = true;
//                    }
//                    mapOrgIndicatorsEntity.remove(item.getIndicatorId());
//                }

                IndicatorMastersEntity indicatorMastersEntity = null;

                for (OrganizationIndicatorsEntity orgEntity : organizationIndicatorsEntities) {
                    if (orgEntity != null && orgEntity.getIndicatorConversionId() != null) {
                        IndicatorConversionsEntity conversionEntity = employeeEvaluationsRepository.get(
                                IndicatorConversionsEntity.class,
                                orgEntity.getIndicatorConversionId()
                        );

                        if (conversionEntity != null && conversionEntity.getIndicatorMasterId() != null) {
                            indicatorMastersEntity = employeeEvaluationsRepository.get(
                                    IndicatorMastersEntity.class,
                                    conversionEntity.getIndicatorMasterId()
                            );
                            break;
                        }
                    }
                }
                List<IndicatorConversionsEntity> empConversionEntities = employeeEvaluationsRepository.findByProperties(IndicatorConversionsEntity.class, "indicatorMasterId", indicatorMastersRepository.getIndicatorMaster(
                        indicatorMastersEntity.getOrganizationId(), indicatorMastersEntity.getManagerJobId(), indicatorMastersEntity.getOrgTypeId()
                ).getIndicatorMasterId());
                Map<Long, IndicatorConversionsEntity> mapConversion = empConversionEntities.stream().filter(item -> item.getIndicatorId() != null)
                        .collect(Collectors.toMap(item -> item.getIndicatorId(), item -> item, (existing, replacement) -> replacement));


                mapOrgIndicatorsEntity.forEach((indicatorId, value) -> {
                    EmployeeIndicatorsEntity employeeIndicatorsEntity = new EmployeeIndicatorsEntity();
                    employeeIndicatorsEntity.setEmployeeEvaluationId(employeeEvaluationsEntity.getEmployeeEvaluationId());
                    employeeIndicatorsEntity.setIndicatorId(indicatorId);
                    employeeIndicatorsEntity.setIndicatorConversionId(mapConversion.get(indicatorId) != null ? mapConversion.get(indicatorId).getIndicatorConversionId() : null);
                    employeeIndicatorsEntity.setCreatedBy(userName);
                    employeeIndicatorsEntity.setCreatedTime(new Date());
                    employeeIndicatorsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    employeeIndicatorsEntity.setStatus(BaseConstants.STATUS.ACTIVE);
                    employeeIndicatorsRepositoryJPA.save(employeeIndicatorsEntity);
                });
//                if (!existKHCTCN) {
//                    EmployeeIndicatorsEntity employeeIndicatorsEntity = new EmployeeIndicatorsEntity();
//                    employeeIndicatorsEntity.setEmployeeEvaluationId(employeeEvaluationsEntity.getEmployeeEvaluationId());
//                    employeeIndicatorsEntity.setIndicatorId(idCN);
//                    employeeIndicatorsEntity.setIndicatorConversionId(mapConversion.get(idCN) != null ? mapConversion.get(idCN).getIndicatorConversionId() : null);
//                    employeeIndicatorsEntity.setCreatedBy(userName);
//                    employeeIndicatorsEntity.setCreatedTime(new Date());
//                    employeeIndicatorsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
//                    employeeIndicatorsEntity.setStatus(BaseConstants.STATUS.ACTIVE);
//                    employeeIndicatorsRepositoryJPA.save(employeeIndicatorsEntity);
//                }
            }

        });
        return true;
    }

    private String convertLong(String data) {
        if (Utils.isNullOrEmpty(data)) {
            return "";
        }
        if (data.contains(".")) {
            double value = Double.parseDouble(data);
            if (value == (int) value) {
                return String.valueOf((int) value);
            }
        }
        return data;
    }

    @Override
    public List<OrganizationEvaluationsResponse.OrganizationDto> getOrgParent(
            Long periodId, Long orgId, Long employeeId) {
        if (employeeId == null) {
            //lay danh sach don vi cha
            Long orgParentId = organizationEvaluationsRepository.get(OrganizationEntity.class, orgId).getParentId();
            if (orgParentId != null) {
                return organizationEvaluationsRepository.getOrgParent(periodId,
                        Arrays.asList(orgParentId));
            }
        } else {
            //Lay don vi cua nhan vien va don vi kiem nhiem
            EmployeesEntity employeesEntity = employeeEvaluationsRepository.get(EmployeesEntity.class, employeeId);
            Long orgParentId = employeesEntity.getOrganizationId();
            List<OrganizationEvaluationsResponse.OrganizationDto> result = new ArrayList<>();
            if (orgParentId != null) {
                result.addAll(organizationEvaluationsRepository.getOrgParent(periodId,
                        Arrays.asList(orgParentId)));
            }
            //Lay don vi cong tac kiem nhiem cua nhan vien
            List<Long> orgConcurrents = employeeEvaluationsRepository.getListOrgConcurrent(employeeId);
            if (!Utils.isNullOrEmpty(orgConcurrents)) {
                List<OrganizationEvaluationsResponse.OrganizationDto> temps = organizationEvaluationsRepository.getOrgParent(periodId,
                        orgConcurrents);
                for (OrganizationEvaluationsResponse.OrganizationDto organizationDto : temps) {
                    if (!result.contains(organizationDto)) {
                        organizationDto.setOrganizationName(organizationDto.getOrganizationName() + " (Kiêm nhiệm)");
                        result.add(organizationDto);
                    }
                }
            }
            return result;
        }
        return List.of();
    }

    @Override
    public TableResponseEntity<OrganizationEvaluationsResponse.OrgParent> getTableDataOrgParent(OrganizationEvaluationsRequest.OrgParent data) {
        return organizationIndicatorsService.getDataTableByEvaluationId(data);
    }

    @Override
    public void validatePermissionEvaluateManage(Long organizationEvaluationId) {
        List<Long> orgPermissionIds = authorizationService.getOrgHasPermission(Scope.APPROVE, Constant.RESOURCES.ORGANIZATION_EVALUATION, Utils.getUserNameLogin());
        OrganizationEvaluationsEntity organizationEvaluationsEntity = organizationEvaluationsRepositoryJPA.getById(organizationEvaluationId);
        if (!organizationEvaluationsRepository.checkApprove(organizationEvaluationsEntity.getOrganizationId(), orgPermissionIds)) {
            throw new BaseAppException("Bạn không được nhập thông tin cấp trên đánh giá của đơn vị này!");
        }
    }

    @Override
    public OrganizationEvaluationsResponse.Validate validatePermissionUpdate(Long organizationEvaluationId) {
        OrganizationEvaluationsResponse.Validate result = new OrganizationEvaluationsResponse.Validate();
//        OrganizationEvaluationsEntity entity = organizationEvaluationsRepositoryJPA.getById(organizationEvaluationId);
//        String ngayHetHan = objectAttributesService.getAttribute(entity.getEvaluationPeriodId(), "NGAY_KET_THUC_DIEU_CHINH_DON_VI", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
//        String ngayHetHanKHCT = objectAttributesService.getAttribute(entity.getEvaluationPeriodId(), "NGAY_KET_THUC_DIEU_CHINH_KHCT_DON_VI", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
//        if (!Utils.isNullOrEmpty(ngayHetHan)
//            && Utils.stringToDate(ngayHetHan).before(Utils.truncDate(new Date()))) {
//            if (!utilsService.hasRole("admin", "TCCB-ADMIN")) {
//                result.setAdjust(true);
//            }
//        }
//
//        if (!Utils.isNullOrEmpty(ngayHetHanKHCT)
//            && Utils.stringToDate(ngayHetHanKHCT).before(Utils.truncDate(new Date()))) {
//            if (!utilsService.hasRole("admin", "TCCB-ADMIN")) {
//                result.setAdjustKHCT(true);
//            }
//        }
        return result;
    }

    @Override
    public ResponseEntity getErrorWorkPlanning() {
        List<OrganizationWorkPlanningsEntity> listData = organizationEvaluationsRepository.getListOrgWorkPlanning();
        List<Long> listError = new ArrayList<>();
        for (OrganizationWorkPlanningsEntity data : listData) {
            List<EmployeeWorkPlanningsResponse.Content> contentList = Utils.fromJsonList(data.getContent(), EmployeeWorkPlanningsResponse.Content.class);
            for (EmployeeWorkPlanningsResponse.Content content : contentList) {
                Double selfPoint = null;
                Double fullYear = (!Utils.isNullOrEmpty(content.getFullYear()) && Utils.isNumeric(content.getFullYear())) ? Double.parseDouble(content.getFullYear()) : null;
                String unit = content.getUnit();
                Double result = (!Utils.isNullOrEmpty(content.getResult()) && Utils.isNumeric(content.getResult())) ? Double.parseDouble(content.getResult()) : null;
                Double selfPointFinal = (!Utils.isNullOrEmpty(content.getSelfPoint()) && Utils.isNumeric(content.getSelfPoint())) ? Double.parseDouble(content.getSelfPoint()) : null;
                if (result != null) {
                    if (fullYear != null && Utils.isNumeric(content.getFullYear())) {
                        if (fullYear != 0) {
                            if (Arrays.asList("25", "6", "179", "29", "11", "57").contains(unit)) {
                                double calculated = result != 0 ? ((fullYear / result) * 100) : 120;
                                selfPoint = calculated > 100 ? 120 : calculated;
                            } else {
                                selfPoint = (result / fullYear) * 100;
                            }
                        } else {
                            if (Arrays.asList("25", "6", "179", "29", "11", "57").contains(unit)) {
                                double calculated = result != 0 ? ((fullYear / result) * 100) : 100;
                                selfPoint = calculated > 100 ? 120 : calculated;
                            }
                        }
                    } else {
                        if (fullYear != null || !Utils.isNullOrEmpty(content.getFullYear())) {
                            selfPoint = result;
                        } else if (result != 0) {
                            selfPoint = 120d;
                        }
                    }
                }

                if (selfPoint != null) {
                    selfPoint = Utils.round(selfPoint, 2);
                }

                if (!Objects.equals(selfPoint, selfPointFinal)) {
                    listError.add(data.getOrganizationId());
                    break;
                }
            }
        }
        return ResponseUtils.ok(listError);
    }

    @Override
    @Transactional
    public ResponseEntity confirmResult(List<Long> listId) throws RecordNotExistsException {
        List<OrganizationEvaluationsEntity> listEntity = organizationEvaluationsRepository.getListEntity(listId);
        if (listEntity.isEmpty()) {
            throw new BaseAppException("Không có bản ghi nào thỏa mãn điều kiện.");
        } else if (listEntity.stream().anyMatch(el -> !Constant.STATUS.QLTT_DANH_GIA.equals(el.getStatus()))) {
            throw new BaseAppException("Có nhân sự chưa ở trạng thái QLTT đánh giá.");
        }

        Date date = new Date();
        String userName = Utils.getUserNameLogin();

        List<OrganizationEvaluationsEntity> listEntitySave = listEntity.stream().map(el -> {
            el.setStatus(Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA);
            el.setFinalResultId(el.getResultId());
            el.setFinalPoint(el.getManagerTotalPoint());
            el.setModifiedTime(date);
            el.setModifiedBy(userName);
            return el;
        }).toList();
        organizationEvaluationsRepository.updateBatch(OrganizationEvaluationsEntity.class, listEntitySave, false);

        return ResponseUtils.ok(true);
    }

    @Override
    public ResponseEntity finalResult(List<Long> listId) throws RecordNotExistsException {
        List<OrganizationEvaluationsEntity> listEntity = organizationEvaluationsRepository.getListEntity(listId);
        if (listEntity.isEmpty()) {
            throw new BaseAppException("Không có bản ghi nào thỏa mãn điều kiện.");
        } else if (listEntity.stream().anyMatch(el -> !Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA.equals(el.getStatus()))) {
            throw new BaseAppException("Có nhân sự chưa ở trạng thái Đã xác nhận KQ đánh giá.");
        }

        Date date = new Date();
        String userName = Utils.getUserNameLogin();

        List<OrganizationEvaluationsEntity> listEntitySave = listEntity.stream().map(el -> {
            el.setStatus(Constant.STATUS.CHOT_KQ_DANH_GIA);
            el.setModifiedTime(date);
            el.setModifiedBy(userName);
            return el;
        }).toList();
        organizationEvaluationsRepository.updateBatch(OrganizationEvaluationsEntity.class, listEntitySave, false);

        return ResponseUtils.ok(true);
    }

    @Override
    @Transactional
    public ResponseEntity adjustEvaluate(OrganizationEvaluationsRequest.RejectDto dto) throws RecordNotExistsException {
        Optional<OrganizationEvaluationsEntity> optional = organizationEvaluationsRepositoryJPA.findById(dto.getListId().get(0));
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(dto.getListId().get(0), OrganizationEvaluationsEntity.class);
        }
        if (!Constant.STATUS.QLTT_DANH_GIA.equals(optional.get().getStatus())) {
            throw new BaseAppException("Bản ghi không ở trạng thái QL đánh giá.");
        }

        OrganizationEvaluationsEntity entity = optional.get();
        entity.setStatus(Constant.STATUS.CHO_QLTT_DANH_GIA_LAI);
        entity.setReasonManageRequest(dto.getRejectReason());
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        organizationEvaluationsRepositoryJPA.save(entity);

        return ResponseUtils.ok(true);
    }

    @Override
    public ResponseEntity sendForApprovalLevel1(OrganizationEvaluationsRequest.RejectDto dto) throws RecordNotExistsException {
        List<Long> listId = dto.getListData().stream().map(OrganizationIndicatorsRequest.SubmitForm::getOrganizationIndicatorId).toList();
        List<OrganizationIndicatorsEntity> listEntity = organizationEvaluationsRepository.findByListId(OrganizationIndicatorsEntity.class, listId);
        if (listEntity.stream().anyMatch(el ->
                !OrganizationIndicatorsEntity.STATUS.DU_THAO.equals(el.getStatusLevel1()) && !OrganizationIndicatorsEntity.STATUS.YEU_CAU_NHAP_LAI.equals(el.getStatusLevel1()))) {
            throw new BaseAppException("Có chỉ số chưa ở trạng thái Dự thảo và Yêu cầu nhập lại.");
        }
        Date date = new Date();
        String userName = Utils.getUserNameLogin();

        List<OrganizationIndicatorsEntity> listEntitySave = listEntity.stream().map(el -> {
            el.setStatusLevel1(OrganizationIndicatorsEntity.STATUS.CHO_XAC_NHAN);
            el.setModifiedTime(date);
            el.setModifiedBy(userName);
            return el;
        }).toList();
        organizationEvaluationsRepository.updateBatch(OrganizationIndicatorsEntity.class, listEntitySave, false);

        return ResponseUtils.ok(true);
    }

    @Override
    public ResponseEntity confirmLevel1(OrganizationEvaluationsRequest.RejectDto dto) throws RecordNotExistsException {
        List<OrganizationIndicatorsEntity> listEntity = organizationEvaluationsRepository.findByListId(OrganizationIndicatorsEntity.class, dto.getListId());
        if (listEntity.stream().anyMatch(el -> !OrganizationIndicatorsEntity.STATUS.CHO_XAC_NHAN.equals(el.getStatusLevel1()))) {
            throw new BaseAppException("Có chỉ số chưa ở trạng thái Chờ xác nhận.");
        }
        Date date = new Date();
        String userName = Utils.getUserNameLogin();
        String statusUpdate = "Y".equals(dto.getIsConfirm()) ? OrganizationIndicatorsEntity.STATUS.XAC_NHAN : OrganizationIndicatorsEntity.STATUS.YEU_CAU_NHAP_LAI;
        List<OrganizationIndicatorsEntity> listEntitySave = listEntity.stream().map(el -> {
            el.setStatusLevel1(statusUpdate);
            el.setModifiedTime(date);
            el.setModifiedBy(userName);
            return el;
        }).toList();
        organizationEvaluationsRepository.updateBatch(OrganizationIndicatorsEntity.class, listEntitySave, false);

        return ResponseUtils.ok(true);
    }

    private static List<Map<String, String>> fromJsonListToMap(String jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, String>>>() {
        }.getType();
        return gson.fromJson(jsonArray, listType);
    }

}
