/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.OrgDto;
import vn.hbtplus.models.request.IndicatorMastersRequest;
import vn.hbtplus.models.request.IndicatorsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.IndicatorConversionsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang kpi_indicator_conversions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface IndicatorConversionsService {

    TableResponseEntity<IndicatorConversionsResponse.SearchResult> searchData(IndicatorConversionsRequest.SearchForm dto);

    ResponseEntity saveData(IndicatorConversionsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    ResponseEntity updateStatusById(Long id, IndicatorConversionsRequest.SubmitForm dto) throws RecordNotExistsException;

    BaseResponseEntity<IndicatorConversionsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;


    IndicatorConversionsResponse.Indicators getListConversion(Long indicatorMasterId);

    TableResponseEntity<IndicatorConversionsResponse.Indicator> getListConversionTable(IndicatorConversionsRequest.SearchForm dto);

    TableResponseEntity<IndicatorConversionsResponse.Indicator> getListIndicatorConversion(IndicatorConversionsRequest.SearchForm dto);

    List<IndicatorConversionsResponse.Organization> getListOrganization(Long organizationId, Long orgTypeId);

    String getTemplateIndicator(Long indicatorMasterId, Long orgId) throws Exception;

    boolean importData(MultipartFile fileImport, Long indicatorMasterId) throws Exception;

    Integer getPoint(Long indicatorConversionId, String value);
    ResponseEntity<Object> exportData(IndicatorConversionsRequest.SearchForm dto) throws Exception;

    ListResponseEntity<OrgDto> getOrgList(Long employeeId);
}
