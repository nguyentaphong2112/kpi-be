package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class DownloadReportController {
    @RequestMapping(value = "/v1/download/temp-file", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> downloadTempFile(@RequestParam(value = "fileName") String fileName,
                                                   @RequestParam(value = "isPdf", required = false) boolean isPdf) throws BaseAppException {
        String userName = Utils.getUserNameLogin();
        if (fileName.contains("_" + userName + "_") || isPdf) {
            String filePath = Utils.getExportFolder() + fileName;
            return ResponseUtils.getResponseFileEntity(filePath, true);
        } else {
            log.info("DownloadFileController|downloadTempFile|file name invalid|fileName=" + fileName + "|userName=" + userName);
            throw new BaseAppException("file name invalid");
        }
    }
}
