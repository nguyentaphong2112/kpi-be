package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ImportExcel;

import java.util.List;
import java.util.Map;

public interface UtilsService {
    ResponseEntity<Object> downloadDatabaseStructure(List<String> tables) throws Exception;

    ResponseEntity<Object> validateFileImport(ImportExcel importExcel, MultipartFile file, List<Object[]> dataList);

    List<Map<String, Object>> getSysdate();

    List<String> initDto(String sql);

    ResponseEntity<Object> responseErrorImportFile(ImportExcel importExcel, MultipartFile file);

    ResponseEntity<Object> testExportDoc() throws Exception;

    Integer convertUTF8ColumnDatabase(String tableName, String idColumn, String columnName, boolean normalize);

    String jasyptEncrypt(String inputText);

    String jasyptDecrypt(String inputText);

    boolean hasRole(String... roles);

    ExportExcel initExportExcel(String reportCode, String defaultTemplate, int startDataRow);
    ExportExcel initExportExcelByFileId(String fileId, int startDataRow);

    ResponseEntity compareDatabase(String jdbcUrl, String userName, String password);

    <T> List<T> getListEntitiesFromUrl(String url, Class clazz);
}
