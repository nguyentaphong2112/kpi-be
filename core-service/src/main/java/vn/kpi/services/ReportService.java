package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ExportWorld;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface ReportService {
    ExportExcel exportExcel(String reportCode, Map mapParams) throws ExecutionException, InterruptedException;
    ExportWorld exportWord(String reportCode, Map mapParams) throws Exception;

    ResponseEntity export(String id, Map<String, String> params) throws Exception;
}
