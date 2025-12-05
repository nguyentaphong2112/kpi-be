/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.bean.WarehouseNotifyBean;
import vn.hbtplus.models.request.WarehousesRequest;
import vn.hbtplus.models.request.sendNotify.ChannelRequest;
import vn.hbtplus.models.request.sendNotify.Receiver;
import vn.hbtplus.models.request.sendNotify.SendRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehousesResponse;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.WarehouseManagersEntity;
import vn.hbtplus.repositories.entity.WarehousesEntity;
import vn.hbtplus.repositories.impl.WarehouseManagersRepository;
import vn.hbtplus.repositories.impl.WarehousesRepository;
import vn.hbtplus.repositories.jpa.EmployeeRepositoryJPA;
import vn.hbtplus.repositories.jpa.WarehouseManagersRepositoryJPA;
import vn.hbtplus.repositories.jpa.WarehousesRepositoryJPA;
import vn.hbtplus.services.WarehousesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang stk_warehouses
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class WarehousesServiceImpl implements WarehousesService {

    private final WarehousesRepository warehousesRepository;
    private final WarehousesRepositoryJPA warehousesRepositoryJPA;
    private final WarehouseManagersRepositoryJPA warehouseManagersRepositoryJPA;
    private final WarehouseManagersRepository warehouseManagersRepository;
    private final EmployeeRepositoryJPA employeeRepository;
//    private final NotificationAPIUtils notificationAPIUtils;

    @Value("${sms.alias:}")
    private String smsAlias;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity searchData(WarehousesRequest.SearchForm dto) {
        return ResponseUtils.ok(warehousesRepository.searchData(dto));
    }

    @Override
    public ResponseEntity searchList(WarehousesRequest.SearchForm dto) {
//        List<WarehousesResponse> listData = warehousesRepository.searchData(dto);
//        List<WarehousesResponse> results = new ArrayList<>();
//        Map<Long, WarehousesResponse> mapData = new HashMap<>();
//        listData.stream().forEach(item -> {
//            mapData.put(item.getWarehouseId(), item);
//        });
//        listData.stream().forEach(item -> {
//            if (item.getParentId() == null || mapData.get(item.getParentId()) == null) {
//                results.add(item);
//            } else {
//                WarehousesResponse parent = mapData.get(item.getParentId());
//                parent.addChild(item);
//            }
//        });
//        return ResponseUtils.ok(results);
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity saveData(WarehousesRequest.SubmitForm dto) throws BaseAppException {
        boolean isDuplicate = warehousesRepository.checkDuplicate(dto.getCode(), dto.getWarehouseId());

        if (isDuplicate) {
            throw new BaseAppException("Mã kho đã tồn tại");
        }

        if (dto.getListEmployee() == null || dto.getListEmployee().isEmpty()) {
            throw new BaseAppException("Vui lòng chọn nhân viên kho");
        }

        WarehousesEntity entity;
        if (dto.getWarehouseId() != null && dto.getWarehouseId() > 0L) {
            entity = warehousesRepositoryJPA.getById(dto.getWarehouseId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WarehousesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        warehousesRepositoryJPA.save(entity);

        //Lay danh sach employee
        List<WarehouseManagersEntity> entities = warehouseManagersRepository.findByProperties(WarehouseManagersEntity.class, "warehouseId", entity.getWarehouseId());
        Map<String, WarehouseManagersEntity> mapDetails = new HashMap<>();
        entities.forEach(item -> {
            mapDetails.put(item.getWarehouseId() + "-" + item.getEmployeeId(), item);
        });

        //Luu thong tin employee
        dto.getListEmployee().forEach(detailRequest -> {
            EmployeesEntity employee = employeeRepository.findByEmployeeCodeAndIsDeleted(detailRequest.getEmployeeCode(), BaseConstants.STATUS.NOT_DELETED);
            WarehouseManagersEntity detailsEntity = mapDetails.get(dto.getWarehouseId() + "-" + employee.getEmployeeId());
            if (detailsEntity != null) {
                detailsEntity.setModifiedTime(new Date());
                detailsEntity.setModifiedBy(Utils.getUserNameLogin());
            } else {
                detailsEntity = new WarehouseManagersEntity();
                detailsEntity.setCreatedTime(new Date());
                detailsEntity.setCreatedBy(Utils.getUserNameLogin());
            }
            detailsEntity.setWarehouseId(entity.getWarehouseId());
            detailsEntity.setEmployeeId(employee.getEmployeeId());
            if (Constants.WAREHOUSE_MANAGER_APPROVE.YES.equals(detailRequest.getIsManager())) {
                detailsEntity.setRoleId(Constants.WAREHOUSE_MANAGER_ROLES.THU_KHO);
            } else {
                detailsEntity.setRoleId(Constants.WAREHOUSE_MANAGER_ROLES.NHAN_VIEN);
            }

            detailsEntity.setHasApproveImport(detailRequest.getHasApproveImport());
            detailsEntity.setHasApproveExport(detailRequest.getHasApproveExport());
            detailsEntity.setHasApproveAdjustment(detailRequest.getHasApproveAdjustment());
            detailsEntity.setHasApproveTransfer(detailRequest.getHasApproveTransfer());

            detailsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            warehouseManagersRepositoryJPA.save(detailsEntity);
            mapDetails.remove(dto.getWarehouseId() + "-" + detailRequest.getEmployeeId());
        });

        //check xem can xoa ban ghi nao khong
        if (!mapDetails.isEmpty()) {
            mapDetails.values().forEach(detailEntity -> {
                if (!detailEntity.isDeleted()) {
                    detailEntity.setModifiedTime(new Date());
                    detailEntity.setModifiedBy(Utils.getUserNameLogin());
                    detailEntity.setRoleId(Constants.WAREHOUSE_MANAGER_ROLES.NHAN_VIEN);
                    detailEntity.setIsDeleted(BaseConstants.STATUS.DELETED);
                    warehouseManagersRepositoryJPA.save(detailEntity);
                }
            });
        }

        //xu ly update path của kho
        if (entity.getParentId() != null) {
            warehousesRepository.updatePath(entity.getParentId());
        }

        return ResponseUtils.ok(entity.getWarehouseId());
    }

    private String getApprovalStatus(Boolean hasApproval) {
        if (Boolean.TRUE.equals(hasApproval)) {
            return Constants.WAREHOUSE_MANAGER_APPROVE.YES;
        }
        return Constants.WAREHOUSE_MANAGER_APPROVE.NO;
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WarehousesEntity> optional = warehousesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarehousesEntity.class);
        }
        warehousesRepository.deActiveObject(WarehousesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WarehousesResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<WarehousesEntity> optional = warehousesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WarehousesEntity.class);
        }
        WarehousesResponse dto = new WarehousesResponse();
        Utils.copyProperties(optional.get(), dto);
        List<WarehousesResponse.warehouseEmployeeDTO> listEmp = warehousesRepository.getListEmpByWarehouse(id);
        List<WarehousesResponse.warehouseEquipmentDTO> listEquipment = warehousesRepository.getListEquipmentByWarehouse(id);
        List<WarehousesResponse.warehouseIncomingShipmentDTO> listIncomingShipment = warehousesRepository.getListIncomingShipmentByWarehouse(id);
        List<WarehousesResponse.warehouseOutgoingShipmentDTO> listOutgoingShipment = warehousesRepository.getListOutgoingShipmentByWarehouse(id);
        List<WarehousesResponse.warehouseInventoryAdjustmentDTO> listInventoryAdjustment = warehousesRepository.getListInventoryAdjustmentByWarehouse(id);
        dto.setListEmployee(listEmp);
        dto.setListEquipment(listEquipment);
        dto.setListIncomingShipment(listIncomingShipment);
        dto.setListOutgoingShipment(listOutgoingShipment);
        dto.setListInventoryAdjustment(listInventoryAdjustment);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(WarehousesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh-sach-kho.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = warehousesRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-kho.xlsx");
    }

    @Override
    public ResponseEntity lockOrUnlockWarehouse(Long id) throws RecordNotExistsException {
        WarehousesEntity entity = warehousesRepository.get(WarehousesEntity.class, "warehouseId", id);
        if (entity == null || BaseConstants.STATUS.DELETED.equals(entity.getIsDeleted())) {
            throw new RecordNotExistsException(id, WarehousesEntity.class);
        }
        if (entity.getStatusId().equals(Constants.WAREHOUSE_STATUS.KHONG_HOAT_DONG)) {
            entity.setStatusId(Constants.WAREHOUSE_STATUS.HOAT_DONG);
        } else {
            entity.setStatusId(Constants.WAREHOUSE_STATUS.KHONG_HOAT_DONG);
        }
        warehousesRepositoryJPA.save(entity);
        return ResponseUtils.ok(id);
    }

    @Override
    public BaseResponseEntity<EmployeesResponse> getEmpByCode(String code) throws RecordNotExistsException {
        List<Map<String, Object>> listEmp = warehousesRepository.getEmpByCode(code);
        EmployeesEntity entity = employeeRepository.findByEmployeeCodeAndIsDeleted(code, BaseConstants.STATUS.NOT_DELETED);
        if (listEmp.isEmpty()) {
            throw new RecordNotExistsException(entity.getEmployeeId(), EmployeesResponse.class);
        }
        EmployeesResponse dto = new EmployeesResponse();
        Map<String, Object> test = listEmp.get(0);
        Utils.copyProperties(listEmp.get(0), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    @Async
    public void sendNotification(List<WarehouseNotifyBean> notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES functionCode) {
        if(Utils.isNullOrEmpty(notifyBeanList)) {
            return;
        }
        Map<Long, List<WarehouseNotifyBean>> mapNotify = notifyBeanList.stream().collect(Collectors.groupingBy(WarehouseNotifyBean::getId));
        mapNotify.forEach((id, notifyBeans) -> {
            Map params = new HashMap();
            params.put("nguoi_gui", notifyBeans.get(0).getSenderName());
            params.put("ten_kho", notifyBeans.get(0).getWarehouseName());
            params.put("so_phieu", notifyBeans.get(0).getPickingNo());
            params.put("ly_do", notifyBeans.get(0).getReason());

            List<String> employeeCodes = new ArrayList<>();
            notifyBeans.forEach(notifyBean -> {
                employeeCodes.add(notifyBean.getReceiverCode());
            });
            SendRequest sendRequest = new SendRequest();
            Receiver receiver = new Receiver();
            receiver.setType("employeeCode");
            receiver.setValue(employeeCodes);
            sendRequest.setReceivers(Arrays.asList(receiver));

            ChannelRequest channelRequestMail = new ChannelRequest();
            channelRequestMail.setType("mail");
            channelRequestMail.setSubject(functionCode.getEmailSubject());
            channelRequestMail.setTemplate(functionCode.getEmailContent(params));
            List channels = new ArrayList();
            channels.add(channelRequestMail);


            // Gui tin nhan
            if (!Utils.isNullOrEmpty(smsAlias)) {
                ChannelRequest channelRequestSMS = new ChannelRequest();
                channelRequestSMS.setType("sms");
                channelRequestSMS.setAlias(smsAlias);
                channelRequestSMS.setTemplate(functionCode.getEmailContent(params));
                channels.add(channelRequestSMS);
            }
            sendRequest.setChannels(channels);

//            notificationAPIUtils.send(sendRequest);
        });
    }

}
