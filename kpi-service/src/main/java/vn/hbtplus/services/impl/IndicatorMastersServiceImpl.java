/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.IndicatorMastersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.IndicatorConversionsResponse;
import vn.hbtplus.models.response.IndicatorMastersResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.IndicatorConversionsRepository;
import vn.hbtplus.repositories.impl.IndicatorMastersRepository;
import vn.hbtplus.repositories.impl.IndicatorsRepository;
import vn.hbtplus.repositories.jpa.IndicatorConversionDetailsRepositoryJPA;
import vn.hbtplus.repositories.jpa.IndicatorConversionsRepositoryJPA;
import vn.hbtplus.repositories.jpa.IndicatorMastersRepositoryJPA;
import vn.hbtplus.repositories.jpa.IndicatorsRepositoryJPA;
import vn.hbtplus.services.IndicatorConversionsService;
import vn.hbtplus.services.IndicatorMastersService;
import vn.hbtplus.services.ObjectRelationsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang kpi_indicator_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class IndicatorMastersServiceImpl implements IndicatorMastersService {

    private final IndicatorMastersRepository indicatorMastersRepository;
    private final IndicatorMastersRepositoryJPA indicatorMastersRepositoryJPA;
    private final IndicatorConversionsRepository indicatorConversionsRepository;
    private final IndicatorsRepository indicatorsRepository;
    private final IndicatorsRepositoryJPA indicatorsRepositoryJPA;
    private final ObjectRelationsService objectRelationsService;
    private final IndicatorConversionDetailsRepositoryJPA indicatorConversionDetailsRepositoryJPA;
    private final IndicatorConversionsRepositoryJPA indicatorConversionsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<IndicatorMastersResponse> searchData(IndicatorMastersRequest.SearchForm dto) throws Exception {
        return ResponseUtils.ok(indicatorMastersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(IndicatorMastersRequest.SubmitForm dto) throws BaseAppException {
        IndicatorMastersEntity entity;
        if (dto.getIndicatorMasterId() != null && dto.getIndicatorMasterId() > 0L) {
            entity = indicatorMastersRepositoryJPA.getById(dto.getIndicatorMasterId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new IndicatorMastersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setStatusId(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        indicatorMastersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getIndicatorMasterId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<IndicatorMastersEntity> optional = indicatorMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorMastersEntity.class);
        }
        indicatorMastersRepository.deActiveObject(IndicatorMastersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IndicatorMastersResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<IndicatorMastersEntity> optional = indicatorMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IndicatorMastersEntity.class);
        }
        IndicatorMastersResponse dto = new IndicatorMastersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(IndicatorMastersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_DS_ngan_hang_chi_so_KPI.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = indicatorMastersRepository.getListExport(dto);
        for (Map<String, Object> data : listDataExport) {
            data.put("scope", Utils.join(" - ", Utils.getStringFromMap(data, "jobName"), Utils.getStringFromMap(data, "orgTypeName")));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_ngan_hang_chi_so_KPI.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportDataId(Long id) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_ngan_hang_chi_so_KPI.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<String> resultIdList = Arrays.asList("1", "2", "3", "4", "5");
        Optional<IndicatorMastersEntity> optional = indicatorMastersRepositoryJPA.findById(id);
        IndicatorConversionsResponse.Indicators result = indicatorConversionsRepository.getIndicators(optional.get().getOrgTypeId(), optional.get().getJobId(), optional.get().getOrganizationId().toString());
        dynamicExport.replaceText("${text}", Utils.join(" - ", result.getOrganizationName(), result.getOrgTypeName(), result.getJobName()));
        List<Map<String, Object>> listDataExport = indicatorConversionsRepository.getListIndicatorExport(id);
        Map<String, String> listCategory = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.DKY_MUC_TIEU_DON_VI).stream()
                .collect(Collectors.toMap(CategoryDto::getValue, CategoryDto::getName));
        for (Map<String, Object> mapData : listDataExport) {
            List<IndicatorConversionDetailEntity> conversionDetailEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionDetailEntity.class, "indicatorConversionId", mapData.get("indicator_conversion_id"));
            for (String resultId : resultIdList) {
                mapData.put(resultId, "-");
            }
            conversionDetailEntities.forEach(d -> mapData.put(d.getResultId(), getExpression(d)));
        }
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_ngan_hang_chi_so_KPI.xlsx");
    }

    @Override
    public ResponseEntity updateStatusByOrg(IndicatorMastersRequest.SubmitForm dto) throws RecordNotExistsException {
        IndicatorMastersEntity entity = indicatorMastersRepositoryJPA.getById(dto.getIndicatorMasterId());
        String userName = Utils.getUserNameLogin();
        if (IndicatorMastersEntity.STATUS.PHE_DUYET.equalsIgnoreCase(dto.getStatus())) {
            if (IndicatorMastersEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatusId())
                || IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI.equalsIgnoreCase(entity.getStatusId())
            ) {
                entity.setStatusId(dto.getStatus());
            } else if (IndicatorMastersEntity.STATUS.DE_NGHI_XOA.equalsIgnoreCase(entity.getStatusId())) {
                entity.setStatusId(IndicatorMastersEntity.STATUS.HET_HIEU_LUC);
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(userName);
            indicatorMastersRepositoryJPA.save(entity);
        } else {
            if (IndicatorMastersEntity.STATUS.DE_NGHI_XOA.equalsIgnoreCase(entity.getStatusId())) {
                entity.setStatusId(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET);
            } else if (IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI.equalsIgnoreCase(entity.getStatusId())) {
                entity.setStatusId(IndicatorMastersEntity.STATUS.HET_HIEU_LUC);
            } else {
                entity.setStatusId(dto.getStatus());
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(userName);
            indicatorMastersRepositoryJPA.save(entity);
        }
        indicatorConversionsRepository.updateStatusApprove(entity.getIndicatorMasterId(), entity.getStatusId());
//        if (IndicatorMastersEntity.STATUS.PHE_DUYET.equals(entity.getStatusId()) && IndicatorMastersEntity.TYPE.ORG.equals(entity.getType())) {
//            initKPIForEmp(entity, userName, new Date());
//        }
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity approvalAll() throws RecordNotExistsException {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<IndicatorMastersEntity> entityList = indicatorMastersRepository.findByListObject(IndicatorMastersEntity.class, "statusId", List.of(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET, IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI, IndicatorMastersEntity.STATUS.DE_NGHI_XOA));

        entityList.forEach(entity -> {
            if (IndicatorMastersEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatusId())
                || IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI.equalsIgnoreCase(entity.getStatusId())
            ) {
                entity.setStatusId(IndicatorMastersEntity.STATUS.PHE_DUYET);
            } else if (IndicatorMastersEntity.STATUS.DE_NGHI_XOA.equalsIgnoreCase(entity.getStatusId())) {
                entity.setStatusId(IndicatorMastersEntity.STATUS.HET_HIEU_LUC);
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            indicatorMastersRepositoryJPA.save(entity);
            indicatorConversionsRepository.updateStatusApprove(entity.getIndicatorMasterId(), entity.getStatusId());

            //Phe duyet
            if (IndicatorMastersEntity.STATUS.PHE_DUYET.equals(entity.getStatusId()) && IndicatorMastersEntity.TYPE.ORG.equals(entity.getType())) {
                initKPIForEmp(entity, userName, currentDate);
            }
        });

        return ResponseUtils.ok();
    }

    private void initKPIForEmp(IndicatorMastersEntity entity, String userName, Date currentDate) {
        //thuc hien cap nhat ngan hang kpi cua lanh dao
        IndicatorMastersEntity empMastersEntity = indicatorMastersRepository.getIndicatorMaster(
                entity.getOrganizationId(),
                entity.getManagerJobId(),
                entity.getOrgTypeId());
        if (empMastersEntity == null) {
            empMastersEntity = new IndicatorMastersEntity();
            empMastersEntity.setCreatedBy(userName);
            empMastersEntity.setCreatedTime(currentDate);
        } else {
            empMastersEntity.setModifiedBy(userName);
            empMastersEntity.setModifiedTime(currentDate);
        }
        empMastersEntity.setOrganizationId(entity.getOrganizationId());
        empMastersEntity.setJobId(entity.getManagerJobId());
        empMastersEntity.setManagerJobId(null);
        empMastersEntity.setOrgTypeId(entity.getOrgTypeId());
        empMastersEntity.setStatusId(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET);
        empMastersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        indicatorMastersRepositoryJPA.save(empMastersEntity);
        Long empMasterId = empMastersEntity.getIndicatorMasterId();

        //thuc hien them moi ngan hang kpi
        List<IndicatorConversionsEntity> listEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionsEntity.class, "indicatorMasterId", entity.getIndicatorMasterId());
        List<IndicatorConversionsEntity> listEmpEntities = indicatorConversionsRepository.findByProperties(IndicatorConversionsEntity.class, "indicatorMasterId", empMastersEntity.getIndicatorMasterId());

        Map<Long, IndicatorConversionsEntity> mapConversions = listEntities.stream().collect(Collectors.toMap(item -> item.getIndicatorId(), item -> item, (existing, replacement) -> replacement));
        boolean existsKPICN = false;
        ParameterEntity parameterEntity = indicatorMastersRepository.getParameter("ID_KHCTCN", "PARAMETER_KPI");
        Long idCN = parameterEntity != null ? Long.parseLong(parameterEntity.getConfigValue()) : IndicatorsEntity.ID_KHCTCN;
        for (IndicatorConversionsEntity item : listEmpEntities) {
            if (!item.getIndicatorId().equals(idCN)) { //bo qua kpi ca nhan
                IndicatorConversionsEntity orgConversionsEntity = mapConversions.get(item.getIndicatorId());
                if (orgConversionsEntity == null || orgConversionsEntity.getStatus().equals(IndicatorConversionsEntity.STATUS.HET_HIEU_LUC)) {
                    item.setStatus(IndicatorConversionsEntity.STATUS.DE_NGHI_XOA);
                    item.setModifiedBy(userName);
                    item.setModifiedTime(currentDate);
                } else if (item.getStatus().equals(IndicatorConversionsEntity.STATUS.HET_HIEU_LUC)) {
                    item.setStatus(IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI);
                    item.setModifiedBy(userName);
                    item.setModifiedTime(currentDate);
                }
                indicatorConversionsRepositoryJPA.save(item);
            } else {
                existsKPICN = true;
            }
            mapConversions.remove(item.getIndicatorId());
        }
        if (!existsKPICN) {
            IndicatorConversionsEntity empConversionsEntity = new IndicatorConversionsEntity();
            empConversionsEntity.setIndicatorId(idCN);
            empConversionsEntity.setCreatedBy(userName);
            empConversionsEntity.setCreatedTime(currentDate);
            empConversionsEntity.setStatus(IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET);
            empConversionsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            empConversionsEntity.setIndicatorMasterId(empMasterId);
            indicatorConversionsRepositoryJPA.save(empConversionsEntity);
        }

        mapConversions.forEach((key, item) -> {
            if (item.getStatus().equals(IndicatorConversionsEntity.STATUS.PHE_DUYET)) {
                IndicatorConversionsEntity empConversionsEntity = new IndicatorConversionsEntity();
                Utils.copyProperties(item, empConversionsEntity);
                empConversionsEntity.setIndicatorConversionId(null);
                empConversionsEntity.setCreatedBy(userName);
                empConversionsEntity.setCreatedTime(currentDate);
                empConversionsEntity.setModifiedBy(null);
                empConversionsEntity.setModifiedTime(null);
                empConversionsEntity.setStatus(IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET);
                empConversionsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                empConversionsEntity.setIndicatorMasterId(empMasterId);
                indicatorConversionsRepositoryJPA.save(empConversionsEntity);

                //insert du lieu conversion detail
                indicatorConversionsRepository.copyConversionDetail(item.getIndicatorConversionId(), empConversionsEntity.getIndicatorConversionId());
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> importData(IndicatorMastersRequest.ImportRequest dto) throws IOException {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        String fileConfigName = "BM_import_chi_so_kpi_update.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
//        Map<String, String> requiredList = IndicatorConversionsEntity.REQUIRED_LIST_MAP;
        Map<String, String> mapUnits = indicatorsRepository.getMapData("name", "value", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH);
        Map<String, String> mapPeriodTypes = indicatorsRepository.getMapData("name", "value", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CHU_KY);
        List<CategoryDto> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI);
//        setAttributes(Constant.CATEGORY_TYPES.PHAN_LOAI, listType, "PERIOD_ID");
        Map<String, String> mapTypes = listType.stream().collect(Collectors.toMap(cg -> cg.getName().toLowerCase(), CategoryDto::getValue, (existing, replacement) -> replacement));
        //validate không được import kpi cua ca nhan khi chua import kpi cua don vi
        if (IndicatorMastersEntity.TYPE.EMP.equals(dto.getType())) {
            //lay du lieu kpi cua don vi
            IndicatorMastersEntity indicatorMastersEntity = indicatorMastersRepository.getIndicatorMaster(dto.getOrganizationId(),
                    null,
                    dto.getOrgTypeId());
            if (indicatorMastersEntity == null || !IndicatorMastersEntity.STATUS.PHE_DUYET.equals(indicatorMastersEntity.getStatusId())) {
                throw new BaseAppException("Ngân hàng KPI của đơn vị cần được phê duyệt trước!");
            }
        }


        //Xu ly luu bang master
        IndicatorMastersEntity indicatorMastersEntity = indicatorMastersRepository.getIndicatorMaster(dto.getOrganizationId(),
                IndicatorMastersEntity.TYPE.ORG.equals(dto.getType()) ? null : dto.getJobId(),
                dto.getOrgTypeId());
        if (indicatorMastersEntity == null) {
            indicatorMastersEntity = new IndicatorMastersEntity();
            indicatorMastersEntity.setCreatedBy(userName);
            indicatorMastersEntity.setCreatedTime(currentDate);
        } else {
            indicatorMastersEntity.setModifiedBy(userName);
            indicatorMastersEntity.setModifiedTime(currentDate);
        }
        indicatorMastersEntity.setOrganizationId(dto.getOrganizationId());
        indicatorMastersEntity.setJobId(IndicatorMastersEntity.TYPE.ORG.equals(dto.getType()) ? null : dto.getJobId());
        indicatorMastersEntity.setManagerJobId(dto.getManagerJobId());
        indicatorMastersEntity.setOrgTypeId(dto.getOrgTypeId());
        indicatorMastersEntity.setStatusId(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET);
        indicatorMastersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        indicatorMastersEntity.setType(dto.getType());
        indicatorMastersRepositoryJPA.save(indicatorMastersEntity);
        Long indicatorMasterId = indicatorMastersEntity.getIndicatorMasterId();

        //Lay danh sach indicator của đơn vị
        List<IndicatorsEntity> listIndicators = indicatorsRepositoryJPA.findByOrganizationId(dto.getOrgManageId());
        Map<String, IndicatorsEntity> mapIndicators = new HashMap<>();
        listIndicators.forEach(item -> {
            mapIndicators.put(item.getName().toLowerCase(), item);
        });

        Map<String, IndicatorConversionDetailEntity> mapConversionDetails = indicatorMastersRepository.getListConversionDetails(indicatorMastersEntity.getIndicatorMasterId())
                .stream()
                .collect(Collectors.toMap(item -> item.getIndicatorConversionId() + "-" + item.getResultId(), item -> item));


        Map<Long, IndicatorConversionsEntity> mapIndicatorConversions = indicatorConversionsRepositoryJPA.findByIndicatorMasterId(indicatorMasterId)
                .stream()
                .collect(Collectors.toMap(IndicatorConversionsEntity::getIndicatorId, item -> item));
//        Map<String, String> mapCachDanhGia = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.CACH_DANH_GIA)
//                .stream()
//                .collect(Collectors.toMap(item -> item.getName().toLowerCase(), item -> item.getValue()));

//        Map<String, String> conversionTypeList = IndicatorConversionsEntity.CONVERSION_VALUE_MAP;

        Map<String, String> mapConversionType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.KPI_DKY_MUC_TIEU_DON_VI).stream()
                .collect(Collectors.toMap(categoryDto -> categoryDto.getName().toLowerCase(),
                        CategoryDto::getName, (existing, replacement) -> replacement));

//        Map<Long, IndicatorConversionsEntity> listDataCheck = indicatorConversionsRepository.getDataDup(indicatorMasterId, null);


        if (importExcel.validateCommon(dto.getFile().getInputStream(), dataList)) {
            for (int row = 0; row < dataList.size(); row++) {
                Object[] objects = dataList.get(row);
                if (mapUnits.get(((String) objects[2]).toLowerCase()) == null) {
                    importExcel.addError(row, 2, "Đơn vị tính chưa tồn tại trong danh mục hệ thống!", (String) objects[2]);
                }
                if (mapPeriodTypes.get(((String) objects[3]).toLowerCase()) == null) {
                    importExcel.addError(row, 3, "Dữ liệu kỳ chưa tồn tại trong danh mục hệ thống!", (String) objects[3]);
                }
                if (mapTypes.get(((String) objects[7]).toLowerCase()) == null) {
                    importExcel.addError(row, 7, "Dữ liệu phân loại chưa tồn tại trong danh mục hệ thống!", (String) objects[7]);
                }

//                String conversionType = objects[14] != null ? conversionTypeList.get(((String) objects[14]).trim().toLowerCase()) : IndicatorConversionsEntity.CONVERSION_TYPE.CA_NHAN;
//                if (conversionType == null) {
//                    importExcel.addError(row, 14, I18n.getMessage("error.import.category.invalid"), (String) objects[14]);
//                }
//
//                String isRequired = objects[15] != null ? requiredList.get(((String) objects[15]).trim()) : "N";
//                if (isRequired == null) {
//                    importExcel.addError(row, 15, I18n.getMessage("error.import.category.invalid"), (String) objects[15]);
//                }

//                boolean isOrg = IndicatorConversionsEntity.CONVERSION_TYPE.DON_VI.equals(conversionType);
                //xu ly insert du lieu
                String indicatorName = (String) objects[1];
                IndicatorsEntity indicatorsEntity = mapIndicators.get(indicatorName.toLowerCase());
                if (indicatorsEntity == null) {
                    indicatorsEntity = new IndicatorsEntity();
                    indicatorsEntity.setCreatedBy(userName);
                    indicatorsEntity.setCreatedTime(currentDate);
                } else {
                    indicatorsEntity.setModifiedBy(userName);
                    indicatorsEntity.setModifiedTime(currentDate);
                }
                indicatorsEntity.setName(indicatorName);
                indicatorsEntity.setUnitId(mapUnits.get(((String) objects[2]).toLowerCase()));
                indicatorsEntity.setPeriodType(mapPeriodTypes.get(((String) objects[3]).toLowerCase()));
                indicatorsEntity.setSignificance((String) objects[4]);
                indicatorsEntity.setMeasurement((String) objects[5]);
                indicatorsEntity.setSystemInfo((String) objects[6]);
                indicatorsEntity.setType(mapTypes.get(((String) objects[7]).toLowerCase()));
                indicatorsEntity.setNote(objects[13] != null ? (String) objects[13] : null);
                indicatorsEntity.setOrganizationId(dto.getOrgManageId());
                indicatorsEntity.setRatingType(IndicatorsEntity.RATING_TYPES.NUMBER);
//                if (!IndicatorsEntity.RATING_TYPES.NUMBER.equalsIgnoreCase(indicatorsEntity.getRatingType())) {
//                    indicatorsEntity.setListValues(Utils.join(";", (String) objects[9], (String) objects[10], (String) objects[11], (String) objects[12], (String) objects[13]));
//                }

                indicatorsRepositoryJPA.save(indicatorsEntity);
                objectRelationsService.saveObjectRelations(indicatorsEntity.getIndicatorId(), Utils.castToList(dto.getOrgManageId()), ObjectRelationsEntity.TABLE_NAMES.INDICATORS, ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS, ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);


                IndicatorConversionsEntity indicatorConversionsEntity = mapIndicatorConversions.get(indicatorsEntity.getIndicatorId());
//                IndicatorConversionsEntity isDuplicateEntity = listDataCheck.get(indicatorsEntity.getIndicatorId());
                if (indicatorConversionsEntity == null) {
                    indicatorConversionsEntity = new IndicatorConversionsEntity();
                    indicatorConversionsEntity.setCreatedBy(userName);
                    indicatorConversionsEntity.setCreatedTime(currentDate);
//                    if (isDuplicateEntity != null) {
//                        importExcel.addError(row, 1, I18n.getMessage("error.indicatorConversion.indicator.duplicate"), indicatorName);
//                    }
                } else {
                    indicatorConversionsEntity.setModifiedBy(userName);
                    indicatorConversionsEntity.setModifiedTime(currentDate);
//                    if (isDuplicateEntity != null && !isDuplicateEntity.getIndicatorConversionId().equals(indicatorConversionsEntity.getIndicatorConversionId())) {
//                        importExcel.addError(row, 1, I18n.getMessage("error.indicatorConversion.indicator.duplicate"), indicatorName);
//                    }
                }
                indicatorConversionsEntity.setIndicatorId(indicatorsEntity.getIndicatorId());
                indicatorConversionsEntity.setIndicatorMasterId(indicatorMasterId);
                indicatorConversionsEntity.setIsRequired("N");
                indicatorConversionsEntity.setConversionType("CA_NHAN");
                indicatorConversionsEntity.setIsFocusReduction(IndicatorConversionsEntity.FOCUS_REDUCTION.N);
                indicatorConversionsEntity.setStatus(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET);
                indicatorConversionsRepositoryJPA.save(indicatorConversionsEntity);
                //xu ly luu du lieu thang do
                int i = 0;
                try {
                    for (i = 0; i < 5; i++) {
                        String data = (String) objects[i + 8];
                        if (!Utils.isNullOrEmpty(data)) {
                            IndicatorConversionDetailEntity conversionDetail = mapConversionDetails.get(
                                    indicatorConversionsEntity.getIndicatorConversionId() + "-" + (5 - i)
                            );
                            if (conversionDetail == null) {
                                conversionDetail = new IndicatorConversionDetailEntity();
                                conversionDetail.setIndicatorConversionId(indicatorConversionsEntity.getIndicatorConversionId());
                                conversionDetail.setResultId(String.valueOf(5 - i));
                                conversionDetail.setCreatedBy(userName);
                                conversionDetail.setCreatedTime(currentDate);
                            } else {
                                conversionDetail.setModifiedBy(userName);
                                conversionDetail.setModifiedTime(currentDate);
                            }
                            saveDetail(data.replace((String) objects[2], ""),
                                    data, conversionDetail, IndicatorsEntity.RATING_TYPES.NUMBER.equalsIgnoreCase(indicatorsEntity.getRatingType()), false, mapConversionType);
                        }
                    }
                } catch (Exception e) {
                    importExcel.addError(row, i + 9, "Diễn giải thang đo không hợp lệ!", (String) objects[i + 9]);
                }
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(dto.getFile(), importExcel);
            }
        } else {
            throw new ErrorImportException(dto.getFile(), importExcel);
        }
        return null;
    }

    @Override
    public String getTemplateImport(IndicatorMastersRequest.ImportRequest dto) throws Exception {
        String pathTemplate = "template/import/BM_import_chi_so_kpi_update.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<CategoryDto> listUnit = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.DON_VI_TINH);
        List<CategoryDto> listType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.PHAN_LOAI);
//        setAttributes(Constant.CATEGORY_TYPES.PHAN_LOAI, listType, "PERIOD_ID");
        List<CategoryDto> listPeriodType = indicatorsRepository.getListCategories(Constant.CATEGORY_TYPES.CHU_KY);
        if (IndicatorMastersEntity.TYPE.ORG.equals(dto.getType())) {
            dto.setJobId(null);
        }
        IndicatorConversionsResponse.Indicators data = indicatorMastersRepository.getData(dto);
        String importTemplateName = removeAccent(String.join(" ", Utils.NVL(data.getKpiLevelName()), Utils.NVL(data.getJobName().toUpperCase()), Utils.NVL(data.getOrganizationName()))) + ".xlsx";
        String fileName = Utils.getFilePathExportDefault(importTemplateName);
        dynamicExport.setText(data.getOrganizationName(), 1, 0);
        if (!Utils.isNullOrEmpty(data.getJobName()) && "4".equals(data.getKpiLevel())) {
            dynamicExport.setText("DANH MỤC CHỈ SỐ ĐÁNH GIÁ HIỆU SUẤT LÀM VIỆC CỦA VỊ TRÍ VIỆC LÀM (KPI cấp 4)", 0, 1);
            dynamicExport.setText("Tên vị trí việc làm: " + data.getJobName(), 0, 3);
        } else {
            dynamicExport.setText("HỆ THỐNG THANG ĐO TƯƠNG ỨNG VỚI CÁC CHỈ SỐ ĐÁNH GIÁ HIỆU SUẤT LÀM VIỆC CỦA ĐƠN VỊ CẤU THÀNH ĐƠN VỊ THUỘC TRƯỜNG", 0, 1);
        }
        dynamicExport.setActiveSheet(1);
        int row = 1;
        for (CategoryDto categoryDto : listUnit) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 1;
        dynamicExport.setActiveSheet(2);
        for (CategoryDto categoryDto : listType) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        row = 1;
        dynamicExport.setActiveSheet(3);
        for (CategoryDto categoryDto : listPeriodType) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    private static String removeAccent(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(normalized).replaceAll("");

        noAccent = noAccent.replaceAll("đ", "d");
        noAccent = noAccent.replaceAll("Đ", "D");
        return noAccent;
    }

    private void saveDetail(String comparison, String description, IndicatorConversionDetailEntity conversionDetail, boolean isNumber, boolean isOrg, Map<String, String> mapConversionType) throws Exception {
        try {
            if (isNumber || isOrg) {
                String[] comparisonList;
                if (!Utils.isNullOrEmpty(comparison)) {
                    comparisonList = comparison.split(" - ");
                    if (comparisonList.length == 1) {
                        comparisonList = comparison.split("-");
                    }
                    if (comparisonList.length == 1) {
                        comparisonList = comparison.split(" và ");
                    }
                    Pattern pattern = Pattern.compile("(>=|<=|>|<|=|>= |<= |> |< |= )(.+)");
                    for (String comparisonItem : comparisonList) {
                        Matcher matcher = pattern.matcher(comparisonItem.trim());
                        if (matcher.find()) {
                            if (matcher.group(1).trim().equals(">") || matcher.group(1).trim().equals(">=")) {
                                conversionDetail.setMinComparison(getComparison2(matcher.group(1).trim()));
                                if (isOrg && mapConversionType.get(matcher.group(2).trim().toLowerCase()) == null) {
                                    throw new Exception();
                                } else if (isOrg) {
                                    conversionDetail.setMinValue(mapConversionType.get(matcher.group(2).trim().toLowerCase()).trim());
                                } else {
                                    conversionDetail.setMinValue(matcher.group(2).trim());
                                }


                            } else {
                                conversionDetail.setMaxComparison(getComparison2(matcher.group(1).trim()));
                                if (isOrg && mapConversionType.get(matcher.group(2).trim().toLowerCase()) == null) {
                                    throw new Exception();
                                } else if (isOrg) {
                                    conversionDetail.setMaxValue(mapConversionType.get(matcher.group(2).trim().toLowerCase()).trim());
                                } else {
                                    conversionDetail.setMaxValue(matcher.group(2).trim());
                                }
                            }
                        } else {
                            if (!Utils.isNullOrEmpty(comparisonItem.trim())) {
                                conversionDetail.setMinComparison("EQUAL");
                                if (isOrg) {
                                    conversionDetail.setMinValue(mapConversionType.get(comparisonItem.trim().toLowerCase()).trim());
                                } else {
                                    conversionDetail.setMinValue(comparisonItem.trim());
                                }
                            }
                        }
                    }
//                    conversionDetail.setNote(description);
                    indicatorConversionDetailsRepositoryJPA.save(conversionDetail);
                }
            } else {
                conversionDetail.setMinComparison("EQUAL");
                conversionDetail.setMinValue(description);
                indicatorConversionDetailsRepositoryJPA.save(conversionDetail);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private String getComparison2(String value) {
        switch (value) {
            case "=":
                return "EQUAL";
            case ">":
                return "GREATER_THAN";
            case ">=":
                return "GREATER_THAN_EQUAL";
            case "<":
                return "LESS_THAN";
            case "<=":
                return "LESS_THAN_EQUAL";
        }
        return null;
    }

    private String getExpression(IndicatorConversionDetailEntity conversionDetailEntity) {
        StringBuilder result = new StringBuilder("");
        if (!Utils.isNullOrEmpty(conversionDetailEntity.getMinComparison())) {
            result.append(getComparison(conversionDetailEntity.getMinComparison()));
            result.append(" ");
            result.append(conversionDetailEntity.getMinValue());
        }
        if (!Utils.isNullOrEmpty(conversionDetailEntity.getMaxComparison())) {
            if (!result.isEmpty()) {
                result.append(" và ");
            }
            result.append(getComparison(conversionDetailEntity.getMaxComparison()));
            result.append(" ");
            result.append(conversionDetailEntity.getMaxValue());
        }
        return result.toString();
    }

    private void setAttributes(String categoryType, List<CategoryDto> listCategory, String keyAttribute) {
        List<CategoryAttributeEntity> listAttributes = indicatorsRepository.getAllAttributeByCategoryType(categoryType);
        Map<Long, Map<String, Object>> mapAttributes = new HashMap<>();
        listAttributes.forEach(item -> {
            mapAttributes.computeIfAbsent(item.getCategoryId(), k -> new HashMap<>());
            mapAttributes.get(item.getCategoryId()).put(item.getAttributeCode(), item.getAttributeValue());
        });
        if (!Utils.isNullOrEmpty(keyAttribute)) {
            Map<Integer, String> mapPeriod;
            if ("PERIOD_ID".equalsIgnoreCase(keyAttribute)) {
                mapPeriod = indicatorsRepository.getMapData("evaluationPeriodId", "name", EvaluationPeriodsEntity.class, "evaluationType", 2);
            } else {
                mapPeriod = new HashMap<>();
            }
            listCategory.forEach(dto -> {
                dto.setAttributes(mapAttributes.get(dto.getCategoryId()));
                if ("PERIOD_ID".equalsIgnoreCase(keyAttribute)) {
                    dto.setName(mapPeriod.get(Integer.parseInt(Utils.getStringFromMap(dto.getAttributes(), keyAttribute.toUpperCase()))) + " - " + dto.getName());
                } else {
                    dto.setName(Utils.getStringFromMap(dto.getAttributes(), keyAttribute.toUpperCase()) + " - " + dto.getName());
                }
            });
        } else {
            listCategory.forEach(dto -> {
                dto.setAttributes(mapAttributes.get(dto.getCategoryId()));
            });
        }
    }

    private String getComparison(String value) {
        switch (value) {
            case "EQUAL":
                return "";
            case "GREATER_THAN":
                return ">";
            case "GREATER_THAN_EQUAL":
                return ">=";
            case "LESS_THAN":
                return "<";
            case "LESS_THAN_EQUAL":
                return "<=";
        }
        return null;
    }

}
