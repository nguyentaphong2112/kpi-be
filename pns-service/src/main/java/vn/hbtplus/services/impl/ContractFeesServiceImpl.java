/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.models.dto.ApproveDTO;
import vn.hbtplus.models.dto.ContractFeesDTO;
import vn.hbtplus.models.dto.RejectDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractFeesResponse;
import vn.hbtplus.models.response.EmployeeInfoResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractFeesEntity;
import vn.hbtplus.repositories.impl.ContractFeesRepositoryImpl;
import vn.hbtplus.repositories.impl.EmployeeRepositoryImpl;
import vn.hbtplus.repositories.jpa.ContractFeesRepositoryJPA;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.services.ContractFeesService;
import vn.hbtplus.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.SignatureException;
import java.util.*;

/**
 * Lop impl service ung voi bang PNS_CONTRACT_FEES
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractFeesServiceImpl implements ContractFeesService {

    private final ContractFeesRepositoryImpl contractFeesRepositoryImpl;
    private final ContractFeesRepositoryJPA contractFeesRepositoryJPA;
    private final CommonUtilsServiceImpl commonUtilsService;
    private final EmployeeRepositoryImpl employeeRepositoryImpl;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ContractFeesResponse> searchData(ContractFeesDTO dto) throws SignatureException {
        return ResponseUtils.ok(contractFeesRepositoryImpl.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveData(ContractFeesDTO dto) {
        if (contractFeesRepositoryImpl.isConflictProcess(dto)) {
            throw new BaseAppException("CONFLICT_CONTRACT_FEE", I18n.getMessage("contractFees.validate.process"));
        }

        ContractFeesEntity entity;
        if (dto.getContractFeeId() != null && dto.getContractFeeId() > 0L) {
            entity = contractFeesRepositoryJPA.getById(dto.getContractFeeId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ContractFeesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(entity, dto);
        if (entity.getStatus() == null || entity.getStatus().equals(ContractFeesEntity.STATUS.REJECT)) {
            entity.setStatus(ContractFeesEntity.STATUS.INIT);
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contractFeesRepositoryJPA.save(entity);
        contractFeesRepositoryJPA.flush();

        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveListData(List<ContractFeesEntity> listEntity) {
        if (Utils.isNullOrEmpty(listEntity)) {
            log.info("ContractFeesServiceImpl|saveListData|listEntity is empty");
            throw new BaseAppException("listEntity is empty");
        }

        Date currentDate = new Date();
        for (ContractFeesEntity entity : listEntity) {
            contractFeesRepositoryImpl.autoCancel(entity.getEmployeeId(), entity.getFromDate(), entity.getCreatedBy());
            entity.setCreatedTime(currentDate);
            entity.setStatus(ContractFeesEntity.STATUS.APPROVED);
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        }

        contractFeesRepositoryJPA.saveAll(listEntity);
        return BaseResponseEntity.ok(null);

    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteData(Long id) {
        Optional<ContractFeesEntity> optional = contractFeesRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("NOT_FOUND_DATA", I18n.getMessage("global.notFound"));
        }
        contractFeesRepositoryImpl.deActiveObject(ContractFeesEntity.class, id);
        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getDataById(Long id) throws SignatureException {
        Optional<ContractFeesEntity> optional = contractFeesRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("NOT_FOUND_DATA", I18n.getMessage("global.notFound"));
        }
        ContractFeesResponse dto = new ContractFeesResponse();
        Utils.copyProperties(dto, optional.get());
        return BaseResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ContractFeesDTO dto) throws Exception {
        String pathTemplate = "template/export/contractFee/BM_Xuat_PhiDichVu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 6, true);
        List<Map<String, Object>> listDataExport = contractFeesRepositoryImpl.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceText("${ngay_bao_cao}", Utils.formatDate(new Date()));

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_PhiDichVu.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity<Object> approveById(ApproveDTO dto) {
        ContractFeesEntity draftEntity = validateApprove(dto.getId(), null);
        processApprove(draftEntity);
        return BaseResponseEntity.ok(dto.getId());
    }

    @Override
    @Transactional
    public ResponseEntity<Object> approveByList(ApproveDTO dto) {
        if (dto.getListId() == null || dto.getListId().isEmpty()) {
            throw new BaseAppException("listId is empty");
        }

        List<ContractFeesEntity> listData = new ArrayList<>();
        for (Long id : dto.getListId()) {
            ContractFeesEntity draftEntity = validateApprove(id, null);
            listData.add(draftEntity);
        }

        for (ContractFeesEntity entity : listData) {
            processApprove(entity);
        }
        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> approveAll(ContractFeesDTO dto) {
        dto.setStatus(ContractFeesEntity.STATUS.INIT);
        List<ContractFeesEntity> listEntity = contractFeesRepositoryImpl.getListDataByForm(dto);
        if (listEntity == null || listEntity.isEmpty()) {
            throw new BaseAppException("listEntity is null or empty");
        }

        //validate conflict
        for (ContractFeesEntity entity : listEntity) {
            validateApprove(null, entity);
        }

        for (ContractFeesEntity entity : listEntity) {
            processApprove(entity);
        }
        return BaseResponseEntity.ok(null);
    }

    private ContractFeesEntity validateApprove(Long id, ContractFeesEntity draftEntity) {
        if (draftEntity == null) {
            Optional<ContractFeesEntity> optional = contractFeesRepositoryJPA.findById(id);
            if (optional.isEmpty()) {
                throw new BaseAppException("NOT_FOUND_DATA", I18n.getMessage("global.notFound"));
            }
            draftEntity = optional.get();
        }

        if (!draftEntity.getStatus().equals(ContractFeesEntity.STATUS.INIT)) {
            throw new BaseAppException("STATUS INVALID");
        }

        return draftEntity;
    }

    private void processApprove(ContractFeesEntity draftEntity) {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        draftEntity.setModifiedTime(currentDate);
        draftEntity.setModifiedBy(userName);
        draftEntity.setStatus(ContractFeesEntity.STATUS.APPROVED);
        draftEntity.setRejectReason(null);
        contractFeesRepositoryJPA.saveAndFlush(draftEntity);

        contractFeesRepositoryImpl.updateContractFeeProcess(draftEntity);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> rejectById(RejectDTO dto) {
        Optional<ContractFeesEntity> optional = contractFeesRepositoryJPA.findById(Utils.NVL(dto.getId()));
        if (optional.isEmpty() || !optional.get().getStatus().equals(ContractFeesEntity.STATUS.INIT)) {
            throw new BaseAppException("ContractFeesEntity is null or invalid status invalid");
        }
        ContractFeesEntity entity = optional.get();
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setStatus(ContractFeesEntity.STATUS.REJECT);
        entity.setRejectReason(dto.getRejectReason());

        contractFeesRepositoryJPA.save(entity);
        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> rejectByList(RejectDTO dto) {
        if (dto.getListId() == null || dto.getListId().isEmpty()) {
            throw new BaseAppException("listId is empty");
        }
        List<ContractFeesEntity> listData = contractFeesRepositoryImpl.getListContractFeeById(dto.getListId(), ContractFeesEntity.STATUS.INIT);

        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        for (ContractFeesEntity entity : listData) {
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userName);
            entity.setStatus(ContractFeesEntity.STATUS.REJECT);
            entity.setRejectReason(dto.getRejectReason());
        }

        if (!listData.isEmpty()) {
            contractFeesRepositoryJPA.saveAll(listData);
        }

        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> importContractFee(MultipartFile file, HttpServletRequest req) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/contractFee/BM_NhapMoi_PhiDichVu.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> listEmployeeCode = new ArrayList<>();

            for (Object[] obj : dataList) {
                listEmployeeCode.add(((String) obj[1]).toUpperCase());
            }

            Map<String, EmployeeInfoResponse> mapEmployee = employeeRepositoryImpl.getMapEmpByCodes(listEmployeeCode, Scope.CREATE, Constant.RESOURCE.PNS_FEE, "3");
            Map<String, ContractFeesEntity> mapContractFee = contractFeesRepositoryImpl.getMapContractFeeByEmpCodes(listEmployeeCode);

            Date currentDate = new Date();
            String userName = Utils.getUserNameLogin();
            int row = -1;
            List<ContractFeesDTO> contractFeeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                ContractFeesDTO entity = new ContractFeesDTO();
                row++;
                int col = 1;
                Long employeeId;
                String employeeCode = ((String) obj[col]);
                if (mapEmployee.get(employeeCode) == null) {
                    importExcel.addError(row, col, I18n.getMessage("employee.validate.employeeInvalid"), employeeCode);
                    continue;
                } else {
                    employeeId = mapEmployee.get(employeeCode).getEmployeeId();
                }
                col = col + 2;
                Date fromDate = (Date) obj[col++];

                //validate conflict qua trinh
                ContractFeesEntity contractFeesEntity = mapContractFee.get(employeeId + Utils.formatDate(fromDate));

                if (contractFeesEntity != null && contractFeesEntity.getContractFeeId() == null) {
                    importExcel.addError(row, col, I18n.getMessage("import.contractFee.conflict.file"), Utils.formatDate(fromDate));
                } else if (contractFeesEntity != null) {
                    importExcel.addError(row, col, I18n.getMessage("import.contractFee.conflict.database"), Utils.formatDate(fromDate));
                }
                mapContractFee.put(employeeId + Utils.formatDate(fromDate), contractFeesEntity);
                //check ngay
                ContractFeesDTO dto = new ContractFeesDTO();
                dto.setEmployeeId(employeeId);
                dto.setFromDate(fromDate);

                if (obj[4] != null && fromDate.after((Date) obj[4])) {
                    importExcel.addError(row, 4, I18n.getMessage("contractFees.error.toDateLessThan"), Utils.formatDate(fromDate) + Utils.formatDate((Date) obj[4]));
                    continue;
                }
                entity.setEmployeeId(employeeId);
                entity.setFromDate(fromDate);
                entity.setToDate((Date) obj[4]);
                entity.setAmountFee((Long) obj[5]);
                entity.setEmployeeCode(employeeCode);

                contractFeeList.add(entity);
            }

            if (importExcel.hasError()) { // co loi xay ra
                throw new ErrorImportException(file, importExcel);
            } else { // thuc hien insert vao DB
                for (ContractFeesDTO dto : contractFeeList) {
                    ContractFeesEntity entity = new ContractFeesEntity();
                    entity.setEmployeeId(dto.getEmployeeId());
                    entity.setAmountFee(dto.getAmountFee());
                    entity.setFromDate(dto.getFromDate());
                    entity.setToDate(dto.getToDate());
                    entity.setCreatedBy(userName);
                    entity.setCreatedTime(currentDate);
                    entity.setStatus(ContractFeesEntity.STATUS.INIT);
                    entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    if (dto.getContractFeeId() != null && dto.getContractFeeId() > 0L) {
                        entity.setContractFeeId(dto.getContractFeeId());
                    }
                    contractFeesRepositoryJPA.saveAndFlush(entity);
                }

                return BaseResponseEntity.ok(null);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
    }

    @Override
    public ResponseEntity<Object> getTemplateImport() throws Exception {
        ExportExcel dynamicExport = new ExportExcel("template/import/contractFee/BM_NhapMoi_PhiDichVu.xlsx", 1, true);
        dynamicExport.setCellFormat(0, 0, dynamicExport.getLastRow(), 0, ExportExcel.BORDER_FORMAT);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM_NhapMoi_PhiDichVu.xlsx");
    }
}
