/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.models.request.OrganizationWorkPlanningsRequest;
import vn.kpi.models.response.*;
import vn.kpi.repositories.entity.OrganizationWorkPlanningsEntity;
import vn.kpi.repositories.impl.OrganizationWorkPlanningsRepository;
import vn.kpi.repositories.jpa.OrganizationWorkPlanningsRepositoryJPA;
import vn.kpi.services.OrganizationWorkPlanningsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.ExportExcel;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang kpi_organization_work_plannings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class OrganizationWorkPlanningsServiceImpl implements OrganizationWorkPlanningsService {

    private final OrganizationWorkPlanningsRepository organizationWorkPlanningsRepository;
    private final OrganizationWorkPlanningsRepositoryJPA organizationWorkPlanningsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OrganizationWorkPlanningsResponse> searchData(OrganizationWorkPlanningsRequest.SearchForm dto) {
        return ResponseUtils.ok(organizationWorkPlanningsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OrganizationWorkPlanningsRequest.SubmitForm dto, Long id) throws BaseAppException {
        OrganizationWorkPlanningsEntity entity;
        if (id != null && id > 0L) {
            entity = organizationWorkPlanningsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrganizationWorkPlanningsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setContent(entity.getContent());
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        organizationWorkPlanningsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getOrganizationWorkPlanningId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OrganizationWorkPlanningsEntity> optional = organizationWorkPlanningsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationWorkPlanningsEntity.class);
        }
        organizationWorkPlanningsRepository.deActiveObject(OrganizationWorkPlanningsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OrganizationWorkPlanningsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<OrganizationWorkPlanningsEntity> optional = organizationWorkPlanningsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationWorkPlanningsEntity.class);
        }
        OrganizationWorkPlanningsResponse.DetailBean dto = new OrganizationWorkPlanningsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ListResponseEntity<OrganizationWorkPlanningsEntity> getDataByEvaluationId(Long id) throws RecordNotExistsException {
        List<OrganizationWorkPlanningsEntity> organizationWorkPlanningsEntityList = organizationWorkPlanningsRepository.getListOrganizationWorkPlanning(id);
        return ResponseUtils.ok(organizationWorkPlanningsEntityList);
    }

    @Override
    public ResponseEntity<Object> exportData(OrganizationWorkPlanningsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = organizationWorkPlanningsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<OrganizationWorkPlanningsEntity> getOrgPlanning(Long periodId, List<Long> organizationIds) {
        return organizationWorkPlanningsRepository.getListOrganizationWorkPlanning(periodId, organizationIds);
    }

}
