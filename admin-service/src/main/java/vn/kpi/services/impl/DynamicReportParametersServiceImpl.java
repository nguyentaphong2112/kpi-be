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
import vn.kpi.models.request.DynamicReportParametersRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.DynamicReportParametersResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.DynamicReportParametersEntity;
import vn.kpi.repositories.impl.DynamicReportParametersRepository;
import vn.kpi.repositories.jpa.DynamicReportParametersRepositoryJPA;
import vn.kpi.services.DynamicReportParametersService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang sys_dynamic_report_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class DynamicReportParametersServiceImpl implements DynamicReportParametersService {

    private final DynamicReportParametersRepository dynamicReportParametersRepository;
    private final DynamicReportParametersRepositoryJPA dynamicReportParametersRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DynamicReportParametersResponse> searchData(DynamicReportParametersRequest.SearchForm dto) {
        return ResponseUtils.ok(dynamicReportParametersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(DynamicReportParametersRequest.SubmitForm dto) throws BaseAppException {
        DynamicReportParametersEntity entity;
        if (dto.getDynamicReportParameterId() != null && dto.getDynamicReportParameterId() > 0L) {
            entity = dynamicReportParametersRepositoryJPA.getById(dto.getDynamicReportParameterId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new DynamicReportParametersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        dynamicReportParametersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getDynamicReportParameterId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<DynamicReportParametersEntity> optional = dynamicReportParametersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DynamicReportParametersEntity.class);
        }
        dynamicReportParametersRepository.deActiveObject(DynamicReportParametersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DynamicReportParametersResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<DynamicReportParametersEntity> optional = dynamicReportParametersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DynamicReportParametersEntity.class);
        }
        DynamicReportParametersResponse dto = new DynamicReportParametersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(DynamicReportParametersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = dynamicReportParametersRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
