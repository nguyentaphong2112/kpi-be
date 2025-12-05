/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.tax.personal.models.request.LockRegistrationsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.tax.personal.models.response.LockRegistrationsResponse;
import vn.hbtplus.tax.personal.repositories.entity.LockRegistrationsEntity;
import vn.hbtplus.tax.personal.repositories.impl.LockRegistrationsRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.jpa.LockRegistrationsRepositoryJPA;
import vn.hbtplus.tax.personal.services.LockRegistrationsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Lop impl service ung voi bang PTX_LOCK_REGISTRATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class LockRegistrationsServiceImpl implements LockRegistrationsService {

    private final LockRegistrationsRepositoryImpl lockRegistrationsRepositoryImpl;
    private final LockRegistrationsRepositoryJPA lockRegistrationsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<LockRegistrationsResponse> searchData(LockRegistrationsDTO dto) {
        return ResponseUtils.ok(lockRegistrationsRepositoryImpl.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveData(LockRegistrationsDTO dto, List<MultipartFile> files) {
        LockRegistrationsEntity entity;
        if (dto.getLockRegistrationId() != null && dto.getLockRegistrationId() > 0L) {
            entity = lockRegistrationsRepositoryJPA.getById(dto.getLockRegistrationId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new LockRegistrationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(entity, dto);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        lockRegistrationsRepositoryJPA.save(entity);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteData(Long id) {
        Optional<LockRegistrationsEntity> optional = lockRegistrationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || optional.get().getIsDeleted().equals(BaseConstants.STATUS.NOT_DELETED)) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        lockRegistrationsRepositoryImpl.deActiveObject(LockRegistrationsEntity.class, id);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<LockRegistrationsResponse> getDataById(Long id) {
        Optional<LockRegistrationsEntity> optional = lockRegistrationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || optional.get().getIsDeleted().equals(BaseConstants.STATUS.NOT_DELETED)) {
            throw new RecordNotExistsException(id, LockRegistrationsEntity.class);
        }
        LockRegistrationsResponse dto = new LockRegistrationsResponse();
        Utils.copyProperties(dto, optional.get());
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidPeriod(Date dateRemind) {
        return lockRegistrationsRepositoryImpl.countRemindRegisterTax(dateRemind) > 0;
    }
}
