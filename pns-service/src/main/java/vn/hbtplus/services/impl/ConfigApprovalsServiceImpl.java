/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.dto.ConfigApprovalsDTO;
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigApprovalsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ConfigApprovalsEntity;
import vn.hbtplus.repositories.entity.OrganizationsEntity;
import vn.hbtplus.repositories.impl.ConfigApprovalsRepositoryImpl;
import vn.hbtplus.repositories.jpa.ConfigApprovalsRepositoryJPA;
import vn.hbtplus.services.ConfigApprovalsService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.security.SignatureException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang PNS_CONFIG_APPROVALS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigApprovalsServiceImpl implements ConfigApprovalsService {

    private final ConfigApprovalsRepositoryImpl configApprovalsRepositoryImpl;
    private final ConfigApprovalsRepositoryJPA configApprovalsRepositoryJPA;
    private final FileService fileService;
    private final AttachmentServiceImpl attachmentService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ConfigApprovalsResponse> searchData(ConfigApprovalsDTO dto) {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        return ResponseUtils.ok(configApprovalsRepositoryImpl.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveData(ConfigApprovalsDTO dto, MultipartFile multipartFile) throws SignatureException {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        if (configApprovalsRepositoryImpl.isDuplicate(dto)) {
            throw new BaseAppException("CONFIG_APPROVAL_DUPLICATE", I18n.getMessage("contract.template.duplicate"));
        }
        ConfigApprovalsEntity entity;
        if (dto.getConfigApprovalId() != null && dto.getConfigApprovalId() > 0L) {
            entity = configApprovalsRepositoryJPA.getById(dto.getConfigApprovalId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            if (multipartFile != null) {
                fileService.deActiveFileByAttachmentId(entity.getConfigApprovalId(), Constant.ATTACHMENT.TABLE_NAMES.PNS_CONFIG_APPROVALS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONFIG_APPROVALS);
            }
        } else {
            entity = new ConfigApprovalsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(entity, dto);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        configApprovalsRepositoryJPA.saveAndFlush(entity);
        fileService.uploadFile(multipartFile, entity.getConfigApprovalId(), Constant.ATTACHMENT.TABLE_NAMES.PNS_CONFIG_APPROVALS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONFIG_APPROVALS, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getConfigApprovalId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> deleteData(Long id) {
        Optional<ConfigApprovalsEntity> optional = configApprovalsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("CONFIG_APPROVAL_NOT_FOUND", I18n.getMessage("contract.template.notFound"));
        }
        configApprovalsRepositoryImpl.deActiveObject(ConfigApprovalsEntity.class, id);
        fileService.deActiveFileByAttachmentId(id, Constant.ATTACHMENT.TABLE_NAMES.PNS_CONFIG_APPROVALS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONFIG_APPROVALS);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) throws SignatureException {
        Optional<ConfigApprovalsEntity> optional = configApprovalsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("CONFIG_APPROVAL_NOT_FOUND", I18n.getMessage("contract.template.notFound"));

        }
        ConfigApprovalsResponse dto = new ConfigApprovalsResponse();
        Utils.copyProperties(dto, optional.get());
        if (dto.getOrganizationId() != null) {
            dto.setOrg(configApprovalsRepositoryImpl.getOrg(dto.getOrganizationId()));
        }

        if (dto.getApproverId() != null) {
            dto.setApprover(configApprovalsRepositoryImpl.getEmp(dto.getApproverId()));
        }

        List<Attachment> documentsEntityList = attachmentService.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.PNS_CONFIG_APPROVALS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONFIG_APPROVALS, dto.getConfigApprovalId());
        if (!Utils.isNullOrEmpty(documentsEntityList)) {
            dto.setFile(documentsEntityList.get(0));
        }
        return ResponseUtils.ok(dto);
    }

    @Override
    public ContractApproversDTO getApprover(Integer type, Long empId, Long posId, Long orgId, Date reportDate) {
        List<ConfigApprovalsDTO> configApprovalsDTOS = configApprovalsRepositoryImpl.getConfigApprovers(type, empId, posId, orgId, reportDate);
        if (configApprovalsDTOS == null || configApprovalsDTOS.isEmpty()) {
            return null;
        }
        for (ConfigApprovalsDTO dto : configApprovalsDTOS) {
            //check neu don vi la phong giao dich online thi ten chuc danh nguoi ky phai doi lai
            OrganizationsEntity organizationsEntity = configApprovalsRepositoryImpl.get(OrganizationsEntity.class, dto.getOrganizationId());
            String positionName = null;

            if (organizationsEntity.getOrgTypeId() != null) {
                positionName = I18n.getMessage("contract.signerPosition.pgdOnline",
                        configApprovalsRepositoryImpl.get(OrganizationsEntity.class, dto.getOrganizationId()).getName().replace(".", ""),
                        organizationsEntity.getName().replace("CN.", "").trim());
            }

            if (dto.getApproverId() != null) {
                ContractApproversDTO approverDTO = new ContractApproversDTO();
                approverDTO.setDocumentNo(dto.getDocumentNo());
                approverDTO.setEmployeeId(dto.getApproverId());
                approverDTO.setPositionName(positionName == null ? configApprovalsRepositoryImpl.getPosNameOfEmp(dto.getApproverId()) : positionName);
                return approverDTO;
            }

            ContractApproversDTO approverDTO = configApprovalsRepositoryImpl.getApprover(dto.getOrganizationId(), dto.getJobApproverId(), reportDate);
            if (approverDTO != null && !approverDTO.getEmployeeId().equals(empId) && !Utils.NVL(posId).equals(approverDTO.getPositionId())) {
                approverDTO.setDocumentNo(dto.getDocumentNo());
                if (positionName != null) {
                    approverDTO.setPositionName(positionName);
                }
                return approverDTO;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportData(ConfigApprovalsDTO dto) throws Exception {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        List<Map<String, Object>> listDataExport = configApprovalsRepositoryImpl.getDataConfigApproval(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        String pathTemplate = "template/export/moduleName/BM_Xuat_DS_CauHinhThongTinNguoiKyNguoiPheDuyet.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_CauHinhThongTinNguoiKyNguoiPheDuyet.xlsx");
    }

}
