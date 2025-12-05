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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.feign.clients.HrmClient;
import vn.hbtplus.tax.personal.models.dto.FamilyRelationshipsDTO;
import vn.hbtplus.tax.personal.models.request.*;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.*;
import vn.hbtplus.tax.personal.repositories.impl.DependentRegistersRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.jpa.DependentRegistersRepositoryJPA;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.DependentRegistersService;
import vn.hbtplus.tax.personal.services.FileService;
import vn.hbtplus.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.security.SignatureException;
import java.util.*;

/**
 * Lop impl service ung voi bang PTX_DEPENDENT_REGISTERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DependentRegistersServiceImpl implements DependentRegistersService {

    private final DependentRegistersRepositoryImpl dependentRegistersRepositoryImpl;
    private final DependentRegistersRepositoryJPA dependentRegistersRepositoryJPA;
    private final CommonUtilsService commonUtilsService;
    private final FileService fileService;
    private final LogActionsServiceImpl logActionsService;
    private final AuthorizationService authorizationService;
    private final HrmClient hrmClient;
    private final AttachmentService attachmentService;
    private final UtilsService utilsService;
    private final HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DependentRegistersResponse> searchData(AdminSearchDTO dto) throws SignatureException {
        BaseDataTableDto<DependentRegistersResponse> result = dependentRegistersRepositoryImpl.searchData(dto);
//        for (DependentRegistersResponse res : result.getListData()) {
//            res.setAttachFileList(attachmentService.getAttachmentEntities(Constant.OBJECT_ATTRIBUTES.TABLE_NAMES.PTX_DEPENDENT_REGISTERS, Constant.DOCUMENTS.DEPENDENT, res.getDependentRegisterId()));
//        }
        return ResponseUtils.ok(result);
    }

    public ResponseEntity<Object> exportDataDependentRegister(AdminSearchDTO dto) {
        List<Map<String, Object>> listData = dependentRegistersRepositoryImpl.getDataDependentRegister(dto);
        if (listData.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        try {
            String pathTemplate = "template/export/dependent/BM_Xuat_DS_NguoiPhuThuoc.xlsx";
            ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
            dynamicExport.replaceKeys(listData);
            return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_NguoiPhuThuoc.xlsx");
        } catch (Exception ex) {
            log.error("[exportDataDependentRegister] has error {}", ex);
            return ResponseUtils.getResponseDataNotFound();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveData(DependentRegistersDTO dto, List<MultipartFile> files, boolean isAdmin) {
        Long empId;
        if (isAdmin) {
            empId = dto.getEmployeeId();
        } else {
            empId = commonUtilsService.getEmpIdLogin();
            dto.setEmployeeId(empId);
        }

        validateBeforeSave(dto);

        DependentRegistersEntity entity;
        if (dto.getDependentRegisterId() != null && dto.getDependentRegisterId() > 0L) {
            entity = dependentRegistersRepositoryJPA.getById(dto.getDependentRegisterId());
            if ((!empId.equals(entity.getEmployeeId()) && !isAdmin)
                    || !commonUtilsService.getTaxValidStatusUpdate(isAdmin).contains(entity.getStatus())
            ) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new DependentRegistersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }

        if (Constant.REG_TYPE_NUMBER.DEPENDENT_CREATE.toString().equals(dto.getRegType())) {
            if (!Utils.isNullOrEmpty(dto.getCodeNo())) {//neu dang ky theo giay khai sinh
                DependentNewChildRegistersDTO newDTO = new DependentNewChildRegistersDTO();
                Utils.copyProperties(newDTO, dto);
                Utils.copyProperties(entity, newDTO );
                entity.setIdNo(null);
                entity.setTaxNo(null);
            } else {
                DependentNewRegistersDTO newDTO = new DependentNewRegistersDTO();
                Utils.copyProperties(dto, newDTO);
                Utils.copyProperties(newDTO, entity );
                entity.setCodeNo(null);
                entity.setBookNo(null);
            }
            if (Utils.isNullOrEmpty(entity.getDependentPersonCode())) {
                entity.setDependentPersonCode(generatePersonCode(null));
            }
            entity.setRegType(Constant.REG_TYPE.DEPENDENT_CREATE);
            entity.setFromDate(Utils.getFirstDay(entity.getFromDate()));
            entity.setToDate(Utils.getLastDayOfMonth(entity.getToDate()));
        } else {
            DependentCancelRegistersDTO newDTO = new DependentCancelRegistersDTO();
            Utils.copyProperties(newDTO, dto);
            Utils.copyProperties(entity, newDTO );
            entity.setRegType(Constant.REG_TYPE.DEPENDENT_CANCEL);
            entity.setToDate(Utils.getLastDayOfMonth(entity.getToDate()));

            List<HrDependentPersonsEntity> listPerson = dependentRegistersRepositoryImpl.getListDependentPersonAndFamily(dto.getEmployeeId(), dto.getFamilyRelationshipId());

            if (!Utils.isNullOrEmpty(listPerson)) {
                HrDependentPersonsEntity dependentPersonsEntity = listPerson.get(0);
                if (!Utils.isNullOrEmpty(dependentPersonsEntity.getDependentPersonCode())) {
                    entity.setDependentPersonCode(dependentPersonsEntity.getDependentPersonCode());
                } else {
                    entity.setDependentPersonCode(generatePersonCode(dependentPersonsEntity.getDependentPersonId()));
                }
                entity.setFromDate(dependentPersonsEntity.getFromDate());
            }
        }

        entity.setEmployeeId(empId);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        if (isAdmin) {
            if (Utils.isNullObject(dto.getDependentRegisterId())) {
                boolean hasPermission = authorizationService.hasPermissionWithOrg(empId, Scope.APPROVE, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DEPENDENT_REGISTERS);
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
        dependentRegistersRepositoryJPA.saveAndFlush(entity);

//        fileService.deActiveFile(dto.getDocIdsDelete(), Constant.OBJECT_ATTRIBUTES.TABLE_NAMES.PTX_DEPENDENT_REGISTERS, Constant.DOCUMENTS.DEPENDENT);
//        fileService.uploadFiles(files, entity.getDependentRegisterId(), Constant.OBJECT_ATTRIBUTES.TABLE_NAMES.PTX_DEPENDENT_REGISTERS, Constant.DOCUMENTS.DEPENDENT, Constant.ATTACHMENT.MODULE);

//        String logContent = I18n.getMessage("taxNumber.actionLog.status." + entity.getStatus());
//        logActionsService.saveLog(entity.getDependentRegisterId(), Constant.LOG_OBJECT_TYPE.DEPENDENT, logContent, null, entity.getStatus());
        return ResponseUtils.ok();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteData(Long id, boolean isAdmin) {
        Optional<DependentRegistersEntity> optional = dependentRegistersRepositoryJPA.findById(id);
        if (optional.isEmpty()
                || BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
                || (!commonUtilsService.getEmpIdLogin().equals(optional.get().getEmployeeId()) && !isAdmin)
                || (!commonUtilsService.getTaxValidStatusDelete().contains(optional.get().getStatus()) && !isAdmin)
        ) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        dependentRegistersRepositoryImpl.deActiveObject(DependentRegistersEntity.class, id);
        logActionsService.saveLog(id, Constant.LOG_OBJECT_TYPE.DEPENDENT, null, null, null);
        return ResponseUtils.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DependentRegistersResponse> getDataById(Long id, boolean isAdmin) throws SignatureException {
        Optional<DependentRegistersEntity> optional = dependentRegistersRepositoryJPA.findById(id);
        if (optional.isEmpty()
                || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
        ) {
            throw new RecordNotExistsException(id, DependentRegistersEntity.class);
        }
        DependentRegistersResponse dto = new DependentRegistersResponse();
        Utils.copyProperties(optional.get(), dto);
//        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.PTX_DEPENDENT_REGISTERS, Constant.DOCUMENTS.DEPENDENT, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> updateWorkFLow(Long id, Integer status, boolean isAdmin) {
        DependentRegistersEntity entity = (DependentRegistersEntity) commonUtilsService.validateWorkFlow(DependentRegistersEntity.class, id, status, isAdmin);
        logActionsService.saveLog(entity.getDependentRegisterId(), Constant.LOG_OBJECT_TYPE.DEPENDENT, null, entity.getStatus(), status);
        entity.setStatus(status);
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        dependentRegistersRepositoryJPA.save(entity);
        return ResponseUtils.ok();

    }

    private void validateBeforeSave(DependentRegistersDTO dto) {
        //validate MST
        boolean isValidTaxNo = Utils.isValidTaxNo(dto.getTaxNo());
        if (!isValidTaxNo) {
            log.info("DependentRegistersServiceImpl|taxNo invalid");
            throw new BaseAppException("INVALID_PARAM", I18n.getMessage("taxNumber.invalid"));
        }


        Long empId = dto.getEmployeeId();
        if (Constant.REG_TYPE_NUMBER.DEPENDENT_CREATE.toString().equals(dto.getRegType())) {
            if (dto.getFromDate() == null) {
                log.info("DependentRegistersServiceImpl|fromDate is null");
                throw new BaseAppException("BAD_REQUEST", "fromDate is null");
            }

            Utils.validateDate(dto.getFromDate(), dto.getToDate());

            int countRegister = dependentRegistersRepositoryImpl.countRecordRegister(dto.getDependentRegisterId(), empId, Constant.REG_TYPE.DEPENDENT_CREATE, dto.getFamilyRelationshipId(), commonUtilsService.getTaxStatusProcess());
            if (countRegister > 0) {
                log.info("DependentRegistersServiceImpl|employeeId={}|countRecordRegister={}", empId, countRegister);
                throw new BaseAppException("INVALID_PARAM", I18n.getMessage("dependent.validate.create.invalidRecord"));
            }

            if (dependentRegistersRepositoryImpl.isConflictProcess(dto)) {
                log.info("DependentRegistersServiceImpl|isConflictProcess|employeeId={}", empId);
                throw new BaseAppException("INVALID_PARAM", I18n.getMessage("dependent.validate.create.invalidRecord"));
            }

        } else { // dang ky ket thuc giam tru
            if (dto.getToDate() == null) {
                log.info("DependentRegistersServiceImpl|employeeId={}|fromDate is null|clientMessageId=", empId);
                throw new BaseAppException("BAD_REQUEST");
            }

            int countDependentPersons = dependentRegistersRepositoryImpl.countDependentPersons(dto);
            if (countDependentPersons == 0) {
                log.info("DependentRegistersServiceImpl|employeeId={}|countDependentPersons={}", empId, countDependentPersons);
                throw new BaseAppException("INVALID_PARAM", I18n.getMessage("dependent.validate.cancel.invalidRecord"));
            }

            int countRegister = dependentRegistersRepositoryImpl.countRecordRegister(dto.getDependentRegisterId(), empId, Constant.REG_TYPE.DEPENDENT_CANCEL, dto.getFamilyRelationshipId(), commonUtilsService.getTaxStatusProcess());
            if (countRegister > 0) {
                log.info("DependentRegistersServiceImpl|employeeId={}|countRecordRegister={}", empId, countRegister);
                throw new BaseAppException("INVALID_PARAM", I18n.getMessage("dependent.validate.cancel.invalidRecord"));
            }
        }

        HrFamilyRelationshipsEntity familyRelationshipsEntity = dependentRegistersRepositoryImpl.get(HrFamilyRelationshipsEntity.class, dto.getFamilyRelationshipId());
        if (familyRelationshipsEntity == null || !familyRelationshipsEntity.getEmployeeId().equals(empId)) {
            log.info("DependentRegistersServiceImpl|employeeId={}|familyRelationshipsEntity invalid|clientMessageId=", empId);
            throw new BaseAppException("BAD_REQUEST");
        }
    }

    @Override
    public ResponseEntity<Object> approveByList(List<Long> listId) {
        processApproveOrReject(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED, listId, null);
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> approveAll(AdminSearchDTO dto) {
        List<Long> listId = dependentRegistersRepositoryImpl.getListIdApproveByForm(dto, Constant.TAX_STATUS.WAITING_APPROVAL);
        if (Utils.isNullOrEmpty(listId)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dependentRegistersRepositoryImpl.updateStatusByListId(listId, Constant.TAX_STATUS.ACCOUNTANT_RECEIVED, Constant.TAX_STATUS.WAITING_APPROVAL);
        List<LogActionsEntity> listSaveLog = new ArrayList<>();
        for (Long id : listId) {
            listSaveLog.add(logActionsService.getLogAction(id, Constant.LOG_OBJECT_TYPE.DEPENDENT, null, Constant.TAX_STATUS.WAITING_APPROVAL, Constant.TAX_STATUS.ACCOUNTANT_RECEIVED));
        }
        dependentRegistersRepositoryImpl.insertBatch(LogActionsEntity.class, listSaveLog, Utils.getUserNameLogin());
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> rejectByList(RejectDTO dto) {
        List<Long> listId = dto.getListId();
        processApproveOrReject(Constant.TAX_STATUS.ACCOUNTANT_REJECT, listId, dto.getRejectReason());
        return ResponseUtils.ok();
    }

    private void processApproveOrReject(Integer statusInput, List<Long> listId, String rejectReason) {
        if (Utils.isNullObject(listId)) {
            throw new BaseAppException("BAD_REQUEST");
        }

        List<DependentRegistersEntity> listRegisterEntity = new ArrayList<>();
        for (Long id : listId) {
            DependentRegistersEntity entity = (DependentRegistersEntity) commonUtilsService.validateWorkFlow(DependentRegistersEntity.class, id, statusInput, true);
            listRegisterEntity.add(entity);
        }

        List<String> listEmail = new ArrayList<>();
        for (DependentRegistersEntity entity : listRegisterEntity) {
            logActionsService.saveLog(entity.getDependentRegisterId(), Constant.LOG_OBJECT_TYPE.DEPENDENT, null, entity.getStatus(), statusInput);
            entity.setStatus(statusInput);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            if (Constant.TAX_STATUS.ACCOUNTANT_REJECT.equals(statusInput)) {
                entity.setRejectReason(rejectReason);
                HrEmployeesEntity employeesEntity = dependentRegistersRepositoryImpl.get(HrEmployeesEntity.class, entity.getEmployeeId());
                listEmail.add(employeesEntity.getEmail());
            } else {
                entity.setRejectReason(null);
            }
            dependentRegistersRepositoryJPA.save(entity);
        }
        Map<String, String> params = new HashMap<>();
        params.put("status", I18n.getMessage("tax.status." + statusInput));
        params.put("reason", Utils.NVL(rejectReason));
//        commonUtilsService.sendListEmail(Constant.GROUP_SEND_MAIL.RESULT_DEPENDENT_PERSON, listEmail, params);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getImportTemplateRegisterResult() throws Exception {
        String pathTemplate = "template/import/BM_import_ket_qua_DKM_GTGC.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.setActiveSheet(0);
        String fileName = Utils.getFilePathExport("BM_import_ket_qua_DKM_GTGC.xlsx");
        dynamicExport.exportFile(fileName);
        return ResponseUtils.ok(dynamicExport, "BM_import_ket_qua_DKM_GTGC.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity<Object> importRegisterResult(MultipartFile file, HttpServletRequest req) {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_ket_qua_DKM_GTGC.xml");
        List<Object[]> dataList = new ArrayList<>();
        ResponseEntity<Object> resultValidate = utilsService.validateFileImport(importExcel, file, dataList);
        if (resultValidate != null) {
            return resultValidate;
        }

        List<String> listDependentCode = new ArrayList<>();
        List<String> listEmployeeCode = new ArrayList<>();
        for (Object[] obj : dataList) {
            listDependentCode.add(((String) obj[1]).toUpperCase());
        }

        Map<String, DependentRegistersEntity> mapEntity = dependentRegistersRepositoryImpl.getListRegisterByDependentCodes(null, listDependentCode, commonUtilsService.getValidStatusImportResult());
        for (DependentRegistersEntity dependentRes : mapEntity.values()) {
            listEmployeeCode.add(dependentRes.getEmployeeCode());
        }
        Map<String, HrDependentPersonsEntity> mapDependentPersonsEntity = dependentRegistersRepositoryImpl.getListDependentPersonsByEmpCodes(listEmployeeCode);
        List<DependentRegistersEntity> listSave = new ArrayList<>();
        List<HrDependentPersonsEntity> listSaveDependentPersons = new ArrayList<>();
        List<LogActionsEntity> listSaveLog = new ArrayList<>();
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        List<String> emailListAccept = new ArrayList<>();
        Map<String, Map<String, String>> mapEmailReject = new HashMap<>();
        String statusReject = I18n.getMessage("tax.status." + Constant.TAX_STATUS.TAX_REJECT);
        int row = -1;
        for (Object[] obj : dataList) {
            row++;
            int col = 1;
            String dependentCode = ((String) obj[col]).trim();
            DependentRegistersEntity entity = mapEntity.get((dependentCode).toLowerCase());
            if (entity == null) {
                importExcel.addError(row, col, I18n.getMessage("dependent.validate.import.relationType"), dependentCode);
                continue;
            }
            col++;

            String strStatus = ((String) obj[col++]).trim();
            if (Constant.TAX_STATUS_STR.TAX_APPROVAL.equalsIgnoreCase(strStatus)) {
                listSaveLog.add(logActionsService.getLogAction(entity.getDependentRegisterId(), Constant.LOG_OBJECT_TYPE.DEPENDENT, null, entity.getStatus(), Constant.TAX_STATUS.TAX_APPROVAL));
                entity.setStatus(Constant.TAX_STATUS.TAX_APPROVAL);
                if (entity.getRegType().equals(Constant.REG_TYPE.DEPENDENT_CREATE)) {
                    String taxNo = ((String) obj[col]);
                    if (Utils.isNullOrEmpty(taxNo)) {
                        importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.required.taxNo"), null);
                    } else {
                        boolean isValidTaxNo = Utils.isValidTaxNo(taxNo);
                        if (!isValidTaxNo) {
                            importExcel.addError(row, col, I18n.getMessage("taxNumber.invalid"), taxNo);
                        }
                        entity.setTaxNo(taxNo.trim());
                    }
                    HrDependentPersonsEntity dependentPersonsEntity = mapDependentPersonsEntity.get(entity.getEmployeeCode().toUpperCase() + entity.getFamilyRelationshipId());
                    if (dependentPersonsEntity != null
                            && Utils.isConflictDate(dependentPersonsEntity.getFromDate(), dependentPersonsEntity.getToDate(), entity.getFromDate(), entity.getToDate())) {
                        importExcel.addError(row, col, I18n.getMessage("dependent.validate.import.duplicateDependentPerson"), null);
                    } else {
                        dependentPersonsEntity = new HrDependentPersonsEntity();
                        Utils.copyProperties(dependentPersonsEntity, entity);
                        dependentPersonsEntity.setCreatedBy(userName);
                        dependentPersonsEntity.setStrFromDate(Utils.formatDate(dependentPersonsEntity.getFromDate()));
                        dependentPersonsEntity.setStrToDate(Utils.formatDate(dependentPersonsEntity.getToDate()));
                        dependentPersonsEntity.setModifiedBy(null);
                        dependentPersonsEntity.setModifiedTime(null);
                        dependentPersonsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                        dependentPersonsEntity.setPersonalId(entity.getIdNo());
                        listSaveDependentPersons.add(dependentPersonsEntity);
                    }
                } else {// neu la dang ky giam
                    HrDependentPersonsEntity dependentPersonsEntity = mapDependentPersonsEntity.get(entity.getEmployeeCode().toUpperCase() + entity.getFamilyRelationshipId());
                    if (dependentPersonsEntity == null) {
                        importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.dependentCodeError"), null);
                    } else {
                        HrDependentPersonsEntity entityUpdate = new HrDependentPersonsEntity();
                        entityUpdate.setDependentPersonId(dependentPersonsEntity.getDependentPersonId());
                        entityUpdate.setStrToDate(Utils.formatDate(entity.getToDate()));
                        entityUpdate.setModifiedBy(userName);
                        entityUpdate.setFamilyRelationshipId(dependentPersonsEntity.getFamilyRelationshipId());
                        entityUpdate.setTaxNo(dependentPersonsEntity.getTaxNo());
                        entityUpdate.setDependentPersonCode(entity.getDependentPersonCode());
                        listSaveDependentPersons.add(entityUpdate);
                    }
                }
                if (StringUtils.isNotEmpty(entity.getEmail())) {
                    emailListAccept.add(entity.getEmail());
                }
            } else {
                col++;
                String rejectReason = ((String) obj[col]);
                if (Utils.isNullOrEmpty(rejectReason)) {
                    importExcel.addError(row, col, I18n.getMessage("taxNumber.validate.import.required.rejectReason"), null);
                } else {
                    listSaveLog.add(logActionsService.getLogAction(entity.getDependentRegisterId(), Constant.LOG_OBJECT_TYPE.DEPENDENT, null, entity.getStatus(), Constant.TAX_STATUS.TAX_REJECT));
                    entity.setRejectReason(rejectReason.trim());
                    entity.setStatus(Constant.TAX_STATUS.TAX_REJECT);
                }

                if (StringUtils.isNotEmpty(entity.getEmail())) {
                    Map<String, String> params = new HashMap<>();
                    params.put("status", statusReject);
                    params.put("reason", rejectReason);
                    mapEmailReject.put(entity.getEmail(), params);
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
                dependentRegistersRepositoryJPA.saveAll(listSave);
                dependentRegistersRepositoryImpl.insertBatch(LogActionsEntity.class, listSaveLog, Utils.getUserNameLogin());
            }

            if (!listSaveDependentPersons.isEmpty()) {
                ResponseEntity<Object> resultSaveDependentPersons = hrmClient.saveDependentPersons(Utils.getRequestHeader(request), listSaveDependentPersons);
                log.info("importNewRegisterResult|resultSaveDependentPersons={}", resultSaveDependentPersons.toString());
            }

            // gui mail nhung truong hop dong y
            Map<String, String> params = new HashMap<>();
            params.put("status", I18n.getMessage("tax.status.6"));
            params.put("reason", "");
            log.info("importNewRegisterResult|params={}", params);
//            commonUtilsService.sendListEmail(Constant.GROUP_SEND_MAIL.RESULT_DEPENDENT_PERSON, emailListAccept, params);
            // gui mail nhung truong hop tu choi
//            commonUtilsService.sendEmail(Constant.GROUP_SEND_MAIL.RESULT_DEPENDENT_PERSON, mapEmailReject);

            return ResponseUtils.ok();
        }
    }

    @Override
    public ResponseEntity<Object> exportDataAccordingTaxAuthority(AdminSearchDTO dto) {
        dto.setIsTaxNoOfDependentPerson(0);
        List<Map<String, Object>> listDataNotTaxNo = dependentRegistersRepositoryImpl.getListDPExpsAccordingTaxAuthority(dto); // don't have tax no

        dto.setIsTaxNoOfDependentPerson(1);
        List<Map<String, Object>> listDataHasTaxNo = dependentRegistersRepositoryImpl.getListDPExpsAccordingTaxAuthority(dto); // has tax no

        if (listDataNotTaxNo.isEmpty() && listDataHasTaxNo.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        try {
            String pathTemplate = "template/export/dependent/BM_Xuat_DS_DK_NPT_Theo_Mau_Co_Quan_Thue.xls";
            ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, false);

            if (listDataNotTaxNo.isEmpty()) { // don't have tax no
                Map<String, Object> hm = new HashMap<>();
                hm.put("stt", null);
                hm.put("MA_NV", null);
                hm.put("HO_VA_TEN_NGUOI_NOP_THUE", null);
                hm.put("MST_CUA_NGUOI_NOP_THUE", null);
                hm.put("HO_VA_TEN_NGUOI_PHU_THUOC", null);
                hm.put("NGAY_SINH_NGUOI_PHU_THUOC", null);
                hm.put("MST_CUA_NGUOI_PHU_THUOC", null);
                hm.put("MA_QT_CUA_NGUOI_PHU_THUOC", null);
                hm.put("QUOC_TICH_CUA_NGUOI_PHU_THUOC", null);
                hm.put("CMND_HO_CHIEU_NGUOI_PHU_THUOC", null);
                hm.put("MA_QUAN_HE_VOI_NGUOI_NOP_THUE", null);
                hm.put("QUAN_HE_VOI_NGUOI_NOP_THUE", null);
                hm.put("SO", null);
                hm.put("QUYEN_SO", null);
                hm.put("MA_QUOC_GIA", null);
                hm.put("QUOC_GIA", null);
                hm.put("MA_TINH_THANH_PHO", null);
                hm.put("TINH_THANH_PHO", null);
                hm.put("MA_QUAN_HUYEN", null);
                hm.put("QUAN_HUYEN", null);
                hm.put("MA_PHUONG_XA", null);
                hm.put("PHUONG_XA", null);
                hm.put("TU_THANG", null);
                hm.put("DEN_THANG", null);
                hm.put("MA_NPT", null);
                hm.put("GHI_CHU", null);
                listDataNotTaxNo.add(hm);
            }
            dynamicExport.replaceKeys(listDataNotTaxNo);

            if (listDataHasTaxNo.isEmpty()) { // has tax no
                Map<String, Object> hm = new HashMap<>();
                hm.put("stt", null);
                hm.put("MA_NV1", null);
                hm.put("HO_VA_TEN_NGUOI_NOP_THUE1", null);
                hm.put("MST_CUA_NGUOI_NOP_THUE1", null);
                hm.put("HO_VA_TEN_NGUOI_PHU_THUOC1", null);
                hm.put("NGAY_SINH_NGUOI_PHU_THUOC1", null);
                hm.put("MST_CUA_NGUOI_PHU_THUOC1", null);
                hm.put("MA_QT_CUA_NGUOI_PHU_THUOC1", null);
                hm.put("QUOC_TICH_CUA_NGUOI_PHU_THUOC1", null);
                hm.put("CMND_HO_CHIEU_NGUOI_PHU_THUOC1", null);
                hm.put("MA_QUAN_HE_VOI_NGUOI_NOP_THUE1", null);
                hm.put("QUAN_HE_VOI_NGUOI_NOP_THUE1", null);
                hm.put("SO1", null);
                hm.put("QUYEN_SO1", null);
                hm.put("MA_QUOC_GIA1", null);
                hm.put("QUOC_GIA1", null);
                hm.put("MA_TINH_THANH_PHO1", null);
                hm.put("TINH_THANH_PHO1", null);
                hm.put("MA_QUAN_HUYEN1", null);
                hm.put("QUAN_HUYEN1", null);
                hm.put("MA_PHUONG_XA1", null);
                hm.put("PHUONG_XA1", null);
                hm.put("TU_THANG1", null);
                hm.put("DEN_THANG1", null);
                hm.put("MA_NPT1", null);
                hm.put("GHI_CHU1", null);
                listDataHasTaxNo.add(hm);
            }
            dynamicExport.replaceKeys(listDataHasTaxNo);

            int sizeNotTaxNo = listDataNotTaxNo.size() - 1;
            int sizeHasTaxNo = listDataHasTaxNo.size() - 1;

            dynamicExport.setCellFormat(39, 1, 39 + sizeNotTaxNo, 25, ExportExcel.BORDER_FORMAT);
            dynamicExport.setCellFormat(46 + listDataNotTaxNo.size(), 1, 46 + sizeHasTaxNo, 25, ExportExcel.BORDER_FORMAT);

            String fileName = Utils.getFilePathExport("BM_Xuat_DS_DK_NPT_Theo_Mau_Co_Quan_Thue.xls");
            dynamicExport.exportFile(fileName);
            return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_DK_NPT_Theo_Mau_Co_Quan_Thue.xls");
        } catch (Exception ex) {
            log.error("[exportDataDependentRegisterAccordingTaxAuthority] has error {}", ex);
            return ResponseUtils.getResponseDataNotFound();
        }
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> autoRegister(Long employeeId, String cancelDate, String clientMessageId) {
        log.info("START|autoRegisterDependent|employeeId={}|cancelDate={}|clientMessageId={}", employeeId, cancelDate, clientMessageId);
        Date dateReport = Utils.stringToDate(cancelDate);
        if (Utils.isNullObject(employeeId) || dateReport == null) {
            log.info("BAD_REQUEST|autoRegisterDependent|employeeId={}|cancelDate={}|clientMessageId={}", employeeId, cancelDate, clientMessageId);
            throw new BaseAppException("employeeId or toDate is null");
        }

        List<HrDependentPersonsEntity> listDependentPerson = dependentRegistersRepositoryImpl.findByProperties(HrDependentPersonsEntity.class, "employeeId", employeeId);
        Date currentDate = new Date();
        List<DependentRegistersEntity> listSave = new ArrayList<>();
        for (HrDependentPersonsEntity dependentPersonsEntity : listDependentPerson) {
            if (dependentPersonsEntity.getToDate() == null || dependentPersonsEntity.getToDate().after(dateReport)) {
                List<DependentRegistersEntity> listRegisterOld = dependentRegistersRepositoryImpl.getRecordRegister(employeeId, Constant.REG_TYPE.DEPENDENT_CANCEL, dependentPersonsEntity.getFamilyRelationshipId(), commonUtilsService.getTaxStatusProcess());
                if (!Utils.isNullOrEmpty(listRegisterOld)) {
                    for (DependentRegistersEntity entity : listRegisterOld) {
                        entity.setStatus(Constant.TAX_STATUS.ACCOUNTANT_REJECT);
                        entity.setModifiedTime(currentDate);
                        entity.setModifiedBy("hcm-per-service");
                        entity.setRejectReason(I18n.getMessage("dependent.cancelWp.rejectReason"));
                        listSave.add(entity);
                    }
                }

                // cap nhat neu da dang ky va duoc phe duyet tu truoc do
                List<DependentRegistersEntity> listRegistered = dependentRegistersRepositoryImpl.findByProperties(DependentRegistersEntity.class,
                        "employeeId", employeeId,
                        "regType", Constant.REG_TYPE.DEPENDENT_CANCEL,
                        "familyRelationshipId", dependentPersonsEntity.getFamilyRelationshipId(),
                        "status", Constant.TAX_STATUS.TAX_APPROVAL
                );

                boolean isNewInsert = true;
                if (!Utils.isNullOrEmpty(listRegistered)) {
                    for (DependentRegistersEntity entity : listRegistered) {
                        if (entity.getToDate() != null && entity.getToDate().after(dateReport)) {
                            entity.setNote(I18n.getMessage("dependent.cancelWp.note", Utils.formatDate(entity.getToDate(), BaseConstants.SHORT_DATE_FORMAT)));
                            entity.setModifiedTime(currentDate);
                            entity.setModifiedBy("hcm-per-service");
                            entity.setToDate(Utils.getLastDayOfMonth(dateReport));
                            listSave.add(entity);
                            isNewInsert = false;
                        }
                    }
                }

                if (isNewInsert) {
                    DependentRegistersEntity entity = new DependentRegistersEntity();
                    entity.setEmployeeId(employeeId);
                    entity.setFamilyRelationshipId(dependentPersonsEntity.getFamilyRelationshipId());
                    entity.setRegType(Constant.REG_TYPE.DEPENDENT_CANCEL);
                    HrFamilyRelationshipsEntity familyRelationshipsEntity = dependentRegistersRepositoryImpl.get(HrFamilyRelationshipsEntity.class, dependentPersonsEntity.getFamilyRelationshipId());
                    entity.setDateOfBirth(familyRelationshipsEntity.getDateOfBirth());
                    entity.setTaxNo(familyRelationshipsEntity.getTaxNumber());
                    entity.setFromDate(dependentPersonsEntity.getFromDate());
                    entity.setToDate(Utils.getLastDayOfMonth(dateReport));
                    entity.setNote(I18n.getMessage("dependent.cancelWp.note", Utils.formatDate(entity.getToDate(), BaseConstants.SHORT_DATE_FORMAT)));
                    entity.setCreatedBy("hcm-per-service");
                    entity.setCreatedTime(currentDate);
                    entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    entity.setStatus(Constant.TAX_STATUS.ACCOUNTANT_REJECT);
                    listSave.add(entity);
                }
            }
        }
        dependentRegistersRepositoryJPA.saveAll(listSave);
        log.info("END|autoRegisterDependent|employeeId={}|cancelDate={}|clientMessageId={}", employeeId, cancelDate, clientMessageId);
        return ResponseUtils.ok(employeeId);
    }

    @Override
    public ResponseEntity<Object> sendMailConfirmDependent(AdminSearchDTO dto) {
        // todo send email
        return ResponseUtils.ok();
    }

    private String generatePersonCode(Long dependentPersonId) {
        Long seqValue = dependentRegistersRepositoryImpl.getDependentCodeSequenceValue();
        do {
            String dependentPersonCode = "TA" + String.format("%07d", seqValue);
            boolean isDuplicateRegister = dependentRegistersRepositoryImpl.duplicate(DependentRegistersEntity.class, null, "dependentPersonCode", dependentPersonCode);
            boolean isDuplicate = dependentRegistersRepositoryImpl.duplicate(HrDependentPersonsEntity.class, dependentPersonId, "dependentPersonCode", dependentPersonCode);
            if (!isDuplicate && !isDuplicateRegister) {
                return dependentPersonCode;
            }
            seqValue++;
        } while (true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getListDataByEmpId(FamilyRelationshipsDTO familyRelationshipsDTO) {
        List<HrFamilyRelationshipsEntity> data = dependentRegistersRepositoryImpl.getListDataByEmpId(familyRelationshipsDTO);
        return ResponseEntity.ok(data);
    }
}
