package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Locale;
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
        map.put("ky_bao_cao", String.format("Từ năm %d đến năm %d", dto.getStartDate(), dto.getEndDate()));
        map.put("ngay_bao_cao", Utils.formatDate(new Date()));
        return map;
    }

    private List<Map<String, Object>> getDataToReport(CustomersRequest.ReportForm dto, ReportConfigDto reportConfigDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", Utils.stringToDate("01/01/" +  dto.getStartDate()));
        params.put("endDate", Utils.stringToDate("31/12/" +  dto.getEndDate()));
        params.put("startYear", dto.getStartDate());
        params.put("endYear", dto.getEndDate());
        params.put("organizationId", dto.getOrganizationId());
        params.put("keySearch", Utils.NVL(dto.getKeySearch(),""));

        String sqlQuery = reportConfigDto.getQueryResponseList().get(0).getSqlQuery();
        String sqlLower = sqlQuery.toLowerCase(Locale.ROOT);

        // 👉 Tìm vị trí outermost GROUP BY và ORDER BY
        int groupIndex = findOuterMostKeyword(sqlLower, "group by");
        int orderIndex = findOuterMostKeyword(sqlLower, "order by");

        // Xác định phần tách
        int splitIndex = -1;
        if (groupIndex != -1 && orderIndex != -1) {
            splitIndex = Math.min(groupIndex, orderIndex); // từ khóa nào đến trước thì cắt tại đó
        } else if (groupIndex != -1) {
            splitIndex = groupIndex;
        } else if (orderIndex != -1) {
            splitIndex = orderIndex;
        }

        String mainQuery;
        String tailClause = "";

        if (splitIndex != -1) {
            mainQuery = sqlQuery.substring(0, splitIndex);
            tailClause = sqlQuery.substring(splitIndex);
        } else {
            mainQuery = sqlQuery;
        }

        StringBuilder sql = new StringBuilder(mainQuery.trim());

        // Thêm appendQuery vào phần thân
        reportConfigDto.getParametersResponseList().forEach(item -> {
            if (StringUtils.isNotBlank(item.getAppendQuery()) && params.get(item.getName()) != null) {
                sql.append(" ").append(item.getAppendQuery()).append(" ");
            }
        });

        // Ghép lại phần GROUP/ORDER BY ngoài cùng
        if (StringUtils.isNotBlank(tailClause)) {
            sql.append(" ").append(tailClause);
        }

        List results = reportRepository.getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(reportRepository.getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }

    /**
     * Tìm vị trí từ khóa ngoài cùng (ví dụ: GROUP BY, ORDER BY)
     */
    private int findOuterMostKeyword(String sqlLower, String keyword) {
        int len = sqlLower.length();
        int parenDepth = 0;

        for (int i = 0; i < len - keyword.length(); i++) {
            char c = sqlLower.charAt(i);
            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth = Math.max(0, parenDepth - 1);
            } else if (parenDepth == 0 && sqlLower.startsWith(keyword, i)) {
                return i;
            }
        }
        return -1;
    }


}
