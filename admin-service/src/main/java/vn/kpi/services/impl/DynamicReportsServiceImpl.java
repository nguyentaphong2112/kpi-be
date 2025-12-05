/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.feigns.FileStorageFeignClient;
import vn.kpi.models.AttachmentFileDto;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.request.DynamicReportsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.DynamicReportParametersResponse;
import vn.kpi.models.response.DynamicReportQueriesResponse;
import vn.kpi.models.response.DynamicReportsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.DynamicReportParametersEntity;
import vn.kpi.repositories.entity.DynamicReportQueriesEntity;
import vn.kpi.repositories.entity.DynamicReportsEntity;
import vn.kpi.repositories.impl.DynamicReportsRepository;
import vn.kpi.repositories.jpa.DynamicReportsRepositoryJPA;
import vn.kpi.services.AttachmentService;
import vn.kpi.services.DynamicReportsService;
import vn.kpi.services.FileService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang sys_dynamic_reports
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicReportsServiceImpl implements DynamicReportsService {

    private final DynamicReportsRepository dynamicReportsRepository;

    private final DynamicReportsRepositoryJPA dynamicReportsRepositoryJPA;

    private final AttachmentService attachmentService;

    private final FileStorageFeignClient storageFeignClient;

    private final HttpServletRequest request;

    private final FileService fileService;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DynamicReportsResponse> searchData(DynamicReportsRequest.SearchForm dto) {
        BaseDataTableDto<DynamicReportsResponse> responseData = dynamicReportsRepository.searchData(dto);

        if (!Utils.isNullOrEmpty(responseData.getListData())) {
            List<Long> dynamicReportIds = responseData.getListData().stream().map(DynamicReportsResponse::getDynamicReportId).collect(Collectors.toList());
            Map<Long, List<AttachmentFileDto>> mapAttachment = getMapAttachment(dynamicReportIds);

            responseData.getListData().forEach(item -> item.setAttachmentFileList(mapAttachment.get(item.getDynamicReportId())));
        }
        return ResponseUtils.ok(responseData);
    }

    private Map<Long, List<AttachmentFileDto>> getMapAttachment(List<Long> dynamicReportIds) {
        List<AttachmentFileDto> attachmentFileList = attachmentService.getAttachmentList(Constant.ATTACHMENT.TABLE_NAMES.DYNAMIC_REPORTS,
                Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE,
                dynamicReportIds);
        return attachmentFileList.stream().collect(Collectors.groupingBy(AttachmentFileDto::getObjectId));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(Long id, DynamicReportsRequest.SubmitForm dto) throws BaseAppException {
        DynamicReportsEntity entity;

        if (dynamicReportsRepository.duplicate(DynamicReportsEntity.class, id, "code", dto.getCode())) {
            throw new BaseAppException("ERROR_DYNAMIC_REPORT_CODE_DUPLICATE", "error.dynamicReport.duplicateCode");
        }
        if (id != null && id > 0L) {
            entity = dynamicReportsRepositoryJPA.findById(id).get();
            entity.setModifiedBy(Utils.getUserNameLogin());
            entity.setModifiedTime(new Date());
        } else {
            entity = new DynamicReportsEntity();
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setCreatedTime(new Date());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        dynamicReportsRepositoryJPA.save(entity);

        if (dto.getFileTemplate() != null && StringUtils.isNotBlank(dto.getFileTemplate().getOriginalFilename())) {
            attachmentService.inactiveAttachment(Constant.ATTACHMENT.TABLE_NAMES.DYNAMIC_REPORTS,
                    Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE, entity.getDynamicReportId());

            BaseResponse<AttachmentFileDto> response = storageFeignClient.uploadFile(
                    Utils.getRequestHeader(request), dto.getFileTemplate(), Constant.ATTACHMENT.MODULE,
                    Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE, entity.getDynamicReportId());

            AttachmentFileDto fileResponse = response.getData();
            attachmentService.saveAttachment(Constant.ATTACHMENT.TABLE_NAMES.DYNAMIC_REPORTS,
                    Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE,
                    entity.getDynamicReportId(),
                    fileResponse
            );
        }

        String userName = Utils.getUserNameLogin();
        dynamicReportsRepository.deActiveObject(DynamicReportParametersEntity.class, "dynamicReportId", entity.getDynamicReportId());
        if (!Utils.isNullOrEmpty(dto.getReportParameterList())) {
            List<DynamicReportParametersEntity> parametersEntityList = new ArrayList<>();
            dto.getReportParameterList().forEach(item -> {
                DynamicReportParametersEntity parametersEntity = new DynamicReportParametersEntity();
                Utils.copyProperties(item, parametersEntity);
                parametersEntity.setDynamicReportId(entity.getDynamicReportId());

                parametersEntityList.add(parametersEntity);
            });

            dynamicReportsRepository.insertBatch(DynamicReportParametersEntity.class, parametersEntityList, userName);
        }
        dynamicReportsRepository.deActiveObject(DynamicReportQueriesEntity.class, "dynamicReportId", entity.getDynamicReportId());
        if (!Utils.isNullOrEmpty(dto.getReportQueryList())) {
            List<DynamicReportQueriesEntity> queryEntityList = new ArrayList<>();
            dto.getReportQueryList().forEach(item -> {
                DynamicReportQueriesEntity queryEntity = new DynamicReportQueriesEntity();
                Utils.copyProperties(item, queryEntity);
                queryEntity.setDynamicReportId(entity.getDynamicReportId());

                queryEntityList.add(queryEntity);
            });

            dynamicReportsRepository.insertBatch(DynamicReportQueriesEntity.class, queryEntityList, userName);
        }


        return ResponseUtils.ok(entity.getDynamicReportId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<DynamicReportsEntity> optional = dynamicReportsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DynamicReportsEntity.class);
        }
        dynamicReportsRepository.deActiveObject(DynamicReportsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DynamicReportsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<DynamicReportsEntity> optional = dynamicReportsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DynamicReportsEntity.class);
        }
        DynamicReportsResponse dto = new DynamicReportsResponse();
        Utils.copyProperties(optional.get(), dto);

        List<DynamicReportParametersEntity> parametersEntityList =
                dynamicReportsRepository.findByProperties(DynamicReportParametersEntity.class, "dynamic_report_id", id);
        dto.setParametersResponseList(parametersEntityList.stream().map(item -> {
            DynamicReportParametersResponse parametersResponse = new DynamicReportParametersResponse();
            Utils.copyProperties(item, parametersResponse);

            return parametersResponse;
        }).collect(Collectors.toList()));

        List<DynamicReportQueriesEntity> queriesEntityList =
                dynamicReportsRepository.findByProperties(DynamicReportQueriesEntity.class, "dynamic_report_id", id);
        dto.setQueryResponseList(queriesEntityList.stream().map(item -> {
            DynamicReportQueriesResponse queryResponse = new DynamicReportQueriesResponse();
            Utils.copyProperties(item, queryResponse);

            return queryResponse;
        }).collect(Collectors.toList()));

        dto.setAttachmentFileList(attachmentService.getAttachmentList(Constant.ATTACHMENT.TABLE_NAMES.DYNAMIC_REPORTS, Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE, List.of(id)));

        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(DynamicReportsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = dynamicReportsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }


    @Override
    public Long getReportId(String reportCode) {
        DynamicReportsEntity reportsEntity = dynamicReportsRepository.get(DynamicReportsEntity.class, "code", reportCode);
        return reportsEntity == null ? null : reportsEntity.getDynamicReportId();
    }

    @Override
    public List<ReportConfigDto> getListReportByCode(List<String> reportCodes) {
        return dynamicReportsRepository.getListReportByCode(reportCodes);
    }

    @Override
    public ResponseEntity saveFile(Long id, List<MultipartFile> files, DynamicReportsRequest.FileData data) throws BaseAppException {
        Optional<DynamicReportsEntity> optional = dynamicReportsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DynamicReportsEntity.class);
        }
        DynamicReportsEntity entity = optional.get();
        fileService.deActiveFileByAttachmentId(data.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.DYNAMIC_REPORTS, Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE);
        fileService.uploadFiles(files, entity.getDynamicReportId(), Constant.ATTACHMENT.TABLE_NAMES.DYNAMIC_REPORTS, Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE, Constant.ATTACHMENT.MODULE);

        return ResponseUtils.ok(entity.getDynamicReportId());
    }


}
