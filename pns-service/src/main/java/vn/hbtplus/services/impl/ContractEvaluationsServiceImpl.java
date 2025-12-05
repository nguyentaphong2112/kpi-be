/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.dto.ContractEvaluationsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractEvaluationsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractEvaluationsEntity;
import vn.hbtplus.repositories.impl.ContractEvaluationsRepositoryImpl;
import vn.hbtplus.repositories.jpa.ContractEvaluationsRepositoryJPA;
import vn.hbtplus.services.ContractEvaluationsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Lop impl service ung voi bang PNS_CONTRACT_EVALUATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ContractEvaluationsServiceImpl implements ContractEvaluationsService {

    private final ContractEvaluationsRepositoryImpl contractEvaluationsRepositoryImpl;
    private final ContractEvaluationsRepositoryJPA contractEvaluationsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ContractEvaluationsResponse> searchData(ContractEvaluationsDTO dto) {
        return ResponseUtils.ok(contractEvaluationsRepositoryImpl.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveData(ContractEvaluationsDTO dto, List<MultipartFile> files) {
        ContractEvaluationsEntity entity;
        if (dto.getContractEvaluationId() != null && dto.getContractEvaluationId() > 0L) {
            entity = contractEvaluationsRepositoryJPA.getById(dto.getContractEvaluationId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ContractEvaluationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(entity, dto);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contractEvaluationsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getContractEvaluationId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> deleteData(Long id) {
        Optional<ContractEvaluationsEntity> optional = contractEvaluationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("global.not_found");
        }
        contractEvaluationsRepositoryImpl.deActiveObject(ContractEvaluationsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) {
        Optional<ContractEvaluationsEntity> optional = contractEvaluationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("global.not_found");
        }
        ContractEvaluationsResponse dto = new ContractEvaluationsResponse();
        Utils.copyProperties(dto, optional.get());
        return ResponseUtils.ok(dto);
    }

}
