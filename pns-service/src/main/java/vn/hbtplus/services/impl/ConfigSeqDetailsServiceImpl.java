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
import vn.hbtplus.models.dto.ConfigSeqDetailsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigSeqDetailsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ConfigSeqDetailsEntity;
import vn.hbtplus.repositories.impl.ConfigSeqDetailsRepositoryImpl;
import vn.hbtplus.repositories.jpa.ConfigSeqDetailsRepositoryJPA;
import vn.hbtplus.services.ConfigSeqDetailsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Lop impl service ung voi bang PNS_CONFIG_SEQ_DETAILS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ConfigSeqDetailsServiceImpl implements ConfigSeqDetailsService {

    private final ConfigSeqDetailsRepositoryImpl configSeqDetailsRepositoryImpl;
    private final ConfigSeqDetailsRepositoryJPA configSeqDetailsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ConfigSeqDetailsResponse> searchData(ConfigSeqDetailsDTO dto) {
        return ResponseUtils.ok(configSeqDetailsRepositoryImpl.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveData(ConfigSeqDetailsDTO dto, List<MultipartFile> files) {
        ConfigSeqDetailsEntity entity;
        if (dto.getConfigSeqDetailId() != null && dto.getConfigSeqDetailId() > 0L) {
            entity = configSeqDetailsRepositoryJPA.getById(dto.getConfigSeqDetailId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ConfigSeqDetailsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(entity, dto);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        configSeqDetailsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getConfigSeqDetailId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> deleteData(Long id) {
        Optional<ConfigSeqDetailsEntity> optional = configSeqDetailsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("global.not_found");
        }
        configSeqDetailsRepositoryImpl.deActiveObject(ConfigSeqDetailsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) {
        Optional<ConfigSeqDetailsEntity> optional = configSeqDetailsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("global.not_found");
        }
        ConfigSeqDetailsResponse dto = new ConfigSeqDetailsResponse();
        Utils.copyProperties(dto, optional.get());
        return ResponseUtils.ok(dto);
    }

}
