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
import vn.hbtplus.models.request.OutgoingShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.models.response.OutgoingEquipmentsResponse;
import vn.hbtplus.models.response.OutgoingShipmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.IncomingShipmentsEntity;
import vn.hbtplus.repositories.entity.OutgoingEquipmentsEntity;
import vn.hbtplus.repositories.entity.OutgoingShipmentsEntity;
import vn.hbtplus.repositories.impl.EquipmentsRepository;
import vn.hbtplus.repositories.impl.OutgoingShipmentsRepository;
import vn.hbtplus.repositories.jpa.OutgoingEquipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.OutgoingShipmentsRepositoryJPA;
import vn.hbtplus.services.EmployeeService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.OutgoingShipmentsService;
import vn.hbtplus.services.WarehouseEquipmentsService;
import vn.hbtplus.services.WarehousesService;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.UtilsService;
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
 * Lop impl service ung voi bang stk_outgoing_shipments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class OutgoingShipmentsServiceImpl implements OutgoingShipmentsService {

    private final OutgoingShipmentsRepository outgoingShipmentsRepository;
    private final OutgoingShipmentsRepositoryJPA outgoingShipmentsRepositoryJPA;
    private final OutgoingEquipmentsRepositoryJPA outgoingEquipmentsRepositoryJPA;
    private final WarehouseEquipmentsService warehouseEquipmentsService;
    private final FileService fileService;
    private final AttachmentService attachmentService;
    private final EquipmentsRepository equipmentsRepository;

    private final EmployeeService employeeService;
    private final WarehousesService warehousesService;

    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<OutgoingShipmentsResponse> searchData(OutgoingShipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(outgoingShipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(OutgoingShipmentsRequest.SubmitForm dto) throws BaseAppException {
        OutgoingShipmentsEntity entity;
        if (dto.getOutgoingShipmentId() != null && dto.getOutgoingShipmentId() > 0L) {
            entity = outgoingShipmentsRepositoryJPA.getById(dto.getOutgoingShipmentId());
            //chi duoc update ban ghi o trang thai tu choi, cho phe duyet, du thao
            if (Arrays.asList(IncomingShipmentsEntity.STATUS.PHE_DUYET).contains(entity.getStatusId())) {
                throw new BaseAppException("Trạng thái không hợp lệ!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new OutgoingShipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        if ("Y".equalsIgnoreCase(dto.getIsSendToApprove())) {
            entity.setStatusId(OutgoingShipmentsEntity.STATUS.CHO_DUYET);
        } else {
            entity.setStatusId(OutgoingShipmentsEntity.STATUS.DU_THAO);
        }
        entity.setType(OutgoingShipmentsEntity.TYPES.SU_DUNG);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        outgoingShipmentsRepositoryJPA.save(entity);

        //Luu du lieu vat tu
        List<OutgoingEquipmentsEntity> listEquipmentEntities = new ArrayList<>();
        if (dto.getOutgoingShipmentId() != null && dto.getOutgoingShipmentId() > 0L) {
            listEquipmentEntities = outgoingShipmentsRepository.findByProperties(OutgoingEquipmentsEntity.class, "outgoingShipmentId", dto.getOutgoingShipmentId());
        }
        Map<Long, OutgoingEquipmentsEntity> mapEquipmentEntities = new HashMap<>();
        listEquipmentEntities.forEach(item -> {
            mapEquipmentEntities.put(item.getEquipmentId(), item);
        });

        if (!Utils.isNullOrEmpty(dto.getListEquipments())) {
            dto.getListEquipments().forEach(item -> {
                OutgoingEquipmentsEntity outgoingEquipmentsEntity = mapEquipmentEntities.get(item.getEquipmentId());
                if (outgoingEquipmentsEntity == null) {
                    outgoingEquipmentsEntity = new OutgoingEquipmentsEntity();
                    outgoingEquipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    outgoingEquipmentsEntity.setCreatedTime(new Date());
                } else {
                    outgoingEquipmentsEntity.setModifiedBy(Utils.getUserNameLogin());
                    outgoingEquipmentsEntity.setModifiedTime(new Date());
                }
                outgoingEquipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                outgoingEquipmentsEntity.setEquipmentId(item.getEquipmentId());
                outgoingEquipmentsEntity.setOutgoingShipmentId(entity.getOutgoingShipmentId());
                outgoingEquipmentsEntity.setQuantity(item.getQuantity());
                outgoingEquipmentsEntity.setUnitPrice(item.getUnitPrice());
                outgoingEquipmentsEntity.setInventoryQuantity(item.getInventoryQuantity());
                mapEquipmentEntities.put(item.getEquipmentId(), null);
                outgoingEquipmentsRepositoryJPA.save(outgoingEquipmentsEntity);
            });
        }


        List<Long> listDeleted = mapEquipmentEntities.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!Utils.isNullOrEmpty(listDeleted)) {
            outgoingShipmentsRepository.deActiveObjectByPairList(OutgoingEquipmentsEntity.class, Pair.of("equipmentId", listDeleted), Pair.of("outgoingShipmentId", List.of(entity.getOutgoingShipmentId())));
        }


        //Luu file dinh kem
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            fileService.uploadFiles(dto.getFiles(), entity.getOutgoingShipmentId(), Constants.ATTACHMENT.TABLE_NAMES.MAT_OUTGOING_SHIPMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_OUTGOING_SHIPMENTS, Constants.ATTACHMENT.MODULE);
        }

        if (!Utils.isNullOrEmpty(dto.getDocIdsDelete()) && dto.getOutgoingShipmentId() != null && dto.getOutgoingShipmentId() > 0l) {
            fileService.deActiveFileByAttachmentId(dto.getDocIdsDelete(), Constants.ATTACHMENT.TABLE_NAMES.MAT_OUTGOING_SHIPMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_OUTGOING_SHIPMENTS);
        }
        if (OutgoingShipmentsEntity.STATUS.CHO_DUYET.equalsIgnoreCase(entity.getStatusId())) {
            List<WarehouseNotifyBean> notifyBeanList = outgoingShipmentsRepository.getNotifySenToApprove(Arrays.asList(entity.getOutgoingShipmentId()));
            warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.OUTPUT_SEND_TO_APPROVE);
        }

        return ResponseUtils.ok(entity.getOutgoingShipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<OutgoingShipmentsEntity> optional = outgoingShipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OutgoingShipmentsEntity.class);
        }
        outgoingShipmentsRepository.deActiveObject(OutgoingEquipmentsEntity.class, "outgoingShipmentId", id);
        outgoingShipmentsRepository.deActiveObject(OutgoingShipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OutgoingShipmentsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<OutgoingShipmentsEntity> optional = outgoingShipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, OutgoingShipmentsEntity.class);
        }
        OutgoingShipmentsResponse dto = new OutgoingShipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        if (!Utils.isNullOrEmpty(dto.getApprovedBy())) {
            EmployeesResponse response = employeeService.getEmployeeByEmpCode(dto.getApprovedBy());
            if (response != null) {
                dto.setApprovedId(response.getEmployeeId());
                dto.setApprovedName(response.getFullName());
            }
        }

        if (dto.getTransferringShipmentId() != null) {
            OutgoingShipmentsResponse tmp = outgoingShipmentsRepository.getIncomingWarehouseByTransferShipmentId(dto.getTransferringShipmentId());
            dto.setIncomingWarehouseId(tmp.getIncomingWarehouseId());
            dto.setIncomingTransferPickingNo(tmp.getIncomingTransferPickingNo());
        }
        dto.setHasApproveExport(outgoingShipmentsRepository.getPermissionApprove(dto.getWarehouseId()));
        dto.setFiles(attachmentService.getAttachmentList(Constants.ATTACHMENT.TABLE_NAMES.MAT_OUTGOING_SHIPMENTS,
                Constants.ATTACHMENT.FILE_TYPES.MAT_OUTGOING_SHIPMENTS, List.of(dto.getOutgoingShipmentId())));
        List<OutgoingEquipmentsResponse> listEquipments = outgoingShipmentsRepository.getListEquipmentByOutgoingShipments(id, dto.getWarehouseId());
        dto.setListEquipments(listEquipments);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(OutgoingShipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh-sach-phieu-xuat-kho.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = outgoingShipmentsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-phieu-xuat-kho.xlsx");
    }

    @Override
    public ResponseEntity sendToApprove(List<Long> ids) {
        outgoingShipmentsRepository.sendToApprove(ids);

        List<WarehouseNotifyBean> notifyBeanList = outgoingShipmentsRepository.getNotifySenToApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.OUTPUT_SEND_TO_APPROVE);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity approve(List<Long> ids) throws BaseAppException {
        //validate vat tu trong kho khong duoc nho hon phieu xuat
        String validateQuantity = outgoingShipmentsRepository.validateQuantity(ids);
        if(!Utils.isNullOrEmpty(validateQuantity)) {
            throw new BaseAppException("Vật tư : " + validateQuantity + " có số lượng xuất lớn hơn số lượng vật tư hiện tại trong kho!");
        }

        List<OutgoingShipmentsEntity> outgoingShipmentsEntities = outgoingShipmentsRepositoryJPA.findByOutgoingShipmentIdIn(ids);
        outgoingShipmentsEntities.forEach(item -> {
            if (item.getStatusId().equals(OutgoingShipmentsEntity.STATUS.CHO_DUYET)) {
                outgoingShipmentsRepository.approve(Arrays.asList(item.getOutgoingShipmentId()));
                warehouseEquipmentsService.updateWarehouseEquipments(item.getWarehouseId(), item.getOutgoingShipmentId(), "outgoing");
            }
        });
        List<WarehouseNotifyBean> notifyBeanList = outgoingShipmentsRepository.getNotifyApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.OUTPUT_APPROVE);

        return ResponseUtils.ok();
    }


    @Override
    public ResponseEntity reject(List<Long> ids, String note) {
        outgoingShipmentsRepository.reject(ids, note);
        List<WarehouseNotifyBean> notifyBeanList = outgoingShipmentsRepository.getNotifyApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.OUTPUT_REJECT);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity getSeq() {
        return ResponseUtils.ok(outgoingShipmentsRepository.getNextId(OutgoingShipmentsEntity.class));
    }

    @Override
    public ResponseEntity<Object> downloadImportEquipmentTemplate(Long warehouseId) throws Exception {
        String pathTemplate = "template/import/bm-import-xuat-kho-vat-tu.xlsx";
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
        return ResponseUtils.ok(dynamicExport, "BM-import-danh-sach-xuat-kho-vat-tu.xlsx", false);
    }

    @Override
    public List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport, Long warehouseId) throws ErrorImportException {
        ImportExcel importExcel = new ImportExcel("template/import/bm-import-xuat-kho-vat-tu.xml");
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
            EquipmentsResponse equipment = mapEquipments.get(obj[col].toString().toLowerCase());
            if (equipment == null) {
                importExcel.addError(row, col, "Dữ liệu vật tư không hợp lệ!", (String) obj[col]);
            } else {
                if (equipment.getInventoryQuantity() < (Double) obj[5]) {
                    importExcel.addError(row, 5, "Số lượng vượt quá số lượng tồn kho!", String.valueOf(obj[5]));
                }
                if ((Double) obj[3] < 0) {
                    importExcel.addError(row, 3, "Đơn giá phải > 0", String.valueOf(obj[5]));
                }
                if ((Double) obj[5] < 0) {
                    importExcel.addError(row, 5, "Số lượng xuất kho phải > 0", String.valueOf(obj[5]));
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
