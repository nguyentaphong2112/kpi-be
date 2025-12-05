/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.DeclarationRegistersDTO;
import vn.hbtplus.tax.personal.models.request.InvoiceRequestsDTO;
import vn.hbtplus.tax.personal.models.request.LockRegistrationsDTO;
import vn.hbtplus.tax.personal.models.response.DeclarationRegistersResponse;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.tax.personal.models.response.LockRegistrationsResponse;
import vn.hbtplus.tax.personal.repositories.entity.DeclarationRegistersEntity;
import vn.hbtplus.tax.personal.repositories.entity.HrEmployeesEntity;
import vn.hbtplus.tax.personal.repositories.entity.InvoiceRequestsEntity;
import vn.hbtplus.tax.personal.repositories.impl.DeclarationRegistersRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.impl.EmployeeRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.impl.LockRegistrationsRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.jpa.DeclarationRegistersRepositoryJPA;
import vn.hbtplus.tax.personal.repositories.jpa.InvoiceRequestsRepositoryJPA;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.DeclarationRegistersService;
import vn.hbtplus.tax.personal.services.InvoiceRequestsService;
import vn.hbtplus.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Lop impl service ung voi bang PTX_DECLARATION_REGISTERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DeclarationRegistersServiceImpl implements DeclarationRegistersService {

    private final DeclarationRegistersRepositoryImpl declarationRegistersRepositoryImpl;
    private final DeclarationRegistersRepositoryJPA declarationRegistersRepositoryJPA;
    private final CommonUtilsService commonUtilsService;
    private final LogActionsServiceImpl logActionsService;
    private final AuthorizationService authorizationService;
    private final InvoiceRequestsRepositoryJPA invoiceRequestsRepositoryJPA;
    private final EmployeeRepositoryImpl employeeRepositoryImpl;
    private final LockRegistrationsRepositoryImpl lockRegistrationsRepository;
    private final InvoiceRequestsService invoiceRequestsService;
    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DeclarationRegistersResponse> searchData(AdminSearchDTO dto) {
        return ResponseUtils.ok(declarationRegistersRepositoryImpl.searchData(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportData(AdminSearchDTO dto) {
        List<Map<String, Object>> listData = declarationRegistersRepositoryImpl.getDataDeclarationRegister(dto);
        if (listData.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        try {
            String pathTemplate = "template/export/declaration/BM_Xuat_DS_DangKy_Quyet_Toan_Thue.xlsx";
            ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
            dynamicExport.replaceKeys(listData);
            return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_DangKy_Quyet_Toan_Thue.xlsx");
        } catch (Exception ex) {
            log.error("[exportDataDependentRegister] has error {}", ex);
            return ResponseUtils.getResponseDataNotFound();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveData(DeclarationRegistersDTO dto, boolean isAdmin) {
        // validate du lieu dau vao
        Long empId = isAdmin ? dto.getEmployeeId() : commonUtilsService.getEmpIdLogin();
        HrEmployeesEntity employeesEntity = declarationRegistersRepositoryImpl.get(HrEmployeesEntity.class, empId);
        if (Utils.isNullOrEmpty(employeesEntity.getTaxNo())) {
            String mess = I18n.getMessage("invoiceRequest.validate.import.taxNo", employeesEntity.getEmployeeCode());
            throw new BaseAppException("BAD_REQUEST", mess);
        }
        boolean isDuplicate = declarationRegistersRepositoryImpl.duplicate(DeclarationRegistersEntity.class,
                dto.getDeclarationRegisterId(), "employeeId", empId, "year", dto.getYear());
        if (isDuplicate) {
            throw new BaseAppException("BAD_REQUEST", I18n.getMessage("declaration.validate.declare.duplicate"));
        }

        if (!isAdmin) {
            boolean hasEffectiveDate = lockRegistrationsRepository.hasEffectiveDate(new Date(), dto.getYear());
            if (!hasEffectiveDate) {
                throw new BaseAppException("BAD_REQUEST", I18n.getMessage("declaration.validate.import.dateRegister", dto.getYear()));
            }
        }

        dto.setStatus(Constant.TAX_STATUS.REGISTERED);
        DeclarationRegistersEntity entity;
        if (!Utils.isNullObject(dto.getDeclarationRegisterId())) {
            entity = declarationRegistersRepositoryJPA.getById(dto.getDeclarationRegisterId());
            if ((!empId.equals(entity.getEmployeeId()) && !isAdmin)
                    || !commonUtilsService.getTaxValidStatusUpdate(isAdmin).contains(entity.getStatus())
            ) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());

            // neu truoc day da dang ky xac nhan va sau do update khong xac nhan nua
            if (BaseConstants.YES.equals(entity.getRevInvoice())
                    && BaseConstants.NO.equals(dto.getRevInvoice())) {
                List<InvoiceRequestsEntity> listRevInvoices = declarationRegistersRepositoryImpl.findByProperties(InvoiceRequestsEntity.class, "employeeId", empId, "year", dto.getYear());
                if (!Utils.isNullOrEmpty(listRevInvoices)) {
                    InvoiceRequestsEntity invoiceRequestsEntity = listRevInvoices.get(0);
                    invoiceRequestsEntity.setStatus(BaseConstants.NO);
                    invoiceRequestsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    invoiceRequestsRepositoryJPA.save(invoiceRequestsEntity);
                    logActionsService.saveLog(invoiceRequestsEntity.getInvoiceRequestId(), Constant.LOG_OBJECT_TYPE.INVOICE, null, BaseConstants.YES, BaseConstants.NO);
                }
            }
        } else {
            entity = new DeclarationRegistersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }

        Utils.copyProperties(dto, entity);
        entity.setEmployeeId(empId);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        declarationRegistersRepositoryJPA.saveAndFlush(entity);
//        String logContent = I18n.getMessage("taxNumber.actionLog.status." + entity.getStatus());
//        logActionsService.saveLog(entity.getDeclarationRegisterId(), Constant.LOG_OBJECT_TYPE.DECLARATION, logContent, null, entity.getStatus());

        // nhận chứng từ thuế từ màn quyết toán thuế
        if (BaseConstants.YES.equals(dto.getRevInvoice())) {
            InvoiceRequestsDTO invoiceRequestsDTO = new InvoiceRequestsDTO();
            Utils.copyProperties(invoiceRequestsDTO, dto);
            saveInvoiceRequest(invoiceRequestsDTO, isAdmin);
        }

        if (entity.getEmployeeId().equals(commonUtilsService.getEmpIdLogin())) {
            Map<String, String> params = new HashMap<>();
            params.put("methodCode", entity.getMethodCode());

            LockRegistrationsDTO lockRegistrationsDTO = new LockRegistrationsDTO();
            lockRegistrationsDTO.setYear(entity.getYear());
            lockRegistrationsDTO.setRegistrationType(Constant.DECLARATION_REGISTER);
            List<LockRegistrationsResponse> listLock = lockRegistrationsRepository.searchData(lockRegistrationsDTO);
            if (!Utils.isNullOrEmpty(listLock)) {
                params.put("year", entity.getYear().toString());
                params.put("toDate", Utils.formatDate(listLock.get(0).getToDate()));
            }
//            commonUtilsService.sendEmail(Constant.GROUP_SEND_MAIL.CONFIRM_DECLARATION, employeesEntity.getEmail(), params);
        }
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteData(Long id, boolean isAdmin) {
        Optional<DeclarationRegistersEntity> optional = declarationRegistersRepositoryJPA.findById(id);
        if (optional.isEmpty()
                || BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
                || (!commonUtilsService.getEmpIdLogin().equals(optional.get().getEmployeeId()) && !isAdmin)
                || (!commonUtilsService.getTaxValidStatusDelete().contains(optional.get().getStatus()) && !isAdmin)
        ) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        declarationRegistersRepositoryImpl.deActiveObject(DeclarationRegistersEntity.class, id);
        DeclarationRegistersEntity entity = optional.get();
        if (Constant.METHOD_CODE.SELF_SETTLEMENT.equals(entity.getMethodCode())) {
            List<InvoiceRequestsEntity> listRefEntity = declarationRegistersRepositoryImpl.findByProperties(
                    InvoiceRequestsEntity.class,
                    "employeeId", entity.getEmployeeId(),
                    "year", entity.getYear());
            if (!Utils.isNullOrEmpty(listRefEntity)) {
                InvoiceRequestsEntity invoiceRequestsEntity = listRefEntity.get(0);
                if (BaseConstants.YES.equals(invoiceRequestsEntity.getStatus())) {
                    invoiceRequestsEntity.setStatus(BaseConstants.NO);
                    invoiceRequestsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    invoiceRequestsEntity.setModifiedTime(new Date());
                    invoiceRequestsEntity.setModifiedBy(Utils.getUserNameLogin());
                    invoiceRequestsRepositoryJPA.save(invoiceRequestsEntity);
                    logActionsService.saveLog(invoiceRequestsEntity.getInvoiceRequestId(), Constant.LOG_OBJECT_TYPE.INVOICE, null, BaseConstants.YES, BaseConstants.NO);
                }
            }
        }

        logActionsService.saveLog(id, Constant.LOG_OBJECT_TYPE.DECLARATION, null, null, null);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DeclarationRegistersResponse> getDataById(Long id, boolean isAdmin) {
        Optional<DeclarationRegistersEntity> optional = declarationRegistersRepositoryJPA.findById(id);
        if (optional.isEmpty()
                || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
        ) {
            throw new RecordNotExistsException(id, DeclarationRegistersEntity.class);
        }
        DeclarationRegistersResponse dto = new DeclarationRegistersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<DeclarationRegistersEntity> getDataByProperties(DeclarationRegistersDTO dto, boolean isAdmin) {
        Long empId = isAdmin ? dto.getEmployeeId() : commonUtilsService.getEmpIdLogin();
        List<DeclarationRegistersEntity> declarationRegistersEntities = declarationRegistersRepositoryImpl.findByProperties(DeclarationRegistersEntity.class, "employeeId", empId, "year", dto.getYear(), "methodCode", dto.getMethodCode());
        return ResponseUtils.ok(declarationRegistersEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getImportTemplate() throws Exception {
        String pathTemplate = "template/import/BM_import_DK_quyet_toan_thue.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.setActiveSheet(0);
        String fileName = Utils.getFilePathExport("BM_import_DK_quyet_toan_thue.xlsx");
        dynamicExport.exportFile(fileName);
        return ResponseUtils.ok(dynamicExport, "BM_import_DK_quyet_toan_thue.xlsx");
    }

    @Override
    public ResponseEntity<Object> importProcess(MultipartFile file, HttpServletRequest req) {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_DK_quyet_toan_thue.xml");
        List<Object[]> dataList = new ArrayList<>();
        ResponseEntity<Object> resultValidate = utilsService.validateFileImport(importExcel, file, dataList);
        if (resultValidate != null) {
            return resultValidate;
        }

        List<String> listEmployeeCode = new ArrayList<>();
        for (Object[] obj : dataList) {
            listEmployeeCode.add(((String) obj[1]).toUpperCase());
        }

        List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DECLARATION_REGISTERS, Utils.getUserNameLogin());
        Map<String, EmployeeInfoResponse> mapEmployee = employeeRepositoryImpl.getMapEmpByCodes(listEmployeeCode, listOrgId);
        Map<String, DeclarationRegistersEntity> mapListRegister = declarationRegistersRepositoryImpl.getListRegisterByEmpCodes(listEmployeeCode);
//        Map<Integer, LockRegistrationsResponse> mapYear = lockRegistrationsRepository.getMapYear();
        List<DeclarationRegistersEntity> listSave = new ArrayList<>();
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
//        List<String> emailList = new ArrayList<>();
        int row = -1;
        for (Object[] obj : dataList) {
            row++;
            int col = 1;
            String employeeCode = ((String) obj[col]).trim();
            String employeeName = ((String) obj[col + 1]).trim();
            EmployeeInfoResponse employeeInfo = mapEmployee.get((employeeCode + employeeName).toLowerCase());
            if (employeeInfo == null) {
                importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.employee"), employeeCode + "-" + employeeName);
                continue;
            }

//            if (StringUtils.isNotEmpty(employeeInfo.getEmail())) {
//                emailList.add(employeeInfo.getEmail());
//            }

            col = col + 2;
            Long year = (Long) obj[col];
//            LockRegistrationsResponse lockRegistrationsResponse = mapYear.get(year.intValue());
//            if (lockRegistrationsResponse == null || currentDate.before(lockRegistrationsResponse.getFromDate()) || currentDate.after(lockRegistrationsResponse.getToDate())) {
//                importExcel.addError(row, col, I18n.getMessage("declaration.validate.import.dateRegister"), year.toString());
//                continue;
//            } else {
            DeclarationRegistersEntity entity = mapListRegister.get((employeeCode + year).toLowerCase());
            if (entity != null) {
                importExcel.addError(row, col, I18n.getMessage("declaration.validate.import.duplicate"), employeeCode);
                continue;
            }
//            }
            col++;
            String methodCode = (String) obj[col++];
            String isRev = (String) obj[col];
            Integer revInvoice;
            if (Constant.METHOD_CODE.AUTHORITY.equalsIgnoreCase(methodCode) && Constant.YES_STR.equalsIgnoreCase(isRev)) {
                importExcel.addError(row, col, I18n.getMessage("declaration.validate.import.rev"), employeeCode);
                continue;
            } else {
                revInvoice = Constant.YES_STR.equalsIgnoreCase(isRev) ? BaseConstants.YES : BaseConstants.NO;
            }
            col++;
            String note = (String) obj[col];

            DeclarationRegistersEntity registersEntity = new DeclarationRegistersEntity();
            registersEntity.setCreatedTime(currentDate);
            registersEntity.setCreatedBy(userName);
            registersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            registersEntity.setEmployeeId(employeeInfo.getEmployeeId());
            registersEntity.setYear(year.intValue());
            registersEntity.setMethodCode(methodCode);
            registersEntity.setRevInvoice(revInvoice);
            registersEntity.setNote(note);
            registersEntity.setStatus(Constant.TAX_STATUS.REGISTERED);

            listSave.add(registersEntity);
        }
        if (importExcel.hasError()) {// co loi xay ra
            return utilsService.responseErrorImportFile(importExcel, file);
        } else {// thuc hien insert vao DB
            if (!listSave.isEmpty()) {
                for (DeclarationRegistersEntity entity : listSave) {
                    declarationRegistersRepositoryJPA.saveAndFlush(entity);
                    String logContent = I18n.getMessage("taxNumber.actionLog.status." + entity.getStatus());
                    logActionsService.saveLog(entity.getDeclarationRegisterId(), Constant.LOG_OBJECT_TYPE.DECLARATION, logContent, null, entity.getStatus());

                    if (BaseConstants.YES.equals(entity.getRevInvoice())) {
                        InvoiceRequestsDTO invoiceRequestsDTO = new InvoiceRequestsDTO();
                        Utils.copyProperties(invoiceRequestsDTO, entity);
                        saveInvoiceRequest(invoiceRequestsDTO, true);
                    }
                }
                declarationRegistersRepositoryJPA.saveAll(listSave);
            }

//            commonUtilsService.sendListEmail(Constant.GROUP_SEND_MAIL.RESULT_DEPENDENT_PERSON, emailList, null);
            return ResponseUtils.ok();
        }
    }

    private void saveInvoiceRequest(InvoiceRequestsDTO dto, boolean isAdmin) {
        List<InvoiceRequestsEntity> listRevInvoices = declarationRegistersRepositoryImpl.findByProperties(
                InvoiceRequestsEntity.class,
                "employeeId", dto.getEmployeeId(),
                "year", dto.getYear());
        if (!Utils.isNullOrEmpty(listRevInvoices)) {
            dto.setInvoiceRequestId(listRevInvoices.get(0).getInvoiceRequestId());
        }
        invoiceRequestsService.saveData(dto, isAdmin);
    }

    @Override
    @Transactional
    public void autoRegister() {
        Date currentDate = new Date();
        List<LockRegistrationsResponse> listLockReg = lockRegistrationsRepository.getListLockRegistrationByRegisterDate(currentDate);
        if (!Utils.isNullOrEmpty(listLockReg)) {
            for (LockRegistrationsResponse object : listLockReg) {
                if (object.getToDate() != null && Utils.daysBetween(object.getToDate(), currentDate) == 0) {// vao ngay cuoi cua ky dang ky thi moi thuc hien dang ky tu dong
                    List<DeclarationRegistersEntity> listRegister = declarationRegistersRepositoryImpl.findEmployeeAutoDeclarationRegister(currentDate, object.getYear());
                    if (!Utils.isNullOrEmpty(listRegister)) {
                        for (DeclarationRegistersEntity entity : listRegister) {
                            entity.setYear(object.getYear());
                            entity.setMethodCode(Constant.METHOD_CODE.SELF_SETTLEMENT);
                            entity.setNote(I18n.getMessage("declaration.cron.autoRegister.note"));
                            entity.setCreatedBy("cronTab");
                            entity.setCreatedTime(currentDate);
                            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                            entity.setStatus(Constant.TAX_STATUS.REGISTERED);
                        }
                        declarationRegistersRepositoryJPA.saveAll(listRegister);
                        log.info("schedule.cron.declaration.autoRegister|insert success " + listRegister.size() + " records");
                    }
                    // cap nhat trang thai la tu quyet toan neu nhan vien da dang ky uy quyen truoc do
                    List<DeclarationRegistersEntity> listUpdateRegister = declarationRegistersRepositoryImpl.findUpdateDeclarationRegister(currentDate, object.getYear());
                    if (!Utils.isNullOrEmpty(listUpdateRegister)) {
                        for (DeclarationRegistersEntity entity : listUpdateRegister) {
                            entity.setMethodCode(Constant.METHOD_CODE.SELF_SETTLEMENT);
                            entity.setModifiedTime(currentDate);
                            entity.setModifiedBy("cronTab");
                            entity.setNote(I18n.getMessage("declaration.cron.autoRegister.note"));
                        }
                        declarationRegistersRepositoryJPA.saveAll(listUpdateRegister);
                        log.info("schedule.cron.declaration.autoRegister|update success " + listUpdateRegister.size() + " records");
                    }
                }
            }
        }
    }
}
