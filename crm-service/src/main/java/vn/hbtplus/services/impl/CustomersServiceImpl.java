/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import vn.hbtplus.feigns.AdminFeignClient;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.request.CustomerCareRecordsRequest;
import vn.hbtplus.models.request.CustomersRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.CourseTraineesEntity;
import vn.hbtplus.repositories.entity.CustomersEntity;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.FamilyRelationshipsEntity;
import vn.hbtplus.repositories.impl.CommonRepository;
import vn.hbtplus.repositories.impl.CustomersRepository;
import vn.hbtplus.repositories.impl.FamilyRelationshipsRepository;
import vn.hbtplus.repositories.jpa.CourseTraineesRepositoryJPA;
import vn.hbtplus.repositories.jpa.CustomersRepositoryJPA;
import vn.hbtplus.services.CardObjectService;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.services.CustomerCareRecordsService;
import vn.hbtplus.services.CustomersService;
import vn.hbtplus.services.FamilyRelationshipsService;
import vn.hbtplus.services.LogActionsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
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
 * Lop impl service ung voi bang crm_customers
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomersServiceImpl implements CustomersService {

    private final CustomersRepository customersRepository;
    private final CustomersRepositoryJPA customersRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final CommonUtilsService commonUtilsService;
    private final FamilyRelationshipsRepository familyRelationshipsRepository;
    private final HttpServletRequest request;
    private final LogActionsService logActionsService;
    private final FamilyRelationshipsService familyRelationshipsService;
    private final AdminFeignClient adminFeignClient;
    private final MdcForkJoinPool forkJoinPool;
    private final CommonRepository commonRepository;
    private final CustomerCareRecordsService customerCareRecordsService;
    private final CardObjectService cardObjectService;
    private final CourseTraineesRepositoryJPA courseTraineesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CustomersResponse.SearchResult> searchData(CustomersRequest.SearchForm dto) {
        return ResponseUtils.ok(customersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CustomersRequest.SubmitForm dto, Long customerId) throws BaseAppException {
        CustomersEntity entity;
        Date curDate = new Date();
        String userName = Utils.getUserNameLogin();
        boolean duplicate = customersRepository.duplicate(CustomersEntity.class, customerId, "mobileNumber", dto.getMobileNumber());
        if (duplicate) {
            throw new BaseAppException("SAVE_CUSTOMER_DUPLICATE_MOBILE_NUMBER", I18n.getMessage("error.savePartner.mobileNumber.duplicate"));
        }
        boolean isUpdate = false;
        CustomersEntity oldEntity = new CustomersEntity();
        if (customerId != null && customerId > 0L) {
            entity = customersRepository.get(CustomersEntity.class, customerId);
            Utils.copyProperties(entity, oldEntity);
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
            dto.setCustomerId(customerId);
            isUpdate = true;
        } else {
            entity = new CustomersEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setFullAddress(commonUtilsService.getFullAddress(dto.getVillageAddress(), dto.getWardId(), dto.getDistrictId(), dto.getProvinceId()));

        List<AttributeRequestDto> oldAttribute = null;
        if (isUpdate) {
            oldAttribute = Utils.mapAll(objectAttributesService.getAttributes(customerId, customersRepository.getSQLTableName(CustomersEntity.class)), AttributeRequestDto.class);
        }

        customersRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getCustomerId(), dto.getListAttributes(), CustomersEntity.class, null);
        familyRelationshipsService.saveData(dto.getListFamilyRelationship(), entity.getCustomerId(), entity.getFullName(), FamilyRelationshipsEntity.OBJECT_TYPES.KHACH_HANG);
        logActionsService.saveData(isUpdate ? Constant.LOG_ACTION.UPDATE : Constant.LOG_ACTION.INSERT, oldEntity, entity, oldAttribute, dto.getListAttributes(), null, entity.getFullName());

        if (!isUpdate) {
            createUser(entity, dto.getPassword());
        }
        return ResponseUtils.ok(entity.getCustomerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        CustomersEntity entity = customersRepository.get(CustomersEntity.class, id);
        if (entity == null || BaseConstants.STATUS.DELETED.equals(entity.getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomersEntity.class);
        }

        CustomersEntity newEntity = new CustomersEntity();
        Utils.copyProperties(entity, newEntity);
        newEntity.setIsDeleted(BaseConstants.STATUS.DELETED);

        logActionsService.saveData(Constant.LOG_ACTION.DELETE, entity, newEntity, entity.getFullName());
        customersRepository.deActiveObject(CustomersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CustomersResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<CustomersEntity> optional = customersRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomersEntity.class);
        }
        CustomersResponse.DetailBean dto = new CustomersResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        List<FamilyRelationshipsResponse> listFamily = familyRelationshipsRepository.getListFamilyRelationship(FamilyRelationshipsEntity.OBJECT_TYPES.KHACH_HANG, id);
        for (FamilyRelationshipsResponse familyRelationship : listFamily) {
            familyRelationship.setListAttributes(objectAttributesService.getAttributes(familyRelationship.getFamilyRelationshipId(), customersRepository.getSQLTableName(FamilyRelationshipsEntity.class)));
        }
        dto.setListFamilyRelationship(listFamily);
        dto.setListAttributes(objectAttributesService.getAttributes(id, customersRepository.getSQLTableName(CustomersEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(CustomersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh_sach_khach_hang.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = customersRepository.getListExport(dto);

        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        for (Map<String, Object> mapData : listDataExport) {
            Object productDetail = mapData.get("productDetail");
            if (productDetail != null) {
                mapData.put("productName", productDetail.toString().split("#")[0]);
                mapData.put("productPrice", productDetail.toString().split("#")[1]);
            } else {
                mapData.put("productName", "");
                mapData.put("productPrice", "");
            }
            Long totalOrderAmount = 0L;
            Long paidAmount = 0L;
            if (mapData.get("total_order_amount") != null) {
                totalOrderAmount = Long.valueOf(mapData.get("total_order_amount").toString());
            }
            if (mapData.get("paid_amount") != null) {
                paidAmount = Double.valueOf(mapData.get("paid_amount").toString()).longValue();
            }
            mapData.put("owedAmount", totalOrderAmount - paidAmount);
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh_sach_khach_hang.xlsx");
    }

    @Override
    public ResponseEntity<Object> exportCard(PartnersRequest.PrintCard dto) throws Exception {
        dto.setObjType(Constant.CUSTOMER_CARE_TYPE.KHACH_HANG);
        if (Utils.isNullOrEmpty(dto.getType())) {
            dto.setType(Constant.CARD_TYPE.THU_MOI);
        }
        return cardObjectService.exportCard(dto);
    }

    @Override
    public ResponseEntity<Object> exportTemplate() throws Exception {
        ExportExcel exportExcel = new ExportExcel("template/import/BM_Import_danh-sach-khach-hang.xlsx", 2, true);

        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        // Gioi Tinh
        completableFutures.add(CompletableFuture.supplyAsync(() -> customersRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.GIOI_TINH, "orderNumber"), forkJoinPool));

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
        for (Object obj : objs) {
            exportExcel.setActiveSheet(activeSheet++);
            List<Map<String, Object>> listMapData = (List<Map<String, Object>>) obj;
            exportExcel.replaceKeys(listMapData);
        }

        exportExcel.setActiveSheet(0);
        return ResponseUtils.ok(exportExcel, "BM_Import_danh-sach-khach-hang.xlsx", false);
    }

    @Override
    public ResponseEntity importProcess(MultipartFile file) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_Import_danh-sach-khach-hang.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> listMobileNumbersCus = new ArrayList<>();
            List<String> listMobileNumberEmp = new ArrayList<>();
            List<String> listProvinceName = new ArrayList<>();
            for (Object[] obj : dataList) {
                listMobileNumbersCus.add(((String) obj[1]).trim());

                String mobileNumberEmp = (String) obj[9];
                if (!Utils.isNullOrEmpty(mobileNumberEmp) && !listMobileNumberEmp.contains(mobileNumberEmp)) {
                    listMobileNumberEmp.add(mobileNumberEmp.trim());
                }

                String provinceName = (String) obj[10];
                if (!Utils.isNullOrEmpty(provinceName) && !listProvinceName.contains(provinceName)) {
                    listProvinceName.add(provinceName.trim().toLowerCase());
                }
            }

            Map<String, String> mapFullAddress = commonRepository.getMapFullAddress(listProvinceName);
            Map<String, CustomersResponse.DetailBean> mapCustomerExists = customersRepository.getMapCustomerByMobileNumber(listMobileNumbersCus);
            Map<String, EmployeesResponse.DetailBean> mapEmpExists = customersRepository.getMapEmpByMobileNumber(listMobileNumberEmp);
            Map<String, String> mapGenders = new HashMap<>();
            List<CategoryEntity> listGender = customersRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.GIOI_TINH);
            listGender.forEach(item -> mapGenders.put(item.getName().toLowerCase(), item.getValue()));

            List<CustomersEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;
            for (Object[] obj : dataList) {
                CustomersEntity customersEntity = new CustomersEntity();
                customersEntity.setCreatedBy(userName);
                customersEntity.setCreatedTime(curDate);

                int col = 1;
                String mobileNumber = ((String) obj[col]).trim();
                if (mapCustomerExists.get(mobileNumber) != null) {
                    importExcel.addError(row, col, I18n.getMessage("error.customers.import.mobileNumber"), mobileNumber);
                } else {
                    customersEntity.setMobileNumber(mobileNumber);
                    customersEntity.setLoginName(mobileNumber);
                }

                col++;
                customersEntity.setFullName(((String) obj[col++]).trim());

                String genderName = Utils.NVL(((String) obj[col])).trim();
                String genderId = mapGenders.get(genderName.toLowerCase());
                if (!Utils.isNullOrEmpty(genderName) && genderId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), genderName);
                } else {
                    customersEntity.setGenderId(genderId);
                }

                col++;
//                Long dayOfBirth = (Long) obj[col];
//                Long monthOfBirth = (Long) obj[col + 1];
//                Long yearOfBirth = (Long) obj[col + 2];
//                if (dayOfBirth != null || monthOfBirth != null || yearOfBirth != null) {
//                    String dateOfBirthStr = String.format("%02d", dayOfBirth) + "/" + String.format("%02d", monthOfBirth) + "/" + yearOfBirth;
//                    Date dateOfBirth = Utils.stringToDate(dateOfBirthStr);
//                    if (dateOfBirth == null) {
//                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.dateOfBirth"), dateOfBirthStr);
//                    } else {
//                        customersEntity.setDateOfBirth(dateOfBirth);
//                    }
//                }
//
//                col = col + 3;
                customersEntity.setDateOfBirth((Date) obj[col++]);
                customersEntity.setEmail((String) obj[col++]);
                customersEntity.setZaloAccount((String) obj[col++]);
                customersEntity.setJob((String) obj[col++]);
                customersEntity.setDepartmentName((String) obj[col++]);

                String mobileNumberEmp = (String) obj[col];
                EmployeesResponse.DetailBean empInfo = mapEmpExists.get(Utils.NVL(mobileNumberEmp).trim());
                if (!Utils.isNullOrEmpty(mobileNumberEmp) && empInfo == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.customers.import.mobileNumberEmp"), mobileNumberEmp);
                } else if (empInfo != null) {
                    customersEntity.setIntroducerId(empInfo.getEmployeeId());
                }

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

//                    String fullAddress = Utils.join("#", provinceName, districtName, wardName).toLowerCase();
//                    String keyMap = mapFullAddress.keySet().stream().filter(item -> item.startsWith(fullAddress)).findFirst().orElse(null);
                    String addressValue = getValueByProvinceDistrictAndWard(mapFullAddress, provinceName, districtName, wardName);
                    if (Utils.isNullOrEmpty(addressValue) && !isErrorAddress) {
                        String errorMessage = Utils.join(", ", "Tỉnh", districtName.isEmpty() ? null : "huyện", wardName.isEmpty() ? null : "xã");
                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.invalid", errorMessage), Utils.join(", ", provinceName, districtName, wardName));
                    } else if (!Utils.isNullOrEmpty(addressValue)) {
                        String[] valueArr = addressValue.split("#");
                        customersEntity.setProvinceId(valueArr[0]);
                        customersEntity.setDistrictId(districtName.isEmpty() ? null : valueArr[1]);
                        customersEntity.setWardId(wardName.isEmpty() ? null : valueArr[2]);
                    }
                }

                customersEntity.setVillageAddress(villageAddress);
                customersEntity.setFullAddress(Utils.join(", ", villageAddress, wardName, districtName, provinceName));

                col = col + 4;
                customersEntity.setBankAccount((String) obj[col++]);
                customersEntity.setBankName((String) obj[col++]);
                customersEntity.setBankBranch((String) obj[col]);

                listInsert.add(customersEntity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                customersRepository.insertBatch(CustomersEntity.class, listInsert, userName);
                List<EmployeesRequest.CreateUser> userList = new ArrayList<>();
                listInsert.forEach(item -> {
                    userList.add(setDataToEntity(item, Constant.DEFAULT_PASSWORD));
                });

                adminFeignClient.createUserList(Utils.getRequestHeader(request), userList);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
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

    @Override
    public ResponseEntity<Object> customerCare(CustomerCareRecordsRequest.SubmitForm dto) {
        CustomersEntity customersEntity = customersRepository.get(CustomersEntity.class, dto.getCustomerId());
        if (customersEntity == null) {
            throw new RecordNotExistsException(dto.getCustomerId(), CustomersEntity.class);
        }
        EmployeesEntity employeesEntity = customersRepository.get(EmployeesEntity.class, "loginName", Utils.getUserNameLogin());
        dto.setFullName(customersEntity.getFullName());
        dto.setMobileNumber(customersEntity.getMobileNumber());
        dto.setDateOfBirth(customersEntity.getDateOfBirth());
        dto.setRequestedEmpId(employeesEntity != null ? employeesEntity.getEmployeeId() : null);
        dto.setRequestDate(new Date());
        dto.setType(Constant.CUSTOMER_CARE_TYPE.KHACH_HANG);

        return customerCareRecordsService.saveData(dto, null);
    }


    @Override
    public ListResponseEntity<CustomersResponse.DataSelected> getListData() {
        return ResponseUtils.ok(customersRepository.getListData(null));
    }

    @Override
    public ResponseEntity<Object> addCourse(CustomersRequest.CourseForm dto) {
        CourseTraineesEntity courseTraineesEntity = courseTraineesRepositoryJPA.getByCourseIdAndTraineeId(dto.getCourseId(), dto.getCustomerId());
        if (courseTraineesEntity == null) {
            courseTraineesEntity = new CourseTraineesEntity();
            courseTraineesEntity.setCreatedBy(Utils.getUserNameLogin());
            courseTraineesEntity.setCreatedTime(new Date());
        } else {
            courseTraineesEntity.setModifiedBy(Utils.getUserNameLogin());
            courseTraineesEntity.setModifiedTime(new Date());
        }
        courseTraineesEntity.setCourseId(dto.getCourseId());
        courseTraineesEntity.setTraineeId(dto.getCustomerId());
        courseTraineesEntity.setInstructorId(dto.getInstructorId());
        courseTraineesEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        courseTraineesRepositoryJPA.save(courseTraineesEntity);
        return ResponseUtils.ok();
    }

    @Override
    public TableResponseEntity<CustomersResponse.SearchResult> getListPageable(CustomersRequest.SearchForm dto) {
        return ResponseUtils.ok(customersRepository.getListPageable(dto));
    }

    private void createUser(CustomersEntity entity, String password) {
        EmployeesRequest.CreateUser createUserRequest = new EmployeesRequest.CreateUser();
        createUserRequest.setEmail(entity.getEmail());
        createUserRequest.setLoginName(entity.getLoginName());
        createUserRequest.setPassword(password);
        createUserRequest.setFullName(entity.getFullName());
        createUserRequest.setMobileNumber(entity.getMobileNumber());
        createUserRequest.setDefaultRoles(List.of("CUSTOMER_CRM"));

        //xu ly them moi user
        adminFeignClient.createUser(Utils.getRequestHeader(request), createUserRequest);
    }

    private EmployeesRequest.CreateUser setDataToEntity(CustomersEntity entity, String password) {
        EmployeesRequest.CreateUser userRequest = new EmployeesRequest.CreateUser();
        userRequest.setEmail(entity.getEmail());
        userRequest.setLoginName(entity.getLoginName());
        userRequest.setPassword(password);
        userRequest.setFullName(entity.getFullName());
        userRequest.setMobileNumber(entity.getMobileNumber());
        userRequest.setDefaultRoles(List.of("CUSTOMER_CRM"));

        return userRequest;
    }

}
