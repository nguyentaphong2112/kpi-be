/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import com.aspose.words.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.ContractTemplatesDTO;
import vn.hbtplus.models.dto.OrgDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractTemplatesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractProposalsEntity;
import vn.hbtplus.repositories.entity.ContractTemplatesEntity;
import vn.hbtplus.repositories.entity.HrContractTypesEntity;
import vn.hbtplus.repositories.entity.TemplatePosGroupsEntity;
import vn.hbtplus.repositories.impl.ContractTemplatesRepositoryImpl;
import vn.hbtplus.repositories.jpa.ContractTemplatesRepositoryJPA;
import vn.hbtplus.repositories.jpa.TemplatePosGroupsRepositoryJPA;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.services.ContractTemplatesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang PNS_CONTRACT_TEMPLATES
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractTemplatesServiceImpl implements ContractTemplatesService {

    private final ContractTemplatesRepositoryImpl contractTemplatesRepositoryImpl;
    private final ContractTemplatesRepositoryJPA contractTemplatesRepositoryJPA;
    private final TemplatePosGroupsRepositoryJPA templatePosGroupsRepositoryJPA;
    private final CommonUtilsService commonUtilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ContractTemplatesResponse> searchData(ContractTemplatesDTO dto) {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        BaseDataTableDto<ContractTemplatesResponse> responses = contractTemplatesRepositoryImpl.searchData(dto);
        for (ContractTemplatesResponse e : responses.getListData()) {
            if (e.getPositionGroupStr() != null) {
                e.setPositionGroup(Arrays.asList(e.getPositionGroupStr().split(",")));
            }
        }
        return ResponseUtils.ok(responses);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveData(ContractTemplatesDTO dto, String userNameLogin) throws IOException {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        boolean isDuplicateName = contractTemplatesRepositoryImpl.duplicate(ContractTemplatesEntity.class, dto.getContractTemplateId(), "name", dto.getName());
        if (isDuplicateName) {
            String message = I18n.getMessage("contract.template.duplicate.name");
            throw new BaseAppException(message);
        }

        boolean isConflict = contractTemplatesRepositoryImpl.isConflictProcess(dto);
        if (isConflict) {
            String message = I18n.getMessage("contract.template.duplicate");
            throw new BaseAppException(message);
        }
        Date currentDate = new Date();
        ContractTemplatesEntity entity;
        String fileName = null;
        if(Utils.isNullObject(dto.getContractTemplateId())){
            entity = new ContractTemplatesEntity();
            entity.setCreatedBy(userNameLogin);
            entity.setCreatedTime(currentDate);
        } else {
            entity = contractTemplatesRepositoryImpl.get(ContractTemplatesEntity.class, dto.getContractTemplateId());
            if (!Utils.isNullObject(dto.getOrganizationId())) {
                entity.setOrgGroup(null);
            }
            if (!Utils.isNullObject(dto.getOrgGroup())) {
                entity.setOrganizationId(null);
            }
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userNameLogin);
            fileName = entity.getFileName();
        }
        BeanUtils.copyProperties(dto, entity);
        if (dto.getFileTemplate() != null){
            entity.setFileTemplate(dto.getFileTemplate().getBytes());
            entity.setFileName(dto.getFileTemplate().getOriginalFilename());
        } else {
            entity.setFileName(fileName);
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contractTemplatesRepositoryJPA.saveAndFlush(entity);
        // xu ly nhom chuc danh
        contractTemplatesRepositoryImpl.deleteById(entity.getContractTemplateId(), TemplatePosGroupsEntity.class, "contractTemplateId");
        List<TemplatePosGroupsEntity> listTemplatePosGroup = new ArrayList<>();
        if (dto.getPosition() != null && !dto.getPosition().isEmpty()) {
            for (Long positionGroupId : dto.getPosition()) {
                TemplatePosGroupsEntity templatePosGroupsEntity = new TemplatePosGroupsEntity();
                templatePosGroupsEntity.setContractTemplateId(entity.getContractTemplateId());
                templatePosGroupsEntity.setCreatedBy(userNameLogin);
                templatePosGroupsEntity.setCreatedTime(currentDate);
                templatePosGroupsEntity.setPositionGroupId(positionGroupId);
                listTemplatePosGroup.add(templatePosGroupsEntity);
            }
        }
        if (!listTemplatePosGroup.isEmpty()) {
            templatePosGroupsRepositoryJPA.saveAll(listTemplatePosGroup);
        }
        return ResponseUtils.ok(entity.getContractTemplateId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> deleteData(Long id) {
        Optional<ContractTemplatesEntity> optional = contractTemplatesRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException(I18n.getMessage("contract.template.not_found"));
        }
        contractTemplatesRepositoryImpl.deActiveObject(ContractTemplatesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) {
        ContractTemplatesResponse response = new ContractTemplatesResponse();
        Optional<ContractTemplatesEntity> optional = contractTemplatesRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException(I18n.getMessage("contract.template.not_found"));
        }
        ContractTemplatesEntity pnsContractTemplatesEntity = optional.get();
        BeanUtils.copyProperties(pnsContractTemplatesEntity, response);
        OrgDTO org = contractTemplatesRepositoryImpl.getOrg(pnsContractTemplatesEntity.getOrganizationId());
        response.setOrg(org);
        List<TemplatePosGroupsEntity> templatePosGroupsEntityList = contractTemplatesRepositoryImpl.findByProperties(TemplatePosGroupsEntity.class, "contractTemplateId", id);
        List<Long> position = templatePosGroupsEntityList.stream().map(TemplatePosGroupsEntity::getPositionGroupId).toList();
        response.setPosition(position);
        response.setFileTemplate(null);
        return ResponseUtils.ok(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<HrContractTypesEntity> getContractTypes() {
        List<HrContractTypesEntity> entities = contractTemplatesRepositoryImpl.findByProperties(HrContractTypesEntity.class, "order_number");
        return ResponseUtils.ok(entities);
    }

    @Override
    public byte[] downloadContractTemplates(Long contractTemplateId) {
        ContractTemplatesEntity contractTemplatesEntity = contractTemplatesRepositoryJPA.getById(contractTemplateId);
        return contractTemplatesEntity.getFileTemplate();
    }

    @Override
    @Transactional(readOnly = true)
    public Document getTemplateContract(ContractProposalsEntity entity) {
        try {
            ContractTemplatesResponse contractTemplatesDTO = contractTemplatesRepositoryImpl.getContractTemplate(entity.getContractTypeId(), entity.getOrganizationId(), entity.getPositionId(), entity.getFromDate());
            if (contractTemplatesDTO != null) {
                return new Document(contractTemplatesDTO.getFileTemplate().getBinaryStream());
            }
        } catch (Exception ex) {
            log.info("getTemplateContract|ex={}", ex.getMessage());
        }
        return null;
    }

    @Override
    public ResponseEntity<Object> exportData(ContractTemplatesDTO dto) throws Exception {
        List<Map<String, Object>> listData = contractTemplatesRepositoryImpl.getListContractTemplate(dto);
        if (listData.isEmpty()) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        String file = "BM_Xuat_DS_CauHinhBieuMauHD.xlsx";
        String pathTemplate = "template/export/moduleName/" + file;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listData);
        return ResponseUtils.ok(dynamicExport, file);
    }

}
