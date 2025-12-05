/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.ConcurrentProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ConcurrentProcessResponse;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.ConcurrentProcessEntity;
import vn.kpi.repositories.entity.PositionsEntity;
import vn.kpi.repositories.impl.ConcurrentProcessRepository;
import vn.kpi.repositories.jpa.ConcurrentProcessRepositoryJPA;
import vn.kpi.services.AttachmentService;
import vn.kpi.services.ConcurrentProcessService;
import vn.kpi.services.FileService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_concurrent_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class ConcurrentProcessServiceImpl implements ConcurrentProcessService {

    private final ConcurrentProcessRepository concurrentProcessRepository;
    private final ObjectAttributesService objectAttributesService;
    private final ConcurrentProcessRepositoryJPA concurrentProcessRepositoryJPA;
    private final FileService fileService;
    private final AttachmentService attachmentService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ConcurrentProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(concurrentProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(ConcurrentProcessRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException {
        Utils.validateDate(dto.getStartDate(), dto.getEndDate());

        boolean isDuplicate = concurrentProcessRepository.checkDuplicate(dto, employeeId, id);
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CONCURRENT_PROCESS_DUPLICATE", I18n.getMessage("error.concurrentProcess.validate.duplicate"));
        }

        // kiem tra position hop le
        if (!Utils.isNullObject(dto.getPositionId()) && !Utils.isNullObject(dto.getOrganizationId())) {
            PositionsEntity positionsEntity = concurrentProcessRepository.get(PositionsEntity.class, "position_id", dto.getPositionId(), "organization_id", dto.getOrganizationId());
            if (positionsEntity == null) {
                throw new RecordNotExistsException(dto.getPositionId(), PositionsEntity.class);
            } else {
                dto.setJobId(positionsEntity.getJobId());
            }
        }

        ConcurrentProcessEntity entity;
        if (id != null && id > 0L) {
            entity = concurrentProcessRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("concurrentProcessId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ConcurrentProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        concurrentProcessRepositoryJPA.save(entity);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_CONCURRENT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONCURRENT_PROCESS_EMP);
        fileService.uploadFiles(files, entity.getConcurrentProcessId(), Constant.ATTACHMENT.TABLE_NAMES.HR_CONCURRENT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONCURRENT_PROCESS_EMP, Constant.ATTACHMENT.MODULE);
        objectAttributesService.saveObjectAttributes(entity.getConcurrentProcessId(), dto.getListAttributes(), ConcurrentProcessEntity.class, null);
        return ResponseUtils.ok(entity.getConcurrentProcessId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws RecordNotExistsException {
        Optional<ConcurrentProcessEntity> optional = concurrentProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConcurrentProcessEntity.class);
        }
        concurrentProcessRepository.deActiveObject(ConcurrentProcessEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_CONCURRENT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONCURRENT_PROCESS_EMP);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ConcurrentProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws RecordNotExistsException {
        Optional<ConcurrentProcessEntity> optional = concurrentProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConcurrentProcessEntity.class);
        }
        ConcurrentProcessResponse.DetailBean dto = new ConcurrentProcessResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_CONCURRENT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONCURRENT_PROCESS_EMP, id));
        dto.setListAttributes(objectAttributesService.getAttributes(id, concurrentProcessRepository.getSQLTableName(ConcurrentProcessEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/qua-trinh-kiem-nhiem.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = concurrentProcessRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "qua-trinh-kiem-nhiem.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<ConcurrentProcessResponse.DetailBean> tableDto = concurrentProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getConcurrentProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_education_promotions");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getConcurrentProcessId()));
        });
        return tableDto;
    }

}
