/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.bean.WarehouseNotifyBean;
import vn.hbtplus.models.request.TransferringShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TransferringShipmentsResponse;
import vn.hbtplus.repositories.entity.IncomingShipmentsEntity;
import vn.hbtplus.repositories.entity.OutgoingShipmentsEntity;
import vn.hbtplus.repositories.entity.TransferringEquipmentsEntity;
import vn.hbtplus.repositories.entity.TransferringShipmentsEntity;
import vn.hbtplus.repositories.entity.WarehouseEquipmentsEntity;
import vn.hbtplus.repositories.entity.WarehousesEntity;
import vn.hbtplus.repositories.impl.EquipmentsRepository;
import vn.hbtplus.repositories.impl.TransferringEquipmentsRepository;
import vn.hbtplus.repositories.impl.TransferringShipmentsRepository;
import vn.hbtplus.repositories.jpa.IncomingShipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.OutgoingShipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.TransferringEquipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.TransferringShipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.WarehouseEquipmentsRepositoryJPA;
import vn.hbtplus.services.EmployeeService;
import vn.hbtplus.services.EquipmentsService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.TransferringShipmentsService;
import vn.hbtplus.services.WarehouseEquipmentsService;
import vn.hbtplus.services.WarehousesService;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.util.CommonUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang stk_transfering_shipments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class TransferringShipmentsServiceImpl implements TransferringShipmentsService {

    private final TransferringShipmentsRepository transferringShipmentsRepository;
    private final TransferringShipmentsRepositoryJPA transferringShipmentsRepositoryJPA;

    private final TransferringEquipmentsRepository transferringEquipmentsRepository;

    private final TransferringEquipmentsRepositoryJPA transferringEquipmentsRepositoryJPA;

    private final EquipmentsService equipmentsService;

    private final FileService fileService;

    private final EmployeeService employeeService;

    private final WarehouseEquipmentsRepositoryJPA warehouseEquipmentsRepositoryJPA;

    private final AttachmentService attachmentService;
    private final OutgoingShipmentsRepositoryJPA outgoingShipmentsRepositoryJPA;
    private final IncomingShipmentsRepositoryJPA incomingShipmentsRepositoryJPA;
    private final WarehouseEquipmentsService warehouseEquipmentsService;

    private final UtilsService utilsService;
    private final EquipmentsRepository equipmentsRepository;
    private final WarehousesService warehousesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TransferringShipmentsResponse> searchData(TransferringShipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(transferringShipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(TransferringShipmentsRequest.SubmitForm dto) throws BaseAppException {
        TransferringShipmentsEntity entity;
        // validate 2 kho phai khac cap
        List<Long> warehouseIds = List.of(dto.getWarehouseId(), dto.getReceivedWarehouseId());
        List<WarehousesEntity> warehousesEntityList = transferringEquipmentsRepository.findByListId(WarehousesEntity.class, warehouseIds);
        if (Utils.isNullOrEmpty(warehousesEntityList) || warehousesEntityList.size() < 2) {
            throw new BaseAppException("Bạn phải nhập đầy đủ kho nhập, kho xuất");
        }

        WarehousesEntity warehousesOut = warehousesEntityList.get(0);
        WarehousesEntity warehouseReceipt = warehousesEntityList.get(1);
        if (warehousesOut.getPathLevel() != null && warehousesOut.getPathLevel().equals(warehouseReceipt.getPathLevel())) {
            throw new BaseAppException("Kho xuất và kho nhập không được cùng cấp");
        }
        // them vat tu
        List<TransferringEquipmentsEntity> listEquipmentEntities = new ArrayList<>();
        if (dto.getTransferringShipmentId() != null && dto.getTransferringShipmentId() > 0L) {
            listEquipmentEntities = transferringEquipmentsRepository.findByProperties(TransferringEquipmentsEntity.class, "transferringShipmentId", dto.getTransferringShipmentId());
        }
        Map<Long, TransferringEquipmentsEntity> mapEquipmentEntities = new HashMap<>();
        listEquipmentEntities.forEach(item -> {
            mapEquipmentEntities.put(item.getEquipmentId(), item);
        });

        if (dto.getTransferringShipmentId() != null && dto.getTransferringShipmentId() > 0L) {
            entity = transferringShipmentsRepositoryJPA.getById(dto.getTransferringShipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new TransferringShipmentsEntity();
            Long id = transferringShipmentsRepository.getNextId(TransferringShipmentsEntity.class);
            entity.setTransferringShipmentId(id);
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        if ("Y".equalsIgnoreCase(dto.getIsSendToApprove())) {
            entity.setStatusId(TransferringShipmentsEntity.STATUS.CHO_DUYET);
        } else {
            entity.setStatusId(TransferringShipmentsEntity.STATUS.DU_THAO);
        }

        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        transferringShipmentsRepositoryJPA.save(entity);
        if (!Utils.isNullOrEmpty(dto.getListEquipments())) {
            List<TransferringEquipmentsEntity> transferringEquipmentsEntities = new ArrayList<>();
            List<TransferringEquipmentsEntity> transferringEquipmentsUpdateEntities = new ArrayList<>();
            dto.getListEquipments().forEach(item -> {
                TransferringEquipmentsEntity equipmentsEntity = mapEquipmentEntities.get(item.getEquipmentId());
                boolean isUpdate = false;
                if (equipmentsEntity == null) {
                    equipmentsEntity = new TransferringEquipmentsEntity();
                    equipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    equipmentsEntity.setCreatedTime(new Date());
                } else {
                    equipmentsEntity.setModifiedBy(Utils.getUserNameLogin());
                    equipmentsEntity.setModifiedTime(new Date());
                    isUpdate = true;
                }
                equipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                equipmentsEntity.setEquipmentId(item.getEquipmentId());
                equipmentsEntity.setTransferringShipmentId(entity.getTransferringShipmentId());
                equipmentsEntity.setQuantity(item.getQuantity());
                equipmentsEntity.setUnitPrice(item.getUnitPrice());
                equipmentsEntity.setInventoryQuantity(item.getInventoryQuantity());
                mapEquipmentEntities.put(item.getEquipmentId(), null);

                if (isUpdate) {
                    transferringEquipmentsUpdateEntities.add(equipmentsEntity);
                } else {
                    transferringEquipmentsEntities.add(equipmentsEntity);
                }
            });

            if (!Utils.isNullOrEmpty(transferringEquipmentsEntities)) {
                transferringEquipmentsRepository.insertBatch(TransferringEquipmentsEntity.class, transferringEquipmentsEntities, Utils.getUserNameLogin());
            }

            if (!Utils.isNullOrEmpty(transferringEquipmentsUpdateEntities)) {
                transferringEquipmentsRepository.updateBatch(TransferringEquipmentsEntity.class, transferringEquipmentsUpdateEntities, true);
            }
        }

        List<Long> listDeleted = mapEquipmentEntities.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (!Utils.isNullOrEmpty(listDeleted)) {
            transferringEquipmentsRepository.deActiveObjectByPairList(TransferringEquipmentsEntity.class, Pair.of("equipmentId", listDeleted), Pair.of("transferringShipmentId", List.of(entity.getTransferringShipmentId())));
        }
        if (!Utils.isNullOrEmpty(dto.getFiles())) {
            fileService.uploadFiles(dto.getFiles(), entity.getTransferringShipmentId(), Constants.ATTACHMENT.TABLE_NAMES.MAT_TRANSFERRING_SHIPMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_TRANSFERRING_SHIPMENTS, Constants.ATTACHMENT.MODULE);
        }
        if (!Utils.isNullOrEmpty(dto.getDocIdsDelete())) {
            fileService.deActiveFileByAttachmentId(dto.getDocIdsDelete(), Constants.ATTACHMENT.TABLE_NAMES.MAT_TRANSFERRING_SHIPMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_TRANSFERRING_SHIPMENTS);
        }


        if (TransferringShipmentsEntity.STATUS.CHO_DUYET.equalsIgnoreCase(entity.getStatusId())) {
            List<WarehouseNotifyBean> notifyBeanList = transferringShipmentsRepository.getNotifySenToApprove(Arrays.asList(entity.getTransferringShipmentId()));
            warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.TRANSFER_SEND_TO_APPROVE);
        }

        return ResponseUtils.ok(entity.getTransferringShipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<TransferringShipmentsEntity> optional = transferringShipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TransferringShipmentsEntity.class);
        }
        transferringShipmentsRepository.deActiveObject(TransferringEquipmentsEntity.class, "transferringShipmentId", id);
        transferringShipmentsRepository.deActiveObject(TransferringShipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TransferringShipmentsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<TransferringShipmentsEntity> optional = transferringShipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TransferringShipmentsEntity.class);
        }
        TransferringShipmentsResponse dto = transferringShipmentsRepository.getDataById(id);
        if (!Utils.isNullOrEmpty(dto.getApprovedBy())) {
            EmployeesResponse response = employeeService.getEmployeeByEmpCode(dto.getApprovedBy());
            if (response != null) {
                dto.setApprovedId(response.getEmployeeId());
                dto.setApprovedName(response.getFullName());
            }
        }
        dto.setHasApproveTransfer(transferringEquipmentsRepository.getPermissionApprove(dto.getWarehouseId()));
        dto.setListEquipments(transferringEquipmentsRepository.getListEquipmentByTransferringShipmentId(id, dto.getWarehouseId()));
        dto.setFiles(attachmentService.getAttachmentList(Constants.ATTACHMENT.TABLE_NAMES.MAT_TRANSFERRING_SHIPMENTS,
                Constants.ATTACHMENT.FILE_TYPES.MAT_TRANSFERRING_SHIPMENTS, List.of(id)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(TransferringShipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_phieu_dieu_chuyen.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = transferringShipmentsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        for (Map<String, Object> data : listDataExport) {
            data.put("status", TransferringShipmentsEntity.listStatusMap.get(data.get("status_id").toString()));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_phieu_dieu_chuyen.xlsx");
    }


    @Override
    @Transactional
    public ResponseEntity getSeq() {
        return ResponseUtils.ok(transferringShipmentsRepository.getNextId(TransferringShipmentsEntity.class));
    }

    @Override
    @Transactional
    public ResponseEntity approve(List<Long> ids) throws BaseAppException {
        if (!Utils.isNullOrEmpty(ids)) {
            //validate vat tu trong kho khong duoc nho hon phieu xuat
            String validateQuantity = transferringShipmentsRepository.validateQuantity(ids);
            if(!Utils.isNullOrEmpty(validateQuantity)) {
                throw new BaseAppException("Vật tư : " + validateQuantity + " có số lượng điều chuyển lớn hơn số lượng vật tư hiện tại trong kho!");
            }

            List<TransferringShipmentsEntity> transferringShipmentsEntities = transferringShipmentsRepository.findByListId(TransferringShipmentsEntity.class, ids);
            AtomicInteger index = new AtomicInteger(0);
            transferringShipmentsEntities.forEach(item -> {
                if (item.getStatusId().equals(TransferringShipmentsEntity.STATUS.CHO_DUYET)) {
                    //B1 : Tao phieu xuat noi kho dieu chuyen di
                    OutgoingShipmentsEntity outgoingShipmentsEntity = new OutgoingShipmentsEntity();
                    Long outgoingShipmentId = transferringShipmentsRepository.getNextId(OutgoingShipmentsEntity.class);;
                    outgoingShipmentsEntity.setOutgoingShipmentId(outgoingShipmentId);
                    outgoingShipmentsEntity.setCreatedTime(new Date());
                    outgoingShipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    outgoingShipmentsEntity.setWarehouseId(item.getWarehouseId());
                    outgoingShipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    outgoingShipmentsEntity.setPickingNo(CommonUtils.generatePickingNo(Constants.PREFIX_PICKING_NO.OUTGOING, outgoingShipmentId));
                    outgoingShipmentsEntity.setTransferringShipmentId(item.getTransferringShipmentId());
                    outgoingShipmentsEntity.setStatusId(OutgoingShipmentsEntity.STATUS.PHE_DUYET);
                    outgoingShipmentsEntity.setPickingEmployeeId(item.getTransferredEmployeeId());
                    outgoingShipmentsEntity.setOutgoingDate(item.getTransferringDate());
                    outgoingShipmentsEntity.setType(OutgoingShipmentsEntity.TYPES.DIEU_CHUYEN);
                    outgoingShipmentsEntity.setApprovedTime(new Date());
                    outgoingShipmentsEntity.setApprovedBy(Utils.getUserEmpCode());
                    outgoingShipmentsEntity.setReceiverId(item.getReceivedEmployeeId());
                    outgoingShipmentsRepositoryJPA.save(outgoingShipmentsEntity);
                    transferringShipmentsRepository.insertOutGoingEquipments(item.getTransferringShipmentId(), outgoingShipmentsEntity.getOutgoingShipmentId(), item.getWarehouseId());
                    //B2 : Tao phieu nhap noi kho dieu chuyen den
                    IncomingShipmentsEntity incomingShipmentsEntity = new IncomingShipmentsEntity();
                    incomingShipmentsEntity.setCreatedTime(new Date());
                    incomingShipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    incomingShipmentsEntity.setWarehouseId(item.getReceivedWarehouseId());
                    incomingShipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    incomingShipmentsEntity.setStatusId(IncomingShipmentsEntity.STATUS.PHE_DUYET);
                    incomingShipmentsEntity.setPickingEmployeeId(item.getReceivedEmployeeId());
                    incomingShipmentsEntity.setTransferringShipmentId(item.getTransferringShipmentId());
                    incomingShipmentsEntity.setIncomingDate(item.getTransferringDate());
                    incomingShipmentsEntity.setType(IncomingShipmentsEntity.TYPES.DIEU_CHUYEN);
                    Long seq = transferringShipmentsRepository.getNextId(IncomingShipmentsEntity.class);
                    incomingShipmentsEntity.setIncomingShipmentId(seq);
                    incomingShipmentsEntity.setPickingNo(CommonUtils.generatePickingNo(Constants.PREFIX_PICKING_NO.INCOMING, seq));
                    incomingShipmentsEntity.setApprovedTime(new Date());
                    incomingShipmentsEntity.setApprovedBy(Utils.getUserEmpCode());
                    incomingShipmentsEntity.setAuthorId(item.getCreatedEmployeeId());
                    incomingShipmentsRepositoryJPA.save(incomingShipmentsEntity);
                    transferringShipmentsRepository.insertIncomingEquipments(item.getTransferringShipmentId(), incomingShipmentsEntity.getIncomingShipmentId());
                    //B3 : Cap nhat trang thai phieu
                    transferringEquipmentsRepository.approve(Arrays.asList(item.getTransferringShipmentId()));
                    //B4 : cap nhat vat tu trong kho chuyen di
                    warehouseEquipmentsService.updateWarehouseEquipments(outgoingShipmentsEntity.getWarehouseId(), outgoingShipmentsEntity.getOutgoingShipmentId(), "outgoing");
                    //B4 : cap nhat vat tu trong kho chuyen den
                    warehouseEquipmentsService.updateWarehouseEquipments(incomingShipmentsEntity.getWarehouseId(), incomingShipmentsEntity.getIncomingShipmentId(), "incoming");
                }
            });

            List<WarehouseNotifyBean> notifyBeanList = transferringShipmentsRepository.getNotifyApprove(ids);
            warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.TRANSFER_APPROVE);
        }

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity undoApprove(List<Long> ids) {
        if (!Utils.isNullOrEmpty(ids)) {
            List<TransferringShipmentsEntity> transferringShipmentsEntities = transferringShipmentsRepository.findByListId(TransferringShipmentsEntity.class, ids);
            transferringShipmentsEntities.forEach(item -> {
                if (item.getStatusId().equals(TransferringShipmentsEntity.STATUS.PHE_DUYET)) {
                    transferringEquipmentsRepository.undoApprove(Arrays.asList(item.getTransferringShipmentId()));
                }
            });
        }

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity sendToApprove(List<Long> ids) {
        if (!Utils.isNullOrEmpty(ids)) {
            transferringEquipmentsRepository.sendToApprove(ids);
            List<WarehouseNotifyBean> notifyBeanList = transferringShipmentsRepository.getNotifySenToApprove(ids);
            warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.TRANSFER_SEND_TO_APPROVE);
        }
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity reject(List<Long> ids, String note) {
        if (!Utils.isNullOrEmpty(ids)) {
            transferringEquipmentsRepository.reject(ids, note);
            List<WarehouseNotifyBean> notifyBeanList = transferringShipmentsRepository.getNotifyApprove(ids);
            warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.TRANSFER_REJECT);
        }
        return ResponseUtils.ok();
    }

    private Map<String, WarehouseEquipmentsEntity> getMapEquipmentWarehouseTransfer(Long transferWarehouseId) {
        List<WarehouseEquipmentsEntity> warehouseEquipmentsEntities =
                transferringEquipmentsRepository.findByProperties(WarehouseEquipmentsEntity.class, "warehouseId", transferWarehouseId);

        return warehouseEquipmentsEntities.stream().collect(Collectors.toMap(e -> e.getEquipmentId() + "#" + e.getUnitPrice(), e -> e));
    }


    @Override
    public ResponseEntity<Object> downloadImportEquipmentTemplate(Long warehouseId) throws Exception {
        String pathTemplate = "template/import/bm-import-dieu-chuyen-vat-tu.xlsx";
        int startDataRow = 6;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        List<EquipmentsResponse> listEquipment = equipmentsRepository.getAllEquipment(warehouseId, null);
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (EquipmentsResponse equipmentsResponse : listEquipment) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(equipmentsResponse.getCode(), 1, row);
            dynamicExport.setText(equipmentsResponse.getName(), 2, row);
            dynamicExport.setNumber(equipmentsResponse.getUnitPrice(), 3, row);
            dynamicExport.setNumber(equipmentsResponse.getInventoryQuantity(), 4, row);
            row++;
        }
        dynamicExport.setCellFormat(0, 0, row - 1, 4, ExportExcel.BORDER_FORMAT);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM-import-danh-sach-dieu-chuyen-vat-tu.xlsx", false);
    }

    @Override
    public List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport, Long warehouseId) throws ErrorImportException {
        ImportExcel importExcel = new ImportExcel("template/import/bm-import-dieu-chuyen-vat-tu.xml");
        List<Object[]> dataList = new ArrayList<>();

        ResponseEntity<Object> validateFileImport = utilsService.validateFileImport(importExcel, fileImport, dataList);
        if (validateFileImport != null) {
            throw new ErrorImportException(fileImport, importExcel);
        }
        List<String> equipmentCodes = new ArrayList<>();
        dataList.forEach(item -> {
            equipmentCodes.add(((String) item[1]).toLowerCase());
        });

        //Lay danh sách vật tư theo tên
        List<EquipmentsResponse> equipmentsEntities = equipmentsRepository.getEquipmentByCodes(equipmentCodes, warehouseId, null);
        Map<String, EquipmentsResponse> mapEquipments = new HashMap<>();
        equipmentsEntities.forEach(item -> {
            mapEquipments.put(item.getName().toLowerCase(), item);
        });
        int col;
        int row = 0;
        List<StockEquipmentsResponse> result = new ArrayList<>();
        for (Object[] obj : dataList) {
            col = 2;
            if ((Double) obj[5] < 0) {
                importExcel.addError(row, col, "Số lượng phải lớn hơn hoặc bằng 0", (obj[5]).toString());
                continue;
            }
            if ((Double) obj[3] < 0) {
                importExcel.addError(row, col, "Đơn giá phải lớn hơn hoặc bằng 0", (obj[3]).toString());
                continue;
            }
            EquipmentsResponse equipment = mapEquipments.get(obj[col].toString().toLowerCase());
            if (equipment == null) {
                importExcel.addError(row, col, "Dữ liệu vật tư không hợp lệ!", (String) obj[col]);
            } else {
                if (equipment.getInventoryQuantity() < (Double) obj[5]) {
                    importExcel.addError(row, 3, "Số lượng vượt quá số lượng tồn kho!", String.valueOf(obj[5]));
                }
                StockEquipmentsResponse equipmentsResponse = new StockEquipmentsResponse();
                equipmentsResponse.setEquipmentCode((String) obj[1]);
                equipmentsResponse.setEquipmentName((String) obj[2]);
                equipmentsResponse.setQuantity((Double) obj[5]);
                equipmentsResponse.setUnitPrice((Double) obj[3]);
                equipmentsResponse.setInventoryQuantity(equipment.getInventoryQuantity());
                equipmentsResponse.setEquipmentId(equipment.getEquipmentId());
                equipmentsResponse.setEquipmentTypeId(equipment.getEquipmentTypeId());
                equipmentsResponse.setEquipmentTypeName(equipment.getEquipmentTypeName());
                equipmentsResponse.setEquipmentUnitName(equipment.getEquipmentUnitName());
                result.add(equipmentsResponse);
            }
        }
        if (importExcel.hasError()) {
            throw new ErrorImportException(fileImport, importExcel);
        } else {
            return result;
        }
    }
}
