/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.ReportConfigDto;
import vn.hbtplus.models.request.DynamicReportsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.DynamicReportParametersResponse;
import vn.hbtplus.models.response.DynamicReportQueriesResponse;
import vn.hbtplus.models.response.DynamicReportsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.DynamicReportParametersEntity;
import vn.hbtplus.repositories.entity.DynamicReportQueriesEntity;
import vn.hbtplus.repositories.entity.DynamicReportsEntity;
import vn.hbtplus.repositories.impl.DynamicReportsRepository;
import vn.hbtplus.repositories.jpa.DynamicReportsRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.DynamicReportsService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

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
