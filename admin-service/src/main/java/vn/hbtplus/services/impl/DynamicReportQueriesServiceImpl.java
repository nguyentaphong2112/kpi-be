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
import vn.hbtplus.models.request.DynamicReportQueriesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.DynamicReportQueriesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.DynamicReportQueriesEntity;
import vn.hbtplus.repositories.impl.DynamicReportQueriesRepository;
import vn.hbtplus.repositories.jpa.DynamicReportQueriesRepositoryJPA;
import vn.hbtplus.services.DynamicReportQueriesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang sys_dynamic_report_queries
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class DynamicReportQueriesServiceImpl implements DynamicReportQueriesService {

    private final DynamicReportQueriesRepository dynamicReportQueriesRepository;
    private final DynamicReportQueriesRepositoryJPA dynamicReportQueriesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DynamicReportQueriesResponse> searchData(DynamicReportQueriesRequest.SearchForm dto) {
        return ResponseUtils.ok(dynamicReportQueriesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(DynamicReportQueriesRequest.SubmitForm dto) throws BaseAppException {
        DynamicReportQueriesEntity entity = new DynamicReportQueriesEntity();
        if (dto.getDynamicReportQueryId() != null && dto.getDynamicReportQueryId() > 0L) {
            entity = dynamicReportQueriesRepositoryJPA.getById(dto.getDynamicReportQueryId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new DynamicReportQueriesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        dynamicReportQueriesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getDynamicReportQueryId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<DynamicReportQueriesEntity> optional = dynamicReportQueriesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DynamicReportQueriesEntity.class);
        }
        dynamicReportQueriesRepository.deActiveObject(DynamicReportQueriesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DynamicReportQueriesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<DynamicReportQueriesEntity> optional = dynamicReportQueriesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DynamicReportQueriesEntity.class);
        }
        DynamicReportQueriesResponse dto = new DynamicReportQueriesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(DynamicReportQueriesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = dynamicReportQueriesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
