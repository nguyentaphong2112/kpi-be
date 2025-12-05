/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.ConfigSeqContractsDTO;
import vn.hbtplus.models.dto.OrgDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigSeqContractsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ConfigSeqContractsEntity;
import vn.hbtplus.repositories.entity.ConfigSeqDetailsEntity;
import vn.hbtplus.repositories.impl.ConfigSeqContractsRepositoryImpl;
import vn.hbtplus.repositories.impl.ContractTemplatesRepositoryImpl;
import vn.hbtplus.repositories.jpa.ConfigSeqContractsRepositoryJPA;
import vn.hbtplus.repositories.jpa.ConfigSeqDetailsRepositoryJPA;
import vn.hbtplus.services.ConfigSeqContractsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang PNS_CONFIG_SEQ_CONTRACTS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigSeqContractsServiceImpl implements ConfigSeqContractsService {

    private final ConfigSeqContractsRepositoryImpl configSeqContractsRepositoryImpl;
    private final ConfigSeqContractsRepositoryJPA configSeqContractsRepositoryJPA;
    private final ConfigSeqDetailsRepositoryJPA configSeqDetailsRepositoryJPA;
    private final ContractTemplatesRepositoryImpl contractTemplatesRepositoryImpl;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ConfigSeqContractsResponse> searchData(ConfigSeqContractsDTO dto) {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        BaseDataTableDto<ConfigSeqContractsResponse> responses = configSeqContractsRepositoryImpl.searchData(dto);
        List<ConfigSeqContractsResponse> listData = responses.getListData();
        for (ConfigSeqContractsResponse e : listData) {
            if (e.getContractTypeNameStr() != null) {
                e.setContractTypeName(Arrays.asList(e.getContractTypeNameStr().split(",")));
            }
        }
        return ResponseUtils.ok(responses);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveData(ConfigSeqContractsDTO dto) {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        ConfigSeqContractsEntity entityDuplicate = configSeqContractsRepositoryImpl.isDuplicate(dto);
        if (entityDuplicate != null) {
            if (entityDuplicate.getDocumentNo().equalsIgnoreCase(dto.getDocumentNo())
                || Utils.compareDate(dto.getFromDate(), entityDuplicate.getFromDate(), true)) {
                throw new BaseAppException("SAVE_DATA_DUPLICATE", I18n.getMessage("contract.template.duplicate"));
            } else { // neu các gia tri giong nhau ma khac ma van ban => ket thuc qua trinh cu
                entityDuplicate.setToDate(DateUtils.addDays(dto.getFromDate(), -1));
                entityDuplicate.setModifiedTime(new Date());
                entityDuplicate.setModifiedBy(Utils.getUserNameLogin());
                configSeqContractsRepositoryJPA.save(entityDuplicate);
            }
        }

        ConfigSeqContractsEntity entity;
        if (dto.getConfigSeqContractId() != null && dto.getConfigSeqContractId() > 0L) {
            entity = configSeqContractsRepositoryJPA.getById(dto.getConfigSeqContractId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            configSeqContractsRepositoryImpl.deleteById(dto.getConfigSeqContractId(), ConfigSeqDetailsEntity.class, "configSeqContractId");
        } else {
            entity = new ConfigSeqContractsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }

        Utils.copyProperties(entity, dto);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        configSeqContractsRepositoryJPA.save(entity);
        List<ConfigSeqDetailsEntity> configSeqDetailsEntities = new LinkedList<>();
        for (int i = 0; i < dto.getContractTypeIds().size(); i++) {
            Long contractTypeId = dto.getContractTypeIds().get(i);
            ConfigSeqDetailsEntity configSeqDetailsEntity = new ConfigSeqDetailsEntity();
            configSeqDetailsEntity.setConfigSeqContractId(entity.getConfigSeqContractId());
            configSeqDetailsEntity.setContractTypeId(contractTypeId);
            configSeqDetailsEntity.setOrderNumber(i + 1);
            configSeqDetailsEntity.setCreatedTime(new Date());
            configSeqDetailsEntity.setCreatedBy(Utils.getUserNameLogin());
            configSeqDetailsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            configSeqDetailsEntities.add(configSeqDetailsEntity);
        }
        if (!configSeqDetailsEntities.isEmpty()) {
            configSeqDetailsRepositoryJPA.saveAll(configSeqDetailsEntities);
        }
        return ResponseUtils.ok(entity.getConfigSeqContractId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> deleteData(Long id) {
        Optional<ConfigSeqContractsEntity> optional = configSeqContractsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException(I18n.getMessage("contract.template.not_found"));
        }
        configSeqContractsRepositoryImpl.deActiveObject(ConfigSeqContractsEntity.class, id);
        configSeqContractsRepositoryImpl.deleteById(id, ConfigSeqDetailsEntity.class, "configSeqContractId");
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) {
        Optional<ConfigSeqContractsEntity> optional = configSeqContractsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException(I18n.getMessage("contract.template.not_found"));
        }
        ConfigSeqContractsResponse dto = new ConfigSeqContractsResponse();
        Utils.copyProperties(dto, optional.get());
        List<ConfigSeqDetailsEntity> listConfigSeqDetail = configSeqContractsRepositoryImpl.findByProperties(ConfigSeqDetailsEntity.class, "configSeqContractId", id, "order_number");
        if (listConfigSeqDetail != null && !listConfigSeqDetail.isEmpty()) {
            dto.setContractTypeIds(listConfigSeqDetail.stream().map(ConfigSeqDetailsEntity::getContractTypeId).collect(Collectors.toList()));
        }
        if (dto.getOrganizationId() != null) {
            OrgDTO org = contractTemplatesRepositoryImpl.getOrg(dto.getOrganizationId());
            dto.setOrg(org);
        }
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ConfigSeqContractsDTO dto) throws Exception {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        List<Map<String, Object>> listData = configSeqContractsRepositoryImpl.getDataConfigSeq(dto);
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        for (Map<String, Object> item : listData) {
            if (item.get("CONTRACTTYPENAMESTR") != null) {
                StringTokenizer tokenizer = new StringTokenizer((String) item.get("contractTypeNameStr"), ",");
                StringBuilder newStr = new StringBuilder();
                while (tokenizer.hasMoreTokens()) {
                    newStr.append("+ ").append(tokenizer.nextToken()).append("\n");
                }
                item.put("CONTRACTTYPENAMESTR", newStr.toString());
            }

            if (item.get("ORGGROUP") == null) {
                item.put("SCOPE", "Đơn vị");
            } else {
                item.put("SCOPE", "Nhóm Đơn vị");
            }
        }

        String pathTemplate = "template/export/moduleName/BM_Xuat_DS_TringTuKyHD.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listData);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_TringTuKyHD.xlsx");
    }

}
