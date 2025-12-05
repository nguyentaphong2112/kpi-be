/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.FeeRatio;
import vn.hbtplus.models.dto.OrderDetailDto;
import vn.hbtplus.models.request.OrderPayablesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.OrderPayablesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.CustomerCertificatesEntity;
import vn.hbtplus.repositories.entity.OrderPayablesEntity;
import vn.hbtplus.repositories.impl.OrderPayablesRepository;
import vn.hbtplus.repositories.jpa.OrderPayablesRepositoryJPA;
import vn.hbtplus.services.OrderPayablesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang crm_order_payables
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPayablesServiceImpl implements OrderPayablesService {

    private final OrderPayablesRepository orderPayablesRepository;
    private final OrderPayablesRepositoryJPA orderPayablesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OrderPayablesResponse> searchData(OrderPayablesRequest.SearchForm dto) {
        return ResponseUtils.ok(orderPayablesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OrderPayablesRequest.SubmitForm dto, Long id) throws BaseAppException {
        OrderPayablesEntity entity;
        if (id != null && id > 0L) {
            entity = orderPayablesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrderPayablesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setCareFee(dto.getCareFee());
        entity.setReferralFee(dto.getReferralFee());
        entity.setWelfareFee(dto.getWelfareFee());
        entity.setStatusId(OrderPayablesEntity.STATUS.CHO_PHE_DUYET);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        orderPayablesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getOrderPayableId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OrderPayablesEntity> optional = orderPayablesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrderPayablesEntity.class);
        }
        orderPayablesRepository.deActiveObject(OrderPayablesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OrderPayablesResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<OrderPayablesEntity> optional = orderPayablesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrderPayablesEntity.class);
        }
        OrderPayablesResponse dto = new OrderPayablesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(OrderPayablesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_tong_hop_phi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = orderPayablesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_tong_hop_phi.xlsx");
    }

    @Override
    public boolean makeList(OrderPayablesRequest.MakeListForm dto) {
        dto.setPeriodDate(Utils.getLastDay(dto.getPeriodDate()));
        //Kiem tra du lieu cua cac ky truoc do can duoc phe duyet het
        List<String> listPrePeriods = orderPayablesRepository.getListPrePeriodNotClosed(dto.getPeriodDate());
        if (!listPrePeriods.isEmpty()) {
            throw new BaseAppException(MessageFormat.format("Tồn tại dữ liệu của kỳ {0} chưa được phê duyệt", listPrePeriods.toString()));
        }

        List<OrderDetailDto> listOrdersEntities = orderPayablesRepository.getListOrderEntities(dto.getPeriodDate());
        if (Utils.isNullOrEmpty(listOrdersEntities)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        List<Long> customerIds = new ArrayList<>();
        listOrdersEntities.forEach(item -> {
            if (item.getCustomerReceiveId() != null && !customerIds.contains(item.getCustomerReceiveId())) {
                customerIds.add(item.getCustomerReceiveId());
            }
            if (item.getCustomerIntroducerId() != null && !customerIds.contains(item.getCustomerIntroducerId())) {
                customerIds.add(item.getCustomerIntroducerId());
            }
        });
        if (customerIds.isEmpty()) {
            throw new BaseAppException("Không tồn tại dữ liệu!");
        }
        //Lay ty le phi theo khach hang
        List<FeeRatio> listCustomerRatio = orderPayablesRepository.getRatioByCustomer(customerIds);
        Map<Long, Double> mapTylePhiChamSoc = new HashMap<>();
        Map<Long, Double> mapTylePhiGioiThieu = new HashMap<>();
        Map<Long, Double> mapTylePhiPhucLoi = new HashMap<>();
        listCustomerRatio.forEach(item -> {
            mapTylePhiChamSoc.put(item.getId(), item.getCareRatio());
            mapTylePhiGioiThieu.put(item.getId(), item.getReferralRatio());
            mapTylePhiPhucLoi.put(item.getId(), item.getWelfareRatio());
        });

        List<OrderPayablesEntity> orderPayablesEntities = new ArrayList<>();
        listOrdersEntities.forEach(ordersEntity -> {
            if(ordersEntity.getOrderId().equals(209L)){
                System.out.println("catch you");
            }
            //Neu nguoi gioi thieu = nguoi cham soc thi chi tao moi 1 ban ghi
            OrderPayablesEntity orderPayablesEntity = new OrderPayablesEntity(dto.getPeriodDate(), ordersEntity);
            orderPayablesEntity.setReceiverId(ordersEntity.getCustomerReceiveId());
            orderPayablesEntity.setCareFee(
                    Math.round(Utils.NVL(ordersEntity.getCaregiverAmount())
                               * Utils.NVL(Utils.min(mapTylePhiChamSoc.get(ordersEntity.getCustomerReceiveId()),
                            mapTylePhiGioiThieu.get(Utils.NVL(ordersEntity.getCustomerIntroducerId(), ordersEntity.getCustomerReceiveId()))
                    )) / 100)
                    - Utils.NVL(ordersEntity.getCareFeePayed())
            );
            orderPayablesEntity.setReferralFee(
                    Math.round(Utils.NVL(ordersEntity.getIntroducerAmount()) *
                               Utils.min(Utils.NVL(mapTylePhiGioiThieu.get(ordersEntity.getCustomerReceiveId()))
                               ) / 100)
                    - Utils.NVL(ordersEntity.getReferralFeePayed())

            );
            orderPayablesEntity.setWelfareFee(
                    Math.round(Utils.NVL(ordersEntity.getWelfareRecipientAmount()) *
                               Utils.min(Utils.NVL(mapTylePhiPhucLoi.get(ordersEntity.getCustomerReceiveId()))
                               ) / 100)
                    - Utils.NVL(ordersEntity.getWelfareFeePayed())
            );
            orderPayablesEntity.setStatusId(OrderPayablesEntity.STATUS.CHO_PHE_DUYET);
            if (orderPayablesEntity.existFee()) {
                orderPayablesEntities.add(orderPayablesEntity);
            }
        });
        //xoa du lieu chua phe duyet di
        orderPayablesRepository.deleteOldData(dto.getPeriodDate());
        orderPayablesRepository.insertBatch(OrderPayablesEntity.class, orderPayablesEntities, Utils.getUserNameLogin());
        return false;
    }

    @Override
    public ResponseEntity updateStatusById(OrderPayablesRequest.SubmitForm dto, Long id) throws RecordNotExistsException {
        Optional<OrderPayablesEntity> optional = orderPayablesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomerCertificatesEntity.class);
        }
        OrderPayablesEntity entity = optional.get();
        String userName = Utils.getUserNameLogin();
        if (OrderPayablesEntity.STATUS.PHE_DUYET.equalsIgnoreCase(dto.getStatusId())) {
            if (OrderPayablesEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatusId())
            ) {
                entity.setStatusId(dto.getStatusId());
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(userName);
            entity.setApprovedNote(dto.getApprovedNote());
            orderPayablesRepositoryJPA.save(entity);
        } else {
            entity.setStatusId(dto.getStatusId());
            entity.setApprovedNote(dto.getApprovedNote());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(userName);
            orderPayablesRepositoryJPA.save(entity);
        }
        return ResponseUtils.ok(id);
    }

    @Override
    public ResponseEntity approveAll(OrderPayablesRequest.SubmitForm dto) throws RecordNotExistsException {
        List<OrderPayablesEntity> listEntity = orderPayablesRepository.getListData(dto);
        if (Utils.isNullOrEmpty(listEntity)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        List<Long> ids = new ArrayList<>();
        for (OrderPayablesEntity entity : listEntity) {
            entity.setStatusId(OrderPayablesEntity.STATUS.PHE_DUYET);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            ids.add(entity.getOrderPayableId());
        }
        orderPayablesRepository.updateBatch(OrderPayablesEntity.class, listEntity, true);
        return ResponseUtils.ok(ids);
    }

}
