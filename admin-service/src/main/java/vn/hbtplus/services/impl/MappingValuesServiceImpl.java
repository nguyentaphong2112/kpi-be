/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.models.request.MappingValuesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ConfigMappingsEntity;
import vn.hbtplus.repositories.entity.MappingValuesEntity;
import vn.hbtplus.repositories.impl.ConfigMappingsRepository;
import vn.hbtplus.repositories.impl.MappingValuesRepository;
import vn.hbtplus.repositories.jpa.MappingValuesRepositoryJPA;
import vn.hbtplus.services.MappingValuesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.*;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;

import java.util.*;

/**
 * Lop impl service ung voi bang sys_mapping_values
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class MappingValuesServiceImpl implements MappingValuesService {

    private final MappingValuesRepository mappingValuesRepository;
    private final MappingValuesRepositoryJPA mappingValuesRepositoryJPA;
    private final ConfigMappingsRepository configMappingsRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<MappingValuesResponse> searchData(MappingValuesRequest.SearchForm dto, String configMappingCode) {
        return ResponseUtils.ok(mappingValuesRepository.searchData(dto, configMappingCode));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(MappingValuesRequest.SubmitForm dto, String configMappingCode, Long id) throws BaseAppException {
        boolean duplicate = mappingValuesRepository.duplicate(MappingValuesEntity.class, id, "configMappingCode", configMappingCode, "parameter", dto.getParameter());
        if (duplicate) {
            throw new BaseAppException("SAVE_MAPPING_VALUE_DUPLICATE_PARAMETER", I18n.getMessage("error.mappingValues.duplicateParameter"));
        }
        MappingValuesEntity entity;
        if (id != null && id > 0L) {
            entity = mappingValuesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new MappingValuesEntity();
            entity.setConfigMappingCode(configMappingCode);
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        mappingValuesRepositoryJPA.save(entity);

        //update ngay het hieu luc cua qua trinh cu
        mappingValuesRepository.updateEndDate(dto.getConfigMappingCode(), List.of(dto.getParameter()), dto.getStartDate());
        return ResponseUtils.ok(entity.getMappingValueId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<MappingValuesEntity> optional = mappingValuesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MappingValuesEntity.class);
        }
        mappingValuesRepository.deActiveObject(MappingValuesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<MappingValuesResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<MappingValuesEntity> optional = mappingValuesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MappingValuesEntity.class);
        }
        MappingValuesResponse dto = new MappingValuesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(MappingValuesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = mappingValuesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ResponseEntity<Object> downloadTemplate(String configMappingCode) throws Exception {
        ExportExcel exportExcel = new ExportExcel("template/import/BM_import_du_lieu_mapping.xlsx", 2, true);
        ConfigMappingsEntity config = configMappingsRepository.get(ConfigMappingsEntity.class, "code", configMappingCode);
        HashMap<String, Object> params = new HashMap<>();
        params.put("config_mapping", config.getName());
        params.put("parameter_title", config.getParameterTitle());
        params.put("value_title", config.getValueTitle());
        exportExcel.replaceKeys(params);
        return ResponseUtils.ok(exportExcel, "BM_import_du_lieu_mapping.xlsx", false);
    }

    @Override
    @Transactional
    public ResponseEntity importData(MultipartFile file, MappingValuesRequest.ImportForm dto, String configMappingCode) throws Exception {
        ConfigMappingsEntity configMapping = configMappingsRepository.get(ConfigMappingsEntity.class, "code", configMappingCode);
        List<ImportExcel.ImportConfigBean> columnConfigs = new ArrayList<>();
        columnConfigs.add(new ImportExcel.ImportConfigBean("STT", ImportExcel.STRING, true, 20, false));
        columnConfigs.add(new ImportExcel.ImportConfigBean(configMapping.getParameterTitle(), ImportExcel.STRING, false, 50, false));
        if (configMapping.getDataType().equals(Constant.CONFIG_MAPPING_DATA_TYPE.STRING)) {
            columnConfigs.add(new ImportExcel.ImportConfigBean(configMapping.getValueTitle(), ImportExcel.STRING, false, 50, false));
        } else if (configMapping.getDataType().equals(Constant.CONFIG_MAPPING_DATA_TYPE.DATE)) {
            columnConfigs.add(new ImportExcel.ImportConfigBean(configMapping.getValueTitle(), ImportExcel.DATE, false, 10, false));
        } else if (configMapping.getDataType().equals(Constant.CONFIG_MAPPING_DATA_TYPE.DOUBLE)) {
            columnConfigs.add(new ImportExcel.ImportConfigBean(configMapping.getValueTitle(), ImportExcel.DOUBLE, false, 50, false));
        } else {
            columnConfigs.add(new ImportExcel.ImportConfigBean(configMapping.getValueTitle(), ImportExcel.LONG, false, 50, false));
        }

        ImportExcel importExcel = new ImportExcel(columnConfigs.toArray(new ImportExcel.ImportConfigBean[]{}), 10000, 3);

        List<Object[]> dataList = new ArrayList<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<MappingValuesEntity> listOldData = mappingValuesRepository.findByProperties(MappingValuesEntity.class, "configMappingCode", configMappingCode);
            Map<String, List<MappingValuesEntity>> mapOldData = new HashMap<>();
            listOldData.forEach(record -> {
                if (!mapOldData.containsKey(record.getParameter())) {
                    mapOldData.put(record.getParameter(), new ArrayList<>());
                }
                mapOldData.get(record.getParameter()).add(record);
            });

            List<MappingValuesEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();
            Date startDate = Utils.stringToDate(dto.getStartDate().replaceAll("\\s*\\(.*\\)$", ""), "EEE MMM dd yyyy HH:mm:ss 'GMT'Z");
            Date endDate = Utils.stringToDate(dto.getEndDate().replaceAll("\\s*\\(.*\\)$", ""), "EEE MMM dd yyyy HH:mm:ss 'GMT'Z");

            int row = 0;
            List<String> parameters = new ArrayList<>();
            for (Object[] obj : dataList) {
                MappingValuesEntity entity = new MappingValuesEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                int col = 0;
                String parameter = (String) obj[1];
                parameters.add(parameter);
                entity.setParameter(parameter);

                if (configMapping.getDataType().equals(Constant.CONFIG_MAPPING_DATA_TYPE.STRING)) {
                    String value = (String) obj[2];
                    entity.setValue(value);
                } else if (configMapping.getDataType().equals(Constant.CONFIG_MAPPING_DATA_TYPE.DATE)) {
                    Date value = (Date) obj[2];
                    entity.setValue(Utils.formatDate(value));
                } else if (configMapping.getDataType().equals(Constant.CONFIG_MAPPING_DATA_TYPE.DOUBLE)) {
                    Double value = (Double) obj[2];
                    entity.setValue(value.toString());
                } else {
                    Integer value = (Integer) obj[2];
                    entity.setValue(value.toString());
                }
                entity.setStartDate(startDate);
                entity.setEndDate(endDate);
                entity.setConfigMappingCode(configMappingCode);

                listInsert.add(entity);

                //check conflict trong file
                for (int nextRow = row + 1; nextRow < dataList.size(); nextRow++) {
                    Object[] obj2 = dataList.get(nextRow);
                    if (parameter.equals((String) obj2[1])) {
                        importExcel.addError(row, col, I18n.getMessage("error.mappingValues.duplicateInFile", configMapping.getParameterTitle(), parameter), parameter);
                    }
                }
                //check conflict voi du lieu trong database
                List<MappingValuesEntity> oldDataList = mapOldData.get(parameter);
                if (!Utils.isNullOrEmpty(oldDataList)) {
                    for (MappingValuesEntity oldData : oldDataList) {
                        String oldDataKey = oldData.getParameter();
                        if (oldDataKey.equals(parameter)) {
                            importExcel.addError(row, col, I18n.getMessage("error.mappingValues.duplicateInDatabase", parameter), parameter);
                        }
                    }
                }

                row++;

            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                mappingValuesRepository.insertBatch(MappingValuesEntity.class, listInsert, userName);
                mappingValuesRepository.updateEndDate(configMappingCode, parameters, Utils.stringToDate(dto.getStartDate()));

            }

        } else {
            throw new ErrorImportException(file, importExcel);
        }

        return ResponseUtils.ok(true);
    }

}
