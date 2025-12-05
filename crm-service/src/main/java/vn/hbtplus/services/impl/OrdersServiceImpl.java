/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.request.FamilyRelationshipsRequest;
import vn.hbtplus.models.request.OrderDetailsRequest;
import vn.hbtplus.models.request.OrdersRequest;
import vn.hbtplus.models.request.PaymentsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.OrderDetailsRepository;
import vn.hbtplus.repositories.impl.OrdersRepository;
import vn.hbtplus.repositories.impl.PaymentsRepository;
import vn.hbtplus.repositories.jpa.CustomersRepositoryJPA;
import vn.hbtplus.repositories.jpa.OrderDetailsRepositoryJPA;
import vn.hbtplus.repositories.jpa.OrdersRepositoryJPA;
import vn.hbtplus.repositories.jpa.PaymentsRepositoryJPA;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.LogActionsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.OrdersService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.I18n;
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
 * Lop impl service ung voi bang crm_orders
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final LogActionsService logActionsService;
    private final OrdersRepository ordersRepository;
    private final OrdersRepositoryJPA ordersRepositoryJPA;
    private final CustomersRepositoryJPA customersRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderDetailsRepositoryJPA orderDetailsRepositoryJPA;
    private final PaymentsRepository paymentsRepository;
    private final PaymentsRepositoryJPA paymentsRepositoryJPA;
    private final FileService fileService;
    private final AttachmentServiceImpl attachmentServiceImpl;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OrdersResponse> searchData(OrdersRequest.SearchForm dto) {
        return ResponseUtils.ok(ordersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OrdersRequest.SubmitForm dto) throws BaseAppException {
        OrdersEntity entity;
        boolean isUpdate = false;
        OrdersEntity oldEntity = new OrdersEntity();
        if (dto.getOrderId() != null && dto.getOrderId() > 0L) {
            entity = ordersRepository.get(OrdersEntity.class, dto.getOrderId());
            Utils.copyProperties(entity, oldEntity);
            isUpdate = true;
            dto.setOrderNo(entity.getOrderNo());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OrdersEntity();
            dto.setOrderNo(generateCode("ĐH"));
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
//        double totalAmount = dto.getOrderDetails().stream()
//                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
//                .sum();
//        double finalAmount = dto.getOrderDetails().stream()
//                .mapToDouble(OrderDetailsRequest.SubmitForm::getTotalPrice)
//                .sum();
//        dto.setTotalAmount(Math.round(totalAmount));
//        dto.setFinalAmount(Math.round(finalAmount));
//        dto.setDiscountAmount(dto.getTotalAmount() - dto.getFinalAmount());
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        ordersRepositoryJPA.save(entity);
        //Luu thuoc tinh bo sung
        List<AttributeRequestDto> oldAttribute = null;

        if (isUpdate) {
            oldAttribute = Utils.mapAll(objectAttributesService.getAttributes(dto.getOrderId(), ordersRepository.getSQLTableName(OrdersEntity.class)), AttributeRequestDto.class);
        }
        logActionsService.saveData(isUpdate ? Constant.LOG_ACTION.UPDATE : Constant.LOG_ACTION.INSERT, oldEntity, entity, oldAttribute, dto.getListAttributes(), dto.getOrderId(), dto.getOrderNo());
        objectAttributesService.saveObjectAttributes(entity.getOrderId(), dto.getListAttributes(), OrdersEntity.class, Constant.RESOURCES.ORDER);

        saveOrderDetails(entity.getOrderId(), dto.getOrderDetails(), dto.getOrderNo());
        if (!Utils.isNullOrEmpty(dto.getOrderDetailIds())) {
            dto.getOrderDetailIds().forEach(id -> {
                OrderDetailsEntity oldEntityDetail = orderDetailsRepository.get(OrderDetailsEntity.class, id);
                OrderDetailsEntity newEntityDetail = oldEntityDetail;
                newEntityDetail.setIsDeleted(BaseConstants.STATUS.DELETED);
                logActionsService.saveData(Constant.LOG_ACTION.DELETE, oldEntityDetail, newEntityDetail, null, null, id, dto.getOrderNo());
            });
            orderDetailsRepository.deleteOrderDetails(dto.getOrderDetailIds(), entity.getOrderId());
        }

        savePayments(entity.getOrderId(), dto.getPayments(), dto.getOrderNo());
        if (!Utils.isNullOrEmpty(dto.getPaymentIds())) {
            dto.getPaymentIds().forEach(id -> {
                PaymentsEntity oldEntityPayment = paymentsRepository.get(PaymentsEntity.class, id);
                PaymentsEntity newEntityPayment = oldEntityPayment;
                newEntityPayment.setIsDeleted(BaseConstants.STATUS.DELETED);
                logActionsService.saveData(Constant.LOG_ACTION.DELETE, oldEntityPayment, newEntityPayment, null, null, id, dto.getOrderNo());
            });
            paymentsRepository.deletePayments(dto.getPaymentIds(), entity.getOrderId());
        }
        return ResponseUtils.ok(entity.getOrderId());
    }

    private String generateCode(String prefix) {
        String orderNo = ordersRepository.getMaxOrderNo(prefix);
        return prefix + String.format("%1$06d", Integer.valueOf(orderNo.replace(prefix, "")) + 1);
    }

    private void saveOrderDetails(Long orderId, List<OrderDetailsRequest.SubmitForm> orderDetails, String objName) {
        if (!Utils.isNullOrEmpty(orderDetails)) {
            orderDetails.forEach(item -> {
                OrderDetailsEntity entity;
                boolean isUpdate = false;
                OrderDetailsEntity oldEntity = new OrderDetailsEntity();
                if (item.getOrderDetailId() != null && item.getOrderDetailId() > 0L) {
                    entity = orderDetailsRepository.get(OrderDetailsEntity.class, item.getOrderDetailId());
                    Utils.copyProperties(entity, oldEntity);
                    isUpdate = true;
                    entity.setModifiedTime(new Date());
                    entity.setModifiedBy(Utils.getUserNameLogin());
                } else {
                    entity = new OrderDetailsEntity();
                    entity.setCreatedTime(new Date());
                    entity.setCreatedBy(Utils.getUserNameLogin());
                }
                Utils.copyProperties(item, entity);
                entity.setOrderId(orderId);
                entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                orderDetailsRepositoryJPA.save(entity);
                logActionsService.saveData(isUpdate ? Constant.LOG_ACTION.UPDATE : Constant.LOG_ACTION.INSERT, oldEntity, entity, null, null, item.getOrderDetailId(), objName);
            });
        }
    }

    private void savePayments(Long orderId, List<PaymentsRequest.SubmitForm> payments, String objName) {
        if (!Utils.isNullOrEmpty(payments)) {
            payments.forEach(item -> {
                PaymentsEntity entity;
                boolean isUpdate = false;
                PaymentsEntity oldEntity = new PaymentsEntity();
                if (item.getPaymentId() != null && item.getPaymentId() > 0L) {
                    entity = paymentsRepository.get(PaymentsEntity.class, item.getPaymentId());
                    Utils.copyProperties(entity, oldEntity);
                    isUpdate = true;
                    entity.setModifiedTime(new Date());
                    entity.setModifiedBy(Utils.getUserNameLogin());
                } else {
                    entity = new PaymentsEntity();
                    entity.setCreatedTime(new Date());
                    entity.setCreatedBy(Utils.getUserNameLogin());
                }
                Utils.copyProperties(item, entity);
                entity.setOrderId(orderId);
                entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                paymentsRepositoryJPA.save(entity);
                //Luu file dinh kem
                logActionsService.saveData(isUpdate ? Constant.LOG_ACTION.UPDATE : Constant.LOG_ACTION.INSERT, oldEntity, entity, null, null, item.getPaymentId(), objName);

                fileService.deActiveFileByAttachmentId(item.getIdsDelete(), Constant.ATTACHMENT.TABLE_NAMES.PAYMENT, Constant.RESOURCES.ORDER);
                fileService.uploadFiles(item.getFileAttachments(), entity.getPaymentId(), Constant.ATTACHMENT.TABLE_NAMES.PAYMENT, Constant.RESOURCES.ORDER, Constant.ATTACHMENT.MODULE);
            });
        }
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OrdersEntity> optional = ordersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrdersEntity.class);
        }
        ordersRepository.deActiveObject(OrdersEntity.class, id);
        orderDetailsRepository.deleteOrderDetails(null, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OrdersResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<OrdersEntity> optional = ordersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OrdersEntity.class);
        }
        OrdersResponse dto = new OrdersResponse();
        Utils.copyProperties(optional.get(), dto);
        Optional<CustomersEntity> optionalCustomer = customersRepositoryJPA.findById(optional.get().getCustomerId());
        if (optionalCustomer.isPresent()) {
            dto.setFullName(optionalCustomer.get().getFullName());
            dto.setMobileNumber(optionalCustomer.get().getMobileNumber());
        }
        dto.setListAttributes(objectAttributesService.getAttributes(id, "crm_orders"));
        dto.setOrderDetails(orderDetailsRepository.getOrderDetailsByOrderId(id));
        List<PaymentsResponse> payments = paymentsRepository.getPayments(id);
        payments.forEach(e -> {
            e.setAttachFileList(attachmentServiceImpl.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.PAYMENT, Constant.RESOURCES.ORDER, e.getPaymentId()));
        });
        dto.setPayments(payments);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(OrdersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh_sach_don_hang.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = ordersRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh_sach_don_hang.xlsx");
    }

}
