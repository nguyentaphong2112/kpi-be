package vn.kpi.services.impl;

import com.jxcell.CellException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.kpi.configs.MdcForkJoinPool;
import vn.kpi.feigns.FileStorageFeignClient;
import vn.kpi.feigns.ReportFeignClient;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.repositories.impl.UtilsRepository;
import vn.kpi.services.ReportService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ExportWorld;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportFeignClient reportFeignClient;
    private final FileStorageFeignClient fileStorageFeignClient;
    private final HttpServletRequest request;
    private final UtilsRepository utilsRepository;
    private final MdcForkJoinPool forkJoinPool;

    @Override
    public ExportExcel exportExcel(String reportCode, Map mapParams) throws ExecutionException, InterruptedException {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request),
                reportCode);
        return exportExcel(response.getData(), mapParams);

    }

    private ExportExcel exportExcel(ReportConfigDto reportConfigDto, Map mapParams) throws ExecutionException, InterruptedException {
        List<List<Map<String, Object>>> queryDataList = getDataToReport(mapParams, reportConfigDto);

        byte[] bytes = fileStorageFeignClient.downloadFile(Utils.getRequestHeader(request), "admin", reportConfigDto.getAttachmentFileList().get(0).getFileId());
        ExportExcel exportExcel = new ExportExcel(new ByteArrayInputStream(bytes), 1, true);
        queryDataList.forEach(data -> {
            try {
                exportExcel.replaceKeys(data);
            } catch (CellException e) {
                throw new RuntimeException(e);
            }
        });
        return exportExcel;
    }

    private List<List<Map<String, Object>>> getDataToReport(Map mapParams, ReportConfigDto reportConfigDto) throws InterruptedException, ExecutionException {
        Map<String, Object> mapParamToQuery = getMapParamToQuery(mapParams, reportConfigDto.getParametersResponseList());
        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        reportConfigDto.getQueryResponseList().forEach(item -> {
            Supplier<Object> supplier = () -> executeQuery(item.getSqlQuery(), mapParamToQuery, reportConfigDto.getParametersResponseList());
            completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        });

        CompletableFuture<Void> allReturns = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFeatures = allReturns.thenApply(item -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<Object> listData = allFeatures.get();
        List<List<Map<String, Object>>> queryDataList = listData.stream().map(item -> (List<Map<String, Object>>) item).toList();
        return queryDataList;
    }

    private Map<String, Object> getMapParamToQuery(Map params, List<ReportConfigDto.Parameter> parametersResponseList) {
        Map<String, Object> mapResult = new HashMap<>();
        parametersResponseList.forEach(item -> {
            if (params.get(item.getName()) != null && params.get(item.getName()) instanceof String) {
                String value = (String) params.get(item.getName());
                switch (item.getDataType()) {
                    case DYNAMIC_REPORT_DATA_TYPE.ORG:
                    case DYNAMIC_REPORT_DATA_TYPE.LONG:
                    case DYNAMIC_REPORT_DATA_TYPE.LIST:
                    case DYNAMIC_REPORT_DATA_TYPE.EMP:
                        mapResult.put(item.getName(), Long.parseLong(value));
                        break;
                    case DYNAMIC_REPORT_DATA_TYPE.DOUBLE:
                        BigDecimal decimal = new BigDecimal(value);
                        mapResult.put(item.getName(), decimal.doubleValue());
                        break;
                    case DYNAMIC_REPORT_DATA_TYPE.DATE:
                        mapResult.put(item.getName(), Utils.stringToDate(value));
                        break;
                    case DYNAMIC_REPORT_DATA_TYPE.MULTI_LIST:
                    case DYNAMIC_REPORT_DATA_TYPE.MULTI_ORG:
                        mapResult.put(item.getName(), Utils.stringToListLong(value, ","));
                        break;
                    case DYNAMIC_REPORT_DATA_TYPE.STRING:
                    default:
                        mapResult.put(item.getName(), value);
                        break;
                }
            } else if (params.get(item.getName()) != null) {
                mapResult.put(item.getName(), params.get(item.getName()));
            }
        });
        return mapResult;
    }

    @Override
    public ExportWorld exportWord(String reportCode, Map mapParams) throws Exception {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request),
                reportCode);
        return exportWord(response.getData(), mapParams);
    }

    private ExportWorld exportWord(ReportConfigDto reportConfigDto, Map mapParams) throws Exception {
        List<List<Map<String, Object>>> queryDataList = getDataToReport(mapParams, reportConfigDto);

        byte[] bytes = fileStorageFeignClient.downloadFile(Utils.getRequestHeader(request), "admin", reportConfigDto.getAttachmentFileList().get(0).getFileId());
        ExportWorld exportWorld = new ExportWorld(new ByteArrayInputStream(bytes));
        queryDataList.forEach(data -> {
            try {
                exportWorld.replaceKeys(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return exportWorld;
    }

    @Override
    public ResponseEntity export(String reportCode, Map<String, String> params) throws Exception {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request),
                reportCode);
        ReportConfigDto reportConfigDto = response.getData();
        if (reportConfigDto.getReportType().equalsIgnoreCase("doc")) {
            return ResponseUtils.ok(exportWord(reportConfigDto, params), Utils.removeSign(reportConfigDto.getName()) + ".docx");
        } else if (reportConfigDto.getReportType().equalsIgnoreCase("excel")) {
            return ResponseUtils.ok(exportExcel(reportConfigDto, params), Utils.removeSign(reportConfigDto.getName()) + ".xlsx");
        } else {
            return ResponseUtils.getResponseFileEntity(fileStorageFeignClient.downloadFile(Utils.getRequestHeader(request), "admin", reportConfigDto.getAttachmentFileList().get(0).getFileId()),Utils.removeSign(reportConfigDto.getName()) + ".pdf");
        }
    }

    private List<Map<String, Object>> executeQuery(String query, Map<String, Object> params, List<ReportConfigDto.Parameter> parametersEntityList) {
        StringBuilder sql = new StringBuilder(query);
        parametersEntityList.forEach(item -> {
            if (StringUtils.isNotBlank(item.getAppendQuery())
                    && params.get(item.getName()) != null) {
                sql.append(" " + item.getAppendQuery());
            }
        });
        List results = utilsRepository.getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(utilsRepository.getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }

    public interface DYNAMIC_REPORT_DATA_TYPE {
        String LONG = "LONG";
        String DOUBLE = "DOUBLE";
        String STRING = "STRING";
        String LIST = "LIST";
        String MULTI_LIST = "MULTI_LIST";
        String EMP = "EMP";
        String ORG = "ORG";
        String MULTI_ORG = "MULTI_ORG";
        String DATE = "DATE";
    }
}
