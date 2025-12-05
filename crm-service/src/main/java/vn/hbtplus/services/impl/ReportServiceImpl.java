package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.feigns.ReportFeignClient;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.ReportConfigDto;
import vn.hbtplus.models.request.CustomersRequest;
import vn.hbtplus.repositories.impl.ReportRepository;
import vn.hbtplus.services.ReportService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportFeignClient reportFeignClient;
    private final FileStorageFeignClient fileStorageFeignClient;
    private final HttpServletRequest request;
    private final ReportRepository reportRepository;

    @Override
    public ResponseEntity<Object> exportData(String reportType, CustomersRequest.ReportForm dto) throws Exception {
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

    private Map<String, Object> getReportParameter(CustomersRequest.ReportForm dto) {
        Map map = new HashMap();
        map.put("ky_bao_cao", String.format("Từ ngày %s đến ngày %s", Utils.formatDate(dto.getStartDate()), Utils.formatDate(dto.getEndDate())));
        map.put("ngay_bao_cao", Utils.formatDate(new Date()));
        return map;
    }

    private List<Map<String, Object>> getDataToReport(CustomersRequest.ReportForm dto, ReportConfigDto reportConfigDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", dto.getStartDate());
        params.put("endDate", dto.getEndDate());
        params.put("customerId", dto.getCustomerId());
        String sqlQuery = reportConfigDto.getQueryResponseList().get(0).getSqlQuery();
        String sqlSelect = sqlQuery.substring(0, sqlQuery.toLowerCase().lastIndexOf(" order by"));
        String orderBy = sqlQuery.substring(sqlQuery.toLowerCase().lastIndexOf(" order by"));

        StringBuilder sql = new StringBuilder(sqlSelect);
        if(dto.getCustomerId() != null){
            sql.append(" and c.customer_id = :customerId");
        }
        sql.append(orderBy);

        List results = reportRepository.getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(reportRepository.getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }
}
