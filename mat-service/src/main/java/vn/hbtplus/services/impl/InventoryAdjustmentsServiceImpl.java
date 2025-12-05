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
import vn.hbtplus.models.request.InventoryAdjustmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.models.response.InventoryAdjustEquipmentsResponse;
import vn.hbtplus.models.response.InventoryAdjustmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.InventoryAdjustEquipmentsEntity;
import vn.hbtplus.repositories.entity.InventoryAdjustmentsEntity;
import vn.hbtplus.repositories.entity.WarehouseEquipmentsEntity;
import vn.hbtplus.repositories.impl.EquipmentsRepository;
import vn.hbtplus.repositories.impl.InventoryAdjustmentsRepository;
import vn.hbtplus.repositories.jpa.InventoryAdjustEquipmentsRepositoryJPA;
import vn.hbtplus.repositories.jpa.InventoryAdjustmentsRepositoryJPA;
import vn.hbtplus.services.EmployeeService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.InventoryAdjustmentsService;
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
 * Lop impl service ung voi bang stk_inventory_adjustments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class InventoryAdjustmentsServiceImpl implements InventoryAdjustmentsService {

    private final InventoryAdjustmentsRepository inventoryAdjustmentsRepository;
    private final InventoryAdjustmentsRepositoryJPA inventoryAdjustmentsRepositoryJPA;
    private final InventoryAdjustEquipmentsRepositoryJPA inventoryAdjustEquipmentsRepositoryJPA;
    private final FileService fileService;
    private final AttachmentService attachmentService;
    private final WarehouseEquipmentsService warehouseEquipmentsService;
    private final EquipmentsRepository equipmentsRepository;

    private final EmployeeService employeeService;
    private final WarehousesService warehousesService;

    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InventoryAdjustmentsResponse> searchData(InventoryAdjustmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(inventoryAdjustmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(InventoryAdjustmentsRequest.SubmitForm dto) throws BaseAppException {
        InventoryAdjustmentsEntity entity;
        if (dto.getInventoryAdjustmentId() != null && dto.getInventoryAdjustmentId() > 0L) {
            entity = inventoryAdjustmentsRepositoryJPA.getById(dto.getInventoryAdjustmentId());
            //chi duoc update ban ghi o trang thai tu choi, cho phe duyet, du thao
            if (Arrays.asList(InventoryAdjustmentsEntity.STATUS.PHE_DUYET).contains(entity.getStatusId())) {
                throw new BaseAppException("Trạng thái không hợp lệ!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new InventoryAdjustmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        if ("Y".equalsIgnoreCase(dto.getIsSendToApprove())) {
            entity.setStatusId(InventoryAdjustmentsEntity.STATUS.CHO_DUYET);
        } else {
            entity.setStatusId(InventoryAdjustmentsEntity.STATUS.DU_THAO);
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        inventoryAdjustmentsRepositoryJPA.save(entity);

        //Luu du lieu vat tu
        List<InventoryAdjustEquipmentsEntity> listEquipmentEntities = new ArrayList<>();
        if (dto.getInventoryAdjustmentId() != null && dto.getInventoryAdjustmentId() > 0L) {
            listEquipmentEntities = inventoryAdjustmentsRepository.findByProperties(InventoryAdjustEquipmentsEntity.class, "inventoryAjustmentId", dto.getInventoryAdjustmentId());
        }
        Map<Long, InventoryAdjustEquipmentsEntity> mapEquipmentEntities = new HashMap<>();
        listEquipmentEntities.forEach(item -> {
            mapEquipmentEntities.put(item.getEquipmentId(), item);
        });

        List<WarehouseEquipmentsEntity> listWarehouseEquipmentsEntities = inventoryAdjustmentsRepository.findByProperties(WarehouseEquipmentsEntity.class,
                "warehouseId", dto.getWarehouseId());
        Map<Long, WarehouseEquipmentsEntity> mapWarehouseEquipmentsEntities = new HashMap<>();
        listWarehouseEquipmentsEntities.forEach(item -> {
            mapWarehouseEquipmentsEntities.put(item.getEquipmentId(), item);
        });

        if (!Utils.isNullOrEmpty(dto.getListEquipments())) {
            dto.getListEquipments().forEach(item -> {
                InventoryAdjustEquipmentsEntity inventoryAdjustEquipmentsEntity = mapEquipmentEntities.get(item.getEquipmentId());
                if (inventoryAdjustEquipmentsEntity == null) {
                    inventoryAdjustEquipmentsEntity = new InventoryAdjustEquipmentsEntity();
                    inventoryAdjustEquipmentsEntity.setCreatedBy(Utils.getUserNameLogin());
                    inventoryAdjustEquipmentsEntity.setCreatedTime(new Date());
                } else {
                    inventoryAdjustEquipmentsEntity.setModifiedBy(Utils.getUserNameLogin());
                    inventoryAdjustEquipmentsEntity.setModifiedTime(new Date());
                }
                inventoryAdjustEquipmentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                inventoryAdjustEquipmentsEntity.setEquipmentId(item.getEquipmentId());
                inventoryAdjustEquipmentsEntity.setInventoryAjustmentId(entity.getInventoryAdjustmentId());
                inventoryAdjustEquipmentsEntity.setQuantity(item.getQuantity());
                inventoryAdjustEquipmentsEntity.setUnitPrice(item.getUnitPrice());
                inventoryAdjustEquipmentsEntity.setInventoryQuantity(
                        mapWarehouseEquipmentsEntities.get(item.getEquipmentId()) == null ? 0 : mapWarehouseEquipmentsEntities.get(item.getEquipmentId()).getQuantity()
                );
                mapEquipmentEntities.put(item.getEquipmentId(), null);
                inventoryAdjustEquipmentsRepositoryJPA.save(inventoryAdjustEquipmentsEntity);
            });
        }

        List<Long> listDeleted = mapEquipmentEntities.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!Utils.isNullOrEmpty(listDeleted)) {
            inventoryAdjustmentsRepository.deActiveObjectByPairList(InventoryAdjustEquipmentsEntity.class, Pair.of("equipmentId", listDeleted), Pair.of("inventoryAdjustmentId", List.of(entity.getInventoryAdjustmentId())));
        }


        //Luu file dinh kem
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            fileService.uploadFiles(dto.getFiles(), entity.getInventoryAdjustmentId(), Constants.ATTACHMENT.TABLE_NAMES.MAT_INVENTORY_ADJUSTMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_INVENTORY_ADJUSTMENTS, Constants.ATTACHMENT.MODULE);
        }

        if (!Utils.isNullOrEmpty(dto.getDocIdsDelete()) && dto.getInventoryAdjustmentId() != null && dto.getInventoryAdjustmentId() > 0l) {
            fileService.deActiveFileByAttachmentId(dto.getDocIdsDelete(), Constants.ATTACHMENT.TABLE_NAMES.MAT_INVENTORY_ADJUSTMENTS,
                    Constants.ATTACHMENT.FILE_TYPES.MAT_INVENTORY_ADJUSTMENTS);
        }
        if (InventoryAdjustmentsEntity.STATUS.CHO_DUYET.equalsIgnoreCase(entity.getStatusId())) {
            List<WarehouseNotifyBean> notifyBeanList = inventoryAdjustmentsRepository.getNotifySenToApprove(Arrays.asList(entity.getInventoryAdjustmentId()));
            warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.ADJUSTMENT_SEND_TO_APPROVE);
        }
        return ResponseUtils.ok(entity.getInventoryAdjustmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<InventoryAdjustmentsEntity> optional = inventoryAdjustmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InventoryAdjustmentsEntity.class);
        }
        inventoryAdjustmentsRepository.deActiveObject(InventoryAdjustEquipmentsEntity.class, "inventoryAjustmentId", id);
        inventoryAdjustmentsRepository.deActiveObject(InventoryAdjustmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<InventoryAdjustmentsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<InventoryAdjustmentsEntity> optional = inventoryAdjustmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InventoryAdjustmentsEntity.class);
        }
        InventoryAdjustmentsResponse dto = new InventoryAdjustmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        if (!Utils.isNullOrEmpty(dto.getApprovedBy())) {
            EmployeesResponse response = employeeService.getEmployeeByEmpCode(dto.getApprovedBy());
            if (response != null) {
                dto.setApprovedId(response.getEmployeeId());
                dto.setApprovedName(response.getFullName());
            }
        }
        dto.setHasApproveAdjustment(inventoryAdjustmentsRepository.getPermissionApprove(dto.getWarehouseId()));
        dto.setFiles(attachmentService.getAttachmentList(Constants.ATTACHMENT.TABLE_NAMES.MAT_INVENTORY_ADJUSTMENTS,
                Constants.ATTACHMENT.FILE_TYPES.MAT_INVENTORY_ADJUSTMENTS, List.of(id)));

        List<InventoryAdjustEquipmentsResponse> listEquipments = inventoryAdjustmentsRepository.getListEquipmentByInventoryAdjustment(id, dto.getWarehouseId());
        dto.setListEquipments(listEquipments);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(InventoryAdjustmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh-sach-kiem-ke.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = inventoryAdjustmentsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-kiem-ke.xlsx");
    }

    @Override
    public ResponseEntity getSeq() {
        return ResponseUtils.ok(inventoryAdjustmentsRepository.getNextId(InventoryAdjustmentsEntity.class));
    }

    @Override
    @Transactional
    public ResponseEntity sendToApprove(List<Long> ids) {
        inventoryAdjustmentsRepository.sendToApprove(ids);
        List<WarehouseNotifyBean> notifyBeanList = inventoryAdjustmentsRepository.getNotifySenToApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.ADJUSTMENT_SEND_TO_APPROVE);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity approve(List<Long> ids) {
        List<InventoryAdjustmentsEntity> outgoingShipmentsEntities = inventoryAdjustmentsRepositoryJPA.findByInventoryAdjustmentIdIn(ids);
        outgoingShipmentsEntities.forEach(item -> {
            if (item.getStatusId().equals(InventoryAdjustmentsEntity.STATUS.CHO_DUYET)) {
                inventoryAdjustmentsRepository.approve(Arrays.asList(item.getInventoryAdjustmentId()));
                //thuc hien cap nhat lai vat tu trong kho
                warehouseEquipmentsService.updateWarehouseEquipments(item.getWarehouseId(), item.getInventoryAdjustmentId(), "adjustment");
            }
        });

        List<WarehouseNotifyBean> notifyBeanList = inventoryAdjustmentsRepository.getNotifyApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.ADJUSTMENT_APPROVE);

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity undoApprove(List<Long> ids) {
        inventoryAdjustmentsRepository.undoApprove(ids);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity reject(List<Long> ids, String note) {
        inventoryAdjustmentsRepository.reject(ids, note);
        List<WarehouseNotifyBean> notifyBeanList = inventoryAdjustmentsRepository.getNotifyApprove(ids);
        warehousesService.sendNotification(notifyBeanList, WarehouseNotifyBean.FUNCTION_CODES.ADJUSTMENT_REJECT);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> downloadImportEquipmentTemplate(Long warehouseId, String isIncrease) throws Exception {
        String pathTemplate = "template/import/bm-import-kiem-ke-vat-tu.xlsx";
        int startDataRow = 6;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        List<EquipmentsResponse> listEquipment = equipmentsRepository.getAllEquipment(warehouseId, isIncrease);
        int row = 1;
        dynamicExport.setActiveSheet(1);
        for (EquipmentsResponse equipmentsResponse : listEquipment) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(equipmentsResponse.getCode(), 1, row);
            dynamicExport.setText(equipmentsResponse.getName(), 2, row);
            dynamicExport.setNumber(equipmentsResponse.getUnitPrice(), 3, row);
            dynamicExport.setNumber(equipmentsResponse.getInventoryQuantity() != null ? equipmentsResponse.getInventoryQuantity() : 0, 4, row);
            row++;
        }
        dynamicExport.setCellFormat(0, 0, row - 1, 4, ExportExcel.BORDER_FORMAT);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM-import-danh-sach-kiem-ke-vat-tu.xlsx", false);
    }

    @Override
    public List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport, Long warehouseId, String isIncrease) throws ErrorImportException {
        ImportExcel importExcel = new ImportExcel("template/import/bm-import-kiem-ke-vat-tu.xml");
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
        List<EquipmentsResponse> equipmentsEntities = equipmentsRepository.getEquipmentByCodes(equipmentCodes, warehouseId, isIncrease);
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
                if (Utils.NVL(equipment.getInventoryQuantity(), 0D) < (Double) obj[5] && !"Y".equals(isIncrease)) {
                    importExcel.addError(row, 5, "Số lượng vượt quá số lượng tồn kho!", String.valueOf(obj[5]));
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
