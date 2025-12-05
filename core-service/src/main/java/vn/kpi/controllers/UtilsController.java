package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.services.UtilsService;
import vn.kpi.utils.ResponseUtils;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/v1/public")
@RequiredArgsConstructor
public class UtilsController {

    private final UtilsService utilsService;
    @GetMapping(value = "/download/database-structure", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> downloadDatabaseStructure(@RequestParam String tables) throws Exception {
        tables = tables.replace(" ","");

        return utilsService.downloadDatabaseStructure(Arrays.asList(tables.split(",")));
    }
    @GetMapping(value = "/health-check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseUtils.ok(utilsService.getSysdate());
    }

    @PostMapping(value = "/init-dto", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<String> initDto(@RequestPart(value = "sql") String sql) {
        return ResponseUtils.ok(utilsService.initDto(sql));
    }

    @PutMapping(value = "/update-column-utf8", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Integer> convertUTF8ColumnDatabase(
            @RequestParam String tableName,
            @RequestParam String idColumn,
            @RequestParam String columnName,
            @RequestParam boolean normalize
    ) {
        return ResponseUtils.ok(utilsService.convertUTF8ColumnDatabase(tableName, idColumn, columnName, normalize));
    }

    @GetMapping(value = "/compare-database", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity compareDatabase(
            @RequestParam String jdbcUrl,
            @RequestParam String userName,
            @RequestParam String password

    ) {
        return utilsService.compareDatabase(jdbcUrl, userName, password);
    }

    @GetMapping(value = "/jasypt-encrypt", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<String> jasyptEncrypt( @RequestParam String inputText) {
        return ResponseUtils.ok(utilsService.jasyptEncrypt( inputText));
    }
    @GetMapping(value = "/jasypt-decrypt", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<String> jasyptDecrypt( @RequestParam String inputText) {
        return ResponseUtils.ok(utilsService.jasyptDecrypt( inputText));
    }



    @GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> test() throws Exception {

        return utilsService.testExportDoc();
    }

}
