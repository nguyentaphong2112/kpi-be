/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.PaymentsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.PaymentsEntity;
import vn.hbtplus.repositories.impl.PaymentsRepository;
import vn.hbtplus.repositories.jpa.PaymentsRepositoryJPA;
import vn.hbtplus.services.PaymentsService;
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
 * Lop impl service ung voi bang crm_payments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class PaymentsServiceImpl implements PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentsRepositoryJPA paymentsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PaymentsResponse> searchData(PaymentsRequest.SearchForm dto) {
        return ResponseUtils.ok(paymentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(PaymentsRequest.SubmitForm dto) throws BaseAppException {
        PaymentsEntity entity;
        if (dto.getPaymentId() != null && dto.getPaymentId() > 0L) {
            entity = paymentsRepositoryJPA.getById(dto.getPaymentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new PaymentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        paymentsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getPaymentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<PaymentsEntity> optional = paymentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PaymentsEntity.class);
        }
        paymentsRepository.deActiveObject(PaymentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PaymentsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<PaymentsEntity> optional = paymentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PaymentsEntity.class);
        }
        PaymentsResponse dto = new PaymentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(PaymentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = paymentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
