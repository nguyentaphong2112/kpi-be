/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.OrderDetailsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.OrderDetailsEntity;
import vn.hbtplus.repositories.impl.OrderDetailsRepository;
import vn.hbtplus.repositories.jpa.OrderDetailsRepositoryJPA;
import vn.hbtplus.services.OrderDetailsService;
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
 * Lop impl service ung voi bang crm_order_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class OrderDetailsServiceImpl implements OrderDetailsService {

    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderDetailsRepositoryJPA orderDetailsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OrderDetailsResponse> searchData(OrderDetailsRequest.SearchForm dto) {
        return ResponseUtils.ok(orderDetailsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OrderDetailsRequest.SubmitForm dto) throws BaseAppException {
        OrderDetailsEntity entity;
        if (dto.getOrderDetailId() != null && dto.getOrderDetailId() > 0L) {
            entity = orderDetailsRepositoryJPA.getById(dto.getOrderDetailId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrderDetailsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        orderDetailsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getOrderDetailId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OrderDetailsEntity> optional = orderDetailsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrderDetailsEntity.class);
        }
        orderDetailsRepository.deActiveObject(OrderDetailsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OrderDetailsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<OrderDetailsEntity> optional = orderDetailsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrderDetailsEntity.class);
        }
        OrderDetailsResponse dto = new OrderDetailsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(OrderDetailsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = orderDetailsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
