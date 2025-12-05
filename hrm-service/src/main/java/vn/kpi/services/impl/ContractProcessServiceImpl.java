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
import vn.kpi.config.ApplicationConfig;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.feigns.FileStorageFeignClient;
import vn.kpi.feigns.ReportFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.request.ContractProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ContractProcessResponse;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.ContractProcessEntity;
import vn.kpi.repositories.entity.ContractTypesEntity;
import vn.kpi.repositories.impl.ContractProcessRepository;
import vn.kpi.repositories.jpa.ContractProcessRepositoryJPA;
import vn.kpi.services.AttachmentService;
import vn.kpi.services.ContractProcessService;
import vn.kpi.services.FileService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Lop impl service ung voi bang hr_contract_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class ContractProcessServiceImpl implements ContractProcessService {

    private final ContractProcessRepository contractProcessRepository;
    private final ObjectAttributesService objectAttributesService;
    private final ContractProcessRepositoryJPA contractProcessRepositoryJPA;
    private final ApplicationConfig applicationConfig;
    private final FileService fileService;
    private final AttachmentService attachmentService;
    private final FileStorageFeignClient storageFeignClient;
    private final ReportFeignClient reportFeignClient;
    private final HttpServletRequest request;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ContractProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(contractProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(ContractProcessRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException {
        // check phu hop dong phai thuoc 1 hop dong
        if (Utils.isNullOrEmpty(dto.getClassifyCode())){
            dto.setClassifyCode(Constant.CLASSIFY_CONTRACT.HOP_DONG);
        }
        if (Constant.CLASSIFY_CONTRACT.PHU_LUC_HOP_DONG.equals(dto.getClassifyCode()) &&
                !contractProcessRepository.checkExitContractProcess(employeeId, dto.getStartDate(), dto.getEndDate(), id)) {
            throw new BaseAppException("ERROR_CONTRACT_PROCESS_NOT_EXISTS", I18n.getMessage("error.contractProcess.validate.phd"));

        }

        // validate conflict qua trinh
        if (this.isConflict(dto, employeeId, id)) {
            throw new BaseAppException("ERROR_CONTRACT_PROCESS_CONFIG", I18n.getMessage("error.contractProcess.validate.process"));
        }

        ContractProcessEntity entity;
        if (id != null && id > 0L) {
            entity = contractProcessRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("contractProcessId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());

        } else {
            entity = new ContractProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contractProcessRepositoryJPA.saveAndFlush(entity);
        objectAttributesService.saveObjectAttributes(entity.getContractProcessId(), dto.getListAttributes(), ContractProcessEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_CONTRACT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONTRACT_PROCESS_EMP);
        fileService.uploadFiles(files, entity.getContractProcessId(), Constant.ATTACHMENT.TABLE_NAMES.HR_CONTRACT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONTRACT_PROCESS_EMP, Constant.ATTACHMENT.MODULE);

        //update emp_type_id
        contractProcessRepository.autoUpdateEmpType(Utils.getUserNameLogin(), employeeId);
        return ResponseUtils.ok(entity.getContractProcessId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws RecordNotExistsException {
        Optional<ContractProcessEntity> optional = contractProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ContractProcessEntity.class);
        }
        contractProcessRepository.deActiveObject(ContractProcessEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_CONTRACT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONTRACT_PROCESS_EMP);

        contractProcessRepository.autoUpdateEmpType(Utils.getUserNameLogin(), employeeId);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ContractProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws RecordNotExistsException {
        Optional<ContractProcessEntity> optional = contractProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ContractProcessEntity.class);
        }
        ContractProcessResponse.DetailBean dto = new ContractProcessResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, contractProcessRepository.getSQLTableName(ContractProcessEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_CONTRACT_PROCESS, Constant.ATTACHMENT.FILE_TYPES.CONTRACT_PROCESS_EMP, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request), Constant.REPORT_CONFIG_CODES.DANH_SACH_QT_HOP_DONG);
        ExportExcel dynamicExport;
        List<Map<String, Object>> listDataExport;
        if (response != null && response.getData() != null) {
            ReportConfigDto reportConfigDto = response.getData();
            byte[] bytes = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.ADMIN_MODULE,
                    reportConfigDto.getAttachmentFileList().get(0).getFileId());
            dynamicExport = new ExportExcel(new ByteArrayInputStream(bytes), 2, true);
            listDataExport = contractProcessRepository.getListExport(reportConfigDto.getQueryResponseList().get(0).getSqlQuery(), dto);
        } else {
            String pathTemplate = "template/export/employee/thong-tin-hop-dong.xlsx";
            dynamicExport = new ExportExcel(pathTemplate, 2, true);
            listDataExport = contractProcessRepository.getListExport(null, dto);
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "qua-trinh-thong-tin-hop-dong.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<ContractProcessResponse.SearchResult> tableDto = contractProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getContractProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_contract_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getContractProcessId()));
        });
        return tableDto;
    }

    @Override
    public void autoUpdateEmpType() {
        contractProcessRepository.autoUpdateEmpType("job-update", null);
    }

    private boolean isConflict(ContractProcessRequest.SubmitForm dto, Long employeeId, Long id) {
        boolean isCheckConflict = false;
        if (Constant.CLASSIFY_CONTRACT.HOP_DONG.equals(dto.getClassifyCode())) {
            isCheckConflict = true;
        } else if (Constant.CLASSIFY_CONTRACT.PHU_LUC_HOP_DONG.equals(dto.getClassifyCode())) {
            ContractTypesEntity contractTypesEntity = contractProcessRepository.get(ContractTypesEntity.class, dto.getContractTypeId());
            if (applicationConfig.getContractTypePause().contains(contractTypesEntity.getCode())) {
                isCheckConflict = true;
                dto.setIsDelayContract(1);
            }
        }

        if (isCheckConflict) {
            List<ContractProcessResponse.DetailBean> listData = contractProcessRepository.getConflictProcess(dto, employeeId, id);
            return this.isConflict(dto, listData, true);
        }
        return false;
    }

    private boolean isConflict(ContractProcessRequest.SubmitForm inputDTO, List<ContractProcessResponse.DetailBean> listData, boolean isDefaultCheck) {
        boolean isCheckConflict = false;
        if (!isDefaultCheck) {
            if (Constant.CLASSIFY_CONTRACT.HOP_DONG.equals(inputDTO.getClassifyCode())) {
                isCheckConflict = true;
            } else if (Constant.CLASSIFY_CONTRACT.PHU_LUC_HOP_DONG.equals(inputDTO.getClassifyCode())) {
                ContractTypesEntity contractTypesEntity = contractProcessRepository.get(ContractTypesEntity.class, inputDTO.getContractTypeId());
                if (applicationConfig.getContractTypePause().contains(contractTypesEntity.getCode())) {
                    isCheckConflict = true;
                    inputDTO.setIsDelayContract(1);
                }
            }
        }

        if (isCheckConflict || isDefaultCheck) {
            if (listData == null || listData.isEmpty()) {
                return false;
            }
            if (Constant.CLASSIFY_CONTRACT.HOP_DONG.equalsIgnoreCase(inputDTO.getClassifyCode())) {
                if (listData.size() > 1) {
                    for (ContractProcessResponse.DetailBean dto : listData) {// dieu chinh nghiep vu cho phep long qua trinh neu ngay ky khac nhau
                        Date signedDateExist = Utils.NVL(dto.getDocumentSignedDate());
                        Date signedDateInput = Utils.NVL(inputDTO.getDocumentSignedDate());
                        if (Utils.daysBetween(signedDateExist, signedDateInput) == 0
                                || Utils.daysBetween(dto.getStartDate(), inputDTO.getStartDate()) != 0
                                || Utils.daysBetween(Utils.NVL(dto.getEndDate()), Utils.NVL(inputDTO.getEndDate())) != 0
                                || !dto.getContractTypeId().equals(inputDTO.getContractTypeId())
                        ) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    ContractProcessResponse.DetailBean dto = listData.get(0);
                    boolean isConflict = dto.getEndDate() != null || !dto.getStartDate().before(inputDTO.getStartDate());
                    //neu da bi conflic thi phai thoa man them dieu kien KHONG phai HD ky lai
                    return isConflict &&
                            (Utils.daysBetween(Utils.NVL(dto.getDocumentSignedDate()), Utils.NVL(inputDTO.getDocumentSignedDate())) == 0
                                    || Utils.daysBetween(dto.getStartDate(), inputDTO.getStartDate()) != 0
                                    || Utils.daysBetween(Utils.NVL(dto.getEndDate()), Utils.NVL(inputDTO.getEndDate())) != 0
                                    || !dto.getContractTypeId().equals(inputDTO.getContractTypeId()));
                }
            } else if (inputDTO.getIsDelayContract() != null) {
                for (ContractProcessResponse.DetailBean resultDTO : listData) {
                    if (Utils.isConflictDate(resultDTO.getStartDate(), resultDTO.getEndDate(), inputDTO.getStartDate(), inputDTO.getEndDate())) {
                        return true;
                    }
                }
            } else {
                for (ContractProcessResponse.DetailBean dto : listData) {
                    if (Utils.formatDate(dto.getStartDate()).equals(Utils.formatDate(inputDTO.getStartDate()))) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

}
