/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.dto.ContractEvaluationsDTO;
import vn.hbtplus.models.dto.RejectDTO;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.ContractApproversRepositoryImpl;
import vn.hbtplus.repositories.jpa.ContractApproversRepositoryJPA;
import vn.hbtplus.repositories.jpa.ContractEvaluationsRepositoryJPA;
import vn.hbtplus.repositories.jpa.ContractProposalsRepositoryJPA;
import vn.hbtplus.services.*;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Lop impl service ung voi bang PNS_CONTRACT_APPROVERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractApproversServiceImpl implements ContractApproversService {

    private final ContractApproversRepositoryImpl contractApproversImpl;
    private final ContractApproversRepositoryJPA contractApproversJPA;
    private final CommonUtilsService commonUtilsService;
    private final ContractProposalsRepositoryJPA contractProposalsJPA;
    private final ContractEvaluationsRepositoryJPA contractEvaluationsJPA;
    private final FileService fileService;
    private final ContractProposalsService contractProposalsService;
    private final AttachmentService attachmentService;
//    private final MailWarningRepositoryImpl mailWarningRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ContractApproversResponse> searchData(ContractApproversDTO dto) {
        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        BaseDataTableDto<ContractApproversResponse> BaseDataTableDto = contractApproversImpl.searchData(dto);
        for (ContractApproversResponse obj : BaseDataTableDto.getListData()) {
            if (dto.getEmployeeId().equals(obj.getDirectManagerId())) {
                obj.setContractApproverId(obj.getContractApproverId1());
                obj.setApproverId(obj.getDirectManagerId());
                obj.setCursorCurrent(obj.getDmCursorCurrent());
                obj.setApproverLevel(obj.getDmApproverLevel());
            } else if (dto.getEmployeeId().equals(obj.getApprovalLevelPersonId())) {
                obj.setApproverId(obj.getApprovalLevelPersonId());
                obj.setContractApproverId(obj.getContractApproverId2());
                obj.setCursorCurrent(obj.getAlCursorCurrent());
                obj.setApproverLevel(obj.getAlApproverLevel());
            } else {
                obj.setApproverId(obj.getChangeApprovalLevelId());
                obj.setContractApproverId(obj.getContractApproverId3());
                obj.setCursorCurrent(obj.getCalCursorCurrent());
                obj.setApproverLevel(obj.getCalApproverLevel());
            }

            // lay trang thai danh gia cua cap tren => disable button Phe duyet/Thanh ly khi cap tren da danh gia
            Integer isLiquidationNext = null;
            if (obj.getApproverLevel() != null) {
                if (obj.getApproverLevel().equals(1)) {
                    if (Utils.NVL(obj.getDirectManagerId()).equals(obj.getApprovalLevelPersonId())) {
                        isLiquidationNext = obj.getCalIsLiquidation();
                    } else {
                        isLiquidationNext = obj.getAlIsLiquidation();
                    }
                } else if (obj.getApproverLevel().equals(2) && !Utils.NVL(obj.getContractTypeId()).equals(obj.getContractByLawId())) {
                    isLiquidationNext = obj.getCalIsLiquidation();
                }
            }

            obj.setLiquidationStatusOfNext(isLiquidationNext);
        }
        return ResponseUtils.ok(BaseDataTableDto);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveData(ContractApproversDTO dto, List<MultipartFile> files) {
        ContractApproversEntity entity = contractApproversImpl.get(ContractApproversEntity.class, Utils.NVL(dto.getContractApproverId()));
        Long empId = commonUtilsService.getEmpIdLogin();
        if (entity == null || !empId.equals(entity.getApproverId())) {
            log.info("ContractApproversService|saveData|empId invalid|empIdLogin={}|contractApproverId={}", empId, dto.getContractApproverId());
            throw new BaseAppException("ContractApproversService empId invalid");
        }

        ContractProposalsEntity contractProposalsEntity = contractApproversImpl.get(ContractProposalsEntity.class, entity.getContractProposalId());
        if (contractProposalsEntity == null
                || BaseConstants.STATUS.NOT_DELETED.equals(contractProposalsEntity.getIsDeleted())
                || contractProposalsEntity.getStatus() > ContractProposalsEntity.STATUS.WAITING_SIGN) {
            log.info("ContractApproversService|saveData|status invalid|id={}", entity.getContractProposalId());
            throw new BaseAppException("ContractApproversService status invalid");
        }

        List<ContractApproversEntity> listContractApproverNext = contractApproversImpl.findByProperties(ContractApproversEntity.class,
                "contractProposalId", entity.getContractProposalId(), "approverLevel", entity.getApproverLevel() + 1);
        if (!Utils.isNullOrEmpty(listContractApproverNext)) {
            ContractApproversEntity nextEntity = listContractApproverNext.get(0);
            if ((nextEntity.getApproverLevel().equals(2) && nextEntity.getIsLiquidation() != null)
                    || (nextEntity.getApproverLevel().equals(3)
                    && !Utils.NVL(nextEntity.getContractTypeId()).equals(contractProposalsEntity.getContractByLawId())
                    && nextEntity.getIsLiquidation() != null)
            ) { //neu la cap 2 va da danh gia la thanh ly
                log.info("ContractApproversService|saveData|liquidation invalid|id={}", entity.getContractProposalId());
                throw new BaseAppException("ContractApproversService liquidation invalid");
            }
        }
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setContractTypeId(dto.getContractTypeId());
        entity.setFromDate(contractProposalsEntity.getFromDate());
        HrContractTypesEntity contractTypesEntity = contractApproversImpl.get(HrContractTypesEntity.class, entity.getContractTypeId());
//        entity.setToDate(Utils.getToDateContract(entity.getFromDate(), contractTypesEntity.getDuration()));
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contractApproversJPA.save(entity);

        return ResponseUtils.ok(entity.getContractApproverId());
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getDataById(Long id) throws SignatureException {
        Optional<ContractApproversEntity> optional = contractApproversJPA.findById(id);
        Long empId = commonUtilsService.getEmpIdLogin();
        if (optional.isEmpty()
                || !BaseConstants.STATUS.NOT_DELETED.equals(optional.get().getIsDeleted())
                || !empId.equals(optional.get().getApproverId())) {
            log.info("ContractApproversService|getDataById|empId invalid|empIdLogin={}|approverId={}", empId, id);
            throw new BaseAppException("ContractApproversService empId invalid");
        }
        ContractApproversResponse dto = new ContractApproversResponse();
        Utils.copyProperties(dto, optional.get());
        // set thuoc tinh proposal
        ContractProposalsEntity contractProposalsEntity = contractApproversImpl.get(ContractProposalsEntity.class, dto.getContractProposalId());
        ContractProposalsResponse contractProposalsResponse = new ContractProposalsResponse();
        Utils.copyProperties(contractProposalsResponse, contractProposalsEntity);
        dto.setContractProposalsResponse(contractProposalsResponse);

        // set thuoc tinh evaluation
        List<ContractEvaluationsEntity> contractEvaluationsList = contractApproversImpl.findByProperties(ContractEvaluationsEntity.class, "contractProposalId", dto.getContractProposalId());
        if (!contractEvaluationsList.isEmpty()) {
            ContractEvaluationsEntity contractEvaluationsEntity = contractEvaluationsList.get(0);
            ContractEvaluationsResponse contractEvaluationsResponse = new ContractEvaluationsResponse();
            Utils.copyProperties(contractEvaluationsResponse, contractEvaluationsEntity);
            List<Attachment> documentsEntityList = attachmentService.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.PNS_CONTRACT_EVALUATIONS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONTRACT_EVALUATIONS, contractProposalsEntity.getContractProposalId());
            contractEvaluationsResponse.setAttachFileList(documentsEntityList);
            dto.setContractEvaluationsResponse(contractEvaluationsResponse);
        }

        // set thong tin danh gia cua 3 level
        List<ContractApproversEntity> contractApproversEntityList = contractApproversImpl.findByProperties(ContractApproversEntity.class, "contractProposalId", dto.getContractProposalId());
        dto.setListContractApproverEntity(contractApproversEntityList);

        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> keepSigningByList(List<Long> listId) {
        if (Utils.isNullOrEmpty(listId)) {
            throw new BaseAppException("listId is null");
        }

        Long empId = commonUtilsService.getEmpIdLogin();
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<ContractApproversEntity> listSave = new ArrayList<>();
        List<ContractProposalsEntity> listSaveContractProposals = new ArrayList<>();
        for (Long contractProposalId : listId) {
            ContractProposalsEntity contractProposalsEntity = contractApproversImpl.get(ContractProposalsEntity.class, contractProposalId);
            if (contractProposalsEntity == null
                    || BaseConstants.STATUS.NOT_DELETED.equals(contractProposalsEntity.getIsDeleted())
                    || contractProposalsEntity.getStatus() >= ContractProposalsEntity.STATUS.WAITING_SIGN) {
                log.info("ContractApproversService|saveData|status invalid|id={}", contractProposalId);
                throw new BaseAppException("ContractApproversService status invalid");
            }

            ContractApproversEntity entity = contractApproversImpl.getContractApprovers(contractProposalId, empId);
            if (entity == null) {
                log.info("ContractApproversService|keepSigningByList|listContractApprover invalid|empIdLogin={}|contractProposalId={}", empId, contractProposalId);
                throw new BaseAppException("ContractApproversService listContractApprover invalid");
            }

            // validate thong tin truoc khi phe duyet
            validateLevelInfo(entity, contractProposalsEntity);

            if (Utils.isNullObject(entity.getContractTypeId())) {
                entity.setContractTypeId(contractProposalsEntity.getContractTypeId());
                entity.setToDate(contractProposalsEntity.getToDate());
            }
            entity.setContractByLawId(contractProposalsEntity.getContractByLawId());
            entity.setIsLiquidation(Constant.IS_LIQUIDATION.KEEP_SIGNING);
            entity.setCursorCurrent(null);
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userName);

            //xu ly next cursor
            int statusProposal = processNextCursor(entity, listSave);

            if (contractProposalsEntity.getContractTypeId() != null && !contractProposalsEntity.getContractTypeId().equals(entity.getContractTypeId())) {
                HrContractTypesEntity contractTypesEntity = contractApproversImpl.get(HrContractTypesEntity.class, entity.getContractTypeId());
                contractProposalsEntity.setContractNumber(contractProposalsService.getNewOrContinueContractNumber(contractTypesEntity, contractProposalsEntity));
//                contractProposalsEntity.setToDate(Utils.getToDateContract(contractProposalsEntity.getFromDate(), contractTypesEntity.getDuration()));
                entity.setToDate(contractProposalsEntity.getToDate());
            }
            contractProposalsEntity.setModifiedTime(currentDate);
            contractProposalsEntity.setModifiedBy(userName);
            contractProposalsEntity.setStatus(statusProposal);

            listSave.add(entity);
            listSaveContractProposals.add(contractProposalsEntity);

        }
        contractApproversJPA.saveAll(listSave);
        contractProposalsJPA.saveAll(listSaveContractProposals);
//        this.sendEmpMailContract(listSaveContractProposals);

        return ResponseUtils.ok(true);
    }

    private void validateLevelInfo(ContractApproversEntity entity, ContractProposalsEntity contractProposalsEntity) {
        if (Utils.isNullObject(contractProposalsEntity.getContractTypeId())) {
            HrEmployeesEntity employeesEntity = contractApproversImpl.get(HrEmployeesEntity.class, contractProposalsEntity.getEmployeeId());
            throw new BaseAppException(I18n.getMessage("contract.approve.validate.contractType", employeesEntity.getEmployeeCode()));
        }


        if (entity.getApproverLevel().equals(1)) {
            List<ContractApproversEntity> listContractApproverNext = contractApproversImpl.findByProperties(ContractApproversEntity.class,
                    "contractProposalId", entity.getContractProposalId(), "approverLevel", 2);
            if (Utils.isNullOrEmpty(listContractApproverNext) || Utils.isNullObject(listContractApproverNext.get(0).getApproverId())) {
                HrEmployeesEntity employeesEntity = contractApproversImpl.get(HrEmployeesEntity.class, contractProposalsEntity.getEmployeeId());
                throw new BaseAppException(I18n.getMessage("contract.approve.validate.notExistLevel", employeesEntity.getEmployeeCode()));
            }

            if (!contractProposalsEntity.getContractTypeId().equals(contractProposalsEntity.getContractByLawId())) {
                listContractApproverNext = contractApproversImpl.findByProperties(ContractApproversEntity.class,
                        "contractProposalId", entity.getContractProposalId(), "approverLevel", 3);
                if (Utils.isNullOrEmpty(listContractApproverNext) || Utils.isNullObject(listContractApproverNext.get(0).getApproverId())) {
                    HrEmployeesEntity employeesEntity = contractApproversImpl.get(HrEmployeesEntity.class, contractProposalsEntity.getEmployeeId());
                    throw new BaseAppException(I18n.getMessage("contract.approve.validate.notExistLevel", employeesEntity.getEmployeeCode()));
                }
            }
        } else if (entity.getApproverLevel().equals(2)
                && !contractProposalsEntity.getContractTypeId().equals(contractProposalsEntity.getContractByLawId())) {
            List<ContractApproversEntity> listContractApproverNext = contractApproversImpl.findByProperties(
                    ContractApproversEntity.class,
                    "contractProposalId", entity.getContractProposalId(),
                    "approverLevel", 3);
            if (Utils.isNullOrEmpty(listContractApproverNext) || Utils.isNullObject(listContractApproverNext.get(0).getApproverId())) {
                HrEmployeesEntity employeesEntity = contractApproversImpl.get(HrEmployeesEntity.class, contractProposalsEntity.getEmployeeId());
                throw new BaseAppException(I18n.getMessage("contract.approve.validate.notExistLevel3", employeesEntity.getEmployeeCode()));
            }
        }
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> keepSigningAll(ContractApproversDTO dto) {
        dto.setEmployeeId(commonUtilsService.getEmpIdLogin());
        List<ContractApproversEntity> listContractApprover = contractApproversImpl.getListDataByForm(dto);
        if (Utils.isNullOrEmpty(listContractApprover)) {
            throw new BaseAppException(I18n.getMessage("contract.validate.approve.notExists"));
        }
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<ContractApproversEntity> listSave = new ArrayList<>();
        List<ContractProposalsEntity> listSaveContractProposals = new ArrayList<>();
        for (ContractApproversEntity entity : listContractApprover) {
            ContractProposalsEntity contractProposalsEntity = contractApproversImpl.get(ContractProposalsEntity.class, entity.getContractProposalId());
            //validate du lieu dau vao
            validateLevelInfo(entity, contractProposalsEntity);

            entity.setIsLiquidation(Constant.IS_LIQUIDATION.KEEP_SIGNING);
            entity.setCursorCurrent(null);
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userName);

            //xu ly next cursor
            int statusProposal = processNextCursor(entity, listSave);
            if (contractProposalsEntity.getContractTypeId() != null && !contractProposalsEntity.getContractTypeId().equals(entity.getContractTypeId())) {
                HrContractTypesEntity contractTypesEntity = contractApproversImpl.get(HrContractTypesEntity.class, entity.getContractTypeId());
                contractProposalsEntity.setContractNumber(contractProposalsService.getNewOrContinueContractNumber(contractTypesEntity, contractProposalsEntity));
//                contractProposalsEntity.setToDate(Utils.getToDateContract(contractProposalsEntity.getFromDate(), contractTypesEntity.getDuration()));
                entity.setToDate(contractProposalsEntity.getToDate());
            }
            contractProposalsEntity.setModifiedTime(currentDate);
            contractProposalsEntity.setModifiedBy(userName);
            contractProposalsEntity.setStatus(statusProposal);

            listSave.add(entity);
            listSaveContractProposals.add(contractProposalsEntity);

        }
        contractApproversJPA.saveAll(listSave);
        contractProposalsJPA.saveAll(listSaveContractProposals);
//        this.sendEmpMailContract(listSaveContractProposals);
        return ResponseUtils.ok(true);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> liquidationByList(RejectDTO dto) {
        if (Utils.isNullOrEmpty(dto.getListId())) {
            throw new BaseAppException("listId is null");
        }

        Long empId = commonUtilsService.getEmpIdLogin();
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<ContractApproversEntity> listSave = new ArrayList<>();
        List<ContractProposalsEntity> listSaveContractProposals = new ArrayList<>();
        for (Long contractProposalId : dto.getListId()) {
            ContractProposalsEntity contractProposalsEntity = contractApproversImpl.get(ContractProposalsEntity.class, contractProposalId);
            if (contractProposalsEntity == null
                    || BaseConstants.STATUS.NOT_DELETED.equals(contractProposalsEntity.getIsDeleted())
                    || contractProposalsEntity.getStatus() >= ContractProposalsEntity.STATUS.WAITING_SIGN) {
                log.info("ContractApproversService|saveData|status invalid|id={}", contractProposalId);
                throw new BaseAppException("ContractApproversService status invalid");
            }

            ContractApproversEntity entity = contractApproversImpl.getContractApprovers(contractProposalId, empId);
            if (entity == null) {
                log.info("ContractApproversService|liquidationByList|listContractApprover invalid|empIdLogin={}|contractProposalId={}", empId, contractProposalId);
                throw new BaseAppException("ContractApproversService listContractApprover invalid");
            }
            entity.setIsLiquidation(Constant.IS_LIQUIDATION.LIQUIDATION);
            entity.setCursorCurrent(null);
            entity.setModifiedTime(currentDate);
            entity.setModifiedBy(userName);
            entity.setContractByLawId(contractProposalsEntity.getContractByLawId());
            //xu ly next cursor
            int statusProposal = processNextCursor(entity, listSave);
            contractProposalsEntity.setModifiedTime(currentDate);
            contractProposalsEntity.setModifiedBy(userName);
            contractProposalsEntity.setStatus(statusProposal);

            listSave.add(entity);
            listSaveContractProposals.add(contractProposalsEntity);
        }
        contractApproversJPA.saveAll(listSave);
        contractProposalsJPA.saveAll(listSaveContractProposals);

        return ResponseUtils.ok(true);
    }

    private int processNextCursor(ContractApproversEntity entity, List<ContractApproversEntity> listSaveApprover) {
        // xu ly nhay cursor
        int statusProposal;
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        if (entity.getApproverLevel().equals(1)
                || (entity.getApproverLevel().equals(2)
                && !Utils.NVL(entity.getContractTypeId()).equals(entity.getContractByLawId()))
        ) {
            List<ContractApproversEntity> listContractApproverNext = contractApproversImpl.findByProperties(ContractApproversEntity.class,
                    "contractProposalId", entity.getContractProposalId(), "approverLevel", entity.getApproverLevel() + 1);
            ContractApproversEntity nextEntity = listContractApproverNext.get(0);
            if (nextEntity.getIsLiquidation() != null) {
                log.info("processNextCursor|nextEntity={}", new Gson().toJson(nextEntity));
                throw new BaseAppException("ContractApproversService nextEntity is liquidation");
            }

            if (entity.getApproverLevel().equals(1)) {
                //neu 2 cap cung 1 nguoi xet duyet
                if (entity.getApproverId().equals(nextEntity.getApproverId())) {
                    nextEntity.setContractTypeId(entity.getContractTypeId());
                    nextEntity.setIsLiquidation(entity.getIsLiquidation());
                    nextEntity.setFromDate(entity.getFromDate());
                    nextEntity.setToDate(entity.getToDate());
                    nextEntity.setModifiedTime(currentDate);
                    nextEntity.setModifiedBy("clone level 1");
                    if (Utils.NVL(entity.getContractTypeId()).equals(entity.getContractByLawId())) {
                        statusProposal = ContractProposalsEntity.STATUS.WAITING_SIGN;
                        nextEntity.setCursorCurrent(1);
                    } else {
                        List<ContractApproversEntity> listApproverLevel3 = contractApproversImpl.findByProperties(
                                ContractApproversEntity.class,
                                "contractProposalId", entity.getContractProposalId(),
                                "approverLevel", 3);
                        ContractApproversEntity nextLevel3Entity = listApproverLevel3.get(0);
                        nextLevel3Entity.setCursorCurrent(1);
                        nextLevel3Entity.setModifiedTime(currentDate);
                        nextLevel3Entity.setModifiedBy(userName);
                        listSaveApprover.add(nextLevel3Entity);
                        statusProposal = ContractProposalsEntity.STATUS.WAITING_APPROVE;
                    }
                } else {
                    nextEntity.setCursorCurrent(1);
                    nextEntity.setModifiedTime(currentDate);
                    nextEntity.setModifiedBy(userName);
                    statusProposal = ContractProposalsEntity.STATUS.MANAGER_EVALUATE;
                }
                listSaveApprover.add(nextEntity);
            } else {// neu la level 2
                if (entity.getApproverId().equals(nextEntity.getApproverId())) {
                    nextEntity.setContractTypeId(entity.getContractTypeId());
                    nextEntity.setIsLiquidation(entity.getIsLiquidation());
                    nextEntity.setFromDate(entity.getFromDate());
                    nextEntity.setToDate(entity.getToDate());
                    nextEntity.setModifiedTime(currentDate);
                    nextEntity.setModifiedBy("clone level 2");
                    nextEntity.setCursorCurrent(1);
                    listSaveApprover.add(nextEntity);
                    statusProposal = ContractProposalsEntity.STATUS.WAITING_SIGN;
                } else {
                    nextEntity.setCursorCurrent(1);
                    nextEntity.setModifiedTime(currentDate);
                    nextEntity.setModifiedBy(userName);
                    statusProposal = ContractProposalsEntity.STATUS.WAITING_APPROVE;
                }
                listSaveApprover.add(nextEntity);
            }

        } else {// hoan tat quy trinh phe duyet
            statusProposal = ContractProposalsEntity.STATUS.WAITING_SIGN;
        }

        if (ContractProposalsEntity.STATUS.WAITING_SIGN.equals(statusProposal)
                && entity.getIsLiquidation().equals(Constant.IS_LIQUIDATION.LIQUIDATION)) {
            statusProposal = ContractProposalsEntity.STATUS.LIQUIDATION;
        }
        return statusProposal;
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> countValidRecord(ContractApproversDTO dto) {
        dto.setEmployeeId(commonUtilsService.getEmpIdLogin());
        int count = contractApproversImpl.countApprovalRecord(dto);
        return ResponseUtils.ok(count);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Object> saveEvaluation(ContractEvaluationsDTO dto, List<MultipartFile> files) {
        ContractProposalsEntity contractProposalsEntity = contractApproversImpl.get(ContractProposalsEntity.class, dto.getContractProposalId());
        if (contractProposalsEntity == null
                || BaseConstants.STATUS.NOT_DELETED.equals(contractProposalsEntity.getIsDeleted())
                || contractProposalsEntity.getStatus() > ContractProposalsEntity.STATUS.WAITING_SIGN) {
            throw new BaseAppException("ContractApproversService status invalid");
        }

        Long empId = commonUtilsService.getEmpIdLogin();
        List<ContractApproversEntity> listContractApprover = contractApproversImpl.findByProperties(ContractApproversEntity.class,
                "contractProposalId", dto.getContractProposalId(), "approverId", empId, "approverLevel", 1);
        // neu khong phai la quan ly truc tiep => khong dc phep danh gia
        if (Utils.isNullOrEmpty(listContractApprover)) {
            log.info("ContractApproversService|saveEvaluation|listContractApprover is null or level invalid|contractProposalId={}", dto.getContractProposalId());
            throw new BaseAppException("ContractApproversService listContractApprover is null or level invalid");
        }

        List<ContractEvaluationsEntity> listEvaluation = contractApproversImpl.findByProperties(ContractEvaluationsEntity.class, "contractProposalId", dto.getContractProposalId());
        ContractEvaluationsEntity entity;
        if (Utils.isNullOrEmpty(listEvaluation)) {
            entity = new ContractEvaluationsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        } else {
            entity = listEvaluation.get(0);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(entity, dto);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        if (entity.getIsDisciplined().equals(0)) {
            entity.setDisciplinedNote(null);
        }
        contractEvaluationsJPA.save(entity);
        fileService.deActiveFile(dto.getDocIdsDelete(), Constant.ATTACHMENT.TABLE_NAMES.PNS_CONTRACT_EVALUATIONS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONTRACT_EVALUATIONS);
        fileService.uploadFiles(files, entity.getContractProposalId(), Constant.ATTACHMENT.TABLE_NAMES.PNS_CONTRACT_EVALUATIONS, Constant.ATTACHMENT.FILE_TYPES.PNS_CONTRACT_EVALUATIONS, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getContractEvaluationId());
    }

//    /**
//     * mail cho nhan vien khi hop dong duoc phe duyet
//     *
//     * @param lstEntity
//     */
//    private void sendEmpMailContract(List<ContractProposalsEntity> lstEntity) {
//        CompletableFuture.supplyAsync(() -> {
//            for (ContractProposalsEntity entity : lstEntity) {
//                if (ContractProposalsEntity.STATUS.WAITING_SIGN.equals(entity.getStatus())) {
//                    EmployeesDTO employeesDTO = mailWarningRepository.getEmployeeInfo(entity.getEmployeeId());
//                    if (Utils.isNullOrEmpty(employeesDTO.getEmail())) {
//                        continue;
//                    }
//                    entity.setEmployeeCode(employeesDTO.getEmployeeCode());
//                    entity.setFullName(employeesDTO.getFullName());
//
//                    log.info("START|sendEmailContract|email=" + employeesDTO.getEmail());
//                    try {
//                        String fileName = contractProposalsService.getFileNameContract(entity);
//                        if (Utils.isNotBlank(fileName)) {
//                            Map<String, String> params = new HashMap<>();
//                            params.put("employeeName", entity.getFullName());
//                            String expiredContactDate = Utils.formatDate(entity.getCurToDate());
//                            params.put("expiredContactDate", expiredContactDate);
//                            params.put("jobName", employeesDTO.getJobName());
//                            HrContractTypesEntity contractTypesEntity = contractApproversImpl.get(HrContractTypesEntity.class, Utils.NVL(entity.getCurContractTypeId()));
//                            params.put("curContractTypeName", contractTypesEntity.getName());
//
//                            SendMailParamDTO mailParamDTO = new SendMailParamDTO(Constants.GROUP_SEND_MAIL.PNS_SEND_MAIL_CONTRACT, employeesDTO.getEmail(), params, fileName);
//                            commonUtilsService.sendMailNotAsync(mailParamDTO);
//                            entity.setIsSentMail(1);
//                            contractProposalsJPA.save(entity);
//                        }
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                    }
//                    log.info("END|sendEmail|email=" + employeesDTO.getEmail());
//                }
//            }
//            return true;
//        });
//    }

}
