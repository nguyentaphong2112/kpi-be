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
import vn.hbtplus.models.request.IncomingShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.models.response.IncomingEquipmentsResponse;
import vn.hbtplus.models.response.IncomingShipmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.IncomingEquipmentsEntity;
import vn.hbtplus.repositories.entity.IncomingShipmentsEntity;
import vn.hbtplus.repositories.entity.OutgoingShipmentsEntity;
import vn.hbtplus.repositories.entity.TransferringShipmentsEntity;
import vn.hbtplus.repositories.impl.EquipmentsRepository;
import vn.hbtplus.repositories.impl.IncomingShipmentsRepository;
import vn.hbtplus.repositories.jpa.IncomingEquipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.IncomingShipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.OutgoingShipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.TransferringShipmentsRepositoryJPA;
import vn.hbtplus.services.EmployeeService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.IncomingShipmentsService;
import vn.hbtplus.services.WarehouseEquipmentsService;
import vn.hbtplus.services.WarehousesService;
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
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang stk_incoming_shipments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class IncomingShipmentsServiceImpl implements IncomingShipmentsService {

    private final IncomingShipmentsRepository incomingShipmentsRepository;
    private final IncomingShipmentsRepositoryJPA incomingShipmentsRepositoryJPA;
    private final IncomingEquipmentsRepositoryJPA incomingEquipmentsRepositoryJPA;
    private final OutgoingShipmentsRepositoryJPA outgoingShipmentsRepositoryJPA;
    private final EmployeeService employeeService;
    private final FileService fileService;
    private final AttachmentServiceImpl attachmentService;
    private final WarehouseEquipmentsService warehouseEquipmentsService;
//    private final ShoppingContractRepositoryJPA shoppingContractRepositoryJPA;
    private final TransferringShipmentsRepositoryJPA transferringShipmentsRepositoryJPA;
    private final EquipmentsRepository equipmentsRepository;
    private final WarehousesService warehousesService;

    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<IncomingShipmentsResponse> searchData(IncomingShipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(incomingShipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(IncomingShipmentsRequest.SubmitForm dto) throws BaseAppException {
        IncomingShipmentsEntity entity;

        if (dto.getIncomingShipmentId() != null && dto.getIncomingShipmentId() > 0L) {
            entity = incomingShipmentsRepositoryJPA.getById(dto.getIncomingShipmentId());
            //chi duoc update ban ghi o trang thai tu choi, cho phe duyet, du thao
            if (Arrays.asList(IncomingShipmentsEntity.STATUS.PHE_DUYET).contains(entity.getStatusId())) {
                throw new BaseAppException("Trạng thái không hợp lệ!");
            }

            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new IncomingShipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        if ("Y".equalsIgnoreCase(dto.getIsSendToApprove())) {
            entity.setStatusId(IncomingShipmentsEntity.STATUS.CHO_DUYET);
        } else {
            entity.setStatusId(IncomingShipmentsEntity.STATUS.DU_THAO);
        }
        entity.setType(IncomingShipmentsEntity.TYPES.NHAP_MOI);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        incomingShipmentsRepositoryJPA.save(entity);

        //Luu du lieu vat tu
        List<IncomingEquipmentsEntity> listEquipmentEntities = new ArrayList<>();
        if (dto.getIncomingShipmentId() != null && dto.getIncomingShipmentId() > 0L) {
            listEquipmentEntities = incomingShipmentsRepository.findByProperties(IncomingEquipmentsEntity.class, "incomingShipmentId", dto.getIncomingShipmentId());
        }
        Map<Long, IncomingEquipmentsEntity> mapEquipmentEntities = new HashMap<>();
        for (IncomingEquipmentsEntity item : listEquipmentEntities) {
            mapEquipmentEntities.put(item.getEquipmentId(), item);
        }


        if (!Utils.isNullOrEmpty(dto.getListEquipments())) {
            dto.getListEquipments().forEach(item -> {
                IncomingEquipmentsEntity incomingEquipmentsEntity = mapEquipmentEntities.get(item.getEquipmentId());
                if (incomingEquipmentsEntity == null) {
                    incomingEquipmentsEntity = new IncomingEquipmentsEntity();
                    Long id = incomingShipmentsRepository.getNextId(IncomingShipmentsEntity.class);
                    incomingEquipmentsEntity.setIncomingShipmentId(id);
                    incomingEquipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    incomingEquipmentsEntity.setCreatedTime(new Date());
                } else {
                    incomingEquipmentsEntity.setModifiedBy(Utils.getUserNameLogin());
                    incomingEquipmentsEntity.setModifiedTime(new Date());
                }
                incomingEquipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                incomingEquipmentsEntity.setEquipmentId(item.getEquipmentId());
                incomingEquipmentsEntity.setIncomingShipmentId(entity.getIncomingShipmentId());
                incomingEquipmentsEntity.setQuantity(item.getQuantity());
                incomingEquipmentsEntity.setUnitPrice(item.getUnitPrice());
                mapEquipmentEntities.put(item.getEquipmentId(), null);
                incomingEquipmentsRepositoryJPA.save(incomingEquipmentsEntity);
            });
        }


        List<Long> listDeleted = mapEquipmentEntities.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (!Utils.isNullOrEmpty(listDeleted)) {
            incomingShipmentsRepository.deActiveObjectByPairList(IncomingEquipmentsEntity.class, Pair.of("equipmentId", listDeleted), Pair.of("incomingShipmentId", List.of(entity.getIncomingShipmentId())));
        }


        //Luu file dinh kem
        if (!Utils.isNullOrEmpty(dto.getFiles())) {
            fileService.uploadFiles(dto.getFiles(), entity.getIncomingShipmentId(), Constants.ATTACHMENT.TABLE_NAMES.MAT_INCOMING_SHIPMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_INCOMING_SHIPMENTS, Constants.ATTACHMENT.MODULE);
        }
        //Luu file dinh kem
        if (!Utils.isNullOrEmpty(dto.getDocIdsDelete())) {
            fileService.deActiveFileByAttachmentId(dto.getDocIdsDelete(), Constants.ATTACHMENT.TABLE_NAMES.MAT_INCOMING_SHIPMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_INCOMING_SHIPMENTS);
        }

        if (IncomingShipmentsEntity.STATUS.CHO_DUYET.equalsIgnoreCase(entity.getStatusId())) {
            List<WarehouseNotifyBean> notifyBeanList = incomingShipmentsRepository.getNotifySenToApprove(Arrays.asList(entity.getIncomingShipmentId()));
            warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.IMPORT_SEND_TO_APPROVE);
        }
        return ResponseUtils.ok(entity.getIncomingShipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<IncomingShipmentsEntity> optional = incomingShipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomingShipmentsEntity.class);
        }
        incomingShipmentsRepository.deActiveObject(IncomingEquipmentsEntity.class, "incomingShipmentId", id);
        incomingShipmentsRepository.deActiveObject(IncomingShipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IncomingShipmentsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<IncomingShipmentsEntity> optional = incomingShipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomingShipmentsEntity.class);
        }
        IncomingShipmentsResponse dto = new IncomingShipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        if (!Utils.isNullOrEmpty(dto.getApprovedBy())) {
            EmployeesResponse response = employeeService.getEmployeeByEmpCode(dto.getApprovedBy());
            if (response != null) {
                dto.setApprovedId(response.getEmployeeId());
                dto.setApprovedName(response.getFullName());
            }
        }
        if (dto.getTransferringShipmentId() != null) {
            IncomingShipmentsResponse tmp = incomingShipmentsRepository.getOutgoingWarehouseByTransferShipmentId(dto.getTransferringShipmentId());
            dto.setOutgoingWarehouseId(tmp.getOutgoingWarehouseId());
            dto.setOutgoingTransferPickingNo(tmp.getOutgoingTransferPickingNo());
        }
        dto.setHasApproveImport(incomingShipmentsRepository.getPermissionApprove(dto.getWarehouseId()));
        dto.setFiles(attachmentService.getAttachmentList(Constants.ATTACHMENT.TABLE_NAMES.MAT_INCOMING_SHIPMENTS,
                Constants.ATTACHMENT.FILE_TYPES.MAT_INCOMING_SHIPMENTS, List.of(id)));
        List<IncomingEquipmentsResponse> listEquipment = incomingShipmentsRepository.getListEquipmentByIncomingShipments(id);
        dto.setListEquipments(listEquipment);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(IncomingShipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_phieu_nhap_kho.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = incomingShipmentsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        for (Map<String, Object> data : listDataExport) {
            data.put("typeName", IncomingShipmentsEntity.listTypeMap.get(data.get("type").toString()));
            data.put("status", IncomingShipmentsEntity.listStatusMap.get(data.get("status_id").toString()));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_phieu_nhap_kho.xlsx", true);
    }

    @Override
    public ResponseEntity sendToApprove(List<Long> ids) throws BaseAppException {
        incomingShipmentsRepository.sendToApprove(ids);

        //Lay nguoi co quyen phe duyet
        List<WarehouseNotifyBean> notifyBeanList = incomingShipmentsRepository.getNotifySenToApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.IMPORT_SEND_TO_APPROVE);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity approve(List<Long> ids) {
        List<IncomingShipmentsEntity> incomingShipmentsEntities = incomingShipmentsRepositoryJPA.findByIncomingShipmentIdIn(ids);
        incomingShipmentsEntities.forEach(item -> {
            if (item.getStatusId().equals(IncomingShipmentsEntity.STATUS.CHO_DUYET)) {
                List<IncomingEquipmentsEntity> entityList = incomingShipmentsRepository.findByProperties(IncomingEquipmentsEntity.class, "incomingShipmentId", item.getIncomingShipmentId());
                if (item.getTransferWarehouseId() != null) {
                    TransferringShipmentsEntity transferringShipmentsEntity = new TransferringShipmentsEntity();
                    transferringShipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    transferringShipmentsEntity.setCreatedTime(new Date());
                    transferringShipmentsEntity.setWarehouseId(item.getWarehouseId());
                    transferringShipmentsEntity.setReceivedWarehouseId(item.getTransferWarehouseId());
                    transferringShipmentsEntity.setTransferringDate(item.getTransferredDate());
                    transferringShipmentsEntity.setTransferredEmployeeId(item.getPickingEmployeeId());
                    transferringShipmentsEntity.setCreatedEmployeeId(item.getPickingEmployeeId());
                    transferringShipmentsEntity.setReceivedEmployeeId(item.getReceiverId());
                    transferringShipmentsEntity.setPickingNo(item.getTransferPickingNo());

                    String note = "Điều chuyển vật tư từ kho tổng tháng " + Utils.formatDate(new Date(), "MM");
                    transferringShipmentsEntity.setName(note);
                    transferringShipmentsEntity.setNote(note);
                    transferringShipmentsEntity.setApprovedBy(Utils.getUserEmpCode());
                    transferringShipmentsEntity.setApprovedTime(new Date());
                    transferringShipmentsEntity.setStatusId(TransferringShipmentsEntity.STATUS.PHE_DUYET);
                    transferringShipmentsRepositoryJPA.save(transferringShipmentsEntity);

                    incomingShipmentsRepository.insertTransferEquipments(transferringShipmentsEntity.getTransferringShipmentId(), item.getIncomingShipmentId(), item.getWarehouseId());

                    //truong hop nhap ve va thuc hien xuat kho luon
                    //tao phieu xuat kho tai kho hien tai
                    //dong thoi tao phieu nhap ho tren kho moi
                    OutgoingShipmentsEntity outgoingShipmentsEntity = new OutgoingShipmentsEntity();
                    Long outgoingShipmentId = incomingShipmentsRepository.getNextId(OutgoingShipmentsEntity.class);
                    outgoingShipmentsEntity.setOutgoingShipmentId(outgoingShipmentId);
                    outgoingShipmentsEntity.setCreatedTime(new Date());
                    outgoingShipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    outgoingShipmentsEntity.setWarehouseId(item.getWarehouseId());
                    outgoingShipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    outgoingShipmentsEntity.setPickingNo(CommonUtils.generatePickingNo(Constants.PREFIX_PICKING_NO.OUTGOING, outgoingShipmentId));
                    outgoingShipmentsEntity.setIncomingShipmentId(item.getIncomingShipmentId());
                    outgoingShipmentsEntity.setStatusId(OutgoingShipmentsEntity.STATUS.PHE_DUYET);
                    outgoingShipmentsEntity.setPickingEmployeeId(item.getPickingEmployeeId());
                    outgoingShipmentsEntity.setOutgoingDate(item.getIncomingDate());
                    outgoingShipmentsEntity.setType(OutgoingShipmentsEntity.TYPES.DIEU_CHUYEN);
                    outgoingShipmentsEntity.setApprovedTime(new Date());
                    outgoingShipmentsEntity.setApprovedBy(Utils.getUserEmpCode());
                    outgoingShipmentsEntity.setTransferringShipmentId(transferringShipmentsEntity.getTransferringShipmentId());
                    outgoingShipmentsEntity.setReceiverId(item.getReceiverId());
                    outgoingShipmentsRepositoryJPA.save(outgoingShipmentsEntity);

                    //insert vat tu
                    incomingShipmentsRepository.insertOutGoingEquipments(outgoingShipmentsEntity.getOutgoingShipmentId(), item.getIncomingShipmentId(), item.getWarehouseId());

                    //tao phieu nhap kho
                    //truong hop nhap ve va thuc hien xuat kho luon
                    //tao phieu xuat kho tai kho hien tai
                    //dong thoi tao phieu nhap ho tren kho moi
                    IncomingShipmentsEntity incomingShipmentsEntity = new IncomingShipmentsEntity();
                    incomingShipmentsEntity.setCreatedTime(new Date());
                    incomingShipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    incomingShipmentsEntity.setWarehouseId(item.getTransferWarehouseId());
                    incomingShipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    incomingShipmentsEntity.setStatusId(IncomingShipmentsEntity.STATUS.PHE_DUYET);
                    incomingShipmentsEntity.setPickingEmployeeId(item.getReceiverId());
                    incomingShipmentsEntity.setIncomingDate(item.getTransferredDate());
                    incomingShipmentsEntity.setType(IncomingShipmentsEntity.TYPES.DIEU_CHUYEN);
                    incomingShipmentsEntity.setAuthorId(item.getAuthorId());
                    Long seq = incomingShipmentsRepository.getNextId(IncomingShipmentsEntity.class);
                    incomingShipmentsEntity.setIncomingShipmentId(seq);
                    incomingShipmentsEntity.setPickingNo(CommonUtils.generatePickingNo(Constants.PREFIX_PICKING_NO.INCOMING, seq));
                    incomingShipmentsEntity.setApprovedTime(new Date());
                    incomingShipmentsEntity.setApprovedBy(Utils.getUserEmpCode());
                    incomingShipmentsEntity.setShippedBy(item.getShippedBy());
                    incomingShipmentsEntity.setTransferringShipmentId(transferringShipmentsEntity.getTransferringShipmentId());
                    incomingShipmentsRepositoryJPA.save(incomingShipmentsEntity);

                    //insert vat tu
                    incomingShipmentsRepository.insertIncomingEquipments(incomingShipmentsEntity.getIncomingShipmentId(), item.getIncomingShipmentId());
                }

                incomingShipmentsRepository.approve(Arrays.asList(item.getIncomingShipmentId()));

                if (!Utils.isNullOrEmpty(entityList)) {
                    warehouseEquipmentsService.updateWarehouseEquipments(item.getTransferWarehouseId() != null ? item.getTransferWarehouseId() : item.getWarehouseId(), entityList.get(0).getIncomingShipmentId(), "incoming");
                }
            }
        });

        //Lay nguoi co quyen phe duyet
        List<WarehouseNotifyBean> notifyBeanList = incomingShipmentsRepository.getNotifyApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.IMPORT_APPROVE);
        return ResponseUtils.ok();
    }


    @Override
    public ResponseEntity undoApprove(List<Long> ids) {
        incomingShipmentsRepository.undoApprove(ids);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> downloadImportEquipmentTemplate() throws Exception {
        String pathTemplate = "template/import/bm-import-nhap-moi-vat-tu.xlsx";
        int startDataRow = 6;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        List<EquipmentsResponse> listEquipment = equipmentsRepository.getAllEquipment(null, null);
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (EquipmentsResponse equipmentsResponse : listEquipment) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(equipmentsResponse.getCode(), 1, row);
            dynamicExport.setText(equipmentsResponse.getName(), 2, row);
            dynamicExport.setNumber(equipmentsResponse.getUnitPrice(), 3, row);
            row++;
        }
        dynamicExport.setCellFormat(0, 0, row - 1, 4, ExportExcel.BORDER_FORMAT);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM-import-danh-sach-nhap-moi-vat-tu.xlsx", false);
    }

    @Override
    public List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport) throws ErrorImportException {
        ImportExcel importExcel = new ImportExcel("template/import/bm-import-vat-tu.xml");
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
        List<EquipmentsResponse> equipmentsEntities = equipmentsRepository.getEquipmentByCodes(equipmentCodes, null, null);
        Map<String, EquipmentsResponse> mapEquipments = new HashMap<>();
        equipmentsEntities.forEach(item -> {
            mapEquipments.put(item.getName().toLowerCase(), item);
        });
        int col;
        int row = 0;
        List<StockEquipmentsResponse> result = new ArrayList<>();
        for (Object[] obj : dataList) {
            col = 2;
            EquipmentsResponse equipment = mapEquipments.get(obj[col].toString().toLowerCase());
            if (equipment == null) {
                importExcel.addError(row, col, "Vật tư không hợp lệ", (obj[2]).toString());
                continue;
            }
            if ((Double) obj[3] < 0) {
                importExcel.addError(row, col, "Số lượng phải lớn hơn hoặc bằng 0", (obj[3]).toString());
                continue;
            }
            if ((Double) obj[4] < 0) {
                importExcel.addError(row, col, "Đơn giá phải lớn hơn hoặc bằng 0", (obj[4]).toString());
                continue;
            }
            if (equipment == null) {
                importExcel.addError(row, col, "Dữ liệu vật tư không hợp lệ!", (String) obj[col]);
            } else {
                StockEquipmentsResponse equipmentsResponse = new StockEquipmentsResponse();
                equipmentsResponse.setEquipmentCode((String) obj[1]);
                equipmentsResponse.setEquipmentName((String) obj[2]);
                equipmentsResponse.setQuantity((Double) obj[3]);
                equipmentsResponse.setUnitPrice((Double) obj[4]);
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

    @Override
    public ResponseEntity reject(List<Long> ids, String note) {
        incomingShipmentsRepository.reject(ids, note);

        //Lay nguoi co quyen phe duyet
        List<WarehouseNotifyBean> notifyBeanList = incomingShipmentsRepository.getNotifyApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.IMPORT_REJECT);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity getSeq() {
        return ResponseUtils.ok(incomingShipmentsRepository.getNextId(IncomingShipmentsEntity.class));
    }

}
