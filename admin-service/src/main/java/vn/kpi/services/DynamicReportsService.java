/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.request.DynamicReportsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.DynamicReportsResponse;
import vn.kpi.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_dynamic_reports
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface DynamicReportsService {

    TableResponseEntity<DynamicReportsResponse> searchData(DynamicReportsRequest.SearchForm dto);

    ResponseEntity saveData(Long id, DynamicReportsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<DynamicReportsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(DynamicReportsRequest.SearchForm dto) throws Exception;

    Long getReportId(String reportCode);

    List<ReportConfigDto> getListReportByCode(List<String> reportCodes);

    ResponseEntity saveFile(Long id,  List<MultipartFile> listFileTemplate, DynamicReportsRequest.FileData data) throws BaseAppException;
}
