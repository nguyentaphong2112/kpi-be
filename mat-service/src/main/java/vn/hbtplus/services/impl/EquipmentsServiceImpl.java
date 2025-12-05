/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.request.EquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.EquipmentsEntity;
import vn.hbtplus.repositories.impl.EquipmentsRepository;
import vn.hbtplus.repositories.jpa.EquipmentsRepositoryJPA;
import vn.hbtplus.services.EquipmentsService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang fpn_equipments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EquipmentsServiceImpl implements EquipmentsService {

    private final EquipmentsRepository equipmentsRepository;
    private final EquipmentsRepositoryJPA equipmentsRepositoryJPA;

    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EquipmentsResponse> searchData(EquipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(equipmentsRepository.searchData(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EquipmentsResponse> searchListData(EquipmentsRequest.SearchForm dto) {
        return ResponseUtils.ok(equipmentsRepository.searchListData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EquipmentsRequest.SubmitForm dto) throws BaseAppException {
        EquipmentsEntity entity;

        if (equipmentsRepository.duplicate(EquipmentsEntity.class, dto.getEquipmentId(), "code", dto.getCode())) {
            throw new BaseAppException("Mã vật tư đã tồn tại!");
        }

        if (equipmentsRepository.duplicate(EquipmentsEntity.class, dto.getEquipmentId(), "name", dto.getName())) {
            throw new BaseAppException("Tên vật tư đã tồn tại!");
        }

        if (dto.getEquipmentId() != null && dto.getEquipmentId() > 0L) {
            entity = equipmentsRepositoryJPA.getById(dto.getEquipmentId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EquipmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        equipmentsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getEquipmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EquipmentsEntity> optional = equipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EquipmentsEntity.class);
        }

        if (equipmentsRepository.isUsedEquipment(id)) {
            throw new RecordNotExistsException("Bản ghi đã được sử dụng!");
        }
        equipmentsRepository.deActiveObject(EquipmentsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EquipmentsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<EquipmentsEntity> optional = equipmentsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EquipmentsEntity.class);
        }
        EquipmentsResponse dto = new EquipmentsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EquipmentsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh-muc-vat-tu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = equipmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-muc-vat-tu.xlsx", false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentsResponse> getAllEquipment() {
        return equipmentsRepository.getAllEquipment(null, null);
    }

    @Override
    public List<EquipmentsResponse> getListEquipment(EquipmentsRequest.SearchForm dto) {
        return equipmentsRepository.getListEquipment(dto);
    }


    @Override
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        String pathTemplate = "template/import/bm-import-danh-muc-vat-tu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.setActiveSheet(1);
        List<Map<String, Object>> equipmentGroupMap = equipmentsRepository.getMapCategory(Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        dynamicExport.replaceKeys(equipmentGroupMap);
        dynamicExport.setActiveSheet(2);
        List<Map<String, Object>> equipmentTypeMap = equipmentsRepository.getMapCategory(Constants.CATEGORY_TYPE.EQUIPMENT_TYPE);
        dynamicExport.replaceKeys(equipmentTypeMap);
        dynamicExport.setActiveSheet(3);
        List<Map<String, Object>> equipmentUnitToExport = equipmentsRepository.getMapCategory(Constants.CATEGORY_TYPE.EQUIPMENT_UNIT);
        dynamicExport.replaceKeys(equipmentUnitToExport);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "bm-import-danh-muc-vat-tu.xlsx", false);
    }

    @Override
    @Transactional
    public Object processImport(MultipartFile file) throws Exception {
        ImportExcel importExcel = new ImportExcel("template/import/bm-import-danh-muc-vat-tu.xml");
        List<Object[]> dataList = new ArrayList<>();

        List<EquipmentsEntity> entityList = new ArrayList<>();

        ResponseEntity<Object> validateFileImport = utilsService.validateFileImport(importExcel, file, dataList);
        if (validateFileImport != null) {
            throw new ErrorImportException(file, importExcel);
        }

        List<String> equipmentCodeList = new ArrayList<>();
        List<String> equipmentTypeCodeList = new ArrayList<>();
        List<String> equipmentGroupCodeList = new ArrayList<>();
        List<String> equipmentNameList = new ArrayList<>();
        List<String> equipmentUnitCodeList = new ArrayList<>();

        int col = 1;
        for (Object[] obj : dataList) {
            col = 1;
            String equipmentCode = (String) obj[col++];
            equipmentCodeList.add(StringUtils.trimToEmpty(equipmentCode).toLowerCase());

            String equipmentTypeCode = (String) obj[col++];
            equipmentTypeCodeList.add(StringUtils.trimToEmpty(equipmentTypeCode).toLowerCase());

            String equipmentGroupCode = (String) obj[col++];
            equipmentGroupCodeList.add(StringUtils.trimToEmpty(equipmentGroupCode).toLowerCase());

            String equipmentName = (String) obj[col++];
            equipmentNameList.add(StringUtils.trimToEmpty(equipmentName).toLowerCase());

            col++;
            String equipmentUnitCode = (String) obj[col];
            equipmentUnitCodeList.add(StringUtils.trimToEmpty(equipmentUnitCode).toLowerCase());
        }

        Map<String, Long> equipmentCodeMap = equipmentsRepository.getMapCodeByCodes(equipmentCodeList);
        Map<String, String> equipmentTypeCodeMap = equipmentsRepository.getListCategory(Constants.CATEGORY_TYPE.EQUIPMENT_TYPE, BaseCategoryDto.class).stream().collect(Collectors.toMap(BaseCategoryDto::getName, BaseCategoryDto::getValue, (existing, replacement) -> replacement));
        Map<String, String> equipmentGroupCodeMap = equipmentsRepository.getListCategory(Constants.CATEGORY_TYPE.EQUIPMENT_GROUP, BaseCategoryDto.class).stream().collect(Collectors.toMap(BaseCategoryDto::getName, BaseCategoryDto::getValue, (existing, replacement) -> replacement));
        Map<String, String> equipmentUnitCodeMap = equipmentsRepository.getListCategory(Constants.CATEGORY_TYPE.EQUIPMENT_UNIT, BaseCategoryDto.class).stream().collect(Collectors.toMap(BaseCategoryDto::getName, BaseCategoryDto::getValue, (existing, replacement) -> replacement));
        Map<String, Long> equipmentNameMap = equipmentsRepository.getMapNameByNames(equipmentNameList);

        int row = 0;
        for (Object[] obj : dataList) {
            col = 1;
            EquipmentsEntity entity = new EquipmentsEntity();

            String equipmentCode = (String) obj[col];
            if (equipmentCodeMap.get(StringUtils.trimToEmpty(equipmentCode).toLowerCase()) != null) {
                importExcel.addError(row, col, "Mã vật tư đã tồn tại", equipmentCode);
            } else {
                entity.setCode(StringUtils.trimToEmpty(equipmentCode));
            }
            col++;

            String inputEquipmentTypeCode = (String) obj[col];
            String equipmentTypeCode = StringUtils.trimToEmpty(inputEquipmentTypeCode).toLowerCase();
            if (equipmentTypeCodeMap.get(equipmentTypeCode) == null) {
                importExcel.addError(row, col, "Loại vật tư không tồn tại", inputEquipmentTypeCode);
            } else {
                entity.setEquipmentTypeId(equipmentTypeCodeMap.get(equipmentTypeCode));
            }
            col++;

            String inputEquipmentGroupCode = (String) obj[col];
            String equipmentGroupCode = StringUtils.trimToEmpty(inputEquipmentGroupCode).toLowerCase();
            if (equipmentGroupCodeMap.get(equipmentGroupCode) == null) {
                importExcel.addError(row, col, "Kiểu loại vật tư không tồn tại", inputEquipmentGroupCode);
            } else {
                entity.setEquipmentGroupId(equipmentGroupCodeMap.get(equipmentGroupCode));
            }
            col++;

            String equipmentName = (String) obj[col];
            if (equipmentNameMap.get(StringUtils.trimToEmpty(equipmentName).toLowerCase()) != null) {
                importExcel.addError(row, col, "Tên vật tư đã tồn tại", equipmentName);
            } else {
                entity.setName(StringUtils.trimToEmpty(equipmentName));
            }
            col++;

            String description = (String) obj[col];
            entity.setDescription(StringUtils.trimToEmpty(description));
            col++;

            String inputEquipmentUnitCode = (String) obj[col];
            String equipmentUnit = StringUtils.trimToEmpty(inputEquipmentUnitCode).toLowerCase();
            if (equipmentUnitCodeMap.get(equipmentUnit) == null) {
                importExcel.addError(row, col, "Đơn vị vật tư không tồn tại", inputEquipmentUnitCode);
            } else {
                entity.setEquipmentUnitId(equipmentUnitCodeMap.get(equipmentUnit));
            }
            col++;

            entity.setWarningDays((Long) obj[col]);
            col++;

            String inputFlag = (String) obj[col];
            inputFlag = StringUtils.trimToEmpty(inputFlag);
            entity.setIsSerialChecking(StringUtils.equalsAnyIgnoreCase(inputFlag, "có") ? "Y" : "N");

            col++;

            String serialNo = (String) obj[col];
            entity.setSerialNo(StringUtils.trimToEmpty(serialNo));
            col++;

            entity.setUnitPrice((Long) obj[col]);
            col++;

            String location = (String) obj[col];
            entity.setLocation(StringUtils.trimToEmpty(location));
            col++;

            String note = (String) obj[col];
            entity.setNote(StringUtils.trimToEmpty(note));

            row++;
            entityList.add(entity);
        }
        if (importExcel.hasError()) {
            throw new ErrorImportException(file, importExcel);
        } else {
            equipmentsRepository.insertBatch(EquipmentsEntity.class, entityList, Utils.getUserNameLogin());
        }

        return null;
    }

    @Override
    public List<EquipmentsResponse> getEquipmentByNames(List<String> equipmentNames) {
        return equipmentsRepository.getEquipmentByNames(equipmentNames);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentsResponse> getEquipmentByIds(List<Long> equipmentIds) {
        if (Utils.isNullOrEmpty(equipmentIds)) {
            return new ArrayList<>();
        }

        return equipmentsRepository.getEquipmentByIds(equipmentIds);
    }

    @Override
    public List<EquipmentsResponse> getListByType(Long equipmentTypeId) {
        return equipmentsRepository.getListByType(equipmentTypeId);
    }
}
