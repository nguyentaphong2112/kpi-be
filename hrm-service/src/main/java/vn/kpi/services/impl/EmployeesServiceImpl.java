/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.configs.BaseCachingConfiguration;
import vn.kpi.configs.MdcForkJoinPool;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.feigns.AdminFeignClient;
import vn.kpi.feigns.FileStorageFeignClient;
import vn.kpi.feigns.ReportFeignClient;
import vn.kpi.models.Attachment;
import vn.kpi.models.AttachmentFileDto;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.dto.AttachmentDto;
import vn.kpi.models.dto.ConcurrentProcessDto;
import vn.kpi.models.dto.EmployeeInfoDto;
import vn.kpi.models.dto.HrmParameterDto;
import vn.kpi.models.request.ContactAddressesRequest;
import vn.kpi.models.request.CreateUserRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.AllowanceProcessResponse;
import vn.kpi.models.response.BankAccountsResponse;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ContractProcessResponse;
import vn.kpi.models.response.EducationCertificatesResponse;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.EvaluationResultsResponse;
import vn.kpi.models.response.FamilyRelationshipsResponse;
import vn.kpi.models.response.InsuranceSalaryProcessResponse;
import vn.kpi.models.response.PositionSalaryProcessResponse;
import vn.kpi.repositories.entity.CategoryEntity;
import vn.kpi.repositories.entity.ContactAddressesEntity;
import vn.kpi.repositories.entity.EmployeesEntity;
import vn.kpi.repositories.impl.AllowanceProcessRepository;
import vn.kpi.repositories.impl.BankAccountsRepository;
import vn.kpi.repositories.impl.ConcurrentProcessRepository;
import vn.kpi.repositories.impl.EmployeesRepository;
import vn.kpi.repositories.impl.EvaluationResultsRepository;
import vn.kpi.repositories.impl.InsuranceSalaryProcessRepository;
import vn.kpi.repositories.impl.PositionSalaryProcessRepository;
import vn.kpi.repositories.jpa.ContactAddressesRepositoryJPA;
import vn.kpi.repositories.jpa.EmployeeRequestsRepositoryJPA;
import vn.kpi.repositories.jpa.EmployeesRepositoryJPA;
import vn.kpi.repositories.jpa.WorkProcessRepositoryJPA;
import vn.kpi.services.AttachmentService;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.ParameterService;
import vn.kpi.services.ReportService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ExportWorld;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang hr_employees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeesServiceImpl implements EmployeesService {

    private final EmployeesRepository employeesRepository;
    private final AdminFeignClient adminFeignClient;
    private final BankAccountsRepository bankAccountsRepository;
    private final EmployeesRepositoryJPA employeesRepositoryJPA;
    private final WorkProcessRepositoryJPA workProcessRepositoryJPA;
    private final ParameterService parameterService;
    private final EmployeeRequestsRepositoryJPA employeeRequestsRepositoryJPA;
    private final ConcurrentProcessRepository concurrentProcessRepository;
    private final AllowanceProcessRepository allowanceProcessRepository;
    private final InsuranceSalaryProcessRepository insuranceSalaryProcessRepository;
    private final PositionSalaryProcessRepository positionSalaryProcessRepository;
    private final EvaluationResultsRepository evaluationResultsRepository;
    private final AttachmentService attachmentService;
    private final FileStorageFeignClient storageFeignClient;
    private final HttpServletRequest request;
    private final ObjectAttributesService objectAttributesService;
    private final ContactAddressesRepositoryJPA contactAddressesRepositoryJPA;
    private final MdcForkJoinPool forkJoinPool;
    private final ReportService reportService;
    private final ReportFeignClient reportFeignClient;


    @Override
    @Transactional
    public ResponseEntity saveData(EmployeesRequest.SubmitForm dto) throws BaseAppException, InstantiationException, IllegalAccessException {
        if (Utils.isNullOrEmpty(dto.getEmployeeCode())) {
            //check xem employeeCode da ton tai
            if (getEmployeeId(dto.getEmployeeCode()) != null) {
                throw new BaseAppException("Mã nhân viên đã tồn tại trong hệ thống!");
            }
        }
        EmployeesEntity entity;
        if (dto.getEmployeeId() != null && dto.getEmployeeId() > 0L) {
            entity = employeesRepositoryJPA.getById(dto.getEmployeeId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        if (Utils.isNullOrEmpty(entity.getEmployeeCode())) {
            entity.setEmployeeCode(this.getNextEmployeeCode());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setStatus(Constant.EMP_STATUS.WORK_IN);
        employeesRepositoryJPA.save(entity);
        if (dto.getEmployeeId() == null) {
            CreateUserRequest createUserRequest = new CreateUserRequest();
            createUserRequest.setEmail(entity.getEmail());
            createUserRequest.setEmployeeCode(entity.getEmployeeCode());
            createUserRequest.setLoginName(entity.getEmployeeCode());
            createUserRequest.setFullName(entity.getFullName());
            createUserRequest.setMobileNumber(entity.getMobileNumber());
            //xu ly them moi user
            adminFeignClient.createUser(Utils.getRequestHeader(request), createUserRequest);
        }

        return ResponseUtils.ok(entity.getEmployeeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmployeesEntity> optional = employeesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeesEntity.class);
        }
        employeesRepository.deActiveObject(EmployeesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeesResponse.PersonalInfo getDataById(Long employeeId) throws RecordNotExistsException, ExecutionException, InterruptedException {
        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        //#0
        Supplier<Object> supplier = () -> employeesRepository.getEmployeeInfo(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#1
        supplier = () -> employeesRepository.getJobBeforeRecruitment(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#2
        supplier = () -> employeesRepository.getListFamilyRelationship(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#3
        supplier = () -> employeesRepository.getListCertificates(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#4
        supplier = () -> allowanceProcessRepository.getListCurrent(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#5
        supplier = () -> employeesRepository.getContractInfo(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#6
        supplier = () -> evaluationResultsRepository.getLastEvaluation(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#7
        supplier = () -> bankAccountsRepository.getBankAccount(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#8
        supplier = () -> concurrentProcessRepository.getListProcess(employeeId, new Date());
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#9
        supplier = () -> insuranceSalaryProcessRepository.getCurrentProcess(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#10
        supplier = () -> positionSalaryProcessRepository.getCurrentProcess(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#11
        supplier = () -> employeesRepository.getAwardTitle(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));
        //#12
        supplier = () -> employeesRepository.getDisciplineTitle(employeeId);
        completableFutures.add(CompletableFuture.supplyAsync(supplier, forkJoinPool));

        CompletableFuture<Void> allReturns = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFeatures = allReturns.thenApply(item -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<Object> listData = allFeatures.get();


        EmployeeInfoDto employeeInfoDto = (EmployeeInfoDto) listData.get(0);

        String jobBeforeRecruitment = (String) listData.get(1);
        List<FamilyRelationshipsResponse.DetailBean> listFamily = (List<FamilyRelationshipsResponse.DetailBean>) listData.get(2);
        List<EducationCertificatesResponse.DetailBean> listCertificates = (List<EducationCertificatesResponse.DetailBean>) listData.get(3);
        List<AllowanceProcessResponse.DetailBean> listAllowances = (List<AllowanceProcessResponse.DetailBean>) listData.get(4);
        ContractProcessResponse.DetailBean contractInfo = (ContractProcessResponse.DetailBean) listData.get(5);
        EvaluationResultsResponse.DetailBean evaluationResult = (EvaluationResultsResponse.DetailBean) listData.get(6);
        BankAccountsResponse.DetailBean bankAccount = (BankAccountsResponse.DetailBean) listData.get(7);

        InsuranceSalaryProcessResponse.DetailBean insuranceSalaryDto = (InsuranceSalaryProcessResponse.DetailBean) listData.get(9);
        if (insuranceSalaryDto == null) {
            insuranceSalaryDto = new InsuranceSalaryProcessResponse.DetailBean();
        }

        PositionSalaryProcessResponse.DetailBean positionSalaryDto = (PositionSalaryProcessResponse.DetailBean) listData.get(10);
        if (positionSalaryDto == null) {
            positionSalaryDto = new PositionSalaryProcessResponse.DetailBean();
        }
        List<String> awardTitle = (List<String>) listData.get(11);
        List<String> disciplineTitle = (List<String>) listData.get(12);


        //lay thong tin bref-info
        EmployeesResponse.PersonalInfo personalInfo = Utils.copyProperties(employeeInfoDto, new EmployeesResponse.PersonalInfo());
        personalInfo.setPositionTitle(getPositionTitle(employeeInfoDto));

        EmployeesResponse.InfoBean infoBean = new EmployeesResponse.InfoBean("BRIEF_INFO");
        infoBean.addInfo("mobileNumber", "employee.mobileNumber", employeeInfoDto.getMobileNumber(), 1);
        infoBean.addInfo("dateOfBirth", "employee.dateOfBirth", Utils.formatDate(employeeInfoDto.getDateOfBirth()), 1);
        infoBean.addInfo("personalIdNo", Utils.NVL(employeeInfoDto.getIdentityType(), "employee.personalId"),
                Utils.NVL(employeeInfoDto.getIdentityNo(), "Chưa cập nhật"), 1);
        infoBean.addInfo("empType", "employee.empType", contractInfo == null ? "Chưa cập nhật" :
                Utils.NVL(contractInfo.getContractTypeName(), Utils.NVL(contractInfo.getEmpTypeName(), "Chưa cập nhật")), 1);
        infoBean.addInfo("seniority", "employee.seniority",
                employeeInfoDto.getSeniority() % 12 == 0 ? String.format("%d năm", employeeInfoDto.getSeniority() / 12) : String.format("%d năm %d tháng", employeeInfoDto.getSeniority() / 12, employeeInfoDto.getSeniority() % 12), 1);
        personalInfo.getInfoBeans().add(infoBean);

        EmployeesResponse.InfoBean baseInfo = new EmployeesResponse.InfoBean("BASIC_INFO");
        baseInfo.addInfo("ethnicId", "employee.ethnic",
                Utils.NVL(employeeInfoDto.getEthnicName(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("religionId", "employee.religion",
                Utils.NVL(employeeInfoDto.getReligionName(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("maritalStatus", "employee.maritalStatus",
                Utils.NVL(employeeInfoDto.getMaritalStatusName(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("educationLevelId", "employee.educationLevelId",
                Utils.NVL(employeeInfoDto.getEducationLevelName(), "Chưa cập nhật"), 1);

        baseInfo.addInfo("mobileNumber", "employee.mobileNumber", employeeInfoDto.getMobileNumber(), 1);
        baseInfo.addInfo("personalIdNo", Utils.NVL(employeeInfoDto.getIdentityType(), "employee.personalId"),
                Utils.NVL(employeeInfoDto.getIdentityNo(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("taxNo", "employee.taxNo",
                Utils.NVL(employeeInfoDto.getTaxNo(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("insuranceNo", "employee.insuranceNo",
                Utils.NVL(employeeInfoDto.getInsuranceNo(), "Chưa cập nhật"), 1);

        baseInfo.addInfo("placeOfBith", "employee.placeOfBith",
                Utils.NVL(employeeInfoDto.getPlaceOfBirth(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("originalAddress", "employee.originalAddress",
                Utils.NVL(employeeInfoDto.getOriginalAddress(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("permanentAddress", "employee.permanentAddress",
                Utils.NVL(employeeInfoDto.getPermanentAddress(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("currentAddress", "employee.currentAddress",
                Utils.NVL(employeeInfoDto.getCurrentAddress(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("bankAccount", "employee.bankAccount",
                bankAccount == null ? "Chưa cập nhật" :
                        MessageFormat.format("{0} ({1})", bankAccount.getAccountNo(), bankAccount.getBankCode()), 1);
        baseInfo.addInfo("jobBeforeRecruitment", "employee.jobBeforeRecruitment",
                Utils.NVL(jobBeforeRecruitment, "Chưa cập nhật"), 1);

        personalInfo.getInfoBeans().add(baseInfo);

        final EmployeesResponse.InfoBean familyInfo = new EmployeesResponse.InfoBean("FAMILY_INFO");
        listFamily.forEach(bean -> {
            familyInfo.addInfo(bean.getRelationTypeId(),
                    bean.getRelationTypeName(),
                    Utils.NVL(Utils.join(", ", String.format("%s (%s)", bean.getFamilyRelationshipName(), Utils.NVL(Utils.formatDate(bean.getDateOfBirth(), "yyyy"), " - ")),
                            bean.getRelationStatusName(), bean.getJob(), bean.getOrganizationAddress()), "Chưa cập nhật"), 1);
        });
        personalInfo.getInfoBeans().add(familyInfo);

        EmployeesResponse.InfoBean eduInfo = new EmployeesResponse.InfoBean("EDU_INFO");
        if (!Utils.isNullOrEmpty(employeeInfoDto.getPromotionRankName())) {
            eduInfo.addInfo("01", "Học hàm được phong",
                    Utils.NVL(String.format("%s (%d)", employeeInfoDto.getPromotionRankName(),
                            Utils.NVL(employeeInfoDto.getPromotionRankYear())), "Chưa cập nhật"), 1);
        }
        eduInfo.addInfo("01", "Trình độ chuyên môn",
                Utils.NVL(Utils.join(" - ", employeeInfoDto.getMajorLevelName(), employeeInfoDto.getMajorName()), "Chưa cập nhật"), 1);
        listCertificates.forEach(item -> {
            eduInfo.addInfo("01", item.getCertificateTypeName(),
                    Utils.NVL(Utils.join(", ", item.getCertificateName(), item.getResult()), "Chưa cập nhật"), 1);
        });
        personalInfo.getInfoBeans().add(eduInfo);

        baseInfo = new EmployeesResponse.InfoBean("WORK_INFO");

        baseInfo.addInfo("01", "Đối tượng", contractInfo == null ? "Chưa cập nhật" : Utils.NVL(contractInfo.getContractTypeName(), contractInfo.getEmpTypeName()), 1);
        if (contractInfo != null && contractInfo.getContractTypeName() != null) {
            baseInfo.addInfo("01", "Ngày hết hạn HĐ",
                    Utils.NVL(Utils.formatDate(contractInfo == null ? null : contractInfo.getEndDate()), "Không xác định"), 1);
        }

        if (employeeInfoDto.getPositionName() != null) {
            baseInfo.addInfo("01", "Chức vụ quản lý",
                    Utils.NVL(Utils.join(", ", employeeInfoDto.getPositionName(), employeeInfoDto.getOrganizationName()), "Chưa cập nhật")
                    , 1);
            if (employeeInfoDto.getJobName() != null) {
                baseInfo.addInfo("01", "Chức danh nghề nghiệp", Utils.NVL(employeeInfoDto.getJobName(), "Chưa cập nhật"), 1);
            }
        } else {
            if (employeeInfoDto.getJobName() != null) {
                baseInfo.addInfo("01", "Chức danh nghề nghiệp",
                        Utils.NVL(Utils.join(", ", employeeInfoDto.getJobName(), employeeInfoDto.getOrganizationName()), "Chưa cập nhật")
                        , 1);
            }
        }
        if (!Utils.isNullOrEmpty(employeeInfoDto.getOtherPositionName())) {
            baseInfo.addInfo("01", "Chức vụ kiêm nhiệm", employeeInfoDto.getOtherPositionName(), 1);
        }

        if (Utils.NVL(insuranceSalaryDto.getSalaryAmount()) > 1000) {
            baseInfo.addInfo("01", "Mức hưởng lương nhà nước",
                    Utils.formatNumber(insuranceSalaryDto.getSalaryAmount())
                    , 1);
        } else {
            baseInfo.addInfo("01", "Bậc lương nhà nước",
                    Utils.NVL(Utils.join(" - ", insuranceSalaryDto.getSalaryRankCode(), insuranceSalaryDto.getSalaryGradeName()), "Chưa cập nhật")
                    , 1);
            String heSo = "";
            if (insuranceSalaryDto.getSalaryAmount() == null || insuranceSalaryDto.getSalaryAmount() <= 0) {
                heSo = "Chưa cập nhật";
            } else {
                if (Utils.NVL(insuranceSalaryDto.getPercent(), 100l) < 100) {
                    heSo = Utils.formatNumber(insuranceSalaryDto.getSalaryAmount(), "0.00") + MessageFormat.format(" ({0}%)", insuranceSalaryDto.getPercent());
                } else {
                    heSo = Utils.formatNumber(insuranceSalaryDto.getSalaryAmount(), "0.00");
                }
            }
            baseInfo.addInfo("01", "Hệ số lương nhà nước", heSo, 1);
            baseInfo.addInfo("01", "Hệ số chênh lệch bảo lưu", Utils.NVL(Utils.formatNumber(insuranceSalaryDto.getReserveFactor(), "0.00"), "......"), 1);
            baseInfo.addInfo("01", "Mốc xét nâng bậc", Utils.NVL(Utils.formatDate(insuranceSalaryDto.getIncrementDate(), "MM/yyyy"), "Chưa cập nhật"), 1);
        }

        baseInfo.addInfo("01", "Bậc lương trường",
                Utils.NVL(Utils.join(" - ", positionSalaryDto.getSalaryRankName(), positionSalaryDto.getSalaryGradeName()), "Chưa cập nhật")
                , 1);
        baseInfo.addInfo("01", "Mức hưởng",
                Utils.formatNumber(positionSalaryDto.getSalaryAmount(), "0.00")
                + (Utils.NVL(positionSalaryDto.getPercent(), 100l) < 100 ? MessageFormat.format(" ({0}%)", positionSalaryDto.getPercent()) : "")
                , 1);
        if (listAllowances.isEmpty()) {
            baseInfo.addInfo("01", "Phụ cấp chức vụ", "Không có", 1);
        } else {
            for (AllowanceProcessResponse.DetailBean bean : listAllowances) {
                baseInfo.addInfo("01", bean.getAllowanceTypeName(),
                        Utils.NVL(Utils.formatNumber(bean.getAmount(),
                                bean.getAmount() < 1000d ? "0.00" : "###,###.###"), "Chưa cập nhật")
                        , 1);
            }
        }

        personalInfo.getInfoBeans().add(baseInfo);

        baseInfo = new EmployeesResponse.InfoBean("AWARD_INFO");

        baseInfo.addInfo("01", "Khen thưởng",
                awardTitle.isEmpty() ? "Không có" : Utils.join(", ", awardTitle.toArray(new String[]{})), 1);
        baseInfo.addInfo("01", "Kỷ luật",
                disciplineTitle.isEmpty() ? "Không có" : Utils.join(", ", disciplineTitle.toArray(new String[]{})), 1);
        if (evaluationResult != null) {
            String kpiPoint = "";
            if (evaluationResult.getKpiPoint() != null) {
                kpiPoint = ", Điểm: " + Utils.formatNumber(evaluationResult.getKpiPoint(),
                        evaluationResult.getKpiPoint() < 100d ? "###.##" : "###,###.###");
            }
            baseInfo.addInfo("01", evaluationResult.getEvaluationPeriodName(),
                    "Kết quả: " + evaluationResult.getKpiResult()
                    + evaluationResult.getKpiPoint() == null ? "" : kpiPoint
                    , 1);
        }
        personalInfo.getInfoBeans().add(baseInfo);


        baseInfo = new EmployeesResponse.InfoBean("POLITICAL_INFO");
        baseInfo.addInfo("01", "Ngày vào Đảng",
                Utils.NVL(Utils.formatDate(employeeInfoDto.getPartyDate()), "Không"), 1);
        baseInfo.addInfo("01", "Nơi kết nạp Đảng", Utils.NVL(employeeInfoDto.getPartyPlace(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("01", "Ngày vào Đảng chính thức", Utils.NVL(Utils.formatDate(employeeInfoDto.getPartyOfficialDate()), "Chưa cập nhật"), 1);
        baseInfo.addInfo("01", "Số thẻ Đảng", Utils.NVL(employeeInfoDto.getPartyNumber(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("01", "Thành phần gia đình",
                Utils.NVL(employeeInfoDto.getFamilyPolicyName(), "Chưa cập nhật"), 1);
        baseInfo.addInfo("01", "Thành phần bản thân",
                Utils.NVL(employeeInfoDto.getSelfPolicyName(), "Chưa cập nhật"), 1);
        personalInfo.getInfoBeans().add(baseInfo);

        return personalInfo;
    }

    private String getPositionTitle(EmployeeInfoDto employeeInfoDto) {
        if (employeeInfoDto == null) {
            return "";
        }
        StringBuilder positionTitle = new StringBuilder(Utils.join(", ", employeeInfoDto.getPromotionRankName(),
                employeeInfoDto.getMajorLevelName(),
                Utils.NVL(employeeInfoDto.getPositionName(), employeeInfoDto.getJobName()))
        );
        positionTitle.append(" " + employeeInfoDto.getOrganizationName());
        //Lay chuc danh kiem nhiem
        List<String> positionNames = new ArrayList<>();
        List<ConcurrentProcessDto> listCurrent = concurrentProcessRepository.getListProcess(employeeInfoDto.getEmployeeId(), new Date());
        if (!listCurrent.isEmpty()) {
            listCurrent.forEach(dto -> {
                positionNames.add(Utils.join(" ", dto.getJobName(), dto.getOrganizationName()));
            });
        }
        if (!Utils.isNullOrEmpty(positionNames)) {
            positionTitle.append(", Kiêm ").append(Utils.join(", ", positionNames));
        }
        return positionTitle.toString();
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        ExportExcel dynamicExport;
        List<Map<String, Object>> listDataExport;
        String pathTemplate = "template/export/employee/danh-sach-nhan-su.xlsx";
        dynamicExport = new ExportExcel(pathTemplate, 2, true);
        listDataExport = employeesRepository.getListExport(null, dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-nhan-su.xlsx");
    }

    @Override
    public BaseDataTableDto<EmployeesResponse.SearchResult> searchBasicInfoEmployee(EmployeesRequest.SearchForm dto) {
        return employeesRepository.searchBasicInfoEmployee(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<Object> getAvatar(Long employeeId) {

        Attachment attachmentEntity = attachmentService.getAttachmentEntity(Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE, Constant.ATTACHMENT.FILE_TYPES.EMPLOYEE_AVATAR, employeeId);
        if (attachmentEntity != null) {
            //Lay du lieu file
            byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, ((AttachmentDto) attachmentEntity).getFileId());
            String base64Image = null;
            if (fileContent != null) {
                base64Image = Base64.getEncoder().encodeToString(fileContent);
            }
            return ResponseUtils.ok(base64Image);
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<Object> uploadAvatar(Long employeeId, MultipartFile fileAvatar) throws IOException {
        if (fileAvatar == null) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }

        Optional<EmployeesEntity> optional = employeesRepositoryJPA.findById(employeeId);
        if (optional.isEmpty()) {
            String errorMsg = I18n.getMessage("employee.not.exists");
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, errorMsg);
        }

        String extension = Utils.getExtension(fileAvatar.getOriginalFilename());
        if (StringUtils.isEmpty(extension)
            || !("jpg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension))
//                || Utils.filterImage(fileAvatar) == null
        ) {
            String errorMsg = I18n.getMessage("employee.avatar.not.valid");
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, errorMsg);
        }

        BaseResponse<AttachmentFileDto> response = storageFeignClient.uploadFile(
                Utils.getRequestHeader(request), fileAvatar, Constant.ATTACHMENT.MODULE, Constant.ATTACHMENT.FILE_TYPES.EMPLOYEE_AVATAR, employeeId);


        AttachmentFileDto fileResponse = response.getData();
        attachmentService.inactiveAttachment(
                Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE,
                Constant.ATTACHMENT.FILE_TYPES.EMPLOYEE_AVATAR,
                employeeId);

        attachmentService.saveAttachment(Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE,
                Constant.ATTACHMENT.FILE_TYPES.EMPLOYEE_AVATAR,
                employeeId,
                fileResponse
        );

        return ResponseUtils.ok();
    }


    @Override
    @Transactional
    public ResponseEntity<Object> deleteAvatar(Long employeeId) {
        try {
            Optional<EmployeesEntity> optional = employeesRepositoryJPA.findById(employeeId);
            if (optional.isEmpty()) {
                String errorMsg = I18n.getMessage("employee.not.exists");
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, errorMsg);
            }

            attachmentService.inactiveAttachment(
                    Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE,
                    Constant.ATTACHMENT.FILE_TYPES.EMPLOYEE_AVATAR,
                    employeeId);

            return ResponseUtils.ok();
        } catch (Exception ex) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    public BaseDataTableDto<EmployeesResponse.SearchResult> getEmpDataPicker(EmployeesRequest.SearchForm dto) {
        return employeesRepository.getEmpDataPicker(dto);
    }

    @Override
    public BaseResponseEntity<EmployeeInfoDto> getBasicInfo(Long employeeId) throws RecordNotExistsException {
        EmployeeInfoDto dto = employeesRepository.getPersonalInformation(employeeId);
//        dto.setListAttributes(objectAttributesService.getAttributes(employeeId, employeesRepository.getSQLTableName(EmployeesEntity.class)));
//        List<ContactAddressesEntity> listContactAddresses = employeesRepository.findByProperties(ContactAddressesEntity.class, "employeeId", employeeId);
//        dto.setListContactAddresses(Utils.mapAll(listContactAddresses, ContactAddressesResponse.DetailBean.class));
        return ResponseUtils.ok(dto);
    }

    @Override
    public BaseResponseEntity<Long> saveBasicInfo(EmployeesRequest.PersonalInfoSubmitForm dto, Long employeeId) throws BaseAppException {
        EmployeesEntity entity = employeesRepository.get(EmployeesEntity.class, employeeId);
        if (entity == null) {
            throw new RecordNotExistsException(employeeId, EmployeesEntity.class);
        }
        if (employeesRepository.checkDuplicateTaxNoWithEmp(dto.getTaxNo(), employeeId)) {
            throw new BaseAppException("ERROR_DUPLICATE_TAX_NO", I18n.getMessage("error.personalInfo.taxNo.validate.duplicate"));
        }

        Utils.copyProperties(dto, entity);
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setModifiedTime(new Date());

        //dia chi thuong tru/hien tai
        if (!Utils.isNullOrEmpty(dto.getListContactAddresses())) {
            List<ContactAddressesEntity> listContactAddress = new ArrayList<>();
            employeesRepository.deleteByProperties(ContactAddressesEntity.class, "employeeId", employeeId);
            for (ContactAddressesRequest.SubmitForm contactAddresses : dto.getListContactAddresses()) {
                ContactAddressesEntity contactAddressesEntity = new ContactAddressesEntity();
                Utils.copyProperties(contactAddresses, contactAddressesEntity);
                contactAddressesEntity.setEmployeeId(employeeId);
                listContactAddress.add(contactAddressesEntity);
                if (Constant.ADDRESS_TYPE.THUONG_TRU.equalsIgnoreCase(contactAddresses.getAddressType())) {
                    entity.setPermanentAddress(getFullAddress(contactAddresses));
                } else if (Constant.ADDRESS_TYPE.HIEN_TAI.equalsIgnoreCase(contactAddresses.getAddressType())) {
                    entity.setCurrentAddress(getFullAddress(contactAddresses));
                }
            }
            contactAddressesRepositoryJPA.saveAll(listContactAddress);
        }
        employeesRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(employeeId, dto.getListAttributes(), EmployeesEntity.class, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.THONG_TIN_CO_BAN);
        return ResponseUtils.ok(employeeId);
    }

    private String getFullAddress(ContactAddressesRequest.SubmitForm contactAddresses) {
        StringBuilder strAddress = new StringBuilder();
        if (!Utils.isNullOrEmpty(contactAddresses.getVillageAddress())) {
            strAddress.append(contactAddresses.getVillageAddress()).append(", ");
        }
        String xa = null;
        if (!Utils.isNullOrEmpty(contactAddresses.getWardId())) {
            CategoryEntity categoryWard = employeesRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.XA, "value", contactAddresses.getWardId());
            xa = categoryWard == null ? null : categoryWard.getName();
        }
        String huyen = null;
        if (!Utils.isNullOrEmpty(contactAddresses.getDistrictId())) {
            CategoryEntity categoryDistrict = employeesRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HUYEN, "value", contactAddresses.getDistrictId());
            huyen = categoryDistrict == null ? null : categoryDistrict.getName();
        }
        String tinh = null;

        if (!Utils.isNullOrEmpty(contactAddresses.getProvinceId())) {
            CategoryEntity categoryProvince = employeesRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.TINH, "value", contactAddresses.getProvinceId());
            tinh = categoryProvince == null ? null : categoryProvince.getName();
        }
        return Utils.join(", ", contactAddresses.getVillageAddress(), xa, huyen, tinh);

    }

    @Override
    public boolean updatePoliticalInfo(EmployeesRequest.PoliticalInfo dto, Long employeeId) {
        EmployeesEntity entity = employeesRepositoryJPA.getById(employeeId);
        Utils.copyProperties(dto, entity);
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setModifiedTime(new Date());
        employeesRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(employeeId, dto.getListAttributes(), EmployeesEntity.class, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.CTRI_XAHOI);
        return true;
    }

    @Override
    public EmployeesResponse.PoliticalInfo getPoliticalInfo(Long employeeId) {
        EmployeesEntity entity = employeesRepositoryJPA.getById(employeeId);
        EmployeesResponse.PoliticalInfo politicalInfo = Utils.copyProperties(entity, new EmployeesResponse.PoliticalInfo());
        politicalInfo.setListAttributes(objectAttributesService.getAttributes(employeeId, "hr_employees"));
        return politicalInfo;
    }

    @Override
    @Cacheable(cacheNames = BaseCachingConfiguration.ADMIN_USER_ROLE, key = "'getEmployeeId-' + #employeeCode")
    public Long getEmployeeId(String employeeCode) {
        return employeesRepositoryJPA.getIdByEmployeeCode(employeeCode);
    }

    @Override
    public ExportWorld exportWord(Long employeeId) throws Exception {
        Map params = new HashMap<>();
        params.put("employeeId", employeeId);
        ExportWorld exportWorld = reportService.exportWord("SO_YEU_LY_LICH", params);
        Attachment attachmentEntity = attachmentService.getAttachmentEntity(Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE, Constant.ATTACHMENT.FILE_TYPES.EMPLOYEE_AVATAR, employeeId);
        if (attachmentEntity != null) {
            byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, ((AttachmentDto) attachmentEntity).getFileId());
            exportWorld.insertImageDoc(fileContent);
        }
        return exportWorld;
    }

    @Override
    public String getNextEmployeeCode() throws InstantiationException, IllegalAccessException {
        HrmParameterDto parameterDto = parameterService.getConfig(HrmParameterDto.class, new Date());
        //Lay ma lon nhat
        String prefixEmpCode = Utils.NVL(parameterDto.getPrefixEmpCode()).trim();
        String maxBookNo = employeesRepository.getMaxEmpCode(prefixEmpCode);
        String maxNumber;
        if (Utils.isNullOrEmpty(maxBookNo)) {
            maxNumber = "0";
        } else {
            maxNumber = maxBookNo.replace(prefixEmpCode, "");
        }
        if (!maxNumber.matches("\\d+")) {
            throw new BaseAppException("EMPLOYEE_CODE_INVALID", "error.employee.increment.notIsNumber");
        }
        int startNumber = Integer.valueOf(maxNumber) + 1;
        String empCode = MessageFormat.format("{0}{1}", prefixEmpCode,
                String.format("%0" + (parameterDto.getEmpCodeLength() - prefixEmpCode.length()) + "d", startNumber));
        return empCode;
    }

    @Override
    public BaseDataTableDto<EmployeesResponse.SearchResult> searchEmployeeDirectory(EmployeesRequest.SearchForm dto) {
        return employeesRepository.searchEmployeeDirectory(dto);
    }


    @Override
    public Long getOrgByOrgLevelManage(String employeeCode, Long orgLevelManage) {
        return employeesRepository.getOrgByOrgLevelManage(employeeCode, orgLevelManage);
    }
}
