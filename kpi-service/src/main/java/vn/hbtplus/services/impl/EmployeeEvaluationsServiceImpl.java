/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.feigns.ReportFeignClient;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.ReportConfigDto;
import vn.hbtplus.models.dto.ConcurrentProcessDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.dto.EmployeeInfoDto;
import vn.hbtplus.models.dto.ParameterDto;
import vn.hbtplus.models.request.EmployeeEvaluationsRequest;
import vn.hbtplus.models.request.EmployeeIndicatorsRequest;
import vn.hbtplus.models.request.EmployeeWorkPlanningsRequest;
import vn.hbtplus.models.request.OrganizationIndicatorsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.EmployeeEvaluationsRepository;
import vn.hbtplus.repositories.impl.OrganizationEvaluationsRepository;
import vn.hbtplus.repositories.impl.ParameterRepositoryImpl;
import vn.hbtplus.repositories.jpa.ApprovalHistoryRepositoryJPA;
import vn.hbtplus.repositories.jpa.EmployeeEvaluationsRepositoryJPA;
import vn.hbtplus.repositories.jpa.EmployeeIndicatorsRepositoryJPA;
import vn.hbtplus.repositories.jpa.EmployeeWorkPlanningsRepositoryJPA;
import vn.hbtplus.services.*;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang kpi_employee_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeEvaluationsServiceImpl implements EmployeeEvaluationsService {

    private final EmployeeEvaluationsRepository employeeEvaluationsRepository;
    private final EmployeeEvaluationsRepositoryJPA employeeEvaluationsRepositoryJPA;
    private final EmployeeIndicatorsService employeeIndicatorsService;
    private final EmployeeWorkPlanningsService employeeWorkPlanningsService;
    private final ObjectAttributesService objectAttributesService;
    private final PermissionFeignClient permissionFeignClient;
    private final ApprovalHistoryRepositoryJPA approvalHistoryRepositoryJPA;
    private final OrganizationEvaluationsRepository organizationEvaluationsRepository;
    private final HttpServletRequest request;
    private final ReportFeignClient reportFeignClient;
    private final FileStorageFeignClient fileStorageFeignClient;
    private final AuthorizationService authorizationService;
    private final IndicatorsService indicatorsService;
    private final EmployeeIndicatorsRepositoryJPA employeeIndicatorsRepositoryJPA;
    private final EmployeeWorkPlanningsRepositoryJPA employeeWorkPlanningsRepositoryJPA;
    private final UtilsService utilsService;
    private final ParameterRepositoryImpl parameterRepositoryImpl;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmployeeEvaluationsResponse.SearchResult> searchData(EmployeeEvaluationsRequest.SearchForm dto) {
        return ResponseUtils.ok(employeeEvaluationsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        List<Long> listId = dto.getEmployeeIndicatorList().stream()
                .map(EmployeeIndicatorsRequest.SubmitForm::getEmployeeIndicatorId)
                .collect(Collectors.toList());
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        EmployeeEvaluationsEntity entity;
        entity = employeeEvaluationsRepositoryJPA.getById(dto.getEmployeeEvaluationId());
//        Integer empKPISize = dto.getEmployeeIndicatorList().size();
//        //validate so luong kpi cua nhan vien
//        String kpiConfig = employeeEvaluationsRepository.getKPIConfig(entity.getEmployeeId(), entity.getEvaluationPeriodId());
//        Integer minKPI = "null".equals(kpiConfig.split("-")[0]) ? null : Integer.parseInt(kpiConfig.split("-")[0]);
//        Integer maxKPI = "null".equals(kpiConfig.split("-")[1]) ? null : Integer.parseInt(kpiConfig.split("-")[1]);
//        if (minKPI != null && maxKPI != null
//            && maxKPI.equals(minKPI) && !minKPI.equals(empKPISize)) {
//            throw new BaseAppException(String.format("Bạn bắt buộc đăng ký %d chỉ số!", minKPI));
//        } else if (minKPI != null && minKPI > empKPISize) {
//            throw new BaseAppException(String.format("Bạn bắt buộc đăng ký tối thiểu %d chỉ số!", minKPI));
//        } else if (maxKPI != null && maxKPI < empKPISize) {
//            throw new BaseAppException(String.format("Bạn chỉ được đăng ký tối đa %d chỉ số!", maxKPI));
//        }

        entity.setAdjustReason(dto.getAdjustReason());
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
        } else if (EmployeeEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || EmployeeEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        } else if (Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) && !Utils.isNullOrEmpty(dto.getAdjustReason())) {
            entity.setStatus(Constant.STATUS.CHO_PHE_DUYET);
        } else if ((Constant.STATUS.CHO_QLTT_DANH_GIA_LAI.equalsIgnoreCase(entity.getStatus()) || Constant.STATUS.CHO_QLTT_DANH_GIA.equalsIgnoreCase(entity.getStatus())) && "Y".equalsIgnoreCase(dto.getIsEvaluateManage())) {
            entity.setStatus(Constant.STATUS.QLTT_DANH_GIA);
        } else if ((Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || Constant.STATUS.YC_DANH_GIA_LAI.equalsIgnoreCase(entity.getStatus()))
                   && "Y".equalsIgnoreCase(dto.getIsEvaluate())) {
            entity.setStatus(Constant.STATUS.DANH_GIA);
        }
        employeeEvaluationsRepositoryJPA.save(entity);
        employeeIndicatorsService.deleteListData(listId, dto.getEmployeeEvaluationId(), dto.getAdjustReason());

        //Lay ra config trong so toi thieu
        List<EmployeeIndicatorsEntity> listIndicatorAdd = new ArrayList<>();
        List<EmployeeIndicatorsEntity> listIndicatorUpdate = new ArrayList<>();
        //Kiem tra trong so toi thieu
//        String configKpiPercent = indicatorsService.getMappingValue(String.valueOf(empKPISize), "KPI_CA_NHAN_TRONG_SO_TOI_THIEU", new Date());
//        Integer kpiMinPercent = Utils.isNullOrEmpty(configKpiPercent) ? null : Integer.parseInt(configKpiPercent);
//        Integer khcnMinPercent = "null".equals(kpiConfig.split("-")[2]) ? null : Integer.parseInt(kpiConfig.split("-")[2]);
//        Integer khdvMinPercent = "null".equals(kpiConfig.split("-")[3]) ? null : Integer.parseInt(kpiConfig.split("-")[3]);
//        Integer khNghienCuuMinPercent = "null".equals(kpiConfig.split("-")[4]) ? null : Integer.parseInt(kpiConfig.split("-")[4]);
//        String idKPICanhan = parameterRepositoryImpl.getConfigValue("ID_KPI_KHCT_CA_NHAN", new Date(), String.class);
//        String idKPIDonvi = parameterRepositoryImpl.getConfigValue("ID_KPI_KHCT_DON_VI", new Date(), String.class);
//        String idKPINghienCuu = parameterRepositoryImpl.getConfigValue("ID_KPI_NGHIEN_CUU_KHOA_HOC", new Date(), String.class);

        for (EmployeeIndicatorsRequest.SubmitForm it : dto.getEmployeeIndicatorList()) {
//            if (kpiMinPercent != null && kpiMinPercent > it.getPercent()) {
//                throw new BaseAppException(String.format("Trọng số kpi tối thiểu phải bằng %s", kpiMinPercent));
//            }
//            if (khcnMinPercent != null && khcnMinPercent > it.getPercent()
//                && Utils.stringToListLong(idKPICanhan, ",").contains(it.getIndicatorId())) {
//                throw new BaseAppException(String.format("Trọng số kpi kế hoạch công tác cá nhân tối thiểu phải bằng %s", khcnMinPercent));
//            }
//            if (khdvMinPercent != null && khdvMinPercent > it.getPercent()
//                && Utils.stringToListLong(idKPIDonvi, ",").contains(it.getIndicatorId())) {
//                throw new BaseAppException(String.format("Trọng số kpi kế hoạch công tác đơn vị tối thiểu phải bằng %s", khdvMinPercent));
//            }
//            if (khNghienCuuMinPercent != null && khNghienCuuMinPercent > it.getPercent()
//                && Utils.stringToListLong(idKPINghienCuu, ",").contains(it.getIndicatorId())) {
//                throw new BaseAppException(String.format("Trọng số kpi nghiên cứu khoa học tối thiểu phải bằng %s", khNghienCuuMinPercent));
//            }

            EmployeeIndicatorsEntity entityIndicator;
            boolean isUpdate = it.getEmployeeIndicatorId() != null && it.getEmployeeIndicatorId() > 0L;

            if (isUpdate) {
                entityIndicator = employeeIndicatorsRepositoryJPA.getById(it.getEmployeeIndicatorId());
                entityIndicator.setModifiedTime(currentDate);
                entityIndicator.setModifiedBy(userName);
            } else {
                entityIndicator = new EmployeeIndicatorsEntity();
            }

            if (entityIndicator.getOldPercent() == null && dto.getAdjustReason() != null) {
                it.setOldPercent(entityIndicator.getPercent());
            }

            Utils.copyProperties(it, entityIndicator);
            entityIndicator.setStatus(BaseConstants.STATUS.ACTIVE);
            entityIndicator.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);

            if (isUpdate) {
                listIndicatorUpdate.add(entityIndicator);
            } else {
                listIndicatorAdd.add(entityIndicator);
            }
        }


        employeeEvaluationsRepository.insertBatch(EmployeeIndicatorsEntity.class, listIndicatorAdd, userName);
        employeeEvaluationsRepository.updateBatch(EmployeeIndicatorsEntity.class, listIndicatorUpdate, true);


//        List<EmployeeWorkPlanningsEntity> listWorkPlanningAdd = new ArrayList<>();
//        List<EmployeeWorkPlanningsEntity> listWorkPlanningUpdate = new ArrayList<>();
//        for (EmployeeWorkPlanningsRequest.SubmitForm workPlanning : dto.getWorkPlanningList()) {
//            EmployeeWorkPlanningsEntity workPlanningEntity;
//            boolean isUpdate = workPlanning.getEmployeeWorkPlanningId() != null && workPlanning.getEmployeeWorkPlanningId() > 0L;
//            if (isUpdate) {
//                workPlanningEntity = employeeWorkPlanningsRepositoryJPA.getById(workPlanning.getEmployeeWorkPlanningId());
//                workPlanningEntity.setModifiedTime(currentDate);
//                workPlanningEntity.setModifiedBy(userName);
//            } else {
//                workPlanningEntity = new EmployeeWorkPlanningsEntity();
//            }
//            Utils.copyProperties(workPlanning, workPlanningEntity);
//            workPlanningEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
//            if (isUpdate) {
//                listWorkPlanningUpdate.add(workPlanningEntity);
//            } else {
//                listWorkPlanningAdd.add(workPlanningEntity);
//            }
//        }
//
//        employeeEvaluationsRepository.insertBatch(EmployeeWorkPlanningsEntity.class, listWorkPlanningAdd, userName);
//        employeeEvaluationsRepository.updateBatch(EmployeeWorkPlanningsEntity.class, listWorkPlanningUpdate, true);
//        //Lưu thêm thuộc tính bổ sung
//        objectAttributesService.saveObjectAttributes(entity.getEmployeeEvaluationId(),
//                dto.getListAttributes(), EmployeeEvaluationsEntity.class, null);

        return ResponseUtils.ok(dto.getEmployeeEvaluationId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmployeeEvaluationsEntity> optional = employeeEvaluationsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeEvaluationsEntity.class);
        }
        employeeEvaluationsRepository.deActiveObject(EmployeeEvaluationsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EmployeeEvaluationsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException {
        Optional<EmployeeEvaluationsEntity> optional = employeeEvaluationsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeEvaluationsEntity.class);
        }
        EmployeeEvaluationsResponse.SearchResult dto = new EmployeeEvaluationsResponse.SearchResult();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, employeeEvaluationsRepository.getSQLTableName(EmployeeEvaluationsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeeEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_giao_nhan_KPI_nhan_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = employeeEvaluationsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_giao_nhan_KPI_nhan_vien.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportDataEvaluation(EmployeeEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_danh_gia_KPI_nhan_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = employeeEvaluationsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_danh_gia_KPI_nhan_vien.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportEmpSummary(EmployeeEvaluationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_Tong_hop_xep_loai_KPI_nhan_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);

        List<Map<String, Object>> listDataExport = employeeEvaluationsRepository.getExportEmpSummary(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);

        return ResponseUtils.ok(dynamicExport, "BM_Xuat_Tong_hop_xep_loai_KPI_nhan_vien.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity updateEmpSummary(EmployeeEvaluationsRequest.EmpSummarySubmitForm dto, Long id) throws BaseAppException {
        EmployeeEvaluationsEntity entity;
        if (id != null && id > 0L) {
            entity = employeeEvaluationsRepositoryJPA.getById(id);
            entity.setFinalPoint(dto.getFinalPoint());
            entity.setFinalResultId(dto.getFinalResultId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeeEvaluationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        employeeEvaluationsRepositoryJPA.save(entity);

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity managerUpdateEmpSummary(EmployeeEvaluationsRequest.EmpSummarySubmitForm dto, Long id) throws BaseAppException {
        EmployeeEvaluationsEntity entity;
        if (id != null && id > 0L) {
            entity = employeeEvaluationsRepositoryJPA.getById(id);
            entity.setResultId(dto.getResultId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeeEvaluationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        employeeEvaluationsRepositoryJPA.save(entity);

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> exportDataById(Long id) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_KPI_nhan_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<String> resultIdList = Arrays.asList("1", "2", "3", "4", "5");
        List<String> resultIdData = new ArrayList<>();

        EmployeeEvaluationsResponse.EmpBean dataEmployeeExport = employeeEvaluationsRepository.getExportEmployeeData(id);
        List<Map<String, Object>> listDataKpiExport = employeeEvaluationsRepository.getExportKpiData(id);
        List<Map<String, Object>> listDataEmpWorkPlanningExport = employeeEvaluationsRepository.getExportEmpWorkPlanningData(id);
        Map<String, String> mapUnit = organizationEvaluationsRepository.getMapData("value", "name", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH);
        EmployeeDto employeeDto = employeeEvaluationsRepository.getEmployeeInfo(id);

        Date currentDate = new Date();
        dynamicExport.replaceText("${day}", Utils.formatDate(currentDate, "dd"));
        dynamicExport.replaceText("${month}", Utils.formatDate(currentDate, "MM"));
        dynamicExport.replaceText("${year}", Utils.formatDate(currentDate, "YYYY"));
        dynamicExport.replaceText("${job1}", "N".equals(Utils.NVL(dataEmployeeExport.getIsHeadLv2())) ? "TRƯỞNG ĐƠN VỊ" : "HIỆU TRƯỞNG");
        dynamicExport.replaceText("${job2}", "N".equals(Utils.NVL(dataEmployeeExport.getIsHeadLv2())) ? "TRƯỞNG ĐƠN VỊ CẤU THÀNH" : "");

        String yearStr = objectAttributesService.getAttribute(dataEmployeeExport.getEvaluationPeriodId(), "NAM_HOC", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
//        dynamicExport.setText("KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + listDataEmployeeExport.get(0).get("evaluationPeriodYear"), 0, 3);
        dynamicExport.setText(List.of(Constant.STATUS.QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA).contains(Utils.NVL(dataEmployeeExport.getStatus())) ?
                "TỔNG KẾT CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr : "KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr, 0, 3);
        dynamicExport.setText(Utils.NVL(dataEmployeeExport.getEmployeeName()), 2, 7);
        dynamicExport.setText(employeeDto.getPromotionRankName(), 2, 8);
        dynamicExport.setText(employeeDto.getJobName(), 2, 9);
        dynamicExport.setText(employeeDto.getJobNameManage(), 2, 10);
        dynamicExport.setText(employeeDto.getPositionConcurrent(), 7, 7);
        for (int i = 0; i < listDataKpiExport.size(); i++) {
            Map<String, Object> dataKpiExport = listDataKpiExport.get(i);
            dataKpiExport.put("stt_kpi", i + 1);
            boolean isSelect = "SELECT".equals(Utils.getStringFromMap(dataKpiExport, "rating_type").trim()) && !"DON_VI".equals(Utils.getStringFromMap(dataKpiExport, "conversion_type").trim());
            String percent = convertLong(dataKpiExport.get("percent") == null ? null : dataKpiExport.get("percent").toString());
            if (!Utils.isNullOrEmpty(percent)) {
                percent += "%";
                dataKpiExport.put("percent", percent);
            }
            String target = dataKpiExport.get("target") == null ? "" : dataKpiExport.get("target").toString();
            dataKpiExport.put("target", target);
            String expressionList = (String) dataKpiExport.get("expressionList");
            if (expressionList != null) {
                Map<String, String> targetMap = new HashMap<>();
                if (dataKpiExport.get("targetStr") != null) {
                    String target1 = null;
                    String target2 = null;
                    String target3 = null;
                    OrganizationIndicatorsRequest.Target targetStr = Utils.fromJson(dataKpiExport.get("targetStr").toString(), OrganizationIndicatorsRequest.Target.class);
                    if (target != null) {
                        if (targetStr.getM1() != null) target1 = getValueTarget(targetStr.getM1());
                        if (targetStr.getM2() != null) target2 = getValueTarget(targetStr.getM2());
                        if (targetStr.getM3() != null) target3 = getValueTarget(targetStr.getM3());
                    }
                    targetMap.put("Mức ngưỡng", target1);
                    targetMap.put("Mức cơ bản", target2);
                    targetMap.put("Mức đẩy mạnh", target3);
                }
                String[] expressions = expressionList.split("; ");
                for (int j = 0; j < expressions.length; j++) {
                    String[] conditionAndValue = expressions[j].split("#");

                    if (conditionAndValue.length == 2) {
                        String condition = extractCondition(conditionAndValue[0], isSelect, targetMap);
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
            mapData.put("target", "");
            mapData.put("5", "");
            mapData.put("4", "");
            mapData.put("3", "");
            mapData.put("2", "");
            mapData.put("1", "");
            mapData.put("note", "");
            listDataKpiExport.add(mapData);
        }

        int number = 3;
        dynamicExport.replaceKeys(listDataKpiExport);
        dynamicExport.setLastRow(18 + listDataKpiExport.size());
        if (Utils.isNullOrEmpty(listDataEmpWorkPlanningExport)) {
            dynamicExport.setText(Utils.intToRoman(number++) + ". " + "Kế hoạch công tác", 1);
            dynamicExport.setCellFormat(1, 1, ExportExcel.BOLD_FORMAT);
            dynamicExport.copyRange(dynamicExport.getLastRow() + 1, 0, dynamicExport.getLastRow() + 2, 13, 0, 15, 1, 28);
            dynamicExport.increaseRow();
            dynamicExport.increaseRow();
            dynamicExport.increaseRow();
            dynamicExport.increaseRow();
        }
        for (int j = 0; j < listDataEmpWorkPlanningExport.size(); j++) {
            String content = (String) listDataEmpWorkPlanningExport.get(j).get("content");
            String name = (String) listDataEmpWorkPlanningExport.get(j).get("name");
//            String year = listDataEmpWorkPlanningExport.get(j).get("evaluationPeriodYear").toString();
            List<EmployeeWorkPlanningsResponse.Content> contentList = Utils.fromJsonList(content, EmployeeWorkPlanningsResponse.Content.class);
            int lastRow = dynamicExport.getLastRow();
            dynamicExport.setText(Utils.intToRoman(number++) + ". " + name + " năm học " + yearStr, 1);
            dynamicExport.setCellFormat(1, 1, ExportExcel.BOLD_FORMAT);
            dynamicExport.copyRange(lastRow + 1, 0, lastRow + 2, 13, 0, 15, 1, 28);
            dynamicExport.increaseRow();
            dynamicExport.increaseRow();
            dynamicExport.increaseRow();
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
                    dynamicExport.mergeCell(columnIndex, columnIndex + 7);
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
            dynamicExport.setCellFormat(dynamicExport.getLastRow() - contentList.size(), 0, dynamicExport.getLastRow() - 1, 13, ExportExcel.BORDER_FORMAT);
            dynamicExport.increaseRow();
            dynamicExport.increaseRow();
        }
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.copyRange(dynamicExport.getLastRow() + 1, 1, dynamicExport.getLastRow() + 8, 7, 4, 15, 11, 21);
        dynamicExport.deleteRange(0, 15, dynamicExport.getLastRow(), 28);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_KPI_nhan_vien.xlsx");
    }

    public static String extractCondition(String input, boolean isSelect, Map<String, String> targetMap) {
        if (isSelect) {
            Pattern pattern = Pattern.compile("=\\s*(.+)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1).trim();
            } else {
                return "";
            }
        } else if (!targetMap.isEmpty()) {
            String result = input;
            for (Map.Entry<String, String> entry : targetMap.entrySet()) {
                if (!Utils.isNullOrEmpty(entry.getValue())) {
                    result = result.replace(entry.getKey(), entry.getValue());
                }
            }
            return result.trim();
        } else {
            return input.trim();
        }
    }

    @Override
    public ResponseEntity<Object> exportEvaluationsById(Long id) throws Exception {
        EmployeeEvaluationsResponse.EmpBean dataEmployeeExport = employeeEvaluationsRepository.getExportEmployeeData(id);
        List<Map<String, Object>> listDataKpiExport = employeeEvaluationsRepository.getExportKpiData(id);
        Map<String, String> mapUnit = organizationEvaluationsRepository.getMapData("value", "name", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH);
        ExportExcel dynamicExport = handleExportEvaluation(dataEmployeeExport, listDataKpiExport, mapUnit);

        return ResponseUtils.ok(dynamicExport, "BM_Xuat_danh_gia_KPI_nhan_vien.xlsx", false);
    }

    public ExportExcel handleExportEvaluation(EmployeeEvaluationsResponse.EmpBean dataEmployeeExport, List<Map<String, Object>> listDataKpiExport,
                                              Map<String, String> mapUnit) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_danh_gia_KPI_nhan_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<String> resultIdList = Arrays.asList("1", "2", "3", "4", "5");
        List<String> resultIdData = new ArrayList<>();
        Date currentDate = new Date();
        String yearStr = objectAttributesService.getAttribute(dataEmployeeExport.getEvaluationPeriodId(), "NAM_HOC", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
        dynamicExport.setText(List.of(Constant.STATUS.QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA).contains(Utils.NVL(dataEmployeeExport.getStatus())) ?
                "TỔNG KẾT CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr : "KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + yearStr, 0, 3);
        dynamicExport.replaceText("${day}", Utils.formatDate(currentDate, "dd"));
        dynamicExport.replaceText("${month}", Utils.formatDate(currentDate, "MM"));
        dynamicExport.replaceText("${year}", Utils.formatDate(currentDate, "YYYY"));
        dynamicExport.replaceText("${self_total_point}", Utils.NVL(dataEmployeeExport.getSelfTotalPoint()).toString());
        dynamicExport.replaceText("${manager_total_point}", Utils.NVL(dataEmployeeExport.getManagePoint()).toString());
        dynamicExport.replaceText("${job1}", "N".equals(Utils.NVL(dataEmployeeExport.getIsHeadLv2())) ? "TRƯỞNG ĐƠN VỊ" : "HIỆU TRƯỞNG");
        dynamicExport.replaceText("${job2}", "N".equals(Utils.NVL(dataEmployeeExport.getIsHeadLv2())) ? "TRƯỞNG ĐƠN VỊ CẤU THÀNH" : "");
//        dynamicExport.setText("KẾ HOẠCH CÔNG TÁC VÀ KPI NĂM HỌC " + listDataEmployeeExport.get(0).get("evaluationPeriodYear"), 0, 3);
        dynamicExport.setText(Utils.NVL(dataEmployeeExport.getEmployeeName()), 2, 7);

        for (int i = 0; i < listDataKpiExport.size(); i++) {
            Map<String, Object> dataKpiExport = listDataKpiExport.get(i);
            dataKpiExport.put("stt_kpi", i + 1);
            boolean isSelect = "SELECT".equals(Utils.getStringFromMap(dataKpiExport, "rating_type").trim()) && !"DON_VI".equals(Utils.getStringFromMap(dataKpiExport, "conversion_type").trim());
            String percent = convertLong(dataKpiExport.get("percent").toString());
            if (!Utils.isNullOrEmpty(percent)) {
                percent += "%";
                dataKpiExport.put("percent", percent);
            }
            String target = dataKpiExport.get("target").toString();
            dataKpiExport.put("target", target);
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
            String expressionList = (String) dataKpiExport.get("expressionList");
            if (expressionList != null) {
                Map<String, String> targetMap = new HashMap<>();
                if (dataKpiExport.get("targetStr") != null) {
                    String target1 = null;
                    String target2 = null;
                    String target3 = null;
                    OrganizationIndicatorsRequest.Target targetStr = Utils.fromJson(dataKpiExport.get("targetStr").toString(), OrganizationIndicatorsRequest.Target.class);
                    if (target != null) {
                        if (targetStr.getM1() != null) target1 = getValueTarget(targetStr.getM1());
                        if (targetStr.getM2() != null) target2 = getValueTarget(targetStr.getM2());
                        if (targetStr.getM3() != null) target3 = getValueTarget(targetStr.getM3());
                    }
                    targetMap.put("Mức ngưỡng", target1);
                    targetMap.put("Mức cơ bản", target2);
                    targetMap.put("Mức đẩy mạnh", target3);
                }
                String[] expressions = expressionList.split("; ");
                for (int j = 0; j < expressions.length; j++) {
                    String[] conditionAndValue = expressions[j].split("#");

                    if (conditionAndValue.length == 2) {
                        String condition = extractCondition(conditionAndValue[0], isSelect, targetMap);
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
            mapData.put("target", "");
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
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.copyRange(dynamicExport.getLastRow() + 1, 1, dynamicExport.getLastRow() + 8, 7, 4, 19, 11, 25);
        return dynamicExport;
    }

    ;

//    private void viewPurpose(ExportExcel exportExcel, int row) throws Exception {
//        int col = 0;
//        exportExcel.setText("STT", col, row);
//        exportExcel.mergeCell(row, col, row + 1, col);
//        col++;
//
//        exportExcel.setText("Các trách nhiệm, nhiệm vụ chính theo mô tả vị trí việc làm", col, row);
//        exportExcel.mergeCell(row, col, row + 1, col);
//        col++;
//
//        exportExcel.setText("Đvt", col, row);
//        exportExcel.mergeCell(row, col, row + 1, col);
//        col++;
//
//        exportExcel.setText("Kế hoạch công tác", col, row);
//        exportExcel.mergeCell(row, col, row, col + 2);
//
//        exportExcel.setText("HK I", col++, row + 1);
//        exportExcel.setText("HK II", col++, row + 1);
//        exportExcel.setText("Cả năm", col++, row + 1);
//
//        exportExcel.setText("Ghi chú", col, row);
//        exportExcel.mergeCell(row, col, row + 1, col);
//
//        exportExcel.setCellFormat(row, 0, row + 1, col, ExportExcel.BOLD_FORMAT);
//        exportExcel.setCellFormat(row, 0, row + 1, col, ExportExcel.BORDER_FORMAT);
//        exportExcel.setCellFormat(row, 0, row + 1, col, ExportExcel.CENTER_FORMAT);
//    }

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
    public ResponseEntity updateStatusById(EmployeeWorkPlanningsRequest.Status dto, Long employeeEvaluationId) throws RecordNotExistsException {
        Optional<EmployeeEvaluationsEntity> optional = employeeEvaluationsRepositoryJPA.findById(employeeEvaluationId);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(employeeEvaluationId, EmployeeEvaluationsEntity.class);
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
        employeeEvaluationsRepositoryJPA.save(optional.get());
//        if (!Utils.isNullOrEmpty(dto.getContent())) {
//            ObjectAttributesRequest.SubmitForm data = new ObjectAttributesRequest.SubmitForm();
//            data.setAttributeCode(Constant.STATUS.DE_NGHI_DIEU_CHINH);
//            data.setDataType("string");
//            data.setTableName("kpi_employee_evaluations");
//            data.setAttributeValue(dto.getContent());
//            data.setObjectId(employeeEvaluationId);
//            objectAttributesService.saveData(data);
//        }
        return ResponseUtils.ok(employeeEvaluationId);
    }


    @Override
    @Transactional
    public ResponseEntity confirmResult(List<Long> listId) throws RecordNotExistsException {
        List<EmployeeEvaluationsEntity> listEntity = employeeEvaluationsRepository.getListEntity(listId);
        if (listEntity.isEmpty()) {
            throw new BaseAppException("Không có bản ghi nào thỏa mãn điều kiện.");
        } else if (listEntity.stream().anyMatch(el -> !Constant.STATUS.QLTT_DANH_GIA.equals(el.getStatus()))) {
            throw new BaseAppException("Có nhân sự chưa ở trạng thái QLTT đánh giá.");
        }

        Date date = new Date();
        String userName = Utils.getUserNameLogin();

        List<EmployeeEvaluationsEntity> listEntitySave = listEntity.stream().map(el -> {
            el.setStatus(Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA);
            el.setFinalResultId(el.getResultId());
            el.setFinalPoint(el.getManagerTotalPoint());
            el.setModifiedTime(date);
            el.setModifiedBy(userName);
            return el;
        }).toList();
        employeeEvaluationsRepository.updateBatch(EmployeeEvaluationsEntity.class, listEntitySave, false);

        return ResponseUtils.ok(true);
    }

    @Override
    public ResponseEntity finalResult(List<Long> listId) throws RecordNotExistsException {
        List<EmployeeEvaluationsEntity> listEntity = employeeEvaluationsRepository.getListEntity(listId);
        if (listEntity.isEmpty()) {
            throw new BaseAppException("Không có bản ghi nào thỏa mãn điều kiện.");
        } else if (listEntity.stream().anyMatch(el -> !Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA.equals(el.getStatus()))) {
            throw new BaseAppException("Có nhân sự chưa ở trạng thái Đã xác nhận KQ đánh giá.");
        }

        Date date = new Date();
        String userName = Utils.getUserNameLogin();

        List<EmployeeEvaluationsEntity> listEntitySave = listEntity.stream().map(el -> {
            el.setStatus(Constant.STATUS.CHOT_KQ_DANH_GIA);
            el.setModifiedTime(date);
            el.setModifiedBy(userName);
            return el;
        }).toList();
        employeeEvaluationsRepository.updateBatch(EmployeeEvaluationsEntity.class, listEntitySave, false);

        return ResponseUtils.ok(true);
    }

    @Override
    @Transactional
    public ResponseEntity adjustEvaluate(EmployeeWorkPlanningsRequest.RejectDto dto) throws RecordNotExistsException {
        Optional<EmployeeEvaluationsEntity> optional = employeeEvaluationsRepositoryJPA.findById(dto.getListId().get(0));
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(dto.getListId().get(0), EmployeeEvaluationsEntity.class);
        }
        if (!Constant.STATUS.QLTT_DANH_GIA.equals(optional.get().getStatus())) {
            throw new BaseAppException("Bản ghi không ở trạng thái QL đánh giá.");
        }

        EmployeeEvaluationsEntity entity = optional.get();
        entity.setStatus(Constant.STATUS.CHO_QLTT_DANH_GIA_LAI);
        entity.setReasonManageRequest(dto.getRejectReason());
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        employeeEvaluationsRepositoryJPA.save(entity);

        return ResponseUtils.ok(true);
    }

    @Override
    public ResponseEntity getIndicatorById(Long id, boolean isGetAll) throws BaseAppException {
        Optional<EmployeeEvaluationsEntity> optional = employeeEvaluationsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeeEvaluationsEntity.class);
        }

        //check xem user co quyen voi don vi khong
        EmployeeEvaluationsEntity entity = optional.get();
        if (!isGetAll) {
            isGetAll = authorizationService.hasPermissionWithOrg(entity.getOrganizationId(), Scope.VIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS);
        }

        EmployeeEvaluationsResponse.DetailBean result = new EmployeeEvaluationsResponse.DetailBean();
        List<EmployeeIndicatorsResponse.EmployeeEvaluation> listData = employeeIndicatorsService.getDataByEvaluationId(id, isGetAll);
        result.setListData(listData);
        result.setAdjustReason(optional.get().getAdjustReason());
        return ResponseUtils.ok(result);
    }

    @Override
    public ResponseEntity getIndicatorByListId(List<Long> listId) throws BaseAppException {
        List<EmployeeEvaluationsEntity> listEntity = employeeEvaluationsRepository.getDataByListId(listId);
        Map<Long, EmployeeEvaluationsResponse.EvaluateBean> map = new HashMap<>();
        EmployeeEvaluationsResponse.EvaluateBeanResult result = new EmployeeEvaluationsResponse.EvaluateBeanResult();
        List<EmployeeEvaluationsResponse.EmpData> listEmp = new ArrayList<>();
        for (EmployeeEvaluationsEntity entity : listEntity) {
            boolean isGetAll = authorizationService.hasPermissionWithOrg(
                    entity.getOrganizationId(), Scope.VIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS
            );

            List<EmployeeIndicatorsResponse.EmployeeEvaluation> listData =
                    employeeIndicatorsService.getDataByEvaluationId(entity.getEmployeeEvaluationId(), isGetAll);

            EmployeeEvaluationsResponse.EmpData employee = new EmployeeEvaluationsResponse.EmpData();
            employee.setEmployeeId(entity.getEmployeeId());
            employee.setEmployeeName(entity.getEmployeeName());
            listEmp.add(employee);
            for (EmployeeIndicatorsResponse.EmployeeEvaluation item : listData) {
                Long indicatorConversionId = item.getIndicatorConversionId();

                EmployeeEvaluationsResponse.EvaluateBean evaluateBean = map.get(indicatorConversionId);
                if (evaluateBean == null) {
                    evaluateBean = new EmployeeEvaluationsResponse.EvaluateBean();
                    Utils.copyProperties(item, evaluateBean);
                    evaluateBean.setListEmp(new ArrayList<>());
                    map.put(indicatorConversionId, evaluateBean);
                }

                EmployeeEvaluationsResponse.EmpBean emp = new EmployeeEvaluationsResponse.EmpBean();
                emp.setEmployeeId(entity.getEmployeeId());
                emp.setEmployeeIndicatorId(item.getEmployeeIndicatorId());
                emp.setResult(item.getResult());
                emp.setResultManage(item.getResultManage());
                emp.setSelfPoint(item.getSelfPoint());
                emp.setManagePoint(item.getManagePoint());
                emp.setStatus(entity.getStatus());
                emp.setEmployeeEvaluationId(entity.getEmployeeEvaluationId());
                evaluateBean.getListEmp().add(emp);
            }
        }
        result.setListData(new ArrayList<>(map.values()));
        result.setListEmp(listEmp);
        return ResponseUtils.ok(result);
    }


    @Override
    @Transactional
    public ResponseEntity saveIndicatorData(EmployeeEvaluationsRequest.IndicatorSubmitForm dto, Long id) throws BaseAppException {
        List<Long> listId = dto.getEmployeeIndicatorList().stream()
                .map(it -> it.getEmployeeIndicatorId())
                .collect(Collectors.toList());
        EmployeeEvaluationsEntity entity;
        entity = employeeEvaluationsRepositoryJPA.getById(id);
        entity.setAdjustReason(dto.getAdjustReason());
        entity.setSelfTotalPoint(dto.getSelfTotalPoint());
        entity.setManagerTotalPoint(dto.getManagerTotalPoint());
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        if ("KHOI_TAO".equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        } else if (EmployeeEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || EmployeeEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DU_THAO);
        } else if ("PHE_DUYET".equalsIgnoreCase(entity.getStatus()) && !Utils.isNullOrEmpty(dto.getAdjustReason())) {
            entity.setStatus(Constant.STATUS.CHO_PHE_DUYET);
        } else if (Constant.STATUS.DANH_GIA.equalsIgnoreCase(entity.getStatus()) && "Y".equalsIgnoreCase(dto.getIsEvaluateManage())) {
            entity.setStatus(Constant.STATUS.QLTT_DANH_GIA);
        } else if (Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) && "Y".equalsIgnoreCase(dto.getIsEvaluate())) {
            entity.setStatus(Constant.STATUS.DANH_GIA);
        }
        employeeEvaluationsRepositoryJPA.save(entity);
        employeeIndicatorsService.deleteListData(listId, id, dto.getAdjustReason());
        for (EmployeeIndicatorsRequest.SubmitForm it : dto.getEmployeeIndicatorList()) {
            employeeIndicatorsService.saveData(it, it.getEmployeeIndicatorId(), dto.getAdjustReason());
        }
        //validate nghiep vu dang ky kpi
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity saveListEvaluate(EmployeeEvaluationsRequest.Evaluate dto) throws BaseAppException {
        List<Long> listEmpIndicatorId = dto.getListData().stream().map(EmployeeEvaluationsResponse.EmpBean::getEmployeeIndicatorId).toList();
        Map<Long, EmployeeIndicatorsEntity> entityMap = employeeEvaluationsRepository
                .findByListId(EmployeeIndicatorsEntity.class, listEmpIndicatorId)
                .stream()
                .collect(Collectors.toMap(
                        EmployeeIndicatorsEntity::getEmployeeIndicatorId,
                        entity -> entity
                ));
        List<EmployeeIndicatorsEntity> entitiesToUpdate = dto.getListData().stream()
                .map(empBean -> updateEntity(entityMap.get(empBean.getEmployeeIndicatorId()), empBean))
                .filter(Objects::nonNull)
                .toList();

        List<Long> listEmpEvaluateId = dto.getListSum().stream().map(EmployeeEvaluationsRequest.Total::getEmployeeEvaluationId).toList();
        Map<Long, EmployeeEvaluationsEntity> entityEvaluateMap = employeeEvaluationsRepository
                .findByListId(EmployeeEvaluationsEntity.class, listEmpEvaluateId)
                .stream()
                .collect(Collectors.toMap(
                        EmployeeEvaluationsEntity::getEmployeeEvaluationId,
                        entity -> entity
                ));

        List<EmployeeEvaluationsEntity> evaluationsToUpdate = dto.getListSum().stream()
                .map(total -> updateEvaluationEntity(entityEvaluateMap.get(total.getEmployeeEvaluationId()), total))
                .filter(Objects::nonNull)
                .toList();

        employeeEvaluationsRepository.updateBatch(EmployeeEvaluationsEntity.class, evaluationsToUpdate, true);
        employeeEvaluationsRepository.updateBatch(EmployeeIndicatorsEntity.class, entitiesToUpdate, true);
        return ResponseUtils.ok(listEmpIndicatorId);
    }

    private EmployeeIndicatorsEntity updateEntity(EmployeeIndicatorsEntity entity,
                                                  EmployeeEvaluationsResponse.EmpBean empBean) {
        if (entity == null) {
            return null;
        }

        entity.setResult(empBean.getResult());
        entity.setResultManage(empBean.getResultManage());
        entity.setSelfPoint(empBean.getSelfPoint());
        entity.setManagePoint(empBean.getManagePoint());
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());

        return entity;
    }

    private EmployeeEvaluationsEntity updateEvaluationEntity(EmployeeEvaluationsEntity entity, EmployeeEvaluationsRequest.Total total) {
        if (entity == null) return null;

        if (Constant.STATUS.DANH_GIA.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.QLTT_DANH_GIA);
        } else if (Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus())) {
            entity.setStatus(Constant.STATUS.DANH_GIA);
        }
        entity.setSelfTotalPoint(total.getTotalSelfPoint());
        entity.setManagerTotalPoint(total.getTotalManagePoint());
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        return entity;
    }

    @Override
    public ResponseEntity saveWorkPlanningData(EmployeeWorkPlanningsRequest.SubmitForm dto) throws BaseAppException {
//        EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(dto.getEmployeeEvaluationId());
//        entity.setAdjustReason(dto.getAdjustReason());
//        entity.setModifiedTime(new Date());
//        entity.setModifiedBy(Utils.getUserNameLogin());
//        if (Constant.STATUS.PHE_DUYET.equalsIgnoreCase(entity.getStatus()) && "Y".equalsIgnoreCase(dto.getIsEvaluate())) {
//            entity.setStatus(Constant.STATUS.DANH_GIA);
//        } else if ("KHOI_TAO".equalsIgnoreCase(entity.getStatus())) {
//            entity.setStatus(Constant.STATUS.DU_THAO);
//        } else if ("PHE_DUYET".equalsIgnoreCase(entity.getStatus()) && !Utils.isNullOrEmpty(dto.getAdjustReason())) {
//            entity.setStatus(Constant.STATUS.CHO_PHE_DUYET);
//        } else if (EmployeeEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET.equalsIgnoreCase(entity.getStatus()) || EmployeeEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
//            entity.setStatus(Constant.STATUS.DU_THAO);
//        }
//        employeeEvaluationsRepositoryJPA.save(entity);
        return employeeWorkPlanningsService.saveData(dto, dto.getEmployeeWorkPlanningId());
    }

    @Override
    public ResponseEntity getWorkPlanningById(Long id) throws BaseAppException {
        return employeeWorkPlanningsService.getDataByEvaluationId(id);
    }

    @Override
    @Transactional
    public boolean review(String type, EmployeeEvaluationsRequest.Review reviewRequest) {
        String userName = Utils.getUserNameLogin();
        HttpHeaders httpHeaders = Utils.getRequestHeader(request);
        reviewRequest.getIds().forEach(id -> {
            EmployeeEvaluationsEntity entity = employeeEvaluationsRepository.get(EmployeeEvaluationsEntity.class, id);
            //validate status
            if (!EmployeeEvaluationsEntity.STATUS.CHO_XET_DUYET.equalsIgnoreCase(entity.getStatus())) {
                throw new BaseAppException("status is invalid!");
            }
            ApprovalHistoryEntity approvalHistoryEntity = approvalHistoryRepositoryJPA.getWaitingApproval(ApprovalHistoryEntity.TABLE_NAMES.KPI_EMPLOYEE_EVALUATIONS, id);
            if (approvalHistoryEntity == null) {
                throw new BaseAppException("not exists record for approval");
            }
            List<Long> orgReviewIds = getOrgReviewIds(Utils.stringToListLong(entity.getOrgConcurrentIds(), ","), entity.getOrganizationId());

            List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(Scope.REVIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, userName);
            boolean hasPermission = false;
            for (PermissionDataDto item : permissionDataDtos) {
                if (item.getOrgIds() != null &&
                    organizationEvaluationsRepository.checkOrg(item.getOrgIds(), approvalHistoryEntity.getApprovalLevel())) {
                    hasPermission = true;
                }
            }
            // neu nhan vien kiem nhiem thi check theo quyen phe duyet
            if (!Utils.isNullOrEmpty(entity.getOrgConcurrentIds()) && !hasPermission) {
                permissionDataDtos = authorizationService.getPermissionData(Scope.APPROVE, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, userName);
                for (PermissionDataDto item : permissionDataDtos) {
                    if (item.getOrgIds() != null &&
                        organizationEvaluationsRepository.checkOrg(item.getOrgIds(), approvalHistoryEntity.getApprovalLevel())) {
                        hasPermission = true;
                    }
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
//                List<Long> orgIds = permissionFeignClient.getGrantedDomain(httpHeaders, Scope.REVIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, approvalHistoryEntity.getApprovalLevel()).getData();
                if (Utils.isNullOrEmpty(orgReviewIds)
                    || orgReviewIds.get(orgReviewIds.size() - 1).equals(approvalHistoryEntity.getApprovalLevel())
                ) {
                    //het cap xet duyet
                    entity.setStatus(EmployeeEvaluationsEntity.STATUS.CHO_PHE_DUYET);
                    entity.setModifiedBy(userName);
                    entity.setModifiedTime(new Date());
                    employeeEvaluationsRepositoryJPA.save(entity);
                } else {
                    //Tao moi cap xét duyệt mới
                    ApprovalHistoryEntity newEntity = new ApprovalHistoryEntity();
                    newEntity.setCreatedBy(userName);
                    newEntity.setCreatedTime(new Date());
                    newEntity.setStatus(ApprovalHistoryEntity.STATUS.WAITING);
                    newEntity.setApprovalLevel(orgReviewIds.get(orgReviewIds.indexOf(approvalHistoryEntity.getApprovalLevel()) + 1));
                    newEntity.setTableName(approvalHistoryEntity.getTableName());
                    newEntity.setObjectId(id);
                    newEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    approvalHistoryRepositoryJPA.save(newEntity);
                }
            } else {
                entity.setStatus(EmployeeEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET);
                entity.setModifiedBy(userName);
                entity.setModifiedTime(new Date());
                employeeEvaluationsRepositoryJPA.save(entity);
            }
        });


        return true;
    }

    @Override
    @Transactional
    public boolean sendForApproval(Long id, boolean isGetAll) {
        EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(id);
        String userName = Utils.getUserNameLogin();
        //validate status
        if (!EmployeeEvaluationsEntity.STATUS.DU_THAO.equalsIgnoreCase(entity.getStatus())) {
            throw new BaseAppException("status is invalid!");
        }
        if (!isGetAll) {
            isGetAll = authorizationService.hasPermissionWithOrg(entity.getOrganizationId(), Scope.VIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS);
        }
        List<EmployeeIndicatorsResponse.EmployeeEvaluation> listData = employeeIndicatorsService.getDataByEvaluationId(id, isGetAll);
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException("Chỉ tiêu đánh giá bắt buộc nhập!");
        }
        //neu nhan vien co cong tac kiem nhiem
        //thi don vi cong tac kiem nhiem can xet duyet kpi
//        List<Long> orgReviewIds = getOrgReviewIds(Utils.stringToListLong(entity.getOrgConcurrentIds(), ","), entity.getOrganizationId());
//
//        //update tat ca cac ban ghi cho xet duyet
//        approvalHistoryRepositoryJPA.inactiveOldData(ApprovalHistoryEntity.TABLE_NAMES.KPI_EMPLOYEE_EVALUATIONS, id);         //het cap xet duyet
        entity.setStatus(EmployeeEvaluationsEntity.STATUS.CHO_PHE_DUYET);
        entity.setModifiedBy(userName);
        entity.setModifiedTime(new Date());
        employeeEvaluationsRepositoryJPA.save(entity);

        return true;
    }

    private List<Long> getOrgReviewIds(List<Long> orgConcurrentIds, Long organizationId) {
        HttpHeaders httpHeaders = Utils.getRequestHeader(request);
        List<Long> orgReviewIds = new ArrayList<>();
        if (!Utils.isNullOrEmpty(orgConcurrentIds)) {
            for (Long orgId : orgConcurrentIds) {
                List<Long> orgIds = permissionFeignClient.getGrantedDomain(
                        httpHeaders, Scope.REVIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, orgId).getData();
                if (Utils.isNullOrEmpty(orgIds)) {
                    orgIds = permissionFeignClient.getGrantedDomain(
                            httpHeaders, Scope.APPROVE, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, orgId).getData();
                }
                //Chi lay don vi cap 2 tro xuong
                if (!Utils.isNullOrEmpty(orgIds)) {
                    for (Long pId : orgIds) {
                        if (!orgReviewIds.contains(pId)) {
                            OrganizationEntity organizationEntity = employeeEvaluationsRepository.get(OrganizationEntity.class, pId);
                            if (organizationEntity.getOrgLevelManage() == null || organizationEntity.getOrgLevelManage() > 1) {
                                orgReviewIds.add(pId);
                            }
                        }
                    }
                }
            }
        }

        List<Long> orgIds = permissionFeignClient.getGrantedDomain(
                httpHeaders, Scope.REVIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, organizationId
        ).getData();
        if (!Utils.isNullOrEmpty(orgIds)) {
            for (Long pId : orgIds) {
                if (!orgReviewIds.contains(pId)) {
                    orgReviewIds.add(pId);
                }
            }
        }
        return orgReviewIds;
    }

    @Override
    @Transactional
    public boolean approve(String type, EmployeeEvaluationsRequest.Review reviewRequest) {
        //check khong duoc phe duyet kpi cua truong don vi
        String userName = Utils.getUserNameLogin();
        HttpHeaders httpHeaders = Utils.getRequestHeader(request);
        reviewRequest.getIds().forEach(id -> {
            EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(id);
            //check xem nhan vien la truong don vi cua don vi nao
            Long organizationId = organizationEvaluationsRepository.getManagedOrganization(entity.getEmployeeId(), entity.getEvaluationPeriodId());
            if (organizationId != null) {
                //Lay quyen phe duyet cua user
                List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.APPROVE, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, Utils.getUserNameLogin());
                if (!organizationEvaluationsRepository.checkApprove(organizationId, orgIds)) {
                    throw new BaseAppException("Bạn không được phê duyệt KPI của trưởng đơn vị!");
                }
            }
            String orgParentNotApproved = organizationEvaluationsRepository.getParentNotApproved(entity.getOrganizationId(), entity.getEvaluationPeriodId());
            if (!Utils.isNullOrEmpty(orgParentNotApproved)) {
                throw new BaseAppException(String.format("Kpi đơn vị cấp trên: %s chưa được phê duyệt!", orgParentNotApproved));
            }

            //validate status
            if (!EmployeeEvaluationsEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatus())) {
                throw new BaseAppException("status is invalid!");
            }
            if (ApprovalHistoryEntity.STATUS.OK.equalsIgnoreCase(type)) {
                //thuc hien chuyen tiep sang ban ghi xet duyet tiep theo
                entity.setStatus(EmployeeEvaluationsEntity.STATUS.PHE_DUYET);
            } else {
                entity.setStatus(EmployeeEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET);
            }
            entity.setApprovedBy(userName);
            entity.setApprovedTime(new Date());
            employeeEvaluationsRepositoryJPA.save(entity);
        });
        return true;
    }

    @Override
    public void validatePermissionEvaluateManagement(Long employeeEvaluationId) {
        EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(employeeEvaluationId);
        EmployeesEntity employeesEntity = employeeEvaluationsRepository.get(EmployeesEntity.class, entity.getEmployeeId());
        if (employeesEntity.getEmployeeCode().equals(Utils.getUserNameLogin())) {
            throw new BaseAppException("Bạn không được nhập thông tin quản lý đánh giá cho chính mình!");
        }
        Long organizationId = organizationEvaluationsRepository.getManagedOrganization(entity.getEmployeeId(), entity.getEvaluationPeriodId());
        if (organizationId != null) {
            //Lay quyen phe duyet cua user
            List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.APPROVE, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, Utils.getUserNameLogin());
            if (!organizationEvaluationsRepository.checkApprove(organizationId, orgIds)) {
                throw new BaseAppException("Bạn không được nhập thông tin cấp trên đánh giá của trưởng đơn vị!");
            }
        }
    }

    @Override
    public EmployeeEvaluationsResponse.Validate validatePermissionUpdate(Long employeeEvaluationId) {
        EmployeeEvaluationsResponse.Validate result = new EmployeeEvaluationsResponse.Validate();
//        EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(employeeEvaluationId);
//        String ngayHetHan = objectAttributesService.getAttribute(entity.getEvaluationPeriodId(), "NGAY_KET_THUC_DIEU_CHINH", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
//        String ngayHetHanKHCT = objectAttributesService.getAttribute(entity.getEvaluationPeriodId(), "NGAY_KET_THUC_DIEU_CHINH_KHCT", employeeEvaluationsRepository.getSQLTableName(EvaluationPeriodsEntity.class));
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
//        result.setListAttributes(objectAttributesService.getAttributes(employeeEvaluationId, employeeEvaluationsRepository.getSQLTableName(EmployeeEvaluationsEntity.class)));
        return result;
    }

    @Override
    public String getCurrentJob(Long employeeEvaluationId) {
        EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(employeeEvaluationId);
        return employeeEvaluationsRepository.getJobType(entity.getEmployeeId());
    }

    @Override
    public ResponseEntity getEmpData(Long empId) {
//        ParameterDto dto = getParamaterData();
        EmployeeInfoDto data = employeeEvaluationsRepository.getEmployeeData(empId, new ParameterDto());
//        data.setPositionTitle(getPositionTitle(data));
        return ResponseUtils.ok(data);
    }

    public ParameterDto getParamaterData() {
        ParameterEntity parameterEntity = organizationEvaluationsRepository.getParameter("LIST_HEAD_ID", "PARAMETER_KPI");
        ParameterEntity parameterProbationaryEntity = organizationEvaluationsRepository.getParameter("ID_TAP_SU", "PARAMETER_KPI");
        ParameterEntity parameterJobFreeEntity = organizationEvaluationsRepository.getParameter("LIST_JOB_FREE_ALL", "PARAMETER_KPI");
        ParameterEntity parameterJobFreeByOrgEntity = organizationEvaluationsRepository.getParameter("LIST_JOB_FREE_BY_ORG", "PARAMETER_KPI");
        ParameterEntity parameterOrgEntity = organizationEvaluationsRepository.getParameter("LIST_ORG", "PARAMETER_KPI");
        List<String> listHeadId = !Utils.isNullOrEmpty(parameterEntity.getConfigValue()) ? Arrays.stream(parameterEntity.getConfigValue().split(",")).toList() : null;
        List<String> listProbationaryId = !Utils.isNullOrEmpty(parameterProbationaryEntity.getConfigValue()) ? Arrays.stream(parameterProbationaryEntity.getConfigValue().split(",")).toList() : null;
        List<String> listJobFreeId = !Utils.isNullOrEmpty(parameterJobFreeEntity.getConfigValue()) ? Arrays.stream(parameterJobFreeEntity.getConfigValue().split(",")).toList() : null;
        List<String> listJobFreeByOrgId = !Utils.isNullOrEmpty(parameterJobFreeByOrgEntity.getConfigValue()) ? Arrays.stream(parameterJobFreeByOrgEntity.getConfigValue().split(",")).toList() : null;
        List<String> listOrgId = !Utils.isNullOrEmpty(parameterOrgEntity.getConfigValue()) ? Arrays.stream(parameterOrgEntity.getConfigValue().split(",")).toList() : null;
        ParameterDto dto = new ParameterDto();
        dto.setListHeadId(listHeadId);
        dto.setListProbationaryId(listProbationaryId);
        dto.setListJobFreeId(listJobFreeId);
        dto.setListJobFreeByOrgId(listJobFreeByOrgId);
        dto.setListOrgId(listOrgId);
        return dto;
    }

    @Override
    public ResponseEntity getErrorWorkPlanning() {
        List<EmployeeWorkPlanningsEntity> listData = employeeEvaluationsRepository.getListEmpWorkPlanning();
        ParameterDto dto = getParamaterData();
        Map<Long, EmployeeInfoDto> empMap = employeeEvaluationsRepository.getMapEmployeeData(dto);
        List<EmployeeEvaluationsResponse.ErrorData> listError = new ArrayList<>();
        Map<Long, List<EmployeeIndicatorsResponse.EmployeeEvaluation>> empMapIndicator = employeeEvaluationsRepository.getMapEmpIndicator();
        Map<Long, List<EmployeeWorkPlanningsEntity>> mapTotalResult = new HashMap<>();
        for (EmployeeWorkPlanningsEntity data : listData) {
            List<EmployeeWorkPlanningsResponse.Content> contentList = Utils.fromJsonList(data.getContent(), EmployeeWorkPlanningsResponse.Content.class);
            EmployeeInfoDto empData = empMap.get(data.getEmployeeId());
            boolean isTeacher = "KHCT Vị trí việc làm Giảng viên".equals(data.getName().trim());
            double sum = 0d;
            long length = 0L;
            double sum1 = 0d;
            long length1 = 0L;
            double sum2 = 0d;
            long length2 = 0L;
            double sum3 = 0d;
            long length3 = 0L;
            Double totalPoint;
            boolean isExist = false;
            List<Long> empEvaluationIds = empMapIndicator.get(data.getEmployeeEvaluationId())
                    .stream()
                    .map(EmployeeIndicatorsResponse.EmployeeEvaluation::getIndicatorId)
                    .collect(Collectors.toList());
            if (!Utils.isNullOrEmpty(contentList)) {
                boolean isValid = true;
                for (EmployeeWorkPlanningsResponse.Content content : contentList) {
                    if (isTeacher) {
                        Double result = (!Utils.isNullOrEmpty(content.getResult()) && Utils.isNumeric(content.getResult())) ? Double.parseDouble(content.getResult()) : null;
                        if (!isInvalid(content.getListIdRelated(), empData.getIsJobFree(), empEvaluationIds) && result != null
                            && ((compareKeys(content.getKey(), "4") >= 0 && compareKeys(content.getKey(), "5") < 0) || (compareKeys(content.getKey(), "6") >= 0 && compareKeys(content.getKey(), "7") < 0))) {
                            isValid = false;
                        }
                    }
                }
                for (EmployeeWorkPlanningsResponse.Content content : contentList) {
                    Double selfPoint = null;
                    Double fullYear = (!Utils.isNullOrEmpty(content.getFullYear()) && Utils.isNumeric(content.getFullYear())) ? Double.parseDouble(content.getFullYear()) : null;
                    String unit = content.getUnit();
//                    Double result = (!Utils.isNullOrEmpty(content.getResult()) && Utils.isNumeric(content.getResult())) ? Double.parseDouble(content.getResult()) : null;
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
                        if (isTeacher) {
                            if (isValid || !isInvalid(content.getListIdRelated(), empData.getIsJobFree(), empEvaluationIds)) {
                                if ("Y".equals(empData.getIsProbationary())) {
                                    if ((compareKeys(content.getKey(), "4") >= 0 && compareKeys(content.getKey(), "5") < 0) || (compareKeys(content.getKey(), "6") >= 0 && compareKeys(content.getKey(), "7") < 0)) {
                                        sum += selfPoint;
                                        length++;
                                    } else if (compareKeys(content.getKey(), "8") >= 0 && compareKeys(content.getKey(), "10") < 0) {
                                        sum += selfPoint;
                                        length++;
                                    }
                                } else {
                                    if (compareKeys(content.getKey(), "1") >= 0 && compareKeys(content.getKey(), "4") < 0) {
                                        sum1 += selfPoint;
                                        length1++;
                                    } else if ((compareKeys(content.getKey(), "4") >= 0 && compareKeys(content.getKey(), "5") < 0) || (compareKeys(content.getKey(), "6") >= 0 && compareKeys(content.getKey(), "7") < 0)) {
                                        sum2 += selfPoint;
                                        length2++;
                                    } else if (compareKeys(content.getKey(), "8") >= 0 && compareKeys(content.getKey(), "10") < 0) {
                                        sum3 += selfPoint;
                                        length3++;
                                    }
                                }
                            } else {
                                isExist = true;
                            }
                        } else {
                            sum += selfPoint;
                            length++;
                        }
                    }

//                if (!Objects.equals(selfPoint, selfPointFinal)) {
//                    listError.add(data.getEmployeeCode());
//                    break;
//                }
                }
            }
            if (isTeacher && "N".equals(empData.getIsProbationary())) {
                totalPoint = (length1 > 0 ? (sum1 / length1) * 0.5 : 0) + (length2 > 0 ? (sum2 / length2) * 0.4 : 0) + (length3 > 0 ? (sum3 / length3) * 0.1 : 0);
            } else {
                totalPoint = length > 0 ? sum / length : 0;
            }
            data.setTotalPoint(totalPoint);
            data.setIsExist(isExist);
            mapTotalResult.computeIfAbsent(data.getEmployeeEvaluationId(), k -> new ArrayList<>());
            mapTotalResult.get(data.getEmployeeEvaluationId()).add(data);
        }

        List<EmployeeIndicatorsEntity> listUpdated = new ArrayList<>();

        for (Map.Entry<Long, List<EmployeeWorkPlanningsEntity>> entry : mapTotalResult.entrySet()) {
            Long employeeEvaluationId = entry.getKey();
            List<EmployeeWorkPlanningsEntity> dataList = entry.getValue();
            List<EmployeeIndicatorsResponse.EmployeeEvaluation> empIndicatorData = empMapIndicator.get(employeeEvaluationId);
            double total = 0;
            boolean isExist = false;
            if (dataList.size() > 1) {
                for (EmployeeWorkPlanningsEntity d : dataList) {
                    if ("KHCT Vị trí việc làm Giảng viên".equals(d.getName().trim()) && d.getIsExist()) {
                        isExist = true;
                    }
                    total += d.getTotalPoint() * d.getPercent() / 100;
                }
            } else {
                total = dataList.get(0).getTotalPoint();
            }
            total = Utils.round(total, 2);
            if (!Utils.isNullOrEmpty(empIndicatorData)) {
                for (EmployeeIndicatorsResponse.EmployeeEvaluation item : empIndicatorData) {
                    if ("Y".equals(item.getIsWorkPlanningIndex()) && "N".equals(item.getIsOrg())) {
//                        Double result = !Utils.isNullOrEmpty(item.getResult()) ? Double.parseDouble(item.getResult()) : null;
                        Double result = !Utils.isNullOrEmpty(item.getResult()) ? Double.parseDouble(item.getResult()) : null;
                        if (total != result) {
                            EmployeeIndicatorsEntity entity = new EmployeeIndicatorsEntity();
                            entity.setEmployeeIndicatorId(item.getEmployeeIndicatorId());
                            entity.setResult(String.valueOf(total));
//                            entity.setResultManage(String.valueOf(total));
                            listUpdated.add(entity);
                            EmployeeEvaluationsResponse.ErrorData errorData = new EmployeeEvaluationsResponse.ErrorData();
                            errorData.setEmployeeName(dataList.get(0).getFullName());
                            errorData.setEmployeeCode(dataList.get(0).getEmployeeCode());
                            errorData.setBeforeResult(result);
                            errorData.setAfterResult(total);
                            listError.add(errorData);
                        }
                    }
                }
            }


        }

//        employeeEvaluationsRepository.updateBatch(EmployeeIndicatorsEntity.class, listUpdated, false);


        return ResponseUtils.ok(listError);
    }

    public boolean isInvalid(List<Long> listIdRelated, String isJobFree, List<Long> empEvaluationIds) {
        if (!Utils.isNullOrEmpty(listIdRelated) && "N".equals(isJobFree)) {
            return empEvaluationIds.stream()
                    .anyMatch(listIdRelated::contains);
        }
        return false;
    }

    @Override
    public List calculateKpiPoints(Long employeeEvaluationId) {
        List<Map<String, Object>> listValues = employeeEvaluationsRepository.getListValues();
        record KpiThreshold(String expression, String label) {
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> values = listValues.get(i);
            List<KpiThreshold> rules = new ArrayList<>();
            chuanHoa(values);
            if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_1"))) {
                rules.add(new KpiThreshold((String) values.get("GIA_TRI_1"), "1"));
            }
            if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_2"))) {
                rules.add(new KpiThreshold((String) values.get("GIA_TRI_2"), "2"));
            }
            if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_3"))) {
                rules.add(new KpiThreshold((String) values.get("GIA_TRI_3"), "3"));
            }
            if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_4"))) {
                rules.add(new KpiThreshold((String) values.get("GIA_TRI_4"), "4"));
            }
            if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_5"))) {
                rules.add(new KpiThreshold((String) values.get("GIA_TRI_5"), "5"));
            }
            String result = (String) values.get("RESULT");
            if (!Utils.isNullOrEmpty(result)) {
                Object value = result;
                if ("PERCENT".equals(values.get("RATING_TYPE")) && !"Y".equalsIgnoreCase((String) values.get("LA_TRUONG_DON_VI"))) {
                    value = Double.valueOf(result) * 100 / Double.valueOf(values.get("TARGET").toString());
                } else if (!"SELECT".equals(values.get("RATING_TYPE"))) {
                    value = Double.valueOf(result);
                } else {
                    try {
                        value = Double.valueOf(result);
                    } catch (NumberFormatException e) {

                    }
                }
                final Object tempValue = value;
                String matchedLabel = rules.stream()
                        .filter(rule -> evaluateExpression(rule.expression(), tempValue))
                        .map(KpiThreshold::label)
                        .findFirst()
                        .orElse("N/A");
                if (!matchedLabel.equals(values.get("SELF_POINT").toString())) {
                    log.error("", values);
                    values.put("ket_qua_danh_gia", matchedLabel);
                    results.add(values);
                }
            }
        }
        return results;
    }

    private void chuanHoa(Map<String, Object> values) {
        if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_1"))) {
            values.put("GIA_TRI_1", chuanHoa((String) values.get("GIA_TRI_1"), values));
        }
        if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_2"))) {
            values.put("GIA_TRI_2", chuanHoa((String) values.get("GIA_TRI_2"), values));
        }
        if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_3"))) {
            values.put("GIA_TRI_3", chuanHoa((String) values.get("GIA_TRI_3"), values));
        }
        if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_4"))) {
            values.put("GIA_TRI_4", chuanHoa((String) values.get("GIA_TRI_4"), values));
        }
        if (!Utils.isNullOrEmpty((String) values.get("GIA_TRI_5"))) {
            values.put("GIA_TRI_5", chuanHoa((String) values.get("GIA_TRI_5"), values));
        }
    }

    private String chuanHoa(String giaTri1, Map<String, Object> values) {
        String result = giaTri1;
        String mucNguong = (String) values.get("MUC_NGUONG");
        if (!Utils.isNullOrEmpty(mucNguong)) {
            if (mucNguong.contains(">=")) {
                mucNguong = mucNguong.replace(">=", "");
            }
            if (mucNguong.contains("<")) {
                mucNguong = mucNguong.replace("<", "");
            }
            result = result.replace("Mức ngưỡng", mucNguong);
        }
        String mucCoban = (String) values.get("MUC_CO_BAN");
        if (!Utils.isNullOrEmpty(mucCoban)) {
            if (mucCoban.contains(">=")) {
                mucCoban = mucCoban.replace(">=", "");
            }
            if (mucCoban.contains("<")) {
                mucCoban = mucCoban.replace("<", "");
            }
            result = result.replace("Mức cơ bản", mucCoban);
        }
        String mucDayManh = (String) values.get("MUC_DAY_MANH");
        if (!Utils.isNullOrEmpty(mucDayManh)) {
            if (mucDayManh.contains(">=")) {
                mucDayManh = mucDayManh.replace(">=", "");
            }
            if (mucDayManh.contains("<")) {
                mucDayManh = mucDayManh.replace("<", "");
            }
            result = result.replace("Mức đẩy mạnh", mucDayManh);
        }
        return result;
    }

    public boolean evaluateExpression(String expr, Object result) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("x", result);  // Gán biến x cho biểu thức

        try {
            Expression exp = parser.parseExpression(expr);
            return exp.getValue(context, Boolean.class);
        } catch (Exception ex) {
            log.error("###################");
            log.error(expr);
            log.error("", result);
            log.error("###################");
        }
        return false;
    }

    public int compareKeys(String a, String b) {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");

        int maxLength = Math.max(aParts.length, bParts.length);

        for (int i = 0; i < maxLength; i++) {
            int numA = i < aParts.length ? Integer.parseInt(aParts[i]) : 0;
            int numB = i < bParts.length ? Integer.parseInt(bParts[i]) : 0;

            if (numA > numB) return 1;
            if (numA < numB) return -1;
        }

        return 0;
    }

    private String getPositionTitle(EmployeeInfoDto employeeInfoDto) {
        if (employeeInfoDto == null) {
            return "";
        }
        StringBuilder positionTitle = new StringBuilder(Utils.join(", ", employeeInfoDto.getPromotionRankName(),
                employeeInfoDto.getMajorLevelName(),
                employeeInfoDto.getPositionName())
        );
        positionTitle.append(" " + employeeInfoDto.getOrganizationName());

        List<String> positionNames = new ArrayList<>();
        List<ConcurrentProcessDto> listCurrent = employeeEvaluationsRepository.getListProcess(employeeInfoDto.getEmployeeId(), new Date());
        if (!listCurrent.isEmpty()) {
            listCurrent.forEach(dto -> {
                positionNames.add(Utils.join(" ", dto.getJobName(), dto.getOrganizationName()));
            });
        }
        if (!Utils.isNullOrEmpty(positionNames)) {
            positionTitle.append(", Kiêm ").append(Utils.join(", ", positionNames));
        }
        return positionTitle.toString();
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
