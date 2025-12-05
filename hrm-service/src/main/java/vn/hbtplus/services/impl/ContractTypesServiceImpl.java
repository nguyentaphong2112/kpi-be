/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.ContractTypesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractTypesResponse;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractTypesEntity;
import vn.hbtplus.repositories.impl.ContractTypesRepository;
import vn.hbtplus.repositories.jpa.ContractTypesRepositoryJPA;
import vn.hbtplus.services.ContractTypesService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_contract_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class ContractTypesServiceImpl implements ContractTypesService {

    private final ContractTypesRepository contractTypesRepository;
    private final ContractTypesRepositoryJPA contractTypesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ContractTypesResponse.SearchResult> searchData(ContractTypesRequest.SearchForm dto) {
        return ResponseUtils.ok(contractTypesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ContractTypesRequest.SubmitForm dto, Long contractTypeId) throws BaseAppException {


        boolean isDuplicate = contractTypesRepository.duplicate(ContractTypesEntity.class, contractTypeId, "code", dto.getCode(), "empTypeId", dto.getEmpTypeId(), "classifyCode", dto.getClassifyCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CONTRACT_DUPLICATE", I18n.getMessage("error.contractTypes.code.duplicate"));
        }

        isDuplicate = contractTypesRepository.duplicate(ContractTypesEntity.class, contractTypeId, "name", dto.getName(), "empTypeId", dto.getEmpTypeId(), "classifyCode", dto.getClassifyCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CONTRACT_DUPLICATE", I18n.getMessage("error.contractTypes.name.duplicate"));
        }


        ContractTypesEntity entity;
        if (contractTypeId != null && contractTypeId > 0L) {
            entity = contractTypesRepositoryJPA.getById(contractTypeId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ContractTypesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contractTypesRepositoryJPA.save(entity);
        contractTypesRepositoryJPA.flush();

        objectAttributesService.saveObjectAttributes(entity.getContractTypeId(), dto.getListAttributes(), ContractTypesEntity.class, null);
        return ResponseUtils.ok(entity.getContractTypeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ContractTypesEntity> optional = contractTypesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ContractTypesEntity.class);
        }
        contractTypesRepository.deActiveObject(ContractTypesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ContractTypesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<ContractTypesEntity> optional = contractTypesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ContractTypesEntity.class);
        }
        ContractTypesResponse.DetailBean dto = new ContractTypesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, contractTypesRepository.getSQLTableName(ContractTypesEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ContractTypesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = contractTypesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<ContractTypesResponse.DetailBean> getListData(String classifyCode, Long empTypeId, boolean isGetAttribute) {
        List<ContractTypesResponse.DetailBean> results = contractTypesRepository.getListContractType(classifyCode, empTypeId);
        if (!isGetAttribute || results.isEmpty()) {
            return results;
        }

        List<Long> ids = results.stream().map(ContractTypesResponse.DetailBean::getContractTypeId).toList();
        Map<Long, List<ObjectAttributesResponse>> maps = objectAttributesService.getListMapAttributes(ids, "hr_contract_types");
        results.forEach(item -> item.setListAttributes(maps.get(item.getContractTypeId())));
        return results;
    }

}
