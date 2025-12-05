/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.nfunk.jep.function.Str;
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
import vn.hbtplus.models.request.FamilyRelationshipsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.FamilyRelationshipsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.CustomersEntity;
import vn.hbtplus.repositories.entity.FamilyRelationshipsEntity;
import vn.hbtplus.repositories.impl.CommonRepository;
import vn.hbtplus.repositories.impl.CustomersRepository;
import vn.hbtplus.repositories.impl.FamilyRelationshipsRepository;
import vn.hbtplus.repositories.jpa.FamilyRelationshipsRepositoryJPA;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.services.FamilyRelationshipsService;
import vn.hbtplus.services.LogActionsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang crm_family_relationships
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class FamilyRelationshipsServiceImpl implements FamilyRelationshipsService {

    private final FamilyRelationshipsRepository familyRelationshipsRepository;
    private final FamilyRelationshipsRepositoryJPA familyRelationshipsRepositoryJPA;
    private final CommonUtilsService commonUtilsService;
    private final LogActionsService logActionsService;
    private final ObjectAttributesService objectAttributesService;
    private final CommonRepository commonRepository;
    private final CustomersRepository customersRepository;
    private final MdcForkJoinPool forkJoinPool;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<FamilyRelationshipsResponse> searchData(FamilyRelationshipsRequest.SearchForm dto) {
        return ResponseUtils.ok(familyRelationshipsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(FamilyRelationshipsRequest.SubmitForm dto) throws BaseAppException {
        FamilyRelationshipsEntity entity;
        if (dto.getFamilyRelationshipId() != null && dto.getFamilyRelationshipId() > 0L) {
            entity = familyRelationshipsRepositoryJPA.getById(dto.getFamilyRelationshipId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new FamilyRelationshipsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        familyRelationshipsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getFamilyRelationshipId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<FamilyRelationshipsEntity> optional = familyRelationshipsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, FamilyRelationshipsEntity.class);
        }
        familyRelationshipsRepository.deActiveObject(FamilyRelationshipsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<FamilyRelationshipsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<FamilyRelationshipsEntity> optional = familyRelationshipsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, FamilyRelationshipsEntity.class);
        }
        FamilyRelationshipsResponse dto = new FamilyRelationshipsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(FamilyRelationshipsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = familyRelationshipsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveData(List<FamilyRelationshipsRequest.SubmitForm> listData, Long objectId, String objName, String objectType) throws BaseAppException {
        if (!Utils.isNullOrEmpty(listData)) {
            List<FamilyRelationshipsEntity> listInsert = new ArrayList<>();
            List<FamilyRelationshipsEntity> listUpdate = new ArrayList<>();
            List<Long> listIdUpdate = new ArrayList<>();
            List<Long> listIdDelete = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            List<FamilyRelationshipsEntity> listOldFamily = familyRelationshipsRepository.findByProperties(FamilyRelationshipsEntity.class, "objectId", objectId, "objectType", objectType);
            for (FamilyRelationshipsRequest.SubmitForm familyRelationship : listData) {
                FamilyRelationshipsEntity entity;
                boolean isUpdate = false;
                if (familyRelationship.getFamilyRelationshipId() != null && familyRelationship.getFamilyRelationshipId() > 0L) {
                    isUpdate = true;
                    entity = familyRelationshipsRepository.get(FamilyRelationshipsEntity.class, "familyRelationshipId", familyRelationship.getFamilyRelationshipId());
                    if (entity == null) {
                        throw new BaseAppException("FamilyRelationshipsEntity not exists");
                    }
                    entity.setModifiedBy(userName);
                    entity.setModifiedTime(curDate);
                } else {
                    entity = new FamilyRelationshipsEntity();
                    entity.setCreatedBy(userName);
                    entity.setCreatedTime(curDate);
                }

                Utils.copyProperties(familyRelationship, entity);
                entity.setCurrentAddress(commonUtilsService.getFullAddress(familyRelationship.getVillageAddress(), familyRelationship.getWardId(), familyRelationship.getDistrictId(), familyRelationship.getProvinceId()));
                entity.setObjectId(objectId);
                entity.setObjectType(objectType);

                if (isUpdate) {
                    listUpdate.add(entity);
                    listIdUpdate.add(familyRelationship.getFamilyRelationshipId());
                } else {
                    entity.setFamilyRelationshipId(familyRelationshipsRepository.getNextId(FamilyRelationshipsEntity.class));
                    listInsert.add(entity);
                }
                objectAttributesService.saveObjectAttributes(entity.getFamilyRelationshipId(), familyRelationship.getListAttributes(), FamilyRelationshipsEntity.class, null);
            }

            String fullObjName = I18n.getMessage("familyRelationship.objectType." + Utils.NVL(objectType).toLowerCase(), objName);
            for (FamilyRelationshipsEntity oldEntity : listOldFamily) {
                if (!listIdUpdate.contains(oldEntity.getFamilyRelationshipId())) {
                    listIdDelete.add(oldEntity.getFamilyRelationshipId());

                    FamilyRelationshipsEntity newEntity = new FamilyRelationshipsEntity();
                    Utils.copyProperties(oldEntity, newEntity);
                    newEntity.setIsDeleted(BaseConstants.STATUS.DELETED);
                    logActionsService.saveData(Constant.LOG_ACTION.DELETE, oldEntity, newEntity, null, null, objectId, fullObjName);
                }
            }

            for (FamilyRelationshipsEntity entity: listInsert) {
                FamilyRelationshipsEntity newEntity = new FamilyRelationshipsEntity();
                Utils.copyProperties(entity, newEntity);
                logActionsService.saveData(Constant.LOG_ACTION.INSERT, null, newEntity, null, null, objectId, fullObjName);
            }

            for (FamilyRelationshipsEntity entity: listUpdate) {
                FamilyRelationshipsEntity newEntity = new FamilyRelationshipsEntity();
                Utils.copyProperties(entity, newEntity);
                FamilyRelationshipsEntity oldEntity = listOldFamily.stream().filter(item -> entity.getFamilyRelationshipId().equals(item.getFamilyRelationshipId())).findFirst().orElse(null);
                logActionsService.saveData(Constant.LOG_ACTION.UPDATE, oldEntity, newEntity, null, null, objectId, fullObjName);
            }

            familyRelationshipsRepository.deActiveObjectByListId(FamilyRelationshipsEntity.class, listIdDelete);
            familyRelationshipsRepository.insertBatch(FamilyRelationshipsEntity.class, listInsert, userName);
            familyRelationshipsRepository.updateBatch(FamilyRelationshipsEntity.class, listUpdate, true);
        }
    }

    @Override
    @Transactional
    public void processImportData(String type, MultipartFile file) throws Exception {
        if (!StringUtils.equalsAnyIgnoreCase(type, FamilyRelationshipsEntity.OBJECT_TYPES.KHACH_HANG, FamilyRelationshipsEntity.OBJECT_TYPES.NHAN_VIEN)) {
            throw new BaseAppException(I18n.getMessage("family.relationship.type.error"));
        }

        ImportExcel importExcel = new ImportExcel("template/import/BM_Import_danh-sach-nguoi-than.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            Set<String> mobileSet = new HashSet<>();
            Set<String> provinceNameSet = new HashSet<>();
            Set<String> relationTypeNameSet = new HashSet<>();
            Set<String> relationStatusNameSet = new HashSet<>();
            for (Object[] obj : dataList) {
                String relationTypeName = (String) obj[2];
                relationTypeNameSet.add(StringUtils.trimToEmpty(relationTypeName).toLowerCase());

                String relationStatusName = (String) obj[3];
                relationStatusNameSet.add(StringUtils.trimToEmpty(relationStatusName).toLowerCase());

                String mobile = (String) obj[6];
                mobileSet.add(StringUtils.trimToEmpty(mobile));
            }

            Map<String, String> mapFullAddress = commonRepository.getMapFullAddress(provinceNameSet.stream().toList());
            Map<String, String> mapRelationType = commonRepository.getMapNameValueByNameList(Constant.CATEGORY_TYPES.MOI_QUAN_HE_TN, relationTypeNameSet.stream().toList());
            Map<String, String> mapRelationStatus = commonRepository.getMapNameValueByNameList(Constant.CATEGORY_TYPES.MOI_QUAN_HE_TN, relationStatusNameSet.stream().toList());
            Map<String, Long> mapCustomer = customersRepository.getMapFullNameAndMobile(mobileSet.stream().toList());

            List<FamilyRelationshipsEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;
            for (Object[] obj : dataList) {
                FamilyRelationshipsEntity entity = new FamilyRelationshipsEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                int col = 1;
                String fullName = (String) obj[col];
                fullName = StringUtils.trim(fullName);
                entity.setFullName(fullName);
                col++;

                String relationTypeName = (String) obj[col];
                relationTypeName = StringUtils.trimToEmpty(relationTypeName);
                if (mapRelationType.get(relationTypeName.toLowerCase()) == null) {
                    importExcel.addError(row, col, I18n.getMessage("family.relationship.relation.type.error"), relationTypeName);
                } else {
                    entity.setRelationTypeId(mapRelationType.get(relationTypeName.toLowerCase()));
                }
                col++;

                String relationTypeStatus = (String) obj[col];
                relationTypeStatus = StringUtils.trimToEmpty(relationTypeStatus);
                if (StringUtils.isNotBlank(relationTypeStatus)) {
                    if (mapRelationStatus.get(relationTypeStatus.toLowerCase()) == null) {
                        importExcel.addError(row, col, I18n.getMessage("family.relationship.relation.status.error"), relationTypeStatus);
                    } else {
                        entity.setRelationStatusId(mapRelationStatus.get(relationTypeStatus.toLowerCase()));
                    }
                }
                col++;

                entity.setDateOfBirth((Date) obj[col]);
                col++;
                String customerFullName = (String) obj[col];
                customerFullName = StringUtils.trim(customerFullName);
                col++;
                String customerMobile = (String) obj[col];
                customerMobile = StringUtils.trim(customerMobile);
                String key = customerMobile + "#" + customerFullName;
                if (mapCustomer.get(key.toLowerCase()) == null) {
                    importExcel.addError(row, col, I18n.getMessage("family.relationship.customer.full.name.mobile"), "Tên phụ huynh: " + customerFullName + ", Số điện thoại: " + customerMobile);
                } else {
                    entity.setObjectId(mapCustomer.get(key.toLowerCase()));
                    entity.setObjectType(type);
                }
                col++;
                String email = (String) obj[col];
                email = StringUtils.trim(email);
                entity.setEmail(email);
                col++;
                String zalo = (String) obj[col];
                zalo = StringUtils.trim(zalo);
                entity.setZaloAccount(zalo);
                col++;

                String facebook = (String) obj[col];
                facebook = StringUtils.trim(facebook);
                entity.setFacebookAccount(facebook);
                col++;

                String jobName = (String) obj[col];
                jobName = StringUtils.trim(jobName);
                entity.setJob(jobName);
                col++;

                String departmentName = (String) obj[col];
                departmentName = StringUtils.trim(departmentName);
                entity.setDepartmentName(departmentName);
                col++;

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

                    String addressValue = getValueByProvinceDistrictAndWard(mapFullAddress, provinceName, districtName, wardName);
                    if (Utils.isNullOrEmpty(addressValue) && !isErrorAddress) {
                        String errorMessage = Utils.join(", ", "Tỉnh", districtName.isEmpty() ? null : "huyện", wardName.isEmpty() ? null : "xã");
                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.invalid", errorMessage), Utils.join(", ", provinceName, districtName, wardName));
                    } else if (!Utils.isNullOrEmpty(addressValue)) {
                        String[] valueArr = addressValue.split("#");
                        entity.setProvinceId(valueArr[0]);
                        entity.setDistrictId(districtName.isEmpty() ? null : valueArr[1]);
                        entity.setWardId(wardName.isEmpty() ? null : valueArr[2]);
                    }
                }

                entity.setVillageAddress(villageAddress);
                listInsert.add(entity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                familyRelationshipsRepository.insertBatch(FamilyRelationshipsEntity.class, listInsert, userName);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
    }

    @Override
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        ExportExcel exportExcel = new ExportExcel("template/import/BM_Import_danh-sach-nguoi-than.xlsx", 2, true);

        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        // Moi quan he
        completableFutures.add(CompletableFuture.supplyAsync(() -> customersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.MOI_QUAN_HE_TN, "orderNumber"), forkJoinPool));

        completableFutures.add(CompletableFuture.supplyAsync(() -> customersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.TINH_TRANG_TN, "orderNumber"), forkJoinPool));

        // tinh
        completableFutures.add(CompletableFuture.supplyAsync(() -> customersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.TINH, "orderNumber"), forkJoinPool));

        // huyen
        completableFutures.add(CompletableFuture.supplyAsync(() -> customersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.HUYEN, "orderNumber"), forkJoinPool));

        //xa
        completableFutures.add(CompletableFuture.supplyAsync(() -> customersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.XA, "orderNumber"), forkJoinPool));

        CompletableFuture<Void> allReturns = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Object>> allFutures = allReturns.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<Object> objs = allFutures.get();

        int activeSheet = 1;
        for (Object obj: objs) {
            exportExcel.setActiveSheet(activeSheet++);
            List<Map<String, Object>> listMapData = (List<Map<String, Object>>) obj;
            exportExcel.replaceKeys(listMapData);
        }

        exportExcel.setActiveSheet(0);
        return ResponseUtils.ok(exportExcel, "BM_Import_danh-sach-nguoi-than.xlsx", false);
    }

    private String getValueByProvinceDistrictAndWard(Map<String, String> fullAddressMap, String province, String district, String ward) {
        String keyFullAddress = "";
        for (String key : fullAddressMap.keySet()) {
            String[] subKeys = key.split("#");
            if (subKeys.length < 2) {
                continue;
            }

            if (subKeys[0].contains(province.toLowerCase()) && subKeys[1].contains(district.toLowerCase()) && StringUtils.isBlank(ward) && subKeys.length == 2) {
                keyFullAddress = key;
                break;
            } else if (subKeys[0].endsWith(province.toLowerCase()) && subKeys[1].endsWith(district.toLowerCase()) && subKeys[2].endsWith(ward.toLowerCase())) {
                keyFullAddress = key;
                break;
            }
        }

        return fullAddressMap.get(keyFullAddress);
    }
}
