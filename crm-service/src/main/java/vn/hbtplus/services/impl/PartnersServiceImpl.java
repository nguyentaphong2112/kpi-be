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
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.PartnersEntity;
import vn.hbtplus.repositories.impl.CommonRepository;
import vn.hbtplus.repositories.impl.PartnersRepository;
import vn.hbtplus.repositories.jpa.PartnersRepositoryJPA;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.services.LogActionsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.PartnersService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang crm_partners
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PartnersServiceImpl implements PartnersService {

    private final PartnersRepository partnersRepository;
    private final PartnersRepositoryJPA partnersRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final CommonUtilsService commonUtilsService;
    private final LogActionsService logActionsService;
    private final MdcForkJoinPool forkJoinPool;
    private final CommonRepository commonRepository;
    private final CardObjectServiceImpl cardObjectService;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PartnersResponse.SearchResult> searchData(PartnersRequest.SearchForm dto) {
        return ResponseUtils.ok(partnersRepository.searchData(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity saveData(PartnersRequest.SubmitForm dto, Long partnerId) throws BaseAppException {
        PartnersEntity entity;
        boolean duplicate = partnersRepository.duplicate(PartnersEntity.class, partnerId, "mobileNumber", dto.getMobileNumber());
        if (duplicate) {
            throw new BaseAppException("SAVE_PARTNER_DUPLICATE_MOBILE_NUMBER", I18n.getMessage("error.savePartner.mobileNumber.duplicate"));
        }
        boolean isUpdate = false;
        PartnersEntity oldEntity = new PartnersEntity();
        if (partnerId != null && partnerId > 0L) {
            entity = partnersRepository.get(PartnersEntity.class, partnerId);
            Utils.copyProperties(entity, oldEntity);
            dto.setPartnerId(partnerId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            isUpdate = true;
        } else {
            entity = new PartnersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setCurrentAddress(commonUtilsService.getFullAddress(dto.getVillageAddress(), dto.getWardId(), dto.getDistrictId(), dto.getProvinceId()));

        List<AttributeRequestDto> oldAttribute = null;
        if (isUpdate) {
            oldAttribute = Utils.mapAll(objectAttributesService.getAttributes(partnerId, partnersRepository.getSQLTableName(PartnersEntity.class)), AttributeRequestDto.class);
        }

        partnersRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getPartnerId(), dto.getListAttributes(), PartnersEntity.class, null);
        logActionsService.saveData(isUpdate ? Constant.LOG_ACTION.UPDATE : Constant.LOG_ACTION.INSERT, oldEntity, entity, oldAttribute, dto.getListAttributes(), null, entity.getFullName());
        return ResponseUtils.ok(entity.getPartnerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        PartnersEntity entity = partnersRepository.get(PartnersEntity.class, id);
        if (entity == null || BaseConstants.STATUS.DELETED.equals(entity.getIsDeleted())) {
            throw new RecordNotExistsException(id, PartnersEntity.class);
        }

        PartnersEntity newEntity = new PartnersEntity();
        Utils.copyProperties(entity, newEntity);
        newEntity.setIsDeleted(BaseConstants.STATUS.DELETED);
        logActionsService.saveData(Constant.LOG_ACTION.DELETE, entity, newEntity, entity.getFullName());
        partnersRepository.deActiveObject(PartnersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PartnersResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<PartnersEntity> optional = partnersRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PartnersEntity.class);
        }
        PartnersResponse.DetailBean dto = new PartnersResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, partnersRepository.getSQLTableName(PartnersEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(PartnersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh_sach_doi_tac.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = partnersRepository.getListExport(dto);

        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh_sach_doi_tac.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportCard(PartnersRequest.PrintCard dto) throws Exception {
        dto.setObjType(Constant.CUSTOMER_CARE_TYPE.DOI_TAC);
        if (Utils.isNullOrEmpty(dto.getType())) {
            dto.setType(Constant.CARD_TYPE.SINH_NHAT);
        }
        return cardObjectService.exportCard(dto);
    }

    @Override
    public ResponseEntity<Object> exportTemplate() throws Exception {
        ExportExcel exportExcel = new ExportExcel("template/import/BM_Import_danh-sach-doi-tac.xlsx", 2, true);

        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        // Gioi Tinh
        completableFutures.add(CompletableFuture.supplyAsync(() -> partnersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.GIOI_TINH, "orderNumber"), forkJoinPool));

        // tinh
        completableFutures.add(CompletableFuture.supplyAsync(() -> partnersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.TINH, "orderNumber"), forkJoinPool));

        // huyen
        completableFutures.add(CompletableFuture.supplyAsync(() -> partnersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.HUYEN, "orderNumber"), forkJoinPool));

        //xa
        completableFutures.add(CompletableFuture.supplyAsync(() -> partnersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.XA, "orderNumber"), forkJoinPool));

        // Phan loai doi tac
        completableFutures.add(CompletableFuture.supplyAsync(() -> partnersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_LOAI_DOI_TAC, "orderNumber"), forkJoinPool));

        CompletableFuture<Void> allReturns = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Object>> allFutures = allReturns.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<Object> objs = allFutures.get();

        int activeSheet = 1;
        for (Object obj : objs) {
            exportExcel.setActiveSheet(activeSheet++);
            List<Map<String, Object>> listMapData = (List<Map<String, Object>>) obj;
            exportExcel.replaceKeys(listMapData);
        }

        exportExcel.setActiveSheet(0);
        return ResponseUtils.ok(exportExcel, "BM_Import_danh-sach-doi-tac.xlsx", false);
    }

    @Override
    public ResponseEntity importProcess(MultipartFile file) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_Import_danh-sach-doi-tac.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> listMobileNumbers = new ArrayList<>();
            List<String> listProvinceName = new ArrayList<>();
            for (Object[] obj : dataList) {
                listMobileNumbers.add(((String) obj[1]).trim());

                String provinceName = (String) obj[11];
                if (!Utils.isNullOrEmpty(provinceName) && !listProvinceName.contains(provinceName)) {
                    listProvinceName.add(provinceName.trim());
                }
            }

            Map<String, String> mapFullAddress = commonRepository.getMapFullAddress(listProvinceName);
            Map<String, PartnersResponse.DetailBean> mapCustomerExists = partnersRepository.getMapPartnerByMobileNumber(listMobileNumbers);
            Map<String, String> mapGenders = new HashMap<>();
            List<CategoryEntity> listGender = partnersRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.GIOI_TINH);
            listGender.forEach(item -> mapGenders.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapPartnerTypes = new HashMap<>();
            List<CategoryEntity> listPartnerType = partnersRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_LOAI_DOI_TAC);
            listPartnerType.forEach(item -> mapPartnerTypes.put(item.getName().toLowerCase(), item.getValue()));

            List<PartnersEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;
            for (Object[] obj : dataList) {
                PartnersEntity entity = new PartnersEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                int col = 1;
                String mobileNumber = Utils.NVL(((String) obj[col])).trim();
                if(!Utils.isValidPhoneNumber(mobileNumber)){
                    importExcel.addError(row, col, I18n.getMessage("error.import.invalid.mobileNumber"), mobileNumber);
                } else if (mapCustomerExists.get(mobileNumber) != null) {
                    importExcel.addError(row, col, I18n.getMessage("error.customers.import.mobileNumber"), mobileNumber);
                } else {
                    entity.setMobileNumber(mobileNumber);
                }

                col++;
                entity.setFullName(((String) obj[col++]).trim());

                String genderName = Utils.NVL(((String) obj[col])).trim();
                String genderId = mapGenders.get(genderName.toLowerCase());
                if (!Utils.isNullOrEmpty(genderName) && genderId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), genderName);
                } else {
                    entity.setGenderId(genderId);
                }

                col++;
                Long dayOfBirth = (Long) obj[col];
                Long monthOfBirth = (Long) obj[col + 1];
                Long yearOfBirth = (Long) obj[col + 2];
                if (dayOfBirth != null || monthOfBirth != null || yearOfBirth != null) {
                    String dateOfBirthStr = String.format("%02d", dayOfBirth) + "/" + String.format("%02d", monthOfBirth) + "/" + yearOfBirth;
                    Date dateOfBirth = Utils.stringToDate(dateOfBirthStr);
                    if (dateOfBirth == null) {
                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.dateOfBirth"), dateOfBirthStr);
                    } else {
                        entity.setDateOfBirth(dateOfBirth);
                    }
                }

                col = col + 3;
                entity.setEmail((String) obj[col++]);
                entity.setZaloAccount((String) obj[col++]);
                entity.setJob((String) obj[col++]);
                entity.setDepartmentName((String) obj[col++]);

                String provinceName = Utils.NVL((String) obj[col]).trim();
                String districtName = Utils.NVL((String) obj[col + 1]).trim();
                String wardName = Utils.NVL((String) obj[col + 2]).trim();
                String villageAddress = (String) obj[col + 3];

                if (!Utils.isNullOrEmpty(provinceName + districtName + wardName)) {
                    boolean isErrorAddress = false;
                    if (Utils.isNullOrEmpty(provinceName)) {
                        isErrorAddress = true;
                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.required.province"), null);
                    }

                    if (Utils.isNullOrEmpty(districtName) && !Utils.isNullOrEmpty(wardName)) {
                        isErrorAddress = true;
                        importExcel.addError(row, col + 1, I18n.getMessage("error.customers.import.required.district"), null);
                    }

                    String fullAddress = Utils.join("#", provinceName, districtName, wardName).toLowerCase();
                    String keyMap = mapFullAddress.keySet().stream().filter(item -> item.startsWith(fullAddress)).findFirst().orElse(null);
                    if (Utils.isNullOrEmpty(keyMap) && !isErrorAddress) {
                        String errorMessage = Utils.join(", ", "Tỉnh", districtName.isEmpty() ? null : "huyện", wardName.isEmpty() ? null : "xã");
                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.invalid", errorMessage), Utils.join(", ", provinceName, districtName, wardName));
                    } else if (!Utils.isNullOrEmpty(keyMap)) {
                        String addressValue = mapFullAddress.get(keyMap);
                        String[] valueArr = addressValue.split("#");
                        entity.setProvinceId(valueArr[0]);
                        entity.setDistrictId(districtName.isEmpty() ? null : valueArr[1]);
                        entity.setWardId(wardName.isEmpty() ? null : valueArr[2]);
                    }
                }

                entity.setVillageAddress(villageAddress);
                entity.setCurrentAddress(commonUtilsService.getFullAddress(villageAddress, entity.getWardId(), entity.getDistrictId(), entity.getProvinceId()));

                col = col + 4;
                entity.setBankAccount((String) obj[col++]);
                entity.setBankName((String) obj[col++]);
                entity.setBankBranch((String) obj[col++]);

                String partnerTypeName = Utils.NVL(((String) obj[col])).trim();
                String partnerTypeId = mapPartnerTypes.get(partnerTypeName.toLowerCase());
                entity.setPartnerType(partnerTypeId);

                listInsert.add(entity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                partnersRepository.insertBatch(PartnersEntity.class, listInsert, userName);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }


    @Override
    public ListResponseEntity<PartnersResponse.DetailBean> getListData() {
        return ResponseUtils.ok(partnersRepository.getListData());
    }



}
