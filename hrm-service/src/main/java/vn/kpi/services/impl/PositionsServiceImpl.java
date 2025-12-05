/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.PositionsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.PositionsResponse;
import vn.kpi.repositories.entity.EmployeesEntity;
import vn.kpi.repositories.entity.JobsEntity;
import vn.kpi.repositories.entity.PositionsEntity;
import vn.kpi.repositories.impl.PositionsRepository;
import vn.kpi.repositories.jpa.PositionsRepositoryJPA;
import vn.kpi.services.PositionsService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Lop impl service ung voi bang hr_positions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PositionsServiceImpl implements PositionsService {

    private final PositionsRepository positionsRepository;
    private final PositionsRepositoryJPA positionsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<PositionsResponse.SearchResult> searchData(PositionsRequest.SearchForm dto) {
        return positionsRepository.searchData(dto);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Boolean> saveData(PositionsRequest.SubmitForm dto, Long positionId) throws BaseAppException {
        String userName = Utils.getUserNameLogin();
        Map<Integer, String> mapJob = positionsRepository.getMapData("jobId", "name", JobsEntity.class, "jobType", dto.getJobType());
        dto.getJobIds().forEach(jobId -> {
            PositionsEntity positionsEntity = positionsRepositoryJPA.findByOrganizationIdAndJobId(dto.getOrganizationId(), jobId);
            if (positionsEntity == null) {
                positionsEntity = new PositionsEntity();
                positionsEntity.setCreatedTime(new Date());
                positionsEntity.setCreatedBy(userName);
                positionsEntity.setJobId(jobId);
                positionsEntity.setName(mapJob.get(Integer.parseInt(jobId.toString())));
                positionsEntity.setOrganizationId(dto.getOrganizationId());
            } else {
                positionsEntity.setName(mapJob.get(Integer.parseInt(jobId.toString())));
                positionsEntity.setModifiedTime(new Date());
                positionsEntity.setModifiedBy(userName);
            }
            positionsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            positionsRepositoryJPA.save(positionsEntity);
        });
        return ResponseUtils.ok(true);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long id) throws BaseAppException {
        PositionsEntity positionsEntity = positionsRepository.get(PositionsEntity.class, id);
        if (positionsEntity == null || BaseConstants.STATUS.DELETED.equals(positionsEntity.getIsDeleted())) {
            throw new RecordNotExistsException(id, PositionsEntity.class);
        }

        // check da su dung
        List<EmployeesEntity> listEmp = positionsRepository.findByProperties(EmployeesEntity.class, "positionId", id);
        if (!Utils.isNullOrEmpty(listEmp)) {
            throw new BaseAppException("ERROR_POSITION_USED", I18n.getMessage("error.positions.used"));
        }
        positionsRepository.deActiveObject(PositionsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PositionsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        PositionsResponse.DetailBean result = positionsRepository.getDataById(id);
        return ResponseUtils.ok(result);
    }

    @Override
    public ResponseEntity<Object> exportData(PositionsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = positionsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<PositionsResponse.DetailBean> getListByOrgId(Long organizationId, String jobType, List<String> listJobType) {
        return positionsRepository.getListDataByOrg(organizationId, jobType, listJobType);
    }

}
