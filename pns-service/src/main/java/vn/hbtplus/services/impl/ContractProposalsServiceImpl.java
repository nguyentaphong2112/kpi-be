/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import com.aspose.words.Document;
import com.aspose.words.ImportFormatMode;
import com.aspose.words.SectionStart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.dto.ContractProposalsDTO;
import vn.hbtplus.models.dto.ContractProposalsFormDTO;
import vn.hbtplus.models.dto.SendMailParamDTO;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.*;
import vn.hbtplus.repositories.jpa.ContractApproversRepositoryJPA;
import vn.hbtplus.repositories.jpa.ContractFeesRepositoryJPA;
import vn.hbtplus.repositories.jpa.ContractProposalsRepositoryJPA;
import vn.hbtplus.services.*;
import vn.hbtplus.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Lop impl service ung voi bang PNS_CONTRACT_PROPOSALS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractProposalsServiceImpl implements ContractProposalsService {

    private final ContractProposalsRepositoryImpl contractProposalsRepositoryImpl;
    private final ContractProposalsRepositoryJPA contractProposalsRepositoryJPA;
    private final ContractTemplatesService contractTemplatesService;
    private final CommonUtilsService commonUtilsService;
    private final ContractApproversRepositoryJPA contractApproversRepositoryJPA;
    private final ConfigApprovalsService configApprovalsService;
    private final SqlConfigsRepositoryImpl sqlConfigsRepository;
    private final ContractApproversRepositoryImpl contractApproversRepositoryImpl;
    private final ContractEvaluationsRepositoryImpl contractEvaluationsRepositoryImpl;
    private final FileService fileService;
    private final EmployeeRepositoryImpl employeeRepositoryImpl;
    private final ContractFeesRepositoryImpl contractFeesRepositoryImpl;
    private final ContractFeesRepositoryJPA contractFeesRepositoryJPA;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ContractProposalsResponse> searchData(ContractProposalsDTO dto) {
        BaseDataTableDto<ContractProposalsResponse> result;
        if (dto.getType() == null || Constant.CONTINUE_SIGN.equals(dto.getType())) {
            result = contractProposalsRepositoryImpl.searchContinueSign(dto);
        } else {
            result = contractProposalsRepositoryImpl.searchNewSignOrAddendumSign(dto);
        }
        return ResponseUtils.ok(result);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveData(ContractProposalsFormDTO dto) {
        ContractProposalsEntity entity;
        if (dto.getContractProposalId() != null && dto.getContractProposalId() > 0L) {
            entity = contractProposalsRepositoryJPA.getById(dto.getContractProposalId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ContractProposalsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }

        if (Constant.CONTRACT_TYPE.NEW.equals(entity.getType())) {
            if (!dto.getContractTypeId().equals(entity.getContractTypeId())) {
                entity.setContractTypeId(dto.getContractTypeId());
                HrContractTypesEntity contractTypesEntity = contractProposalsRepositoryImpl.get(HrContractTypesEntity.class, entity.getContractTypeId());
                if (contractTypesEntity != null) {
//                    entity.setToDate(Utils.getToDateContract(entity.getFromDate(), contractTypesEntity.getDuration()));
                    entity.setToDateByLaw(entity.getToDate());
                    String contractNumber = getNewOrContinueContractNumber(contractTypesEntity, entity);
                    entity.setContractNumber(contractNumber);
                }
            }
        } else if (Constant.CONTRACT_TYPE.CONTINUE.equals(entity.getType())) {
            if (!dto.getContractByLawId().equals(entity.getContractTypeId())) {
                entity.setContractTypeId(dto.getContractByLawId());
                dto.setContractTypeId(dto.getContractByLawId());
                HrContractTypesEntity contractTypesEntity = contractProposalsRepositoryImpl.get(HrContractTypesEntity.class, entity.getContractTypeId());
                if (contractTypesEntity != null) {
//                    entity.setToDateByLaw(Utils.getToDateContract(entity.getFromDate(), contractTypesEntity.getDuration()));
                    String contractNumber = getNewOrContinueContractNumber(contractTypesEntity, entity);
                    entity.setContractNumber(contractNumber);
                }
            }
        } else if (Constant.CONTRACT_TYPE.CONTRACT_FEE.equals(entity.getType())) {// save thay doi phi dich vu
            boolean isConflictProcess = contractProposalsRepositoryImpl.isConflictProcessContractFee(entity.getEmployeeId(), entity.getContractFeeId(), dto.getAmountFeeFromDate(), dto.getAmountFeeToDate());
            if (isConflictProcess) {
                throw new BaseAppException(I18n.getMessage("msg.error.contractFee"));
            } else {
                entity.setFromDate(dto.getAmountFeeFromDate());
                entity.setToDate(dto.getAmountFeeToDate());

                // check hop dong hien tai
                ContractProposalsDTO contractProposalsDTO = contractProposalsRepositoryImpl.getCurrentContractInfo(entity.getEmployeeId(), entity.getFromDate(), entity.getToDate());
                if (Utils.isNullObject(contractProposalsDTO)) {
                    throw new BaseAppException(I18n.getMessage("msg.error.contractProposal"));
                }

                Optional<ContractFeesEntity> optionalContractFeesEntity = contractFeesRepositoryJPA.findById(entity.getContractFeeId());
                if (optionalContractFeesEntity.isPresent()) {
                    ContractFeesEntity contractFeesEntity = optionalContractFeesEntity.get();
                    contractFeesEntity.setFromDate(dto.getAmountFeeFromDate());
                    contractFeesEntity.setToDate(dto.getAmountFeeToDate());
                    contractFeesEntity.setAmountFee(dto.getAmountFee());
                    contractFeesEntity.setModifiedBy(Utils.getUserNameLogin());
                    contractFeesEntity.setModifiedTime(new Date());

                    contractFeesRepositoryJPA.save(contractFeesEntity);
                }
            }
        }

        Utils.copyProperties(entity, dto);
        if (Utils.isNullObject(entity.getContractTypeId())) {
            entity.setContractTypeId(dto.getContractByLawId());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contractProposalsRepositoryJPA.saveAndFlush(entity);

        if (Constant.CONTRACT_TYPE.CONTINUE.equals(entity.getType()) && !Utils.isNullOrEmpty(dto.getContractApproveDTOs())) {
            List<ContractApproversDTO> approveDTOs = dto.getContractApproveDTOs();
            for (ContractApproversDTO approveDTO : approveDTOs) {
                List<ContractApproversEntity> listApprover = contractApproversRepositoryImpl.findByProperties(ContractApproversEntity.class,
                        "contractProposalId", entity.getContractProposalId(),
                        "approverLevel", approveDTO.getApproverLevel());
                ContractApproversEntity contractApproversEntity;
                if (Utils.isNullOrEmpty(listApprover)) {
                    contractApproversEntity = new ContractApproversEntity();
                    contractApproversEntity.setCreatedTime(new Date());
                    contractApproversEntity.setCreatedBy(Utils.getUserNameLogin());
                    contractApproversEntity.setContractProposalId(entity.getContractProposalId());
                    if (approveDTO.getApproverLevel() != null && approveDTO.getApproverLevel().equals(1)) {
                        contractApproversEntity.setCursorCurrent(1);
                    }
                } else {
                    contractApproversEntity = listApprover.get(0);
                    contractApproversEntity.setModifiedTime(new Date());
                    contractApproversEntity.setModifiedBy(Utils.getUserNameLogin());
                }
                contractApproversEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                contractApproversEntity.setApproverId(approveDTO.getApproverId());
                contractApproversEntity.setApproverLevel(approveDTO.getApproverLevel());
                if (Utils.isNullObject(contractApproversEntity.getContractTypeId())) {
                    contractApproversEntity.setContractTypeId(entity.getContractTypeId());
                    contractApproversEntity.setToDate(entity.getToDate());
                }
                contractApproversRepositoryJPA.save(contractApproversEntity);
            }
        }

        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getDataById(Long id) {
        Optional<ContractProposalsEntity> optional = contractProposalsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("ContractProposalsEntity not found");
        }
        ContractProposalsResponse dto = new ContractProposalsResponse();
        Utils.copyProperties(dto, optional.get());
        ContractProposalsResponse salaryDTO = contractProposalsRepositoryImpl.getSalaryProcessByEmpId(dto.getEmployeeId(), dto.getFromDate());
        if (salaryDTO != null) {
            dto.setAmountSalary(salaryDTO.getAmountSalary());
            dto.setSalaryPercent(salaryDTO.getSalaryPercent());
        }

        if (Constant.CONTRACT_TYPE.CONTINUE.equals(optional.get().getType())) {
            List<ContractApproversEntity> contractApproveEntities = contractApproversRepositoryImpl.findByProperties(ContractApproversEntity.class, "contractProposalId", dto.getContractProposalId());
            if (!Utils.isNullOrEmpty(contractApproveEntities)) {
                for (ContractApproversEntity entity : contractApproveEntities) {
                    ContractApproversResponse contractApproversResponse = new ContractApproversResponse();
                    Utils.copyProperties(contractApproversResponse, entity);
                    if (Constant.APPROVE_LEVEL.MANAGER.equals(entity.getApproverLevel())) {
                        dto.setManagerResponse(contractApproversResponse);
                    } else if (Constant.APPROVE_LEVEL.APPROVER.equals(entity.getApproverLevel())) {
                        dto.setApproverResponse(contractApproversResponse);
                    } else if (Constant.APPROVE_LEVEL.BOSS.equals(entity.getApproverLevel())) {
                        dto.setBossResponse(contractApproversResponse);
                    }
                }
            }

            List<ContractEvaluationsEntity> contractEvaluationsEntities = contractEvaluationsRepositoryImpl.findByProperties(ContractEvaluationsEntity.class, "contractProposalId", dto.getContractProposalId());
            if (!Utils.isNullOrEmpty(contractEvaluationsEntities)) {
                ContractEvaluationsResponse contractEvaluationsResponse = new ContractEvaluationsResponse();
                Utils.copyProperties(contractEvaluationsResponse, contractEvaluationsEntities.get(0));
                dto.setEvaluationsResponse(contractEvaluationsResponse);
            }
        } else if (Constant.CONTRACT_TYPE.CONTRACT_FEE.equals(optional.get().getType())) {
            Optional<ContractFeesEntity> contractFeesOption = contractFeesRepositoryJPA.findById(optional.get().getContractFeeId());
            contractFeesOption.ifPresent(contractFeesEntity -> dto.setAmountFee(contractFeesEntity.getAmountFee()));

        }
        return BaseResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportContract(Long id) throws Exception {
        Optional<ContractProposalsEntity> optional = contractProposalsRepositoryJPA.findById(id);
        if (optional.isEmpty() || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException("ContractProposalsEntity not found");
        }

        ContractProposalsEntity entity = optional.get();
        Document document = contractTemplatesService.getTemplateContract(entity);
        if (document == null) {
            HrEmployeesEntity employeesEntity = contractApproversRepositoryImpl.get(HrEmployeesEntity.class, entity.getEmployeeId());
            throw new BaseAppException(I18n.getMessage("contract.template.notExists", employeesEntity.getEmployeeCode() + "-" + employeesEntity.getFullName()));
        }

        // xu lý lay sql theo cau hinh
        List<SqlConfigsResponse> listSQL = sqlConfigsRepository.getListSqlConfig(entity.getContractTypeId(), 1);
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("employeeId", entity.getEmployeeId());
        mapParams.put("contractProposalId", id);
        for (SqlConfigsResponse sqlConfigsResponse : listSQL) {
            List<Map<String, Object>> listMap = contractProposalsRepositoryImpl.getListData(sqlConfigsResponse.getSql(), mapParams);
            if (listMap.size() > 1) {
                FileUtils.aposeReplaceKeys(document, listMap);
            } else if (listMap.size() == 1) {
                listMap.get(0).values().removeAll(Collections.singleton(null));
                FileUtils.aposeReplaceKeys(document, listMap.get(0));
            }
        }
        // replace param mau
        List<TemplateParamsEntity> listTemplateParams = contractProposalsRepositoryImpl.findAll(TemplateParamsEntity.class);
        Map<String, Object> mapTemplateParams = new HashMap<>();
        for (TemplateParamsEntity paramsEntity : listTemplateParams) {
            mapTemplateParams.put(paramsEntity.getCode(), paramsEntity.getDefaultValue());
        }
        FileUtils.aposeReplaceKeys(document, mapTemplateParams);

        String fileName = commonUtilsService.getOnlyFilePathExport(entity.getEmployeeId() + ".pdf");
        commonUtilsService.savePdfFile(document, fileName);
        return ResponseUtils.getResponseFileEntity(fileName, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportContractByListId(List<Long> listId) throws Exception {
        List<ContractProposalsEntity> lisEntity = contractProposalsRepositoryImpl.getListContractProposalByIds(listId);
        if (Utils.isNullOrEmpty(lisEntity)) {
            throw new BaseAppException("ContractProposalsEntity not found");
        }
        Document documentAll = processExportContract(lisEntity);
        String fileName = commonUtilsService.getOnlyFilePathExport("danhSachHD.pdf");
        commonUtilsService.savePdfFile(documentAll, fileName);
        return ResponseUtils.getResponseFileEntity(fileName, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportContractByForm(ContractProposalsDTO dto) throws Exception {
        List<ContractProposalsEntity> lisEntity = contractProposalsRepositoryImpl.getListIdByForm(dto);
        if (Utils.isNullOrEmpty(lisEntity)) {
            throw new BaseAppException(I18n.getMessage("contract.validate.approve.notExists"));
        }

        List<List<ContractProposalsEntity>> listPartition = Utils.partition(lisEntity, 300);
        List<String> filePaths = new ArrayList<>();
        int i = 1;
        for (List<ContractProposalsEntity> entities : listPartition) {
            Document documentAll = processExportContract(entities);
            String filePath = commonUtilsService.getOnlyFilePathExport("danhSachHD_" + i + ".pdf");
            commonUtilsService.savePdfFile(documentAll, filePath);
            filePaths.add(filePath);
            i++;
        }

        String zipPath = commonUtilsService.getFilePathExport("danhSachHD.zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath))) {
            for (String filePath : filePaths) {
                File fileToZip = new File(filePath);
                zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                Files.copy(fileToZip.toPath(), zipOut);
                boolean isDelete = fileToZip.delete();
                if (!isDelete) {
                    log.info("exportContractByForm|delete file fail|{}", fileToZip.getName());
                }
            }
        }

        return ResponseUtils.getResponseFileEntity(zipPath, true);
    }

    private Document processExportContract(List<ContractProposalsEntity> lisEntity) throws Exception {
        long startTime = System.currentTimeMillis();
        Map<Long, Document> mapTemplate = new HashMap<>();
        Set<String> empCodeError = new HashSet<>();
        for (ContractProposalsEntity entity : lisEntity) {
            Document document = contractTemplatesService.getTemplateContract(entity);
            if (document == null) {
                HrEmployeesEntity employeesEntity = contractApproversRepositoryImpl.get(HrEmployeesEntity.class, entity.getEmployeeId());
                empCodeError.add(employeesEntity.getEmployeeCode());
            } else {
                mapTemplate.put(entity.getEmployeeId(), document);
            }
        }
        log.info("processExportContract|getTemplate|time duration={}", System.currentTimeMillis() - startTime);

        if (!Utils.isNullOrEmpty(empCodeError)) {
            throw new BaseAppException(I18n.getMessage("contract.template.notExists", empCodeError.toString()));
        }

        List<Long> listId = new ArrayList<>();
        List<Long> listContractTypeId = new ArrayList<>();
        for (ContractProposalsEntity entity : lisEntity) {
            listId.add(entity.getContractProposalId());
            if (!listContractTypeId.contains(entity.getContractTypeId())) {
                listContractTypeId.add(entity.getContractTypeId());
            }
        }

        startTime = System.currentTimeMillis();
        Map<String, Map<String, Object>> mapData = new HashMap<>();
        List<SqlConfigsResponse> listSQLAll = sqlConfigsRepository.getListSqlConfig(null, 2);
        if (!Utils.isNullOrEmpty(listSQLAll)) {
            List<List<Long>> listPartition = Utils.partition(listId, Constant.SIZE_PARTITION);
            for (List<Long> listProposalId : listPartition) {
                Map<String, Object> mapParams = new HashMap<>();
                mapParams.put("contractProposalIds", listProposalId);
                for (SqlConfigsResponse sqlConfigsResponse : listSQLAll) {
                    List<Map<String, Object>> listMap = contractProposalsRepositoryImpl.getListData(sqlConfigsResponse.getSql(), mapParams);
                    for (Map<String, Object> mapResult : listMap) {
                        String employeeId = mapResult.get("employeeId").toString();
                        mapData.put(employeeId + sqlConfigsResponse.getSqlConfigId(), mapResult);
                    }
                }
            }
        }
        log.info("processExportContract|process SQLAll|time duration={}", System.currentTimeMillis() - startTime);

        // xu lý lay sql theo cau hinh
        Map<Long, List<SqlConfigsResponse>> mapSqlConfigByContractType = new HashMap<>();
        for (Long contractTypeId : listContractTypeId) {
            List<SqlConfigsResponse> listSQL = sqlConfigsRepository.getListSqlConfig(contractTypeId, 2);
            mapSqlConfigByContractType.put(contractTypeId, listSQL);
        }

        // replace param mau
        List<TemplateParamsEntity> listTemplateParams = contractProposalsRepositoryImpl.findAll(TemplateParamsEntity.class);
        Map<String, Object> mapTemplateParams = new HashMap<>();
        for (TemplateParamsEntity paramsEntity : listTemplateParams) {
            mapTemplateParams.put(paramsEntity.getCode(), paramsEntity.getDefaultValue());
        }

        startTime = System.currentTimeMillis();
        Document documentAll = null;
        for (ContractProposalsEntity entity : lisEntity) {
            Document document = mapTemplate.get(entity.getEmployeeId());
            List<SqlConfigsResponse> listSQL = mapSqlConfigByContractType.get(entity.getContractTypeId());
            if (!Utils.isNullOrEmpty(listSQL)) {
                Map<String, Object> mapParams = new HashMap<>();
                mapParams.put("employeeId", entity.getEmployeeId());
                mapParams.put("contractProposalId", entity.getContractProposalId());
                for (SqlConfigsResponse sqlConfigsResponse : listSQL) {
                    List<Map<String, Object>> listMap = contractProposalsRepositoryImpl.getListData(sqlConfigsResponse.getSql(), mapParams);
                    if (listMap.size() > 1) {
                        FileUtils.aposeReplaceKeys(document, listMap);
                    } else if (listMap.size() == 1) {
                        listMap.get(0).values().removeAll(Collections.singleton(null));
                        FileUtils.aposeReplaceKeys(document, listMap.get(0));
                    }
                }
            }

            if (!Utils.isNullOrEmpty(listSQLAll)) {
                for (SqlConfigsResponse sqlConfigsResponse : listSQLAll) {
                    Map<String, Object> mapResult = mapData.get(entity.getEmployeeId().toString() + sqlConfigsResponse.getSqlConfigId());
                    if (mapResult != null) {
                        mapResult.values().removeAll(Collections.singleton(null));
                        FileUtils.aposeReplaceKeys(document, mapResult);
                    }
                }
            }

            FileUtils.aposeReplaceKeys(document, mapTemplateParams);
            document.getFirstSection().getPageSetup().setSectionStart(SectionStart.NEW_PAGE);
            document.getFirstSection().getPageSetup().setRestartPageNumbering(true);
            document.getFirstSection().getPageSetup().setPageStartingNumber(1);
            if (documentAll == null) {
                documentAll = document;
            } else {
                documentAll.appendDocument(document, ImportFormatMode.KEEP_SOURCE_FORMATTING);
            }
        }
        log.info("processExportContract|generate document|time duration={}", System.currentTimeMillis() - startTime);
        return documentAll;
    }

    @Override
    public ResponseEntity<Object> exportData(ContractProposalsDTO dto) {
        List<Map<String, Object>> listData;
        if (dto.getType() == null || Constant.CONTINUE_SIGN.equals(dto.getType())) {
            listData = contractProposalsRepositoryImpl.getContinueSignContract(dto);
        } else {
            listData = contractProposalsRepositoryImpl.getListMapNewOrAppendixContract(dto);
        }
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        try {
            String file;
            if (dto.getType() == null) {
                file = "BM_Xuat_DS_Ky_HĐ.xlsx";
            } else if (Constant.CONTRACT_TYPE.CONTINUE.equals(dto.getType())) {
                file = "BM_Xuat_DS_KyTiepHĐ.xlsx";
            } else if (Constant.CONTRACT_TYPE.NEW.equals(dto.getType())) {
                file = "BM_Xuat_DS_KyMoiHĐ.xlsx";
            } else if (Constant.CONTRACT_TYPE.CONTRACT_FEE.equals(dto.getType())) {
                file = "BM_Xuat_DS_ThayDoiPhiDichVu.xlsx";
            } else {
                file = "BM_Xuat_DS_ThayDoiPhuLucHĐ.xlsx";
            }

            ExportExcel dynamicExport = new ExportExcel("template/export/moduleName/" + file, 2, true);
            dynamicExport.replaceKeys(listData);
            return ResponseUtils.ok(dynamicExport, file);
        } catch (Exception ex) {
            log.error("[exportDataContractProposals] has error {0}", ex);
            throw new BaseAppException("exportDataError");
        }
    }

    @Override
    @Transactional
    public void autoFindExpiredContract(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            Date currentDate = new Date();
            fromDate = currentDate;
            toDate = DateUtils.addDays(currentDate, 45);
        }
        Map<Long, HrContractTypesEntity> mapContractType = getMapContractType();
        List<ContractProposalsDTO> listEmp = contractProposalsRepositoryImpl.getListEmpExpiredContract(fromDate, toDate);
        List<ContractProposalsEntity> listSaveContractProposal = new ArrayList<>();
        List<ContractApproversEntity> listSaveContractApprovers = new ArrayList<>();
        for (ContractProposalsDTO dto : listEmp) {
            ContractProposalsEntity entity = processAddProposal(Constant.CONTRACT_TYPE.CONTINUE, dto, mapContractType);
            // xu ly lay thong tin nguoi quan ly truc tiep
            if (!Utils.isNullObject(dto.getManagerId())) {
                ContractApproversEntity contractApproversEntity = new ContractApproversEntity();
                Utils.copyProperties(contractApproversEntity, entity);
                contractApproversEntity.setApproverId(dto.getManagerId());
                contractApproversEntity.setApproverLevel(Constant.CONFIG_APPROVAL_TYPE.SIGNER);
                contractApproversEntity.setCursorCurrent(1);
                listSaveContractApprovers.add(contractApproversEntity);
            }

            // xu ly lay thong tin nguoi phe duyet
            ContractApproversDTO contractApproverDTO = configApprovalsService.getApprover(Constant.CONFIG_APPROVAL_TYPE.APPROVER, entity.getEmployeeId(), entity.getPositionId(), entity.getOrganizationId(), entity.getFromDate());
            if (contractApproverDTO != null) {
                ContractApproversEntity contractApproversEntity = new ContractApproversEntity();
                Utils.copyProperties(contractApproversEntity, entity);
                contractApproversEntity.setApproverId(contractApproverDTO.getEmployeeId());
                contractApproversEntity.setApproverLevel(Constant.CONFIG_APPROVAL_TYPE.APPROVER);
                listSaveContractApprovers.add(contractApproversEntity);
            }

            // xu ly lay thong tin nguoi phe duyet thay doi
            contractApproverDTO = configApprovalsService.getApprover(Constant.CONFIG_APPROVAL_TYPE.CHANGE_APPROVER, entity.getEmployeeId(), entity.getPositionId(), entity.getOrganizationId(), entity.getFromDate());
            if (contractApproverDTO != null) {
                ContractApproversEntity contractApproversEntity = new ContractApproversEntity();
                Utils.copyProperties(contractApproversEntity, entity);
                contractApproversEntity.setApproverId(contractApproverDTO.getEmployeeId());
                contractApproversEntity.setApproverLevel(Constant.CONFIG_APPROVAL_TYPE.CHANGE_APPROVER);
                listSaveContractApprovers.add(contractApproversEntity);
            }
            // xu ly them nguoi ky duyet
            processAddSigner(entity);
            listSaveContractProposal.add(entity);
        }

        String userName = Utils.getUserNameLogin();
        contractProposalsRepositoryImpl.insertBatch(ContractProposalsEntity.class, listSaveContractProposal, userName);
        contractProposalsRepositoryImpl.insertBatch(ContractApproversEntity.class, listSaveContractApprovers, userName);
        log.info("extendsContract.proposals|insert success {} records", listSaveContractProposal.size());
    }

    @Override
    @Transactional
    public void autoFindNewContract() {
        Map<Long, HrContractTypesEntity> mapContractType = getMapContractType();

        List<ContractProposalsDTO> listEmp = contractProposalsRepositoryImpl.getListEmpNewContract();
        List<ContractProposalsEntity> listSaveContractProposal = new ArrayList<>();

        for (ContractProposalsDTO dto : listEmp) {
            ContractProposalsEntity entity = processAddProposal(Constant.CONTRACT_TYPE.NEW, dto, mapContractType);
            entity.setStatus(ContractProposalsEntity.STATUS.WAITING_SIGN);
            // xu ly them nguoi ky duyet
            processAddSigner(entity);
            listSaveContractProposal.add(entity);
        }
        contractProposalsRepositoryImpl.insertBatch(ContractProposalsEntity.class, listSaveContractProposal, Utils.getUserNameLogin());
        log.info("newContract.proposals|insert success {} records", listSaveContractProposal.size());
    }

    @Override
    @Transactional
    public void autoFindAppendixContract() {
        Map<Long, HrContractTypesEntity> mapContractType = getMapContractType();
        List<ContractProposalsDTO> listEmp = contractProposalsRepositoryImpl.getListEmpAppendixContract();
        List<ContractProposalsEntity> listSaveContractProposal = new ArrayList<>();
        for (ContractProposalsDTO dto : listEmp) {
            ContractProposalsEntity entity = processAddProposal(Constant.CONTRACT_TYPE.APPENDIX_SALARY, dto, mapContractType);
            entity.setToDate(dto.getCurToDate());
            entity.setStatus(ContractProposalsEntity.STATUS.WAITING_SIGN);
            // xu ly them nguoi ky duyet
            processAddSigner(entity);
            listSaveContractProposal.add(entity);
        }
        contractProposalsRepositoryImpl.insertBatch(ContractProposalsEntity.class, listSaveContractProposal, Utils.getUserNameLogin());
        log.info("appendixContract.proposals|insert success " + listSaveContractProposal.size() + " records");
    }

    private ContractProposalsEntity processAddProposal(int type, ContractProposalsDTO dto, Map<Long, HrContractTypesEntity> mapContractType) {
        ContractProposalsEntity entity = new ContractProposalsEntity();
        Utils.copyProperties(entity, dto);
        entity.setContractProposalId(contractProposalsRepositoryImpl.getNextId(ContractProposalsEntity.class));
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setCreatedTime(new Date());
        entity.setCreatedBy("cron_tab");
        entity.setType(type);
        int duration = 0;
        HrContractTypesEntity contractTypesEntity = mapContractType.get(dto.getContractTypeId());
        if (contractTypesEntity != null) {
//            duration = Utils.NVL(contractTypesEntity.getDuration(), 0);
        }

        if (entity.getFromDate() == null) {
            entity.setFromDate(DateUtils.addDays(dto.getCurToDate(), 1));
        }

        entity.setToDate(Utils.getToDateContract(entity.getFromDate(), duration));
        entity.setToDateByLaw(entity.getToDate());
        entity.setContractByLawId(entity.getContractTypeId());
        entity.setStatus(ContractProposalsEntity.STATUS.INIT);
        dto.setFromDate(entity.getFromDate());
        entity.setContractNumber(getContractNumber(type, dto, contractTypesEntity));
        return entity;
    }

    private void processAddSigner(ContractProposalsEntity entity) {
        ContractApproversDTO contractApproverDTO = configApprovalsService.getApprover(Constant.CONFIG_APPROVAL_TYPE.SIGNER, entity.getEmployeeId(), entity.getPositionId(), entity.getOrganizationId(), entity.getFromDate());
        if (contractApproverDTO != null) {
            entity.setSignerId(contractApproverDTO.getEmployeeId());
            entity.setSignerPosition(contractApproverDTO.getPositionName());
            entity.setDelegacyNo(contractApproverDTO.getDocumentNo());
        }
    }

    private Map<Long, HrContractTypesEntity> getMapContractType() {
        List<HrContractTypesEntity> listContractType = contractProposalsRepositoryImpl.findAll(HrContractTypesEntity.class);
        Map<Long, HrContractTypesEntity> mapContractType = new HashMap<>();
        for (HrContractTypesEntity contractTypesEntity : listContractType) {
            mapContractType.put(contractTypesEntity.getContractTypeId(), contractTypesEntity);
        }
        return mapContractType;
    }

    private String getContractNumber(int type, ContractProposalsDTO dto, HrContractTypesEntity contractTypesEntity) {
        String contractNumber = null;//{ma_nv}/MB-{ma_loai_hđ}-{ma_don_vi}/{thang}/{nam}
        String strMonthYear = Utils.formatDate(dto.getFromDate(), Constant.SHORT_DATE_FORMAT);
        if (type == Constant.CONTRACT_TYPE.NEW || type == Constant.CONTRACT_TYPE.CONTINUE) {
            if (contractTypesEntity != null) {
                contractNumber = dto.getEmployeeCode() + "/MB-" + contractTypesEntity.getCode() + "-" + dto.getOrgCode() + "/" + strMonthYear;
            }
        } else {//phu luc: PL{lan_ky_phu_luc}-MB-{don_vi}/{thang}/{nam}[{so_hop_dong_hien_tai}
            contractNumber = "PL" + (dto.getCountSign() + 1) + "-" + "MB-" + dto.getOrgCode() + "/" + strMonthYear + "/" + dto.getCurContractNumber();
        }
        return contractNumber;
    }

    @Override
    public String getNewOrContinueContractNumber(HrContractTypesEntity contractTypesEntity, ContractProposalsEntity entity) {
        HrEmployeesEntity employeesEntity = contractProposalsRepositoryImpl.get(HrEmployeesEntity.class, entity.getEmployeeId());
        String orgCode = contractProposalsRepositoryImpl.getOrgCode(entity.getOrganizationId());
        String strMonthYear = Utils.formatDate(entity.getFromDate(), Constant.SHORT_DATE_FORMAT);
        return employeesEntity.getEmployeeCode() + "/MB-" + contractTypesEntity.getCode() + "-" + orgCode + "/" + strMonthYear;
    }

    @Override
    @Transactional
    public ResponseEntity<Object> uploadFileSigned(List<MultipartFile> listFileSigned, Integer type) throws IOException {
        if (Utils.isNullOrEmpty(listFileSigned)) {
            throw new BaseAppException("listFileSigned is null");
        }
        Map<String, MultipartFile> mapFile = new HashMap<>();
        if (!listFileSigned.isEmpty()) {
            for (MultipartFile multipartFile : listFileSigned) {
                mapFile.putAll(commonUtilsService.readFileZip(multipartFile));
            }
        }

        Set<String> setEmpCodes = mapFile.keySet();
        List<String> listEmpCode = new ArrayList<>(setEmpCodes);
        List<ContractProposalsEntity> listContractProposal = contractProposalsRepositoryImpl.getListContractProposalByEmpCodes(listEmpCode, type);
        if (Utils.isNullOrEmpty(listContractProposal)) {
            throw new BaseAppException("listContractProposal is null");
        }

        List<ContractProposalsEntity> listUpdate = new ArrayList<>();
//        List<HrDraftContractProcessEntity> listSaveDraft = new ArrayList<>();
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
//        Map<Long, HrContractTypesEntity> mapContractType = getMapContractType();

        for (ContractProposalsEntity entity : listContractProposal) {
            MultipartFile multipartFile = mapFile.get(entity.getEmployeeCode());
            if (multipartFile != null) {
                fileService.uploadFile(multipartFile, entity.getContractProposalId(), Constant.ATTACHMENT.TABLE_NAMES.PNS_CONTRACT_PROPOSALS, Constant.ATTACHMENT.FILE_TYPES.CONTRACT_SIGNED, Constant.ATTACHMENT.MODULE);
                entity.setStatus(ContractProposalsEntity.STATUS.SIGNED);
                entity.setModifiedTime(currentDate);
                entity.setModifiedBy(userName);
                listUpdate.add(entity);

                // xu ly day du lieu sang draft
//                HrDraftContractProcessEntity draftEntity = new HrDraftContractProcessEntity();
//                Utils.copyProperties(draftEntity, entity);
//                draftEntity.setStrFromDate(Utils.formatDate(entity.getFromDate()));
//                draftEntity.setStrToDate(Utils.formatDate(entity.getToDate()));
//                draftEntity.setStrSignedDate(draftEntity.getStrFromDate());
//                draftEntity.setNote(I18n.getMessage("contract.signed.note"));
//                HrContractTypesEntity contractTypesEntity = mapContractType.get(entity.getContractTypeId());
//                draftEntity.setClassifyCode(contractTypesEntity.getClassifyCode());
//                draftEntity.setDocumentsEntity(documentsEntity);
//                listSaveDraft.add(draftEntity);
            }
        }

//        if (!Utils.isNullOrEmpty(listUpdate)) {
//            contractProposalsRepositoryJPA.saveAll(listUpdate);
//            // call api luu draft
//            log.info("saveDraftContractProcess|START");
//            ResponseEntity<Object> responseEntity = hcmPerClient.saveDraftContractProcess(keycloakService.getHeader(), listSaveDraft);
//            Gson gson = new Gson();
//            log.info("saveDraftContractProcess|END|listSaveDraft=" + gson.toJson(listSaveDraft) + "|response=" + gson.toJson(responseEntity.getBody()));
//        }

        contractProposalsRepositoryJPA.saveAll(listUpdate);
        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> updateStatusFileSigned(List<ContractProposalsDTO> listDTO) {
        List<ContractProposalsEntity> listContractProposal = contractProposalsRepositoryImpl.getListContractProposalByEmpIds(listDTO);
        if (Utils.isNullOrEmpty(listContractProposal)) {
            throw new BaseAppException(I18n.getMessage("contract.validate.approve.notExists"));
        }
        Date currentDate = new Date();
        List<ContractFeesEntity> listContractFee = new ArrayList<>();
        int status = listDTO.get(0).getStatus();
        List<Long> listContractProposalId = new ArrayList<>();
        for (ContractProposalsEntity entity : listContractProposal) {
            if (entity.getType().equals(Constant.CONTRACT_TYPE.CONTRACT_FEE)) {
                ContractFeesEntity contractFeesEntity = contractFeesRepositoryJPA.getById(entity.getContractFeeId());
                if (contractFeesEntity.getStatus().equals(ContractFeesEntity.STATUS.INIT)) {
                    if (Constant.YES.equals(status)) {
                        contractFeesEntity.setStatus(ContractFeesEntity.STATUS.APPROVED);
                    } else {
                        contractFeesEntity.setStatus(ContractFeesEntity.STATUS.REJECT);
                    }
                    contractFeesEntity.setModifiedTime(currentDate);
                    contractFeesEntity.setModifiedBy("hcm-per-service");
                    listContractFee.add(contractFeesEntity);
                    contractFeesRepositoryImpl.updateContractFeeProcess(contractFeesEntity);
                }
            }
            listContractProposalId.add(entity.getContractProposalId());
        }
        if (!Utils.isNullOrEmpty(listContractFee)) {
            contractFeesRepositoryJPA.saveAll(listContractFee);
        }
        int statusProposal = Constant.YES.equals(status) ? ContractProposalsEntity.STATUS.COMPLETE : ContractProposalsEntity.STATUS.REJECT_FILE_SIGNED;
        contractProposalsRepositoryImpl.updateStatus(listContractProposalId, statusProposal, "hcm-per-service");
//        this.sendEmpMailApproveFileSigned(listContractProposal, statusProposal);
        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteDataById(Long id) {
        Optional<ContractProposalsEntity> optional = contractProposalsRepositoryJPA.findById(id);
        if (optional.isEmpty()
                || (!ContractProposalsEntity.STATUS.INIT.equals(optional.get().getStatus()) && optional.get().getType().equals(Constant.CONTRACT_TYPE.CONTINUE))
                || (!ContractProposalsEntity.STATUS.WAITING_SIGN.equals(optional.get().getStatus()) && !optional.get().getType().equals(Constant.CONTRACT_TYPE.CONTINUE))
        ) {
            throw new BaseAppException("ContractProposalsEntity null or invalid status");
        }

        List<ContractEvaluationsEntity> listContractEvaluations = contractEvaluationsRepositoryImpl.findByProperties(
                ContractEvaluationsEntity.class,
                "contractProposalId", id);
        for (ContractEvaluationsEntity entity : listContractEvaluations) {
            fileService.deActiveFileByAttachmentId(entity.getContractEvaluationId(), Constant.ATTACHMENT.TABLE_NAMES.PNS_CONTRACT_EVALUATIONS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONTRACT_EVALUATIONS);
        }

        if (Constant.CONTRACT_TYPE.CONTRACT_FEE.equals(optional.get().getType())) {
            contractProposalsRepositoryImpl.deleteById(optional.get().getContractFeeId(), ContractFeesEntity.class, "contractFeeId");
        }

        contractProposalsRepositoryImpl.deleteById(id, ContractEvaluationsEntity.class, "contractProposalId");
        contractProposalsRepositoryImpl.deleteById(id, ContractApproversEntity.class, "contractProposalId");
        contractProposalsRepositoryImpl.deleteById(ContractProposalsEntity.class, id);
        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteByForm(ContractProposalsDTO dto) {
        if (Utils.isNullObject(dto.getType())) {
            throw new BaseAppException("type is null");
        }

        List<String> listStatus = new ArrayList<>();
        if (dto.getType().equals(Constant.CONTRACT_TYPE.CONTINUE)) {
            listStatus.add(String.valueOf(ContractProposalsEntity.STATUS.INIT));
        } else {
            listStatus.add(String.valueOf(ContractProposalsEntity.STATUS.WAITING_SIGN));
        }
        dto.setListStatus(listStatus);
        List<ContractProposalsEntity> listContractProposal = contractProposalsRepositoryImpl.getListIdByForm(dto);
        if (Utils.isNullOrEmpty(listContractProposal)) {
            throw new BaseAppException(I18n.getMessage("contract.validate.approve.notExists"));
        }
        List<Long> listId = new ArrayList<>();
        for (ContractProposalsEntity entity : listContractProposal) {
            listId.add(entity.getContractProposalId());
        }

        contractProposalsRepositoryImpl.deleteContractProposal(listId);
        return BaseResponseEntity.ok(null);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class})
    public ResponseEntity<Object> sendMailByList(ContractProposalsDTO dto) throws Exception {
        return sendMailByListFunc(dto, null);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class})
    public ResponseEntity<Object> sendMailAll(ContractProposalsDTO dto) throws Exception {
        List<ContractProposalsEntity> result;
        if (dto.getType() == null || Constant.CONTINUE_SIGN.equals(dto.getType())) {
            result = contractProposalsRepositoryImpl.searchContinueSignFullList(dto);
        } else {
            result = contractProposalsRepositoryImpl.searchNewSignOrAddendumSignFullList(dto);
        }
        return sendMailByListFunc(dto, result);
    }

    private ResponseEntity<Object> sendMailByListFunc(ContractProposalsDTO dto, List<ContractProposalsEntity> listEntitySendMail) throws Exception {
        if (Utils.isNullOrEmpty(dto.getListContractProposalId()) && Utils.isNullOrEmpty(listEntitySendMail)) {
            log.info("ContractProposalsServiceImpl|sendMailByList:listContractProposalId is null or empty!");
            throw new BaseAppException("ContractProposalsServiceImpl|sendMailByList:listContractProposalId is null or empty!");
        }

        List<ContractProposalsEntity> listEntity;
        if (!Utils.isNullOrEmpty(listEntitySendMail)) {
            listEntity = listEntitySendMail;
        } else {
            listEntity = contractProposalsRepositoryImpl.getListContractProposalByIds(dto.getListContractProposalId());
        }
        Map<Long, String> mapContractFile = new HashMap<>();
        Set<String> listErrorTemplate = new HashSet<>();
        Set<String> listErrorIsSent = new HashSet<>();
        for (ContractProposalsEntity entity : listEntity) {
            if (entity.getIsSentMail() == null && entity.getStatus() >= ContractProposalsEntity.STATUS.WAITING_SIGN) {
                String fileName = getFileNameContract(entity);
                if (Utils.isNullOrEmpty(fileName)) {
                    listErrorTemplate.add(entity.getEmployeeCode());
                } else {
                    mapContractFile.put(entity.getContractProposalId(), fileName);
                }
            } else {
                listErrorIsSent.add(entity.getEmployeeCode());
            }
        }

        if (!Utils.isNullOrEmpty(listErrorTemplate)) {
            throw new BaseAppException(I18n.getMessage("contract.template.notExists", listErrorTemplate.toString()));
        }

        if (!Utils.isNullOrEmpty(listErrorIsSent)) {
            throw new BaseAppException(I18n.getMessage("contract.mail.isSentMail", listErrorIsSent.toString()));
        }

        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        CompletableFuture.supplyAsync(() -> {
            for (ContractProposalsEntity entity : listEntity) {
                log.info("START|sendEmailContract|email={}", entity.getEmail());
                String fileName = mapContractFile.get(entity.getContractProposalId());
                if (!Utils.isNullOrEmpty(fileName)) {
                    Map<String, String> params = new HashMap<>();
                    params.put("employeeName", entity.getFullName());
                    String expiredContactDate = Utils.formatDate(entity.getCurToDate());
                    params.put("expiredContactDate", expiredContactDate);
                    params.put("jobName", entity.getJobName());
                    HrContractTypesEntity contractTypesEntity = contractProposalsRepositoryImpl.get(HrContractTypesEntity.class, Utils.NVL(entity.getCurContractTypeId()));
                    params.put("curContractTypeName", contractTypesEntity.getName());

                    String groupMail;
                    if (entity.getType().equals(Constant.CONTRACT_TYPE.NEW)) {
                        groupMail = Constant.GROUP_SEND_MAIL.EMP_NEW_CONTRACT;
                    } else if (entity.getType().equals(Constant.CONTRACT_TYPE.CONTINUE)) {
                        groupMail = Constant.GROUP_SEND_MAIL.PNS_SEND_MAIL_CONTRACT;
                    } else if (entity.getType().equals(Constant.CONTRACT_TYPE.APPENDIX_SALARY)) {
                        groupMail = Constant.GROUP_SEND_MAIL.EMP_APPENDIX_CONTRACT;
                    } else {
                        log.info("GROUP MAIL INVALID|sendEmailContract|email=" + entity.getEmail());
                        continue;
                    }
                    SendMailParamDTO mailParamDTO = new SendMailParamDTO(groupMail, entity.getEmail(), params, fileName);
                    mailParamDTO.setListCcAddress(dto.getListCcAddress());
                    log.info("sendEmailContract|mailParamDTO={}", mailParamDTO);
//                    commonUtilsService.sendMailNotAsync(mailParamDTO);
                    entity.setIsSentMail(1);
                    entity.setModifiedTime(currentDate);
                    entity.setModifiedBy(userName);
                    contractProposalsRepositoryJPA.save(entity);
                    log.info("END|sendEmailContract|email={}", entity.getEmail());
                }
            }
            return true;
        });

        return BaseResponseEntity.ok(null);
    }

    @Override
    public String getFileNameContract(ContractProposalsEntity entity) throws Exception {
        Document document = contractTemplatesService.getTemplateContract(entity);
        if (document == null) {
            return null;
        }

        // xu lý lay sql theo cau hinh
        List<SqlConfigsResponse> listSQL = sqlConfigsRepository.getListSqlConfig(entity.getContractTypeId(), 1);
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("employeeId", entity.getEmployeeId());
        mapParams.put("contractProposalId", entity.getContractProposalId());
        for (SqlConfigsResponse sqlConfigsResponse : listSQL) {
            List<Map<String, Object>> listMap = contractProposalsRepositoryImpl.getListData(sqlConfigsResponse.getSql(), mapParams);
            if (listMap.size() > 1) {
                FileUtils.aposeReplaceKeys(document, listMap);
            } else if (listMap.size() == 1) {
                listMap.get(0).values().removeAll(Collections.singleton(null));
                FileUtils.aposeReplaceKeys(document, listMap.get(0));
            }
        }
        // replace param mau
        List<TemplateParamsEntity> listTemplateParams = contractProposalsRepositoryImpl.findAll(TemplateParamsEntity.class);
        Map<String, Object> mapTemplateParams = new HashMap<>();
        for (TemplateParamsEntity paramsEntity : listTemplateParams) {
            mapTemplateParams.put(paramsEntity.getCode(), paramsEntity.getDefaultValue());
        }
        FileUtils.aposeReplaceKeys(document, mapTemplateParams);
        String fileName = commonUtilsService.getOnlyFilePathExport(entity.getEmployeeCode() + ".pdf");
        commonUtilsService.savePdfFile(document, fileName);
        return fileName;
    }

    @Override
    public ResponseEntity<Object> importTemplateContractFee() throws Exception {
        String pathTemplate = "template/import/contractProposal/BM_ThayDoi_PhiDichVu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM_ThayDoi_PhiDichVu.xlsx");
    }

    @Override
    @Transactional
    public ResponseEntity<Object> importProcessContractFee(MultipartFile file, HttpServletRequest req) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/contractProposal/BM_ThayDoi_PhiDichVu.xml");
        List<Object[]> dataList = new ArrayList<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> listEmployeeCode = new ArrayList<>();
            for (Object[] obj : dataList) {
                int col = 1;
                listEmployeeCode.add(((String) obj[col]).toUpperCase());
            }
            Map<String, EmployeeInfoResponse> mapEmployee = employeeRepositoryImpl.getMapEmpByCodes(listEmployeeCode, Scope.CREATE, Constant.RESOURCE.PNS_CONTRACT_PROPOSALS, Constant.EMP_TYPE.OS);

            Date currentDate = new Date();
            String userName = Utils.getUserNameLogin();
            int row = -1;
            List<ContractFeesEntity> contractFeesEntityList = new ArrayList<>();
            Map<String, ContractProposalsDTO> mapCurrentContract = new HashMap<>();
            for (Object[] obj : dataList) {
                row++;
                ContractFeesEntity contractFeesEntity = new ContractFeesEntity();
                int col = 1;
                String employeeCode = ((String) obj[col]).toUpperCase().trim();
                String employeeName = (String) obj[col + 1];
                EmployeeInfoResponse empResponse = mapEmployee.get(employeeCode);
                if (empResponse == null || !empResponse.getEmpName().equalsIgnoreCase(employeeName)) {
                    importExcel.addError(row, col, I18n.getMessage("employee.validate.employeeInvalid"), employeeCode);
                    continue;
                } else {
                    contractFeesEntity.setEmployeeId(empResponse.getEmployeeId());
                }
                col = col + 2;
                contractFeesEntity.setFromDate((Date) obj[col++]);
                contractFeesEntity.setToDate((Date) obj[col]);

                // check long nhau
                boolean isConflictProcess = contractProposalsRepositoryImpl.isConflictProcessContractFee(contractFeesEntity.getEmployeeId(), contractFeesEntity.getContractFeeId(), contractFeesEntity.getFromDate(), contractFeesEntity.getToDate());
                if (isConflictProcess) {
                    importExcel.addError(row, col - 1, I18n.getMessage("import.input.invalid.contractFee"), Utils.formatDate(contractFeesEntity.getFromDate()));
                    continue;
                }

                // check hop dong hien tai
                ContractProposalsDTO contractProposalsDTO = contractProposalsRepositoryImpl.getCurrentContractInfo(contractFeesEntity.getEmployeeId(), contractFeesEntity.getFromDate(), contractFeesEntity.getToDate());
                if (Utils.isNullObject(contractProposalsDTO)) {
                    String errorDate = Utils.formatDate(contractFeesEntity.getFromDate()) + " - " + Utils.formatDate(contractFeesEntity.getToDate());
                    importExcel.addError(row, col - 1, I18n.getMessage("import.input.invalid.currentContract", errorDate), null);
                    continue;
                } else {
                    mapCurrentContract.put(contractFeesEntity.getEmployeeId() + Utils.formatDate(contractFeesEntity.getFromDate()), contractProposalsDTO);
                }

                col++;
                contractFeesEntity.setAmountFee((Long) obj[col++]);
                contractFeesEntity.setNote((String) obj[col]);

                contractFeesEntityList.add(contractFeesEntity);
            }
            if (importExcel.hasError()) {// co loi xay ra
                throw new ErrorImportException(file, importExcel);
            } else {// thuc hien insert vao DB
                List<HrContractTypesEntity> contractTypesEntityList = contractProposalsRepositoryImpl.findByProperties(HrContractTypesEntity.class, "code", Constant.CONTRACT_TYPE_AMOUNT_FEE);
                Long contractTypeId = null;
                if (!Utils.isNullOrEmpty(contractTypesEntityList)) {
                    contractTypeId = contractTypesEntityList.get(0).getContractTypeId();
                }
                for (ContractFeesEntity entity : contractFeesEntityList) {
                    entity.setCreatedTime(currentDate);
                    entity.setCreatedBy(userName);
                    entity.setStatus(ContractFeesEntity.STATUS.INIT);
                    entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);

                    contractFeesRepositoryJPA.save(entity);

                    // save contractProposal
                    ContractProposalsEntity contractProposalsEntity = new ContractProposalsEntity();
                    contractProposalsEntity.setEmployeeId(entity.getEmployeeId());
                    contractProposalsEntity.setType(Constant.CONTRACT_TYPE.CONTRACT_FEE);
                    contractProposalsEntity.setContractTypeId(contractTypeId);
                    contractProposalsEntity.setContractByLawId(contractTypeId);
                    contractProposalsEntity.setFromDate(entity.getFromDate());
                    contractProposalsEntity.setToDate(entity.getToDate());
                    contractProposalsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    contractProposalsEntity.setCreatedTime(currentDate);
                    contractProposalsEntity.setCreatedBy(userName);
                    contractProposalsEntity.setStatus(ContractProposalsEntity.STATUS.WAITING_SIGN);
                    contractProposalsEntity.setContractFeeId(entity.getContractFeeId());
                    // thong tin hop dong hien tai
                    ContractProposalsDTO contractProposalsDTO = mapCurrentContract.get(entity.getEmployeeId() + Utils.formatDate(entity.getFromDate()));
                    if (!Utils.isNullObject(contractProposalsDTO)) {
                        contractProposalsEntity.setCurContractNumber(contractProposalsDTO.getCurContractNumber());
                        contractProposalsEntity.setCurContractTypeId(contractProposalsDTO.getCurContractTypeId());
                        contractProposalsEntity.setCurFromDate(contractProposalsDTO.getCurFromDate());
                        contractProposalsEntity.setCurToDate(contractProposalsDTO.getCurToDate());
                        contractProposalsEntity.setOrganizationId(contractProposalsDTO.getOrganizationId());
                        contractProposalsEntity.setJobId(contractProposalsDTO.getJobId());
                        contractProposalsEntity.setPositionId(contractProposalsDTO.getPositionId());
                        contractProposalsDTO.setFromDate(entity.getFromDate());
                        contractProposalsEntity.setContractNumber(getContractNumber(Constant.CONTRACT_TYPE.CONTRACT_FEE, contractProposalsDTO, null));
                    }
                    // xu ly them nguoi ky duyet
                    processAddSigner(contractProposalsEntity);

                    contractProposalsRepositoryJPA.save(contractProposalsEntity);
                }

                return ResponseEntity.ok(null);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
    }

    /**
     * mail cho NLĐ khi hợp đồng đươc import lên hệ thống
     *
     * @param lstEntity
     */
//    private void sendEmpMailApproveFileSigned(List<ContractProposalsEntity> lstEntity, int statusProposal) {
//        CompletableFuture.supplyAsync(() -> {
//            for (ContractProposalsEntity entity : lstEntity) {
//                if (ContractProposalsEntity.STATUS.COMPLETE.equals(statusProposal)) {
//                    if (Utils.isNullOrEmpty(entity.getEmail())) {
//                        continue;
//                    }
//                    log.info("START|sendEmpMailApproveFileSigned|email=" + entity.getEmail());
//                    try {
//                        Map<String, String> params = new HashMap<>();
//                        params.put("employeeName", entity.getFullName());
//                        String expiredContactDate = Utils.formatDate(entity.getToDate());
//                        params.put("expiredContactDate", expiredContactDate);
//                        params.put("fromDate", Utils.formatDate(entity.getFromDate()));
//                        params.put("jobName", entity.getJobName());
//                        params.put("contractTypeName", entity.getContractTypeName());
//                        SendMailParamDTO mailParamDTO = new SendMailParamDTO(Constant.GROUP_SEND_MAIL.EMP_APPROVE_FILE_SIGNED, entity.getEmail(), params);
//                        commonUtilsService.sendMailNotAsync(mailParamDTO);  // thuc hien send mail
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                    }
//                    log.info("END|sendEmpMailApproveFileSigned|email=" + entity.getEmail());
//                }
//            }
//            return true;
//        });
//    }

}
