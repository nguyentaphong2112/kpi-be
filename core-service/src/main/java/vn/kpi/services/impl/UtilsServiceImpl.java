package vn.kpi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.feigns.FileStorageFeignClient;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.feigns.ReportFeignClient;
import vn.kpi.models.AttachmentFileDto;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.ImportResultDTO;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.beans.DbColumnBean;
import vn.kpi.repositories.impl.UtilsRepository;
import vn.kpi.services.UtilsService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ExportWorld;
import vn.kpi.utils.ImportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilsServiceImpl implements UtilsService {
    private final UtilsRepository utilsRepository;
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final StringEncryptor jasyptStringEncryptor;
    private final ReportFeignClient reportFeignClient;
    private final FileStorageFeignClient fileStorageFeignClient;
    private final String ADMIN_MODULE = "admin";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${exportFolder:-}")
    private String exportFolder;

    @Override
    public ResponseEntity<Object> downloadDatabaseStructure(List<String> tables) throws Exception {
        List<DbColumnBean> columns = utilsRepository.getColumns(tables);
        Map<String, List<DbColumnBean>> mapColumns = new HashMap<>();
        Map<String, String> mapMotaBang = new HashMap<>();
        columns.stream().forEach(item -> {
            if (mapColumns.get(item.getTableName()) == null) {
                mapColumns.put(item.getTableName(), new ArrayList<>());
                mapMotaBang.put(item.getTableName(), item.getTableComment());
            }
            mapColumns.get(item.getTableName()).add(item);
        });

        String pathTemplate = "template/BM_Export_du_lieu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 6, true);
        int row = dynamicExport.getLastRow();
        for (int i = 0; i < tables.size(); i++) {
            String tableName = tables.get(i);
            if (i != tables.size() - 1) {
                dynamicExport.copyRange(row, 0, row + 4, 100, row - 5, 0, row - 1, 100);
            }
            dynamicExport.replaceText("${ten_bang}", tableName, 1);
            dynamicExport.replaceText("${mo_ta_bang}", mapMotaBang.get(tableName), 1);
            dynamicExport.replaceText("${stt_bang}", String.valueOf(i + 1), 1);

            List<DbColumnBean> tableColumns = mapColumns.get(tableName);
            List expColumns = new ArrayList();
            for (DbColumnBean columnBean : tableColumns) {
                Map map = new HashMap();
                map.put("ten_cot", columnBean.getColumnName());
                map.put("kieu_du_lieu", columnBean.getColumnType());
                map.put("kieu_khoa", columnBean.getColumnKey());
                map.put("du_lieu_index", "");
                map.put("mo_ta_cot", columnBean.getColumnComment());
                expColumns.add(map);
                row++;
            }
            row = row + 4;
            dynamicExport.replaceKeys(expColumns);
        }

        String fileName = "Cau-truc-du-lieu.xlsx";
        return ResponseUtils.ok(dynamicExport, fileName, false);
    }

    @Override
    public List<Map<String, Object>> getSysdate() {
        return utilsRepository.getSysdate();
    }

    @Override
    public List<String> initDto(String sql) {
        List results = new ArrayList();
        Map<String, Object> temps = utilsRepository.getListData(sql, new HashMap<>()).get(0);
        temps.keySet().stream().forEach(str -> {
            Object value = temps.get(str);
            String dataType = "String";
            if (value != null) {
                if (value instanceof Date) {
                    dataType = "Date";
                } else if (value instanceof BigDecimal) {
                    dataType = "Long";
                }
            } else {
                if (str.toLowerCase().endsWith("date")) {
                    dataType = "Date";
                }
            }
            results.add(MessageFormat.format("private {0} {1};", dataType, toCamelCase(str)));
        });
        return results;
    }

    private static String toCamelCase(String originalString) {
        StringBuilder standardizedString = new StringBuilder();
        char last = 'a';

        for (char c : originalString.toCharArray()) {
            if (c == '_') {
                last = c;
                continue;
            }
            if (last == '_') {
                standardizedString.append(String.valueOf(c).toUpperCase());
            } else {
                standardizedString.append(c);
            }
            last = c;
        }

        return standardizedString.toString();
    }

    @Override
    public ResponseEntity validateFileImport(ImportExcel importExcel, MultipartFile file, List<Object[]> dataList) {
        try {
            if (!importExcel.validateCommon(file.getInputStream(), dataList)) {
                ImportResultDTO importResultDTO = new ImportResultDTO();
                importResultDTO.setErrorFile(importExcel.getFileErrorDescription(file, exportFolder));
                importResultDTO.setErrorList(importExcel.getErrorList());

                return ResponseUtils.ok(importResultDTO);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return null;
    }

    public ResponseEntity responseErrorImportFile(ImportExcel importExcel, MultipartFile file) {
        try {
            ImportResultDTO importResultBean = new ImportResultDTO();
            importResultBean.setErrorFile(importExcel.getFileErrorDescription(file, exportFolder));
            importResultBean.setErrorList(importExcel.getErrorList());
            return ResponseUtils.ok(importResultBean);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public ResponseEntity<Object> testExportDoc() throws Exception {
        ExportWorld exportWorld = new ExportWorld("template/export/Template-sach.docx");
        ExportWorld exportWorld1 = new ExportWorld("template/export/Template-chi-so-thai-do.docx");
        exportWorld1.removePageContainKey("${chi_so_thai_do_1}");
        exportWorld1.removePageContainKey("${chi_so_thai_do_2}");
        exportWorld1.removePageContainKey("${chi_so_thai_do_3}");
        exportWorld1.removePageContainKey("${chi_so_thai_do_5}");
        exportWorld1.removePageContainKey("${chi_so_thai_do_6}");
        exportWorld1.removePageContainKey("${chi_so_thai_do_7}");
        exportWorld1.removePageContainKey("${chi_so_thai_do_8}");
        exportWorld.replaceByDocument("${chi_so_thai_do}", exportWorld1.getDocument());
        exportWorld.updateFields();
        return ResponseUtils.ok(exportWorld, "fileTest.docx");
    }

    @Override
    public Integer convertUTF8ColumnDatabase(String tableName, String idColumn, String columnName, boolean normalize) {
        List<AttachmentFileDto> listObjects = utilsRepository.getListObjects(tableName, idColumn, columnName);
        String sql = MessageFormat.format("""
                update {0} set {1} = :value
                where {2} = :id
                """, tableName, columnName, idColumn);
        List<Map> listParams = new ArrayList<>();
        listObjects.forEach(item -> {
            Map map = new HashMap();
            map.put("value", normalize ? Utils.normalizeFullName(item.getFileContent()) : Utils.convertCp1258ToUTF8(item.getFileContent()));
            map.put("id", item.getObjectId());
            listParams.add(map);
        });
        utilsRepository.executeBatch(sql, listParams);
        return listObjects.size();
    }

    @Override
    public String jasyptEncrypt(String inputText) {
        return jasyptStringEncryptor.encrypt(inputText);
    }

    @Override
    public String jasyptDecrypt(String inputText) {
        return jasyptStringEncryptor.decrypt(inputText);
    }

    @Override
    public boolean hasRole(String... roles) {
        List<String> list = Utils.getRoleCodeList();
        if (list == null) {
            return false;
        }
        for (String role : roles) {
            if (list.contains(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ExportExcel initExportExcel(String reportCode, String defaultTemplate, int startDataRow) {
        ExportExcel dynamicExport;
        if (!Utils.isNullOrEmpty(reportCode)) {
            try {
                BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                        Utils.getRequestHeader(request),
                        reportCode);
                ReportConfigDto reportConfigDto = response.getData();

                byte[] bytes = fileStorageFeignClient.downloadFile(Utils.getRequestHeader(request), ADMIN_MODULE,
                        reportConfigDto.getAttachmentFileList().get(0).getFileId());
                dynamicExport = new ExportExcel(new ByteArrayInputStream(bytes), startDataRow, true);
                return dynamicExport;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return new ExportExcel(defaultTemplate, startDataRow, true);
    }

    @Override
    public ExportExcel initExportExcelByFileId(String fileId, int startDataRow) {
        byte[] bytes = fileStorageFeignClient.downloadFile(Utils.getRequestHeader(request), ADMIN_MODULE,
                fileId);
        return new ExportExcel(new ByteArrayInputStream(bytes), startDataRow, true);
    }

    @Override
    public ResponseEntity compareDatabase(String jdbcUrl, String userName, String password) {
        List<String> destTableNames = utilsRepository.getListTables(null);
        List<String> listChanges = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, userName, password)) {
            List<String> sourceTableNames = utilsRepository.getListTables(conn);

            for (String tableName : sourceTableNames) {
                if (!destTableNames.contains(tableName)) {
                    //trường hợp chưa có bảng
                    String ddlCreateTable = utilsRepository.getDdlCreateTable(tableName, conn);
                    listChanges.add(ddlCreateTable + ";");
                } else {
                    List<DbColumnBean> listDestColumn = utilsRepository.getListTableColumn(tableName, null);
                    List<DbColumnBean> listSourceColumn = utilsRepository.getListTableColumn(tableName, conn);
                    for (DbColumnBean sourceColumn : listSourceColumn) {
                        boolean exists = false;
                        for (DbColumnBean destColumn : listDestColumn) {
                            if (sourceColumn.getColumnName().equals(destColumn.getColumnName())) {
                                exists = true;
                                if (!sourceColumn.getModifyStatement().equals(destColumn.getModifyStatement())) {
                                    listChanges.add(sourceColumn.getModifyStatement());
                                }
                                break;
                            }
                        }
                        if (!exists) {
                            listChanges.add(sourceColumn.getAddStatement());
                        }
                    }
                }
            }
            String filePath = Utils.getFilePathExport("schema_update.sql");
            Path outputPath = Paths.get(filePath);
            Files.write(outputPath, listChanges, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return ResponseUtils.getResponseFileEntity(filePath, false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> getListEntitiesFromUrl(String url, Class clazz) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(Utils.getHeader());

        // Gọi API (GET)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        String json = response.getBody();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode dataNode = root.path("data");

            if (dataNode.isMissingNode() || !dataNode.isArray()) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(
                    dataNode.toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (Exception e) {
            throw new RuntimeException("Parse JSON failed: " + e.getMessage(), e);
        }
    }
}
