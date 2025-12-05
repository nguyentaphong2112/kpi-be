
/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.feign.clients.HrmClient;
import vn.hbtplus.tax.personal.models.dto.EmployeeInfosDTO;
import vn.hbtplus.tax.personal.models.dto.PersonalIdentitiesDTO;
import vn.hbtplus.tax.personal.models.request.*;
import vn.hbtplus.tax.personal.models.response.TaxNumberRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.HrEmployeesEntity;
import vn.hbtplus.tax.personal.repositories.entity.LogActionsEntity;
import vn.hbtplus.tax.personal.repositories.entity.TaxNumberRegistersEntity;
import vn.hbtplus.tax.personal.repositories.impl.TaxNumberRegistersRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.jpa.LogActionsRepositoryJPA;
import vn.hbtplus.tax.personal.repositories.jpa.TaxNumberRegistersRepositoryJPA;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.FileService;
import vn.hbtplus.tax.personal.services.LogActionsService;
import vn.hbtplus.tax.personal.services.TaxNumberRegistersService;
import vn.hbtplus.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.security.SignatureException;
import java.util.*;

/**
 * Lop impl service ung voi bang PTX_TAX_NUMBER_REGISTERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxNumberRegistersServiceImpl implements TaxNumberRegistersService {

    private final TaxNumberRegistersRepositoryImpl taxNumberRegistersImpl;
    private final TaxNumberRegistersRepositoryJPA taxNumberRegistersJPA;
    private final CommonUtilsService commonUtilsService;
    private final FileService fileService;
    private final AttachmentService attachmentService;
    private final LogActionsService logActionsService;
    private final AuthorizationService authorizationService;
    private final LogActionsRepositoryJPA logActionsRepositoryJPA;
    private final HrmClient hrmClient;
    private final UtilsService utilsService;
    private final HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TaxNumberRegistersResponse> searchData(TaxNumberRegistersDTO dto) throws SignatureException {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());

        if (dto.getEmployeeId() == null || dto.getEmployeeId() <= 0L) {//danh cho quan tri
            long startTime = System.currentTimeMillis();
            log.info("TaxNumberRegistersServiceImpl|hasPermissionOrg|duration={}", System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();

            AdminSearchDTO searchDTO = new AdminSearchDTO();
            Utils.copyProperties(dto, searchDTO);
            log.info("TaxNumberRegistersServiceImpl|copyProperties|duration={}", System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();

            BaseDataTableDto<TaxNumberRegistersResponse> result = taxNumberRegistersImpl.searchAdminRegister(searchDTO);
            log.info("TaxNumberRegistersServiceImpl|searchAdminRegister|duration={}", System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();
            for (TaxNumberRegistersResponse taxNumberRegistersResponse : result.getListData()) {
                taxNumberRegistersResponse.setAttachFileList(attachmentService.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.PTX_TAX_NUMBER_REGISTERS, Constant.DOCUMENTS.TAX_NUMBER, taxNumberRegistersResponse.getTaxNumberRegisterId()));
            }
            log.info("TaxNumberRegistersServiceImpl|getListFileByObjId|duration={}", System.currentTimeMillis() - startTime);
            return ResponseUtils.ok(result);
        } else {
            return ResponseUtils.ok(taxNumberRegistersImpl.searchPersonalRegister(dto));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveData(TaxNumberRegistersDTO dto, List<MultipartFile> files, boolean isAdmin) {
        // validate tinh/huyen/xa dau vao
        boolean isTaxCreate = StringUtils.equalsIgnoreCase(Constant.REG_TYPE_NUMBER.TAX_CREATE.toString(), dto.getRegType());

        // validate rang buoc du lieu
        Long empId = isAdmin ? dto.getEmployeeId() : commonUtilsService.getEmpIdLogin();
        HrEmployeesEntity employeesEntity = taxNumberRegistersImpl.get(HrEmployeesEntity.class, empId);
        if (employeesEntity == null) {
            throw new BaseAppException("BAD_REQUEST", I18n.getMessage("global.validate.employee.invalid"));
        }

        if (Constant.REG_TYPE_NUMBER.TAX_CREATE.toString().equals(dto.getRegType())) {
            if (!Utils.isNullOrEmpty(employeesEntity.getTaxNo())) {
                throw new BaseAppException("BAD_REQUEST", I18n.getMessage("taxNumber.validate.create.invalidRecord"));
            }

            int countRegister = taxNumberRegistersImpl.countRecordRegister(dto.getTaxNumberRegisterId(), empId, Constant.REG_TYPE.TAX_CREATE, commonUtilsService.getTaxStatusProcess());
            if (countRegister > 0) {
                throw new BaseAppException("BAD_REQUEST", I18n.getMessage("taxNumber.validate.create.invalidRecord"));
            }
        } else {
            int countRegister = taxNumberRegistersImpl.countRecordRegister(dto.getTaxNumberRegisterId(), empId, Constant.REG_TYPE.TAX_CHANGE, commonUtilsService.getTaxStatusProcess());
            if (countRegister > 0) {
                throw new BaseAppException("BAD_REQUEST", I18n.getMessage("taxNumber.validate.changeRegister.invalidRecord"));
            }

            if (Utils.isNullOrEmpty(employeesEntity.getTaxNo())) {
                throw new BaseAppException("BAD_REQUEST", I18n.getMessage("taxNumber.validate.change.invalidRecord"));
            }
        }

        TaxNumberRegistersEntity entity;
        if (dto.getTaxNumberRegisterId() != null && dto.getTaxNumberRegisterId() > 0L) {
            entity = taxNumberRegistersJPA.getById(dto.getTaxNumberRegisterId());
//            if ((!empId.equals(entity.getEmployeeId()) && !isAdmin)
//                    || !commonUtilsService.getTaxValidStatusUpdate(isAdmin).contains(entity.getStatus())
//            ) {
//                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
//            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new TaxNumberRegistersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }

        if (isTaxCreate) {
            TaxNumberNewRegistersDTO newDTO = new TaxNumberNewRegistersDTO();
            Utils.copyProperties(dto, newDTO);
            Utils.copyProperties(newDTO, entity);
            entity.setRegType(Constant.REG_TYPE.TAX_CREATE);
        } else {
            TaxNumberChangeRegistersDTO changeDTO = new TaxNumberChangeRegistersDTO();
            Utils.copyProperties(dto, changeDTO);
            Utils.copyProperties(changeDTO, entity);
            entity.setTaxNo(employeesEntity.getTaxNo());
            entity.setRegType(Constant.REG_TYPE.TAX_CHANGE);
        }

        entity.setEmployeeId(empId);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        if (isAdmin) {
            if (Utils.isNullObject(dto.getTaxNumberRegisterId())) {
                String functionCode = isTaxCreate ? Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_REGISTER : Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_CHANGE;
                boolean hasPermission = authorizationService.hasPermissionWithOrg(empId, Scope.APPROVE, functionCode);
                if (hasPermission) {
                    entity.setStatus(Constant.TAX_STATUS.ACCOUNTANT_PROCESSING);
                } else {
                    entity.setStatus(Constant.TAX_STATUS.WAITING_APPROVAL);
                }
            }
        } else {
            if (Constant.TAX_STATUS.DRAFT.equals(dto.getStatus())) {
                entity.setStatus(Constant.TAX_STATUS.DRAFT);
            } else {
                entity.setStatus(Constant.TAX_STATUS.WAITING_APPROVAL);
            }
        }
        taxNumberRegistersJPA.saveAndFlush(entity);

//        fileService.deActiveFile(dto.getDocIdsDelete(), Constant.DOCUMENTS.TAX_NUMBER, Constant.ATTACHMENT.TABLE_NAMES.PTX_TAX_NUMBER_REGISTERS);
        fileService.uploadFiles(files, entity.getTaxNumberRegisterId(), Constant.ATTACHMENT.TABLE_NAMES.PTX_TAX_NUMBER_REGISTERS, Constant.DOCUMENTS.TAX_NUMBER, Constant.ATTACHMENT.MODULE);
//        String logContent = I18n.getMessage("taxNumber.actionLog.status." + entity.getStatus());
//        logActionsService.saveLog(entity.getTaxNumberRegisterId(), Constant.LOG_OBJECT_TYPE.TAX_NUMBER, logContent, null, entity.getStatus());
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteData(Long id, boolean isAdmin) {
        Optional<TaxNumberRegistersEntity> optional = taxNumberRegistersJPA.findById(id);
        if (optional.isEmpty()
                || BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
                || (!commonUtilsService.getEmpIdLogin().equals(optional.get().getEmployeeId()) && !isAdmin)
                || (!commonUtilsService.getTaxValidStatusDelete().contains(optional.get().getStatus()) && !isAdmin)
        ) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        taxNumberRegistersImpl.deActiveObject(TaxNumberRegistersEntity.class, id);
        logActionsService.saveLog(id, Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, null, null);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TaxNumberRegistersResponse> getDataById(Long id, boolean isAdmin) throws SignatureException {
        Optional<TaxNumberRegistersEntity> optional = taxNumberRegistersJPA.findById(id);
        if (optional.isEmpty()
                || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
        ) {
            throw new BaseAppException("BAD_REQUEST");
        }
        TaxNumberRegistersResponse dto = new TaxNumberRegistersResponse();
        Utils.copyProperties(optional.get(), dto);
        dto.setAttachFileList(attachmentService.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.PTX_TAX_NUMBER_REGISTERS, Constant.DOCUMENTS.TAX_NUMBER, dto.getTaxNumberRegisterId()));
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> updateWorkFLow(Long id, Integer status, boolean isAdmin) {
        TaxNumberRegistersEntity entity = (TaxNumberRegistersEntity) commonUtilsService.validateWorkFlow(TaxNumberRegistersEntity.class, id, status, isAdmin);
        logActionsService.saveLog(entity.getTaxNumberRegisterId(), Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, entity.getStatus(), status);
        entity.setStatus(status);
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        taxNumberRegistersJPA.save(entity);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportNewRegister(AdminSearchDTO dto) throws Exception {
        List<Map<String, Object>> listEmployee = taxNumberRegistersImpl.getListNewRegister(dto);
        if (listEmployee == null || listEmployee.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        String pathTemplate = "template/export/taxNumber/BM_Xuat_DS_DangKyCapMoiMST.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listEmployee);
        String fileName = Utils.getFilePathExport("DS_DangKyCapMoiMST.xlsx");
        dynamicExport.exportFile(fileName);
        return ResponseUtils.ok(dynamicExport, "DS_DangKyCapMoiMST.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportChangeRegister(AdminSearchDTO dto) throws Exception {
        List<Map<String, Object>> listEmployee = taxNumberRegistersImpl.getListChangeRegister(dto);
        if (listEmployee == null || listEmployee.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        String pathTemplate = "template/export/taxNumber/BM_Xuat_DS_ThayDoi_MST.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listEmployee);
        String fileName = Utils.getFilePathExport("BM_Xuat_DS_ThayDoi_MST.xlsx");
        dynamicExport.exportFile(fileName);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_ThayDoi_MST.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity<Object> approveByList(List<Long> listId) {
        processApproveOrReject(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED, listId, null);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> approveAll(AdminSearchDTO dto) {
        List<Long> listId = taxNumberRegistersImpl.getListIdApproveByForm(dto, Constant.TAX_STATUS.WAITING_APPROVAL);
        if (Utils.isNullOrEmpty(listId)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        taxNumberRegistersImpl.updateStatusByListId(listId, Constant.TAX_STATUS.ACCOUNTANT_RECEIVED, Constant.TAX_STATUS.WAITING_APPROVAL);
        List<LogActionsEntity> listSaveLog = new ArrayList<>();
        for (Long id : listId) {
            listSaveLog.add(logActionsService.getLogAction(id, Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, Constant.TAX_STATUS.WAITING_APPROVAL, Constant.TAX_STATUS.ACCOUNTANT_RECEIVED));
        }
        taxNumberRegistersImpl.insertBatch(LogActionsEntity.class, listSaveLog, Utils.getUserNameLogin());
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> rejectByList(RejectDTO dto) {
        List<Long> listId = dto.getListId();
        processApproveOrReject(Constant.TAX_STATUS.ACCOUNTANT_REJECT, listId, dto.getRejectReason());
        return ResponseUtils.ok();
    }

    private void processApproveOrReject(Integer statusInput, List<Long> listId, String rejectReason) {
        if (Utils.isNullObject(listId)) {
            throw new BaseAppException("BAD_REQUEST");
        }

        List<TaxNumberRegistersEntity> listRegisterEntity = new ArrayList<>();
        for (Long id : listId) {
            TaxNumberRegistersEntity entity = (TaxNumberRegistersEntity) commonUtilsService.validateWorkFlow(TaxNumberRegistersEntity.class, id, statusInput, true);
            listRegisterEntity.add(entity);
        }

        List<String> listEmailChangeRegister = new ArrayList<>();
        List<String> listEmailNewRegister = new ArrayList<>();
        for (TaxNumberRegistersEntity entity : listRegisterEntity) {
            logActionsService.saveLog(entity.getTaxNumberRegisterId(), Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, entity.getStatus(), statusInput);
            entity.setStatus(statusInput);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            if (Constant.TAX_STATUS.ACCOUNTANT_REJECT.equals(statusInput)) {
                entity.setRejectReason(rejectReason);
                HrEmployeesEntity employeesEntity = taxNumberRegistersImpl.get(HrEmployeesEntity.class, entity.getEmployeeId());
                if (entity.getRegType().equals(Constant.REG_TYPE.TAX_CREATE)) {
                    listEmailNewRegister.add(employeesEntity.getEmail());
                } else {
                    listEmailChangeRegister.add(employeesEntity.getEmail());
                }
            } else {
                entity.setRejectReason(null);
            }
            taxNumberRegistersJPA.save(entity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<TaxNumberRegistersEntity> getRecentRegister(Long employeeId, boolean isAdmin) {
        if (!isAdmin) {
            employeeId = commonUtilsService.getEmpIdLogin();
        }
        List<TaxNumberRegistersEntity> listEntity = taxNumberRegistersImpl.findByProperties(TaxNumberRegistersEntity.class,
                "employeeId", employeeId,
                "regType", Constant.REG_TYPE.TAX_CREATE,
                "status", Constant.TAX_STATUS.TAX_APPROVAL);
        return ResponseUtils.ok(listEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getImportTemplateNewRegisterResult() throws Exception {
        String pathTemplate = "template/import/BM_import_ket_qua_DKM_MST.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport("BM_import_ket_qua_DKM_MST.xlsx");
        dynamicExport.exportFile(fileName);
        return ResponseUtils.ok(dynamicExport, "BM_import_ket_qua_DKM_MST.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity<Object> importNewRegisterResult(MultipartFile file, HttpServletRequest req) {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_ket_qua_DKM_MST.xml");
        List<Object[]> dataList = new ArrayList<>();
        ResponseEntity<Object> resultValidate = utilsService.validateFileImport(importExcel, file, dataList);
        if (resultValidate != null) {
            return resultValidate;
        }

        List<String> listEmployeeCode = new ArrayList<>();
        for (Object[] obj : dataList) {
            listEmployeeCode.add(((String) obj[1]).toUpperCase());
        }

        Map<String, TaxNumberRegistersEntity> mapEmployee = taxNumberRegistersImpl.getListRegisterByEmpCodes(Constant.REG_TYPE.TAX_CREATE, listEmployeeCode, commonUtilsService.getValidStatusImportResult());
        List<TaxNumberRegistersEntity> listSave = new ArrayList<>();
        List<HrEmployeesEntity> listUpdateEmployee = new ArrayList<>();
        List<LogActionsEntity> listSaveLog = new ArrayList<>();
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        int row = -1;
        Map<String, Map<String, String>> mapEmailReject = new HashMap<>();
        String statusReject = I18n.getMessage("tax.status." + Constant.TAX_STATUS.TAX_REJECT);
        List<String> listMailApprove = new ArrayList<>();
        for (Object[] obj : dataList) {
            row++;
            int col = 1;
            String employeeCode = ((String) obj[col]).trim();
            String employeeName = ((String) obj[col + 1]).trim();
            TaxNumberRegistersEntity entity = mapEmployee.get((employeeCode + employeeName).toLowerCase());
            if (entity == null) {
                importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.employee"), employeeCode + "-" + employeeName);
                continue;
            }

            col = col + 2;
            String strStatus = ((String) obj[col++]).trim();
            String rejectReason;
            if (Constant.TAX_STATUS_STR.TAX_APPROVAL.equalsIgnoreCase(strStatus)) {
                listSaveLog.add(logActionsService.getLogAction(entity.getTaxNumberRegisterId(), Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, entity.getStatus(), Constant.TAX_STATUS.TAX_APPROVAL));
                entity.setStatus(Constant.TAX_STATUS.TAX_APPROVAL);
                String taxNo = ((String) obj[col++]);
                String taxPlace = ((String) obj[col++]);
                Date taxDate = ((Date) obj[col]);
                if (!Utils.isValidTaxNo(taxNo)) {
                    importExcel.addError(row, col, I18n.getMessage("taxNumber.invalid"), taxNo);
                }

                if (Utils.isNullOrEmpty(taxNo) || Utils.isNullOrEmpty(taxPlace) || taxDate == null) {
                    importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.required.taxInfo"), null);
                } else {
                    entity.setTaxNo(taxNo.trim());
                    entity.setTaxPlace(taxPlace.trim());
                    entity.setTaxDate(taxDate);
                    HrEmployeesEntity employeesEntity = new HrEmployeesEntity();
                    employeesEntity.setEmployeeId(entity.getEmployeeId());
                    employeesEntity.setTaxNo(entity.getTaxNo());
                    employeesEntity.setStrTaxDate(Utils.formatDate(entity.getTaxDate()));
//                    employeesEntity.setTaxPlace(entity.getTaxPlace());
                    employeesEntity.setModifiedBy(userName);
                    listUpdateEmployee.add(employeesEntity);
                }
                if (StringUtils.isNotEmpty(entity.getEmailCompany())) {
                    listMailApprove.add(entity.getEmailCompany());
                }
            } else {
                col = col + 3;
                rejectReason = ((String) obj[col]);
                if (Utils.isNullOrEmpty(rejectReason)) {
                    importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.required.rejectReason"), null);
                } else {
                    listSaveLog.add(logActionsService.getLogAction(entity.getTaxNumberRegisterId(), Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, entity.getStatus(), Constant.TAX_STATUS.TAX_REJECT));
                    entity.setStatus(Constant.TAX_STATUS.TAX_REJECT);
                    entity.setRejectReason(rejectReason.trim());
                }
                if (StringUtils.isNotEmpty(entity.getEmailCompany())) {
                    Map<String, String> params = new HashMap<>();
                    params.put("status", statusReject);
                    params.put("reason", rejectReason);
                    mapEmailReject.put(entity.getEmailCompany(), params);
                }
            }
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userName);
            listSave.add(entity);


        }
        if (importExcel.hasError()) {// co loi xay ra
            return utilsService.responseErrorImportFile(importExcel, file);
        } else { // thuc hien insert vao DB
            if (!listSave.isEmpty()) {
                taxNumberRegistersJPA.saveAll(listSave);
                logActionsRepositoryJPA.saveAll(listSaveLog);
            }

            if (!listUpdateEmployee.isEmpty()) {
                ResponseEntity<Object> resultUpdateEmployeeTaxInfo = hrmClient.updateEmployeeTaxInfo(Utils.getRequestHeader(request), listUpdateEmployee);
                log.info("importNewRegisterResult|resultUpdateEmployeeTaxInfo={}", resultUpdateEmployeeTaxInfo.toString());
            }

            return ResponseUtils.ok();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getImportTemplateChangeRegisterResult() throws Exception {
        String pathTemplate = "template/import/BM_import_ket_qua_DK_thay_doi_thong_tin_MST.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        return ResponseUtils.ok(dynamicExport, "BM_import_ket_qua_DK_thay_doi_thong_tin_MST.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity<Object> importChangeRegisterResult(MultipartFile file, HttpServletRequest req) {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_ket_qua_DK_thay_doi_thong_tin_MST.xml");
        List<Object[]> dataList = new ArrayList<>();
        ResponseEntity<Object> resultValidate = utilsService.validateFileImport(importExcel, file, dataList);
        if (resultValidate != null) {
            return resultValidate;
        }

        List<String> listEmployeeCode = new ArrayList<>();
        for (Object[] obj : dataList) {
            listEmployeeCode.add(((String) obj[1]).toUpperCase());
        }

        Map<String, TaxNumberRegistersEntity> mapEmployee = taxNumberRegistersImpl.getListRegisterByEmpCodes(Constant.REG_TYPE.TAX_CHANGE, listEmployeeCode, commonUtilsService.getValidStatusImportResult());
        List<TaxNumberRegistersEntity> listSave = new ArrayList<>();
        List<LogActionsEntity> listSaveLog = new ArrayList<>();
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        int row = -1;
        List<HrEmployeesEntity> listUpdateEmployee = new ArrayList<>();
        Map<String, Map<String, String>> mapEmail = new HashMap<>();
        String statusReject = I18n.getMessage("tax.status." + Constant.TAX_STATUS.TAX_REJECT);

        Map<String, String> approveParams = new HashMap<>();
        approveParams.put("status", I18n.getMessage("tax.status." + Constant.TAX_STATUS.TAX_APPROVAL));
        approveParams.put("reason", "");
        List<String> listMailApprove = new ArrayList<>();
        for (Object[] obj : dataList) {
            row++;
            int col = 1;
            String employeeCode = ((String) obj[col]).trim();
            String employeeName = ((String) obj[col + 1]).trim();
            TaxNumberRegistersEntity entity = mapEmployee.get((employeeCode + employeeName).toLowerCase());
            if (entity == null) {
                importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.employee"), employeeCode + "-" + employeeName);
                continue;
            }

            col = col + 2;
            String strStatus = ((String) obj[col]).trim();
            if (Constant.TAX_STATUS_STR.TAX_APPROVAL.equalsIgnoreCase(strStatus)) {
                listSaveLog.add(logActionsService.getLogAction(entity.getTaxNumberRegisterId(), Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, entity.getStatus(), Constant.TAX_STATUS.TAX_APPROVAL));
                entity.setStatus(Constant.TAX_STATUS.TAX_APPROVAL);
                if (StringUtils.isNotEmpty(entity.getEmailCompany())) {
                    listMailApprove.add(entity.getEmailCompany());
                }
            } else {
                col++;
                String rejectReason = ((String) obj[col]);
                if (Utils.isNullOrEmpty(rejectReason)) {
                    importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.required.rejectReason"), null);
                } else {
                    listSaveLog.add(logActionsService.getLogAction(entity.getTaxNumberRegisterId(), Constant.LOG_OBJECT_TYPE.TAX_NUMBER, null, entity.getStatus(), Constant.TAX_STATUS.TAX_REJECT));
                    entity.setStatus(Constant.TAX_STATUS.TAX_REJECT);
                    entity.setRejectReason(rejectReason.trim());
                }
                if (StringUtils.isNotEmpty(entity.getEmailCompany())) {
                    Map<String, String> params = new HashMap<>();
                    params.put("status", statusReject);
                    params.put("reason", rejectReason);
                    mapEmail.put(entity.getEmailCompany(), params);
                }
            }
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userName);
            listSave.add(entity);
            //thay doi noi cap MST
            if (!Utils.isNullOrEmpty(entity.getTaxPlace())) {
                HrEmployeesEntity employeesEntity = new HrEmployeesEntity();
                employeesEntity.setEmployeeId(entity.getEmployeeId());
//                employeesEntity.setTaxPlace(entity.getTaxPlace());
                employeesEntity.setModifiedBy(userName);
                listUpdateEmployee.add(employeesEntity);
            }
        }
        if (importExcel.hasError()) {// co loi xay ra
            return utilsService.responseErrorImportFile(importExcel, file);
        } else {// thuc hien insert vao DB
            if (!listSave.isEmpty()) {
                taxNumberRegistersJPA.saveAll(listSave);
                logActionsRepositoryJPA.saveAll(listSaveLog);
            }

            if (!listUpdateEmployee.isEmpty()) {
                ResponseEntity<Object> resultUpdateEmployeeTaxInfo = hrmClient.updateEmployeeTaxInfo(Utils.getRequestHeader(request), listUpdateEmployee);
                log.info("importChangeRegisterResult|resultUpdateEmployeeTaxInfo={}", resultUpdateEmployeeTaxInfo.toString());
            }

            return ResponseUtils.ok();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportRegisterByTaxOfficeTemplate(TaxNumberRegistersDTO dto) throws Exception {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        AdminSearchDTO searchDTO = new AdminSearchDTO();
        Utils.copyProperties(searchDTO, dto);

        List<Map<String, Object>> listData = taxNumberRegistersImpl.getListTaxRegisterExps(searchDTO);
        if (listData == null || listData.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        String pathTemplate = "template/export/taxNumber/BM_CQT_DS_DK_MST.xls";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listData);
        dynamicExport.setCellFormat(43, 1, 43 + (listData.size() - 2), 35, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM_CQT_DS_DK_MST.xls");
    }


    @Override
    @Transactional(readOnly = true)
    public EmployeeInfosDTO getContactInfo(Long employeeId) {
        return taxNumberRegistersImpl.getContactInfo(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalIdentitiesDTO> getPersonalIdentities(Long id) {
        return taxNumberRegistersImpl.getPersonalIdentities(id);
    }

}
