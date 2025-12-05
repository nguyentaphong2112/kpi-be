/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.InvoiceRequestsDTO;
import vn.hbtplus.models.response.*;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.tax.personal.models.response.InvoiceRequestsResponse;
import vn.hbtplus.tax.personal.models.response.LockRegistrationsResponse;
import vn.hbtplus.tax.personal.repositories.entity.DeclarationRegistersEntity;
import vn.hbtplus.tax.personal.repositories.entity.HrEmployeesEntity;
import vn.hbtplus.tax.personal.repositories.entity.InvoiceRequestsEntity;
import vn.hbtplus.tax.personal.repositories.impl.EmployeeRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.impl.InvoiceRequestsRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.impl.LockRegistrationsRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.jpa.DeclarationRegistersRepositoryJPA;
import vn.hbtplus.tax.personal.repositories.jpa.InvoiceRequestsRepositoryJPA;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.InvoiceRequestsService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Lop impl service ung voi bang PTX_INVOICE_REQUESTS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceRequestsServiceImpl implements InvoiceRequestsService {

    private final InvoiceRequestsRepositoryImpl invoiceRequestsRepositoryImpl;
    private final InvoiceRequestsRepositoryJPA invoiceRequestsRepositoryJPA;
    private final CommonUtilsService commonUtilsService;
    private final LogActionsServiceImpl logActionsService;
    private final DeclarationRegistersRepositoryJPA declarationRegistersRepositoryJPA;
    private final EmployeeRepositoryImpl employeeRepository;
    private final LockRegistrationsRepositoryImpl lockRegistrationsRepository;
    private final AuthorizationService authorizationService;
    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InvoiceRequestsResponse> searchData(AdminSearchDTO dto) {
        return ResponseUtils.ok(invoiceRequestsRepositoryImpl.searchData(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportData(AdminSearchDTO dto) {
        List<Map<String, Object>> listData = invoiceRequestsRepositoryImpl.getListDataExport(dto);
        if (listData.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        try {
            String pathTemplate = "template/export/declaration/BM_Xuat_DS_DangKy_Nhan_Hoa_Don.xlsx";
            ExportExcel dynamicExport = new ExportExcel(pathTemplate, 7, true);
            dynamicExport.replaceKeys(listData);
            String fileName = Utils.getFilePathExport("BM_Xuat_DS_DangKy_Nhan_Hoa_Don.xlsx");
            dynamicExport.exportFile(fileName);
            return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_DangKy_Nhan_Hoa_Don.xlsx");
        } catch (Exception ex) {
            log.error("[exportDataInvoiceRequests] has error {}", ex);
            return ResponseUtils.getResponseDataNotFound();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveData(InvoiceRequestsDTO dto, boolean isAdmin) {
        // validate du lieu dau vao
        Long empId = null;
        InvoiceRequestsEntity entity = null;
        HrEmployeesEntity employeesEntity = null;
        if (dto.getEmployeeId() != null) {
            empId = isAdmin ? dto.getEmployeeId() : commonUtilsService.getEmpIdLogin();
            employeesEntity = invoiceRequestsRepositoryImpl.get(HrEmployeesEntity.class, empId);
            if (Utils.isNullOrEmpty(employeesEntity.getTaxNo())) {
                String mess = I18n.getMessage("invoiceRequest.validate.import.taxNo", employeesEntity.getEmployeeCode());
                throw new BaseAppException("BAD_REQUEST", mess);
            }
            boolean isDuplicate = invoiceRequestsRepositoryImpl.duplicate(InvoiceRequestsEntity.class,
                    dto.getInvoiceRequestId(),
                    "employeeId", empId,
                    "year", dto.getYear());
            if (isDuplicate) {
                throw new BaseAppException("BAD_REQUEST", I18n.getMessage("declaration.validate.revInvoice.duplicate"));
            }

            if (!isAdmin) {
                dto.setOrgId(invoiceRequestsRepositoryImpl.getOrgManageId(employeesEntity.getOrganizationId()));
            }
        } else {
            boolean isDuplicate = invoiceRequestsRepositoryImpl.duplicate(InvoiceRequestsEntity.class,
                    dto.getInvoiceRequestId(),
                    "idNo", dto.getIdNo(),
                    "year", dto.getYear());
            if (isDuplicate) {
                throw new BaseAppException("BAD_REQUEST", I18n.getMessage("declaration.validate.revInvoice.duplicate"));
            }
        }

        if (!Utils.isNullObject(dto.getInvoiceRequestId())) {
            entity = invoiceRequestsRepositoryJPA.getById(dto.getInvoiceRequestId());
//            if (dto.getStatus().equals(entity.getStatus()) && !isAdmin) {
//                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
//            }
        } else if (BaseConstants.NO.equals(dto.getStatus())) {//neu registerId = null va chon la Huy Dang Ky
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }

        if (entity != null) {
            if ((empId != null && !empId.equals(entity.getEmployeeId()) && !isAdmin)
                    || !commonUtilsService.getTaxValidStatusUpdate(isAdmin).contains(entity.getStatus())
            ) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new InvoiceRequestsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setInvoiceStatus(Constant.INVOICE_STATUS.PROCESSING);
        }

        Utils.copyProperties(dto, entity);
        entity.setEmployeeId(empId);
        entity.setStatus(Constant.TAX_STATUS.REGISTERED);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        if (employeesEntity != null) {
            entity.setEmployeeCode(employeesEntity.getEmployeeCode());
            entity.setTaxNo(employeesEntity.getTaxNo());
//            entity.setIdNo(employeesEntity.getId());
            if (Utils.isNullOrEmpty(entity.getEmail())) {
                entity.setEmail(employeesEntity.getEmail());
            }
            entity.setFullName(employeesEntity.getFullName());
        }
        invoiceRequestsRepositoryJPA.saveAndFlush(entity);
//        String logContent = I18n.getMessage("taxNumber.actionLog.status." + entity.getStatus());
//        logActionsService.saveLog(entity.getInvoiceRequestId(), Constant.LOG_OBJECT_TYPE.INVOICE, logContent, null, entity.getStatus());
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteData(Long id, boolean isAdmin) {
        Optional<InvoiceRequestsEntity> optional = invoiceRequestsRepositoryJPA.findById(id);
        if (optional.isEmpty()
                || BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
                || (!commonUtilsService.getEmpIdLogin().equals(optional.get().getEmployeeId()) && !isAdmin)
                || (!commonUtilsService.getTaxValidStatusDelete().contains(optional.get().getStatus()) && !isAdmin)
        ) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        invoiceRequestsRepositoryImpl.deActiveObject(InvoiceRequestsEntity.class, id);
        logActionsService.saveLog(id, Constant.LOG_OBJECT_TYPE.INVOICE, null, null, null);

        List<DeclarationRegistersEntity> listRefEntity = invoiceRequestsRepositoryImpl.findByProperties(DeclarationRegistersEntity.class, "employeeId", optional.get().getEmployeeId(), "year", optional.get().getYear());
        if (!Utils.isNullOrEmpty(listRefEntity)) {
            DeclarationRegistersEntity declarationRegisters = listRefEntity.get(0);
            if (BaseConstants.YES.equals(declarationRegisters.getRevInvoice())) {
                declarationRegisters.setRevInvoice(BaseConstants.NO);
                declarationRegisters.setModifiedTime(new Date());
                declarationRegisters.setModifiedBy(Utils.getUserNameLogin());
                declarationRegistersRepositoryJPA.save(declarationRegisters);
                logActionsService.saveLog(declarationRegisters.getDeclarationRegisterId(), Constant.LOG_OBJECT_TYPE.DECLARATION, null, BaseConstants.YES, BaseConstants.NO);
            }
        }
        return ResponseUtils.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<InvoiceRequestsResponse> getDataById(Long id, boolean isAdmin) {
        Optional<InvoiceRequestsEntity> optional = invoiceRequestsRepositoryJPA.findById(id);
        if (optional.isEmpty()
                || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
        ) {
            throw new RecordNotExistsException(id, InvoiceRequestsEntity.class);
        }
        InvoiceRequestsResponse dto = new InvoiceRequestsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> getImportTemplate() throws Exception {
        String pathTemplate = "template/import/BM_import_danh_sach_nhan_chung_tu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.setActiveSheet(1);
        List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.RECEIVE_INVOICE, Utils.getUserNameLogin());
        List<String> listOrgManage = invoiceRequestsRepositoryImpl.getListOrgManage(listOrgId);
        int row = 0;
        for (String orgName : listOrgManage) {
            dynamicExport.setText(orgName, 0, row++);
        }
        dynamicExport.setActiveSheet(0);

        String fileName = Utils.getFilePathExport("BM_import_danh_sach_nhan_chung_tu.xlsx");
        dynamicExport.exportFile(fileName);
        return ResponseUtils.ok(dynamicExport, "BM_import_danh_sach_nhan_chung_tu.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity<Object> importProcess(MultipartFile file, HttpServletRequest req) {
        ImportExcel importExcel = new ImportExcel("template/import/CH_import_danh_sach_nhan_chung_tu.xml");
        List<Object[]> dataList = new ArrayList<>();
        ResponseEntity<Object> resultValidate = utilsService.validateFileImport(importExcel, file, dataList);
        if (resultValidate != null) {
            return resultValidate;
        }

        List<String> listEmployeeCode = new ArrayList<>();
        List<String> listIdNo = new ArrayList<>();
        List<String> listOrgName = new ArrayList<>();
        int columnIdNo = 2;
        int columnTaxNo = 4;
        int columnOrg = 7;
        for (Object[] obj : dataList) {
            String empCode = (String) obj[1];
            if (!Utils.isNullOrEmpty(empCode)) {
                listEmployeeCode.add(empCode.toUpperCase());
            }

            String idNo = (String) obj[columnIdNo];
            if (!Utils.isNullOrEmpty(idNo)) {
                listIdNo.add(idNo);
            }

            String orgName = (String) obj[columnOrg];
            listOrgName.add(orgName.toLowerCase());
        }

        Map<String, EmployeeInfoResponse> mapEmployee = employeeRepository.getMapEmpByCodes(listEmployeeCode);
        Map<String, InvoiceRequestsEntity> mapEntity = invoiceRequestsRepositoryImpl.getListInvoiceByEmpCodes(listEmployeeCode);
        mapEntity.putAll(invoiceRequestsRepositoryImpl.getListInvoiceByListIdNo(listIdNo));
        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.IMPORT, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.RECEIVE_INVOICE, Utils.getUserNameLogin());
        Map<String, Long> mapOrg = employeeRepository.getMapOrgManage(listOrgName, permissionDataDtoList);
        List<InvoiceRequestsEntity> listSave = new ArrayList<>();
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        int row = -1;
        for (Object[] obj : dataList) {
            row++;
            int col = 1;
            String employeeCode = ((String) obj[col]);
            String employeeName = ((String) obj[col + 2]).trim();
            String taxNo = ((String) obj[columnTaxNo]).trim();
            Integer year = ((Long) obj[columnTaxNo + 1]).intValue();
            Long employeeId = null;
            String idNo;

            if (!Utils.isValidTaxNo(taxNo)) {
                importExcel.addError(row, columnTaxNo, I18n.getMessage("taxNumber.validate.import.taxNo"), taxNo);
            }

            if (!Utils.isNullOrEmpty(employeeCode)) {
                EmployeeInfoResponse employeeInfoResponse = mapEmployee.get((employeeCode.trim() + employeeName).toLowerCase());
                if (employeeInfoResponse == null) {
                    importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.employee"), employeeCode + "-" + employeeName);
                    continue;
                }

                if (!taxNo.equals(employeeInfoResponse.getTaxNo())) {
                    importExcel.addError(row, col, I18n.getMessage("invoiceRequest.validate.invalid.taxNo", employeeInfoResponse.getTaxNo()), taxNo);
                }

                if (mapEntity.get(employeeCode.toUpperCase().trim() + year) != null) {
                    importExcel.addError(row, col, I18n.getMessage("invoiceRequest.validate.duplicate.register"), employeeCode);
                }

                employeeId = employeeInfoResponse.getEmployeeId();
                idNo = employeeInfoResponse.getPersonalId();
            } else {
                idNo = ((String) obj[columnIdNo]);
                if (Utils.isNullOrEmpty(idNo)) {
                    importExcel.addError(row, columnIdNo, I18n.getMessage("invoice.validate.import.required.idNo", idNo), null);
                    continue;
                } else if (idNo.trim().length() != 9 && idNo.trim().length() != 12) {
                    importExcel.addError(row, columnIdNo, I18n.getMessage("taxNumber.validate.import.idNo"), idNo);
                }
                if (mapEntity.get(idNo.trim() + year) != null) {
                    importExcel.addError(row, columnIdNo, I18n.getMessage("invoiceRequest.validate.duplicate.registerIdNo", idNo), idNo);
                }
            }

            InvoiceRequestsEntity entity = new InvoiceRequestsEntity();
            entity.setCreatedTime(currentDate);
            entity.setCreatedBy(userName);
            entity.setStatus(Constant.TAX_STATUS.REGISTERED);
            entity.setIdNo(idNo);
            col = col + 5;
            String email = (String) obj[col];
            if (!Utils.isNullOrEmpty(email)) {
                if (!Utils.isValidEmail(email)) {
                    importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.email"), email);
                    continue;
                } else {
                    entity.setEmail(email);
                }
            }
            col++;

            String orgName = (String) obj[col];
            Long orgId = mapOrg.get(orgName.toLowerCase());
            if (orgId == null) {
                importExcel.addError(row, columnOrg, I18n.getMessage("invoiceRequest.validate.invalid.orgManage"), orgName);
            }
            entity.setOrgId(orgId);
            col++;

            entity.setNote((String) obj[col]);
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            entity.setEmployeeId(employeeId);
            if (!Utils.isNullOrEmpty(employeeCode)) {
                entity.setEmployeeCode(employeeCode.toUpperCase());
            }
            entity.setFullName(employeeName);
            entity.setTaxNo(taxNo);
            entity.setYear(year);
            listSave.add(entity);
        }
        if (importExcel.hasError()) {// co loi xay ra
            return utilsService.responseErrorImportFile(importExcel, file);
        } else {// thuc hien insert vao DB
            invoiceRequestsRepositoryImpl.insertBatch(InvoiceRequestsEntity.class, listSave, Utils.getUserNameLogin());
            return ResponseUtils.ok();
        }
    }

    @Override
    @Transactional
    public void autoRegister() {
        Date currentDate = new Date();
        List<LockRegistrationsResponse> listLockReg = lockRegistrationsRepository.getListLockRegistrationByRegisterDate(currentDate);
        if (!Utils.isNullOrEmpty(listLockReg)) {
            for (LockRegistrationsResponse object : listLockReg) {
                if (object.getToDate() != null && Utils.daysBetween(object.getToDate(), currentDate) == 0) {// vao ngay cuoi cua ky dang ky thi moi thuc hien dang ky tu dong
                    List<InvoiceRequestsEntity> listRequest = invoiceRequestsRepositoryImpl.findEmployeeAutoInvoiceRequests(currentDate, object.getYear());
                    if (!Utils.isNullOrEmpty(listRequest)) {
                        for (InvoiceRequestsEntity entity : listRequest) {
                            entity.setYear(object.getYear());
                            entity.setNote(I18n.getMessage("declaration.cron.autoRegister.note"));
                            entity.setCreatedBy("cronTab");
                            entity.setCreatedTime(currentDate);
                            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                            entity.setStatus(BaseConstants.YES);
                        }
                        invoiceRequestsRepositoryJPA.saveAll(listRequest);
                        log.info("schedule.cron.invoice.autoRequest|insert success " + listRequest.size() + " records");
                    }
                }
            }
        }
    }

    @Override
    public ResponseEntity<Object> requestExportInvoiceByList(List<Long> listId) {
        if (Utils.isNullObject(listId)) {
            throw new BaseAppException("BAD_REQUEST", "listId is empty");
        }

        List<InvoiceRequestsEntity> listProcess = new ArrayList<>();
        for (Long id : listId) {
            InvoiceRequestsEntity entity = invoiceRequestsRepositoryJPA.getById(id);
            if (!Constant.INVOICE_STATUS.PROCESSING.equals(entity.getInvoiceStatus())) {
                throw new BaseAppException("BAD_REQUEST", "status invalid|" + id);
            }
            authorizationService.hasPermissionWithOrg(entity.getOrgId(), Scope.UPDATE, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.RECEIVE_INVOICE);
            listProcess.add(entity);
        }

        for (InvoiceRequestsEntity entity : listProcess) {
            InvoiceRequestsResponse dto = new InvoiceRequestsResponse();
            Utils.copyProperties(dto, entity);
            dto.setOrgManageId(dto.getOrgId());
            HrEmployeesEntity employeesEntity = null;
            if (dto.getEmployeeId() != null) {
                employeesEntity = invoiceRequestsRepositoryImpl.get(HrEmployeesEntity.class, dto.getEmployeeId());
            }
            invoiceRequestsRepositoryImpl.pushExportInvoice(dto, employeesEntity);
            entity.setInvoiceStatus(Constant.INVOICE_STATUS.ACCOUNTANT_RECEIVED);
            invoiceRequestsRepositoryJPA.save(entity);
        }
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> requestExportInvoiceByForm(AdminSearchDTO searchDTO) {
        List<InvoiceRequestsResponse> listData = invoiceRequestsRepositoryImpl.getListDataByForm(searchDTO);
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        List<Long> listId = new ArrayList<>();
        for (InvoiceRequestsResponse dto : listData) {
//            invoiceRequestsRepositoryImpl.pushExportInvoice(dto, null);
            listId.add(dto.getInvoiceRequestId());
        }
        invoiceRequestsRepositoryImpl.updateInvoiceStatus(listId, Constant.INVOICE_STATUS.ACCOUNTANT_RECEIVED);
        return ResponseUtils.ok();
    }

    @Override
    public void scanInvoiceStatus() {
        List<InvoiceRequestsEntity> listData = invoiceRequestsRepositoryImpl.findByProperties(
                InvoiceRequestsEntity.class, "invoiceStatus", Constant.INVOICE_STATUS.ACCOUNTANT_RECEIVED);
        // xu ly lay du lieu can quet trang thai
        Map<Integer, List<String>> mapEmpCodeByYear = new HashMap<>();
        Map<Integer, List<String>> mapTaxNoByYear = new HashMap<>();
        for (InvoiceRequestsEntity entity : listData) {
            if (!Utils.isNullOrEmpty(entity.getEmployeeCode())) {
                List<String> listEmpCode = mapEmpCodeByYear.get(entity.getYear());
                if (listEmpCode == null) {
                    listEmpCode = new ArrayList<>();
                }
                listEmpCode.add(entity.getEmployeeCode());
                mapEmpCodeByYear.put(entity.getYear(), listEmpCode);
            } else {
                List<String> listTaxNo = mapTaxNoByYear.get(entity.getYear());
                if (listTaxNo == null) {
                    listTaxNo = new ArrayList<>();
                }
                listTaxNo.add(entity.getTaxNo());
                mapTaxNoByYear.put(entity.getYear(), listTaxNo);
            }
        }

        // xu ly quet trang thai theo ma so thue
        List<InvoiceRequestsEntity> listSave = new ArrayList<>();
        Date currentDate = new Date();
        Gson gson = new Gson();
        for (Integer year : mapTaxNoByYear.keySet()) {
            List<String> listTaxNo = mapTaxNoByYear.get(year);
            Map<String, Integer> mapResultByTaxNo = invoiceRequestsRepositoryImpl.getMapTaxPerIncomeByListTaxNo(listTaxNo, year);
            for (InvoiceRequestsEntity entity : listData) {
                int invoiceStatus = Utils.NVL(mapResultByTaxNo.get(entity.getTaxNo()), Constant.INVOICE_STATUS.ACCOUNTANT_RECEIVED);
                if (entity.getYear().equals(year)
                        && Utils.isNullOrEmpty(entity.getEmployeeCode())
                        && invoiceStatus != Constant.INVOICE_STATUS.ACCOUNTANT_RECEIVED) {
                    entity.setInvoiceStatus(invoiceStatus);
                    entity.setModifiedBy("cronTab");
                    entity.setModifiedTime(currentDate);
                    listSave.add(entity);
                }
            }
            log.info("schedule.cron.invoice.scanStatusExport|year=" + year + "|mapResultByTaxNo=" + gson.toJson(mapResultByTaxNo));
        }

        // xu ly quet trang thai theo ma nhan vien
        for (Integer year : mapEmpCodeByYear.keySet()) {
            List<String> listEmpCode = mapEmpCodeByYear.get(year);
            Map<String, Integer> mapResultByEmpCode = invoiceRequestsRepositoryImpl.getMapTaxPerIncomeByListEmpCode(listEmpCode, year);
            for (InvoiceRequestsEntity entity : listData) {
                int invoiceStatus = Utils.NVL(mapResultByEmpCode.get(entity.getEmployeeCode()), Constant.INVOICE_STATUS.ACCOUNTANT_RECEIVED);
                if (entity.getYear().equals(year)
                        && !Utils.isNullOrEmpty(entity.getEmployeeCode())
                        && invoiceStatus != Constant.INVOICE_STATUS.ACCOUNTANT_RECEIVED) {
                    entity.setInvoiceStatus(invoiceStatus);
                    entity.setModifiedBy("cronTab");
                    entity.setModifiedTime(currentDate);
                    listSave.add(entity);
                }
            }
            log.info("schedule.cron.invoice.scanStatusExport|year=" + year + "|mapResultByEmpCode=" + gson.toJson(mapResultByEmpCode));
        }

        invoiceRequestsRepositoryJPA.saveAll(listSave);
        log.info("schedule.cron.invoice.scanStatusExport|update record number=" + listSave.size());
    }

}
