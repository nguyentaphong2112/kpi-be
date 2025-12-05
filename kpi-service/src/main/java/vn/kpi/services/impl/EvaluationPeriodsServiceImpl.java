/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.Constant;
import vn.kpi.models.request.EvaluationPeriodsRequest;
import vn.kpi.models.response.*;
import vn.kpi.repositories.entity.EmployeeEvaluationsEntity;
import vn.kpi.repositories.entity.EvaluationPeriodsEntity;
import vn.kpi.repositories.entity.OrganizationEvaluationsEntity;
import vn.kpi.repositories.impl.EvaluationPeriodsRepository;
import vn.kpi.repositories.jpa.EvaluationPeriodsRepositoryJPA;
import vn.kpi.services.EvaluationPeriodsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.ExportExcel;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang kpi_evaluation_periods
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EvaluationPeriodsServiceImpl implements EvaluationPeriodsService {

    private final EvaluationPeriodsRepository evaluationPeriodsRepository;
    private final EvaluationPeriodsRepositoryJPA evaluationPeriodsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EvaluationPeriodsResponse.SearchResult> searchData(EvaluationPeriodsRequest.SearchForm dto) {
        return ResponseUtils.ok(evaluationPeriodsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EvaluationPeriodsRequest.SubmitForm dto, Long id) throws BaseAppException {
        EvaluationPeriodsEntity entity;
        if (id != null && id > 0L) {
            entity = evaluationPeriodsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EvaluationPeriodsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        evaluationPeriodsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getEvaluationPeriodId(), dto.getListAttributes(), EvaluationPeriodsEntity.class, null);
        return ResponseUtils.ok(entity.getEvaluationPeriodId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EvaluationPeriodsEntity> optional = evaluationPeriodsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EvaluationPeriodsEntity.class);
        }
        evaluationPeriodsRepository.deActiveObject(EvaluationPeriodsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EvaluationPeriodsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<EvaluationPeriodsEntity> optional = evaluationPeriodsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EvaluationPeriodsEntity.class);
        }
        EvaluationPeriodsResponse.DetailBean dto = new EvaluationPeriodsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
//        dto.setListAttributes(objectAttributesService.getAttributes(id, "kpi_evaluation_periods"));
        return ResponseUtils.ok(dto);
    }

    @Override
    public BaseResponseEntity<EvaluationPeriodsResponse.MaxYear> getDataByMaxYear() throws RecordNotExistsException {
        return ResponseUtils.ok(evaluationPeriodsRepository.getDataByMaxYear());
    }

    @Override
    public ResponseEntity<Object> exportData(EvaluationPeriodsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = evaluationPeriodsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    @Transactional
    public boolean initData(Long id) {
        String userName = Utils.getUserNameLogin();
        //insert du lieu danh sach don vi
        evaluationPeriodsRepository.initOrganization(id, userName);
        //insert du lieu danh sach nhan vien
        evaluationPeriodsRepository.initEmployee(id, userName);
        EvaluationPeriodsRequest.Status dto = new EvaluationPeriodsRequest.Status();
        dto.setStatus(Constant.STATUS.LAP_DANH_SACH);
        updateStatusById(dto, id);
        return true;
    }

    @Override
    public ResponseEntity updateStatusById(EvaluationPeriodsRequest.Status dto, Long evaluationPeriodId) throws RecordNotExistsException {
        Optional<EvaluationPeriodsEntity> optional = evaluationPeriodsRepositoryJPA.findById(evaluationPeriodId);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(evaluationPeriodId, EvaluationPeriodsEntity.class);
        }
        List<EmployeeEvaluationsEntity> employeeEvaluationsEntityList;
        List<OrganizationEvaluationsEntity> orgEvaluationsEntityList;
        if (dto.getStatus().equals(Constant.STATUS.CHOT_DU_LIEU1)) {
           boolean checkEmployee =
                    evaluationPeriodsRepository.checkEmployeeEvaluations(evaluationPeriodId, Constant.STATUS.PHE_DUYET);
            boolean checkOrg =
                    evaluationPeriodsRepository.checkOrganizationEvaluations(evaluationPeriodId, Constant.STATUS.PHE_DUYET);
            if (checkEmployee || checkOrg) {
                throw new BaseAppException("ERROR_EVALUATION_PERIOD_APPROVE", I18n.getMessage("error.evaluationPeriod.approve"));
            }
        }
//        if (dto.getStatus().equals(Constant.STATUS.CHOT_DU_LIEU2)) {
//            employeeEvaluationsEntityList =
//                    evaluationPeriodsRepository.findAllByProperties(EmployeeEvaluationsEntity.class, "evaluationPeriodId", evaluationPeriodId, "status", Constant.STATUS.DE_NGHI_DIEU_CHINH);
//            orgEvaluationsEntityList =
//                    evaluationPeriodsRepository.findAllByProperties(OrganizationEvaluationsEntity.class, "evaluationPeriodId", evaluationPeriodId, "status", Constant.STATUS.DE_NGHI_DIEU_CHINH);
//            if (!Utils.isNullOrEmpty(employeeEvaluationsEntityList) || !Utils.isNullOrEmpty(orgEvaluationsEntityList)) {
//                throw new BaseAppException("ERROR_EVALUATION_PERIOD_ADJUST", I18n.getMessage("error.evaluationPeriod.adjust"));
//            }
//        }
        if (dto.getStatus().equals(Constant.STATUS.CHOT_KET_QUA)) {
            boolean checkEmployee =
                    evaluationPeriodsRepository.checkEmployeeEvaluations(evaluationPeriodId, Constant.STATUS.DANH_GIA);
            boolean checkOrg =
                    evaluationPeriodsRepository.checkOrganizationEvaluations(evaluationPeriodId, Constant.STATUS.DANH_GIA);
            if (checkEmployee || checkOrg) {
                throw new BaseAppException("ERROR_EVALUATION_PERIOD_EVALUATE", I18n.getMessage("error.evaluationPeriod.evaluate"));
            }
        }
        optional.get().setStatus(dto.getStatus());
        evaluationPeriodsRepositoryJPA.save(optional.get());
        return ResponseUtils.ok(evaluationPeriodId);
    }

}
