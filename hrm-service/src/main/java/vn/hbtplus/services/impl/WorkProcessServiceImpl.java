/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.WorkProcessRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WorkProcessResponse;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.WorkProcessRepository;
import vn.hbtplus.repositories.jpa.ConcurrentProcessRepositoryJPA;
import vn.hbtplus.repositories.jpa.DocumentTypesRepositoryJPA;
import vn.hbtplus.repositories.jpa.JobsRepositoryJPA;
import vn.hbtplus.repositories.jpa.WorkProcessRepositoryJPA;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.WorkProcessService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_work_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class WorkProcessServiceImpl implements WorkProcessService {

    private final WorkProcessRepository workProcessRepository;
    private final JobsRepositoryJPA jobsRepositoryJPA;
    private final ConcurrentProcessRepositoryJPA concurrentProcessRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final WorkProcessRepositoryJPA workProcessRepositoryJPA;
    private final DocumentTypesRepositoryJPA documentTypesRepositoryJPA;
    private final FileService fileService;
    private final AttachmentServiceImpl attachmentService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WorkProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(workProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(WorkProcessRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long workProcessId) throws BaseAppException {
        if (workProcessRepository.isConflictProcess(dto.getStartDate(), employeeId, workProcessId)) {
            throw new BaseAppException("ERROR_WORK_PROCESS_CONFIG", I18n.getMessage("error.workProcess.validate.process"));
        }
        validateDocumentTypeId(employeeId, dto.getDocumentTypeId(), dto.getStartDate());
        // kiem tra position hop le
        if (!Utils.isNullObject(dto.getPositionId()) && !Utils.isNullObject(dto.getOrganizationId())) {
            PositionsEntity positionsEntity = workProcessRepository.get(PositionsEntity.class, "position_id", dto.getPositionId(), "organization_id", dto.getOrganizationId());
            if (positionsEntity == null) {
                throw new RecordNotExistsException(dto.getPositionId(), PositionsEntity.class);
            } else {
                dto.setJobId(positionsEntity.getJobId());
            }
        }
        WorkProcessEntity entity;
        String userName = Utils.getUserNameLogin();
        Date curDate = new Date();
        if (workProcessId != null && workProcessId > 0L) {
            entity = workProcessRepositoryJPA.getById(workProcessId);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("workProcessId and employeeId not matching!");
            }
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
        } else {
            entity = new WorkProcessEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        // set qua trinh nghi viec
        if (dto.getDocumentTypeId() != null
            && documentTypesRepositoryJPA.getById(dto.getDocumentTypeId()).getType().equals(DocumentTypesEntity.TYPES.OUT)) {
            WorkProcessEntity preProcess = workProcessRepository.getPreProcess(employeeId, dto.getStartDate());
            entity.setOrganizationId(preProcess.getOrganizationId());
            entity.setPositionId(preProcess.getPositionId());
            entity.setJobId(preProcess.getJobId());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        workProcessRepositoryJPA.saveAndFlush(entity);
        objectAttributesService.saveObjectAttributes(entity.getWorkProcessId(), dto.getListAttributes(), WorkProcessEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_WORK_PROCESS, Constant.ATTACHMENT.FILE_TYPES.WORK_PROCESS_EMP);
        fileService.uploadFiles(files, entity.getWorkProcessId(), Constant.ATTACHMENT.TABLE_NAMES.HR_WORK_PROCESS, Constant.ATTACHMENT.FILE_TYPES.WORK_PROCESS_EMP, Constant.ATTACHMENT.MODULE);

        // update qua trinh cong tac cu
        workProcessRepository.updateWorkProcess(entity.getEmployeeId(), userName);
        return ResponseUtils.ok(entity.getWorkProcessId());
    }

    @Override
    public Long saveData(WorkProcessRequest.SubmitFormV2 dto, List<MultipartFile> files, Long employeeId, Long workProcessId) throws BaseAppException {
        if (workProcessRepository.isConflictProcess(dto.getStartDate(), employeeId, workProcessId)) {
            throw new BaseAppException("ERROR_WORK_PROCESS_CONFIG", I18n.getMessage("error.workProcess.validate.process"));
        }
        dto.getPositions().forEach(item -> {
            // kiem tra position hop le
            if (item != null && !Utils.isNullObject(item.getPositionId()) && !Utils.isNullObject(item.getOrganizationId())) {
                PositionsEntity positionsEntity = workProcessRepository.get(PositionsEntity.class, "position_id", item.getPositionId(), "organization_id", item.getOrganizationId());
                if (positionsEntity == null) {
                    throw new RecordNotExistsException(item.getPositionId(), PositionsEntity.class);
                } else {
                    item.setJobId(positionsEntity.getJobId());
                }
            }
        });
        // to continue
        WorkProcessEntity entity;
        String userName = Utils.getUserNameLogin();
        Date curDate = new Date();
        Map<Long, ConcurrentProcessEntity> mapOldConcurrentProcess = new HashMap<>();
        if (workProcessId != null && workProcessId > 0L) {
            entity = workProcessRepositoryJPA.getById(workProcessId);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("workProcessId and employeeId not matching!");
            }
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
            List<ConcurrentProcessEntity> listOldConcurrentProcess = workProcessRepository.findByProperties(ConcurrentProcessEntity.class, "employeeId", entity.getEmployeeId(), "startDate", entity.getStartDate(), "isDeleted", BaseConstants.STATUS.NOT_DELETED);
            listOldConcurrentProcess.forEach(item -> {
                mapOldConcurrentProcess.put(item.getPositionId(), item);
            });

        } else {
            entity = new WorkProcessEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        // set qua trinh nghi viec
        WorkProcessRequest.Position mainPosition = null;
        if (dto.getDocumentTypeId() != null
            && documentTypesRepositoryJPA.getById(dto.getDocumentTypeId()).getType().equals(DocumentTypesEntity.TYPES.OUT)) {
            WorkProcessEntity preProcess = workProcessRepository.getPreProcess(employeeId, dto.getStartDate());
            if (preProcess == null) {
                throw new BaseAppException("ERROR_PRE_PROCESS_CONFIG", I18n.getMessage("error.workProcess.validate.preProcess"));
            }
            entity.setOrganizationId(preProcess.getOrganizationId());
            entity.setPositionId(preProcess.getPositionId());
            entity.setJobId(preProcess.getJobId());
        } else if (!Utils.isNullOrEmpty(dto.getPositions())) {
            //truong hop luu du lieu
            mainPosition = dto.getPositions().get(0);
            if (dto.getPositions().size() > 1 && dto.getPositions().get(1) != null) {
                mainPosition = dto.getPositions().get(1);
            }
            entity.setOrganizationId(mainPosition.getOrganizationId());
            entity.setPositionId(mainPosition.getPositionId());
            entity.setJobId(mainPosition.getJobId());
            entity.setPercentageId(mainPosition.getPercentageId());
            entity.setDocumentNo(mainPosition.getDocumentNo());
            entity.setDocumentSignedDate(mainPosition.getDocumentSignedDate());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        workProcessRepositoryJPA.saveAndFlush(entity);
        objectAttributesService.saveObjectAttributes(entity.getWorkProcessId(), dto.getListAttributes(), WorkProcessEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_WORK_PROCESS, Constant.ATTACHMENT.FILE_TYPES.WORK_PROCESS_EMP);
        fileService.uploadFiles(files, entity.getWorkProcessId(), Constant.ATTACHMENT.TABLE_NAMES.HR_WORK_PROCESS, Constant.ATTACHMENT.FILE_TYPES.WORK_PROCESS_EMP, Constant.ATTACHMENT.MODULE);
        // update qua trinh cong tac cu
        workProcessRepository.updateWorkProcess(entity.getEmployeeId(), userName);

        //Luu du lieu qua trinh kiem nhiem
        if (dto.getPositions() != null && dto.getPositions().size() > 1) {
            for (int i = 0; i < dto.getPositions().size(); i++) {
                WorkProcessRequest.Position position = dto.getPositions().get(i);
                if (position != null && !position.getPositionId().equals(mainPosition.getPositionId())) {
                    ConcurrentProcessEntity concurrentProcessEntity = mapOldConcurrentProcess.get(position.getPositionId());
                    if (concurrentProcessEntity == null) {
                        concurrentProcessEntity = new ConcurrentProcessEntity();
                        concurrentProcessEntity.setCreatedTime(curDate);
                        concurrentProcessEntity.setCreatedBy(userName);
                        concurrentProcessEntity.setEmployeeId(employeeId);
                    } else {
                        concurrentProcessEntity.setModifiedTime(curDate);
                        concurrentProcessEntity.setModifiedBy(userName);
                        mapOldConcurrentProcess.remove(position.getPositionId());
                    }
                    concurrentProcessEntity.setJobId(position.getJobId());
                    concurrentProcessEntity.setPositionId(position.getPositionId());
                    concurrentProcessEntity.setOrganizationId(position.getOrganizationId());
                    concurrentProcessEntity.setDocumentNo(position.getDocumentNo());
                    concurrentProcessEntity.setDocumentSignedDate(position.getDocumentSignedDate());
                    concurrentProcessEntity.setPercentageId(position.getPercentageId());
                    concurrentProcessEntity.setStartDate(entity.getStartDate());
                    concurrentProcessEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    concurrentProcessRepositoryJPA.save(concurrentProcessEntity);
                }
            }
        }
        mapOldConcurrentProcess.values().forEach(item -> {
            item.setIsDeleted(BaseConstants.STATUS.DELETED);
            item.setModifiedTime(curDate);
            item.setModifiedBy(userName);
            concurrentProcessRepositoryJPA.save(item);
        });
        workProcessRepository.updateConcurrentProcess(employeeId, userName);
        return entity.getWorkProcessId();
    }

    private void validateDocumentTypeId(Long employeeId, Long documentTypeId, Date startDate) {
        if (documentTypeId != null) {
            DocumentTypesEntity documentTypesEntity = workProcessRepository.get(DocumentTypesEntity.class, documentTypeId);
            if (documentTypesEntity != null && DocumentTypesEntity.TYPES.OUT.equalsIgnoreCase(documentTypesEntity.getType())) {
                if (workProcessRepository.getPreProcess(employeeId, startDate) == null) {
                    throw new BaseAppException("ERROR_BEFORE_WP_NOT_EXISTS", I18n.getMessage("error.workProcess.validate.preProcessNotExists"));
                }
            }
        }
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<WorkProcessEntity> optional = workProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("workProcessId and employeeId not matching!");
        }
        workProcessRepository.deActiveObject(WorkProcessEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_WORK_PROCESS, Constant.ATTACHMENT.FILE_TYPES.WORK_PROCESS_EMP);
        //update qua trinh cong tac cu
        workProcessRepository.updateWorkProcess(employeeId, Utils.getUserNameLogin());
        List<ConcurrentProcessEntity> listConcurrentProcess = workProcessRepository.findByProperties(ConcurrentProcessEntity.class, "employeeId", employeeId, "startDate", optional.get().getStartDate(), "isDeleted", BaseConstants.STATUS.NOT_DELETED);
        listConcurrentProcess.forEach(item -> {
            item.setIsDeleted(BaseConstants.STATUS.DELETED);
            item.setModifiedTime(new Date());
            item.setModifiedBy(Utils.getUserNameLogin());
            concurrentProcessRepositoryJPA.save(item);
        });
        workProcessRepository.updateConcurrentProcess(employeeId, Utils.getUserNameLogin());
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WorkProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<WorkProcessEntity> optional = workProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("workProcessId and employeeId not matching!");
        }
        WorkProcessResponse.DetailBean dto = new WorkProcessResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, workProcessRepository.getSQLTableName(WorkProcessEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_WORK_PROCESS, Constant.ATTACHMENT.FILE_TYPES.WORK_PROCESS_EMP, id));
        List<ConcurrentProcessEntity> listConcurrent = workProcessRepository.findByProperties(ConcurrentProcessEntity.class, "employeeId", employeeId, "startDate", dto.getStartDate());
        dto.getPositions().add(new WorkProcessResponse.Position(dto.getPositionId(), dto.getJobId(), dto.getOrganizationId(), dto.getPercentageId(), dto.getDocumentNo(), dto.getDocumentSignedDate()));


        if (!listConcurrent.isEmpty()) {
            if (dto.getJobId() != null) {
                JobsEntity mainJobsEntity = jobsRepositoryJPA.getById(dto.getJobId());
                if (mainJobsEntity.getJobType().equals(JobsEntity.JOB_TYPES.CONG_VIEC)) {
                    dto.getPositions().add(null);
                }
            }

            listConcurrent.forEach(item -> {
                JobsEntity jobsEntity = jobsRepositoryJPA.getById(item.getJobId());
                if (jobsEntity.getJobType().equals(JobsEntity.JOB_TYPES.CONG_VIEC)
                    && dto.getPositions().size() == 1) {
                    dto.getPositions().add(0, new WorkProcessResponse.Position(item.getPositionId(), item.getJobId(), item.getOrganizationId(), item.getPercentageId(), item.getDocumentNo(), item.getDocumentSignedDate()));
                } else {
                    dto.getPositions().add(new WorkProcessResponse.Position(item.getPositionId(), item.getJobId(), item.getOrganizationId(), item.getPercentageId(), item.getDocumentNo(), item.getDocumentSignedDate()));
                }
            });
        }
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/qua-trinh-cong-tac.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = workProcessRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "qua-trinh-cong-tac.xlsx");
    }


    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<WorkProcessResponse.DetailBean> tableDto = workProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getWorkProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_work_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getWorkProcessId()));
        });
        return tableDto;
    }

    @Override
    public ResponseEntity autoUpdateWorkProcess(String fromDate, boolean isSchedule) {
        String modifiedBy;
        if (isSchedule) {
            modifiedBy = BaseConstants.SYSTEM_JOB;
        } else {
            modifiedBy = Utils.getUserNameLogin();
        }
        //cap nhat thong tin qua trinh nghi viec
        Date date = Utils.stringToDate(fromDate);
        workProcessRepository.updateEmpInfoByWorkProcess(null, date == null ? new Date() : date, modifiedBy);
        return ResponseUtils.ok(fromDate);
    }

}
