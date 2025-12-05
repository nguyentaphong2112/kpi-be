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
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EducationPromotionsRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EducationPromotionsResponse;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.EducationPromotionsEntity;
import vn.kpi.repositories.impl.EducationPromotionsRepository;
import vn.kpi.repositories.jpa.EducationPromotionsRepositoryJPA;
import vn.kpi.services.EducationPromotionsService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_education_promotions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EducationPromotionsServiceImpl implements EducationPromotionsService {

    private final ObjectAttributesService objectAttributesService;
    private final EducationPromotionsRepository educationPromotionsRepository;
    private final EducationPromotionsRepositoryJPA educationPromotionsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EducationPromotionsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(educationPromotionsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EducationPromotionsRequest.SubmitForm dto, Long employeeId, Long educationPromotionId) throws BaseAppException {
        EducationPromotionsEntity entity;
        if (educationPromotionId != null && educationPromotionId > 0L) {
            entity = educationPromotionsRepositoryJPA.getById(educationPromotionId);
            if(!entity.getEmployeeId().equals(employeeId)){
                throw new BaseAppException("educationPromotionId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EducationPromotionsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        educationPromotionsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getEducationPromotionId(), dto.getListAttributes(), EducationPromotionsEntity.class, null);
        return ResponseUtils.ok(entity.getEducationPromotionId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId,Long id) throws BaseAppException {
        Optional<EducationPromotionsEntity> optional = educationPromotionsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EducationPromotionsEntity.class);
        }
        if(!optional.get().getEmployeeId().equals(employeeId)){
            throw new BaseAppException("educationPromotionId and employeeId not match!");
        }
        educationPromotionsRepository.deActiveObject(EducationPromotionsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EducationPromotionsResponse.DetailBean> getDataById(Long employeeId,Long id) throws BaseAppException {
        Optional<EducationPromotionsEntity> optional = educationPromotionsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EducationPromotionsEntity.class);
        }
        if(!optional.get().getEmployeeId().equals(employeeId)){
            throw new BaseAppException("educationPromotionId and employeeId not match!");
        }
        EducationPromotionsResponse.DetailBean dto = new EducationPromotionsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, educationPromotionsRepository.getSQLTableName(EducationPromotionsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/thong-tin-hoc-ham.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = educationPromotionsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-hoc-ham.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<EducationPromotionsResponse.DetailBean> tableDto = educationPromotionsRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getEducationPromotionId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_education_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getEducationPromotionId()));
        });
        return tableDto;
    }

}
