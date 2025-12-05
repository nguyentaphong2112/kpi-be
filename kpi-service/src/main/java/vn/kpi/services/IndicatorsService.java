/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.response.*;
import vn.kpi.models.request.IndicatorsRequest;

import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang kpi_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface IndicatorsService {

    TableResponseEntity<IndicatorsResponse.SearchResult> searchData(IndicatorsRequest.SearchForm dto);

    ResponseEntity saveData(IndicatorsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<IndicatorsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(IndicatorsRequest.SearchForm dto) throws Exception;

    BaseDataTableDto<IndicatorsResponse.SearchResult> getIndicatorPicker(Long organizationId, IndicatorsRequest.SearchForm dto);

    List<IndicatorsResponse.DetailList> getList(Long organizationId);

    List<IndicatorsResponse.DetailList> getListEmployee(Long employeeId);

    boolean importData(MultipartFile fileImport) throws Exception;

    String getTemplateIndicator() throws Exception;

    String getMappingValue(String parameter, String configCode, Date configDate);
}
