package vn.hbtplus.services.impl;

import com.jxcell.CellException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.feigns.ReportFeignClient;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.ReportConfigDto;
import vn.hbtplus.models.dto.ExpOrganizationDto;
import vn.hbtplus.models.dto.OrganizationDto;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.EmpTypesEntity;
import vn.hbtplus.repositories.impl.REmployeeStatisticRepository;
import vn.hbtplus.services.REmployeeService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class REmployeeServiceImpl implements REmployeeService {
    private final MdcForkJoinPool forkJoinPool;
    private final REmployeeStatisticRepository rEmployeeStatisticRepository;
    private final ReportFeignClient reportFeignClient;
    private final FileStorageFeignClient fileStorageFeignClient;
    private final HttpServletRequest request;

    @Override
    public ResponseEntity<Object> exportData(String reportType, EmployeesRequest.ReportForm dto) throws Exception {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request),
                reportType);
        ReportConfigDto reportConfigDto = response.getData();

        List<Map<String, Object>> queryDataList = getDataToReport(dto, reportConfigDto);

        byte[] bytes = fileStorageFeignClient.downloadFile(Utils.getRequestHeader(request), "admin", reportConfigDto.getAttachmentFileList().get(0).getFileId());
        ExportExcel exportExcel = new ExportExcel(new ByteArrayInputStream(bytes), 1, true);
        exportExcel.replaceKeys(queryDataList);

        exportExcel.replaceKeys(getReportParameter(dto));
        return ResponseUtils.ok(exportExcel, "bao-cao.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportStatisticReport(EmployeesRequest.ReportForm dto) throws Exception {
        ExportExcel exportExcel = initStatisticReportTemplate();

        int col = 3;
        List<CategoryEntity> listGenders = rEmployeeStatisticRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.GIOI_TINH, "orderNumber");
        List<CategoryEntity> listEduPromotions = rEmployeeStatisticRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HOC_HAM, "orderNumber");
        List<CategoryEntity> listMajorLevels = rEmployeeStatisticRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO, "orderNumber");
        List<CategoryEntity> listNhomViTris = rEmployeeStatisticRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.PHAN_NHOM_VI_TRI, "orderNumber");
        List<CategoryEntity> listHangVienChuc = rEmployeeStatisticRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HANG_CHUC_DANH, "orderNumber");
        List<EmpTypesEntity> listEmpTypes = rEmployeeStatisticRepository.findByProperties(EmpTypesEntity.class, "orderNumber");

        col = setReportStatisticHeader("Giới tính", listGenders, col, exportExcel);
        col = setReportStatisticHeader("Đối tượng", listEmpTypes, col, exportExcel);
        col = setReportStatisticHeader("Trình độ chuyên môn", listMajorLevels, col, exportExcel);
        col = setReportStatisticHeader("Học hàm", listEduPromotions, col, exportExcel);
        col = setReportStatisticHeader("Phân theo vị trí việc làm", listNhomViTris, col, exportExcel);
        col = setReportStatisticHeader("Phân theo theo hạng viên chức", listHangVienChuc, col, exportExcel);
        exportExcel.setText("Là Đảng viên", col, 4);
        exportExcel.mergeCell(4, col, 5, col);


        List<OrganizationDto> listOrganizations = rEmployeeStatisticRepository.getListOrgExport(dto);
        List<Long> orgIds = listOrganizations.stream()
                .map(OrganizationDto::getOrganizationId)
                .collect(Collectors.toList());
        Map<String, Object> mapStatistic = getStatisticReport(orgIds, dto);
        ExpOrganizationDto expOrganizationDto = new ExpOrganizationDto();
        for (OrganizationDto orgDto : listOrganizations) {
            col = 0;
            expOrganizationDto.addOrg(orgDto);
            exportExcel.setText(expOrganizationDto.getIdx(), col++);
            exportExcel.setText(orgDto.getName(), col++);
            exportExcel.setFormula(MessageFormat.format("SUM({0}{2}:{1}{2})",
                    exportExcel.convertColumnIndexToLabel(col + 1),
                    exportExcel.convertColumnIndexToLabel(col + listGenders.size()),
                    String.valueOf(exportExcel.getLastRow() + 1)), col++);

            col = setReportStatisticData(orgDto.getOrganizationId(),
                    (Map<String, Integer>) mapStatistic.get("gender"), listGenders, col, exportExcel);
            col = setReportStatisticData(orgDto.getOrganizationId(),
                    (Map<String, Integer>) mapStatistic.get("empType"), listEmpTypes, col, exportExcel);
            col = setReportStatisticData(orgDto.getOrganizationId(),
                    (Map<String, Integer>) mapStatistic.get("eduMajorLevels"), listMajorLevels, col, exportExcel);
            col = setReportStatisticData(orgDto.getOrganizationId(),
                    (Map<String, Integer>) mapStatistic.get("eduPromotions"), listEduPromotions, col, exportExcel);
            col = setReportStatisticData(orgDto.getOrganizationId(),
                    (Map<String, Integer>) mapStatistic.get("position"), listNhomViTris, col, exportExcel);
            col = setReportStatisticData(orgDto.getOrganizationId(),
                    (Map<String, Integer>) mapStatistic.get("positionRank"), listHangVienChuc, col, exportExcel);
            col = setReportStatisticData(orgDto.getOrganizationId(),
                    (Map<String, Integer>) mapStatistic.get("partyMember"), Arrays.asList("X"), col, exportExcel);
            exportExcel.increaseRow();
        }
        exportExcel.setCellFormat(4, 0, exportExcel.getLastRow(), col - 1, ExportExcel.BORDER_FORMAT);
        exportExcel.replaceKeys(getReportParameter(dto));
        return ResponseUtils.ok(exportExcel, "bao-cao-co-cau-lao-dong.xlsx");
    }

    private Map<String, Object> getStatisticReport(List<Long> orgIds, EmployeesRequest.ReportForm dto) throws ExecutionException, InterruptedException {
        Map<String, CompletableFuture<Object>> completableFutureMap = new HashMap<>();

        completableFutureMap.put("gender", CompletableFuture.supplyAsync(()
                -> rEmployeeStatisticRepository.getStatisticByGender(orgIds, dto), forkJoinPool));
        completableFutureMap.put("empType", CompletableFuture.supplyAsync(()
                -> rEmployeeStatisticRepository.getStatisticByEmpType(orgIds, dto), forkJoinPool));
        completableFutureMap.put("eduMajorLevels", CompletableFuture.supplyAsync(()
                -> rEmployeeStatisticRepository.getStatisticByMajorLevel(orgIds, dto), forkJoinPool));
        completableFutureMap.put("eduPromotions", CompletableFuture.supplyAsync(()
                -> rEmployeeStatisticRepository.getStatisticByEduPromotion(orgIds, dto), forkJoinPool));
        completableFutureMap.put("position", CompletableFuture.supplyAsync(()
                -> rEmployeeStatisticRepository.getStatisticByPosition(orgIds, dto), forkJoinPool));
        completableFutureMap.put("positionRank", CompletableFuture.supplyAsync(()
                -> rEmployeeStatisticRepository.getStatisticByPositionRank(orgIds, dto), forkJoinPool));
        completableFutureMap.put("partyMember", CompletableFuture.supplyAsync(()
                -> rEmployeeStatisticRepository.getStatisticByPartyMember(orgIds, dto), forkJoinPool));
        CompletableFuture<Void> allReturns = CompletableFuture.allOf(completableFutureMap.values().toArray(new CompletableFuture[0]));
        CompletableFuture<Map<String, Object>> allFeatures = allReturns.thenApply(item -> {
            return completableFutureMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().join()));
        });

        Map<String, Object> map = allFeatures.get();

        return map;
    }

    private int setReportStatisticData(Long orgId, Map<String, Integer> mapReports, List headers, int col, ExportExcel exportExcel) throws CellException {
        if (mapReports != null) {
            for (Object entity : headers) {
                String key = orgId + "-";
                if (entity instanceof CategoryEntity) {
                    key = key + ((CategoryEntity) entity).getValue();
                } else if (entity instanceof EmpTypesEntity) {
                    key = key + ((EmpTypesEntity) entity).getEmpTypeId();
                } else {
                    key = key + entity;
                }
                exportExcel.setEntry(Utils.formatNumber(mapReports.get(key)), col);
                col++;
            }
        }
        return col;
    }

    private int setReportStatisticHeader(String title, List headers, int col, ExportExcel exportExcel) throws CellException {
        int startHearder = 4;
        exportExcel.setText(title, col, startHearder);
        exportExcel.mergeCell(startHearder, col, startHearder, col + headers.size() - 1);
        for (Object entity : headers) {
            if (entity instanceof CategoryEntity) {
                exportExcel.setText(((CategoryEntity) entity).getName(), col, startHearder + 1);
            }
            if (entity instanceof EmpTypesEntity) {
                exportExcel.setText(((EmpTypesEntity) entity).getName(), col, startHearder + 1);
            }
            col++;
        }
        return col;
    }

    private ExportExcel initStatisticReportTemplate() {
        try {
            BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                    Utils.getRequestHeader(request),
                    "CO_CAU_LAO_DONG");
            ReportConfigDto reportConfigDto = response.getData();
            byte[] bytes = fileStorageFeignClient.downloadFile(Utils.getRequestHeader(request), "admin", reportConfigDto.getAttachmentFileList().get(0).getFileId());
            //xu ly load du lieu
            return new ExportExcel(new ByteArrayInputStream(bytes), 1, true);
        } catch (Exception ex) {
            return new ExportExcel("template/export/employee/BM-bao-cao-co-cau-lao-dong.xlsx", 7, true);
        }

    }

    private Map<String, Object> getReportParameter(EmployeesRequest.ReportForm dto) {
        Map map = new HashMap();
        if (dto.getTypeReportPeriod().equals(EmployeesRequest.PERIOD_TYPES.MONTH)) {
            map.put("ky_bao_cao", String.format("Tháng %s", dto.getMonth()));
        } else {
            map.put("ky_bao_cao", String.format("Từ ngày %s đến ngày %s", Utils.formatDate(dto.getStartDate()), Utils.formatDate(dto.getEndDate())));
        }
        map.put("ngay_bao_cao", Utils.formatDate(new Date()));
        return map;
    }

    private List<Map<String, Object>> getDataToReport(EmployeesRequest.ReportForm dto, ReportConfigDto reportConfigDto) {
        Map<String, Object> params = new HashMap<>();
        if (EmployeesRequest.PERIOD_TYPES.MONTH.equalsIgnoreCase(dto.getTypeReportPeriod())) {
            params.put("startDate", Utils.getFirstDay(Utils.stringToDate(dto.getMonth(), "MM/yyyy")));
            params.put("endDate", Utils.getLastDay(Utils.stringToDate(dto.getMonth(), "MM/yyyy")));
        } else {
            params.put("startDate", dto.getStartDate());
            params.put("endDate", dto.getEndDate());
        }
        params.put("organizationId", dto.getOrganizationId());
        params.put("empTypeIds", dto.getEmpTypeIds());
        StringBuilder sql = new StringBuilder(reportConfigDto.getQueryResponseList().get(0).getSqlQuery());
        reportConfigDto.getParametersResponseList().forEach(item -> {
            if (StringUtils.isNotBlank(item.getAppendQuery())
                && params.get(item.getName()) != null
            ) {
                sql.append(" " + item.getAppendQuery());
            }
        });
        sql.append(" order by org.path_order, mj.order_number, e.employee_code");
        //xu ly add them dieu kien phan quyen


        List results = rEmployeeStatisticRepository.getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(rEmployeeStatisticRepository.getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }
}
