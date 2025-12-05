/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.OrgConfigsRequest;
import vn.kpi.models.response.*;
import vn.kpi.repositories.entity.OrgConfigsEntity;
import vn.kpi.repositories.impl.OrgConfigsRepository;
import vn.kpi.repositories.jpa.OrgConfigsRepositoryJPA;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.OrgConfigsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.ExportExcel;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.utils.Utils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang kpi_org_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class OrgConfigsServiceImpl implements OrgConfigsService {

    private final OrgConfigsRepository orgConfigsRepository;
    private final OrgConfigsRepositoryJPA orgConfigsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OrgConfigsResponse> searchData(OrgConfigsRequest.SearchForm dto) {
        BaseDataTableDto<OrgConfigsResponse> results = orgConfigsRepository.searchData(dto);
        for(OrgConfigsResponse orgConfig : results.getListData()){
            List<ObjectAttributesResponse> listAttributes = objectAttributesService.getAttributes(orgConfig.getOrgConfigId(), orgConfigsRepository.getSQLTableName(OrgConfigsEntity.class));
            orgConfig.setListAttributes(listAttributes);
        }
        return ResponseUtils.ok(results);
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OrgConfigsRequest.SubmitForm dto, Long id) throws BaseAppException {
        OrgConfigsEntity entity;
        if (id != null && id > 0L) {
            entity = orgConfigsRepositoryJPA.getById(dto.getOrgConfigId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrgConfigsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        orgConfigsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getOrgConfigId(), dto.getListAttributes(), OrgConfigsEntity.class, null);
        return ResponseUtils.ok(entity.getOrgConfigId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OrgConfigsEntity> optional = orgConfigsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrgConfigsEntity.class);
        }
        orgConfigsRepository.deActiveObject(OrgConfigsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OrgConfigsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<OrgConfigsEntity> optional = orgConfigsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrgConfigsEntity.class);
        }
        OrgConfigsResponse dto = new OrgConfigsResponse();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, orgConfigsRepository.getSQLTableName(OrgConfigsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(OrgConfigsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = orgConfigsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
