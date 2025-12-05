/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.dto.TemplateParamsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.repositories.entity.TemplateParamsEntity;
import vn.hbtplus.repositories.impl.TemplateParamsRepositoryImpl;
import vn.hbtplus.repositories.jpa.TemplateParamsRepositoryJPA;
import vn.hbtplus.services.TemplateParamsService;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Lop impl service ung voi bang PNS_TEMPLATE_PARAMS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class TemplateParamsServiceImpl implements TemplateParamsService {

    private final TemplateParamsRepositoryImpl templateParamsRepositoryImpl;
    private final TemplateParamsRepositoryJPA templateParamsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<TemplateParamsEntity> searchData(TemplateParamsDTO dto) {
        List<TemplateParamsEntity> listTemplateParams = templateParamsRepositoryImpl.findAll(TemplateParamsEntity.class);
        return ResponseUtils.ok(listTemplateParams);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveData(TemplateParamsDTO dto) {
        String userNameLogin = Utils.getUserNameLogin();
        if (dto.getTemplateParamId() == null) {
            return createTemplateParams(dto, userNameLogin);
        } else {
            return updateTemplateParams(dto, userNameLogin);
        }
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> deleteData(Long id) {
        Optional<TemplateParamsEntity> optional = templateParamsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("TemplateParamsEntity is null");
        }
        templateParamsRepositoryImpl.deActiveObject(TemplateParamsEntity.class, id);
        return ResponseUtils.ok(id);

    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) {
        Optional<TemplateParamsEntity> optional = templateParamsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("TemplateParamsEntity is null");
        }
        return ResponseUtils.ok(optional.get());
    }

    @Transactional
    public BaseResponseEntity<Object> createTemplateParams(TemplateParamsDTO form, String userNameLogin) {
        Date createdDate = new Date();
        List<TemplateParamsEntity> templateParamsEntityList = templateParamsRepositoryImpl.findByProperties(TemplateParamsEntity.class, "code", form.getCode());
        if (!Utils.isNullOrEmpty(templateParamsEntityList)) {
            throw new BaseAppException(I18n.getMessage("msg.record.exists"));
        }
        TemplateParamsEntity entity = new TemplateParamsEntity();
        BeanUtils.copyProperties(form, entity);
        entity.setCreatedBy(userNameLogin);
        entity.setCreatedTime(createdDate);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        templateParamsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getTemplateParamId());
    }

    @Transactional
    public BaseResponseEntity<Object> updateTemplateParams(TemplateParamsDTO form, String userNameLogin) {
        Date updatedDate = new Date();
        boolean isDuplicate = templateParamsRepositoryImpl.duplicate(TemplateParamsEntity.class, form.getTemplateParamId(), "code", form.getCode());
        if (isDuplicate) {
            throw new BaseAppException(I18n.getMessage("msg.record.exists"));
        }
        TemplateParamsEntity entity = templateParamsRepositoryJPA.getById(form.getTemplateParamId());
        BeanUtils.copyProperties(form, entity);
        entity.setModifiedBy(userNameLogin);
        entity.setModifiedTime(updatedDate);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        templateParamsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getTemplateParamId());
    }

}
