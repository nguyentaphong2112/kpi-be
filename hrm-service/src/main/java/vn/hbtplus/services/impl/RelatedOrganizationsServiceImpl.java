/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.RelatedOrganizationsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.RelatedOrganizationsEntity;
import vn.hbtplus.repositories.impl.RelatedOrganizationsRepository;
import vn.hbtplus.repositories.jpa.RelatedOrganizationsRepositoryJPA;
import vn.hbtplus.services.RelatedOrganizationsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_related_organizations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class RelatedOrganizationsServiceImpl implements RelatedOrganizationsService {

    private final RelatedOrganizationsRepository relatedOrganizationsRepository;
    private final RelatedOrganizationsRepositoryJPA relatedOrganizationsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<RelatedOrganizationsResponse> searchData(RelatedOrganizationsRequest.SearchForm dto) {
        return ResponseUtils.ok(relatedOrganizationsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(RelatedOrganizationsRequest.SubmitForm dto) throws BaseAppException {
        RelatedOrganizationsEntity entity;
        if (dto.getRelatedOrganizationId() != null && dto.getRelatedOrganizationId() > 0L) {
            entity = relatedOrganizationsRepositoryJPA.getById(dto.getRelatedOrganizationId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new RelatedOrganizationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        relatedOrganizationsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getRelatedOrganizationId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<RelatedOrganizationsEntity> optional = relatedOrganizationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, RelatedOrganizationsEntity.class);
        }
        relatedOrganizationsRepository.deActiveObject(RelatedOrganizationsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<RelatedOrganizationsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<RelatedOrganizationsEntity> optional = relatedOrganizationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, RelatedOrganizationsEntity.class);
        }
        RelatedOrganizationsResponse dto = new RelatedOrganizationsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(RelatedOrganizationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = relatedOrganizationsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
