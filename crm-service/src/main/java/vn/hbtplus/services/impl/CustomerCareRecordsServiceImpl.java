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
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.CustomerCareRecordsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.CustomerCareRecordsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.CustomerCareRecordsEntity;
import vn.hbtplus.repositories.entity.CustomersEntity;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.impl.CustomerCareRecordsRepository;
import vn.hbtplus.repositories.impl.CustomersRepository;
import vn.hbtplus.repositories.jpa.CustomerCareRecordsRepositoryJPA;
import vn.hbtplus.services.CustomerCareRecordsService;
import vn.hbtplus.utils.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang crm_customer_care_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CustomerCareRecordsServiceImpl implements CustomerCareRecordsService {

    private final CustomerCareRecordsRepository customerCareRecordsRepository;
    private final CustomersRepository customersRepository;
    private final CustomerCareRecordsRepositoryJPA customerCareRecordsRepositoryJPA;
    private final MdcForkJoinPool forkJoinPool;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CustomerCareRecordsResponse> searchData(CustomerCareRecordsRequest.SearchForm dto) {
        return ResponseUtils.ok(customerCareRecordsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CustomerCareRecordsRequest.SubmitForm dto, Long id) throws BaseAppException {
        CustomerCareRecordsEntity entity;
        Date curDate = new Date();
        String userName = Utils.getUserNameLogin();
        if (id != null && id > 0L) {
            entity = customerCareRecordsRepositoryJPA.getById(id);
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
        } else {
            entity = new CustomerCareRecordsEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
        }
        Utils.copyProperties(dto, entity);
        entity.setFullName(dto.getFullName().replaceAll(" - \\d+$", "").trim());
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        customerCareRecordsRepositoryJPA.save(entity);

        //update thong tin nguoi cham soc cho khach hang
        if (StringUtils.equalsIgnoreCase(entity.getType(), Constant.CUSTOMER_CARE_TYPE.KHACH_HANG)) {
            customersRepository.updateUserTakeCare(entity.getCustomerId(), entity.getCaringEmpId(), userName, curDate);
        }
        return ResponseUtils.ok(entity.getCustomerCareRecordId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CustomerCareRecordsEntity> optional = customerCareRecordsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomerCareRecordsEntity.class);
        }
        customerCareRecordsRepository.deActiveObject(CustomerCareRecordsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CustomerCareRecordsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<CustomerCareRecordsEntity> optional = customerCareRecordsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomerCareRecordsEntity.class);
        }

        CustomerCareRecordsResponse dto = new CustomerCareRecordsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        ExportExcel exportExcel = new ExportExcel("template/import/BM_Import_danh-sach-telesales.xlsx", 2, true);

        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        // Phân Loại
        completableFutures.add(CompletableFuture.supplyAsync(() -> customerCareRecordsRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_PHAN_LOAI, "orderNumber"), forkJoinPool));

        // Khách hàng
        completableFutures.add(CompletableFuture.supplyAsync(() -> customerCareRecordsRepository.getListMapObjectByProperties(CustomersEntity.class), forkJoinPool));

        // Nhân viên
        completableFutures.add(CompletableFuture.supplyAsync(() -> customerCareRecordsRepository.getListMapObjectByProperties(EmployeesEntity.class), forkJoinPool));

        // Trạng Thái Chăm Sóc
        completableFutures.add(CompletableFuture.supplyAsync(() -> customerCareRecordsRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_TRANG_THAI_CHAM_SOC, "orderNumber"), forkJoinPool));

        // Tình Trạng
        completableFutures.add(CompletableFuture.supplyAsync(() -> customerCareRecordsRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_TINH_TRANG, "orderNumber"), forkJoinPool));

        CompletableFuture<Void> allReturns = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Object>> allFutures = allReturns.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<Object> objs = allFutures.get();

        int activeSheet = 1;
        for (Object obj : objs) {
            exportExcel.setActiveSheet(activeSheet++);
            List<Map<String, Object>> listMapData = (List<Map<String, Object>>) obj;
            if (!Utils.isNullOrEmpty(listMapData)) {
                for (int row = 1; row <= listMapData.size(); row++) {
                    exportExcel.setText(String.valueOf(row), 0, row);
                    Object name = listMapData.get(row - 1).getOrDefault("full_name", listMapData.get(row - 1).get("name"));
                    exportExcel.setText(name != null ? name.toString() : "", 1, row);
                }
            }

        }

        exportExcel.setActiveSheet(0);
        return ResponseUtils.ok(exportExcel, "BM_Import_danh-sach-telesales.xlsx", false);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity importProcess(MultipartFile file) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_Import_danh-sach-telesales.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

            Map<String, String> mapTypeName = new HashMap<>();
            List<CategoryEntity> listType = customerCareRecordsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_PHAN_LOAI);
            listType.forEach(item -> mapTypeName.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapCaringStatusId = new HashMap<>();
            List<CategoryEntity> listCaringStatus = customerCareRecordsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_TRANG_THAI_CHAM_SOC);
            listCaringStatus.forEach(item -> mapCaringStatusId.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapStatusId = new HashMap<>();
            List<CategoryEntity> listStatus = customerCareRecordsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_TINH_TRANG);
            listStatus.forEach(item -> mapStatusId.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, Long> mapCustomerId = new HashMap<>();
            List<CustomersEntity> listCustomer = customerCareRecordsRepository.findByProperties(CustomersEntity.class);
            listCustomer.forEach(item -> mapCustomerId.put(item.getFullName().toLowerCase(), item.getCustomerId()));

            Map<String, Long> mapEmployeeId = new HashMap<>();
            List<EmployeesEntity> listEmployee = customerCareRecordsRepository.findByProperties(EmployeesEntity.class);
            listEmployee.forEach(item -> mapEmployeeId.put(item.getFullName().toLowerCase(), item.getEmployeeId()));

            List<CustomerCareRecordsEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;
            for (Object[] obj : dataList) {
                CustomerCareRecordsEntity entity = new CustomerCareRecordsEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                int col = 1;
                //Phan Loai
                String typeName = Utils.NVL(((String) obj[col])).trim();
                String typeId = mapTypeName.get(typeName.toLowerCase());
                if (!Utils.isNullOrEmpty(typeName) && typeId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), typeName);
                } else {
                    entity.setType(typeId);
                }
                col++;

                // Sdt
                String mobileNumber = Utils.NVL(((String) obj[col])).trim();
                if (!Utils.isValidPhoneNumber(mobileNumber)) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.invalid.mobileNumber"), mobileNumber);
                } else {
                    entity.setMobileNumber(mobileNumber);
                }
                col++;

                //Khach hang
                String customerName = Utils.NVL(((String) obj[col])).trim();
                Long customerId = mapCustomerId.get(customerName.toLowerCase());
                if (!Utils.isNullOrEmpty(customerName) && customerId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), customerName);
                } else {
                    entity.setCustomerId(customerId);
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
                Long dayOfRequest = (Long) obj[col];
                Long monthOfRequest = (Long) obj[col + 1];
                Long yearOfRequest = (Long) obj[col + 2];
                if (dayOfRequest != null || monthOfRequest != null || yearOfRequest != null) {
                    String dateOfRequestStr = String.format("%02d", dayOfRequest) + "/" + String.format("%02d", monthOfRequest) + "/" + yearOfRequest;
                    Date dateORequest = Utils.stringToDate(dateOfRequestStr);
                    if (dateORequest == null) {
                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.dateOfRequest"), dateOfRequestStr);
                    } else {
                        entity.setRequestDate(dateORequest);
                    }
                }
                col = col + 3;
                //nguoi yeu cau
                String RequestedEmpName = Utils.NVL(((String) obj[col]));
                Long requestedEmpId = mapEmployeeId.get(RequestedEmpName.toLowerCase());
                if (!Utils.isNullOrEmpty(RequestedEmpName) && requestedEmpId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), RequestedEmpName);
                } else {
                    entity.setRequestedEmpId(requestedEmpId);
                }
                col++;

                //Nhan vien cham soc
                String caringEmpName = Utils.NVL(((String) obj[col])).trim();
                Long caringEmpNameId = mapEmployeeId.get(caringEmpName.toLowerCase());
                if (!Utils.isNullOrEmpty(caringEmpName) && caringEmpNameId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), caringEmpName);
                } else {
                    entity.setCaringEmpId(caringEmpNameId);
                }
                col++;

                Long dayOfContact = (Long) obj[col];
                Long monthOfContact = (Long) obj[col + 1];
                Long yearOfContact = (Long) obj[col + 2];
                if (dayOfContact != null || monthOfContact != null || yearOfContact != null) {
                    String dateOfContactStr = String.format("%02d", dayOfContact) + "/" + String.format("%02d", monthOfContact) + "/" + yearOfContact;
                    Date dateOfContact = Utils.stringToDate(dateOfContactStr);
                    if (dateOfContact == null) {
                        importExcel.addError(row, col, I18n.getMessage("error.customers.import.dateOfContact"), dateOfContactStr);
                    } else {
                        entity.setContactDate(dateOfContact);
                    }
                }
                col = col + 3;

                String caringStatusName = Utils.NVL(((String) obj[col])).trim();
                String caringStatusId = mapCaringStatusId.get(caringStatusName.toLowerCase());
                if (!Utils.isNullOrEmpty(caringStatusName) && caringStatusId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), caringStatusName);
                } else {
                    entity.setCaringStatusId(caringStatusId);
                }
                col++;

                String statusName = Utils.NVL(((String) obj[col])).trim();
                String statusId = mapStatusId.get(statusName.toLowerCase());
                if (!Utils.isNullOrEmpty(statusName) && statusId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), statusName);
                } else {
                    entity.setStatusId(statusId);
                }
                entity.setFullName(Utils.normalizeFullName(customerName));

                listInsert.add(entity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                customerCareRecordsRepository.insertBatch(CustomerCareRecordsEntity.class, listInsert, userName);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportData(CustomerCareRecordsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh_sach_telesales.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 3, true);
        List<Map<String, Object>> listDataExport = customerCareRecordsRepository.getListExport(dto);

        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh_sach_telesales.xlsx");
    }

}
