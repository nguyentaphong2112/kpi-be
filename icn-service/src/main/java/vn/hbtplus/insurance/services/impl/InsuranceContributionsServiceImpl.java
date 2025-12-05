/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.insurance.repositories.entity.*;
import vn.hbtplus.insurance.repositories.impl.*;
import vn.hbtplus.models.BaseApproveRequest;
import vn.hbtplus.insurance.models.ContributionParameterDto;
import vn.hbtplus.insurance.models.EmployeeDto;
import vn.hbtplus.insurance.models.InsuranceContributionsDto;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.insurance.repositories.jpa.ContributionRateRepositoryJPA;
import vn.hbtplus.insurance.repositories.jpa.InsuranceContributionsRepositoryJPA;
import vn.hbtplus.insurance.repositories.jpa.InsuranceRetractionsRepositoryJPA;
import vn.hbtplus.insurance.services.InsuranceContributionsService;
import vn.hbtplus.utils.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang icn_insurance_contributions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceContributionsServiceImpl implements InsuranceContributionsService {

    private final InsuranceContributionsRepository insuranceContributionsRepository;
    private final InsuranceRetractionsRepository insuranceRetractionsRepository;
    private final ConfigParameterRepository configParameterRepository;
    private final InsuranceContributionsRepositoryJPA insuranceContributionsRepositoryJPA;
    private final InsuranceRetractionsRepositoryJPA insuranceRetractionsRepositoryJPA;
    private final ContributionRateRepositoryJPA contributionRateRepositoryJPA;
    private final EmployeeRepository employeeRepository;
    private final EmployeeChangesRepository employeeChangesRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InsuranceContributionsResponse> searchData(InsuranceContributionsRequest.SearchForm dto) {
        return ResponseUtils.ok(insuranceContributionsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(InsuranceContributionsRequest.SubmitForm dto) throws BaseAppException {
        InsuranceContributionsEntity entity;
        if (dto.getInsuranceContributionId() != null && dto.getInsuranceContributionId() > 0L) {
            entity = insuranceContributionsRepositoryJPA.getById(dto.getInsuranceContributionId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new InsuranceContributionsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        insuranceContributionsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getInsuranceContributionId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws BaseAppException {
        Optional<InsuranceContributionsEntity> optional = insuranceContributionsRepositoryJPA.findById(id);
        if (!optional.isPresent()
            || optional.get().isDeleted()
        ) {
            throw new RecordNotExistsException(id, InsuranceContributionsEntity.class);
        }
        InsuranceContributionsEntity entity = optional.get();
        if (entity.getStatus().equals(InsuranceContributionsEntity.STATUS.PHE_DUYET)) {
            throw new BaseAppException("Bản ghi đã được phê duyệt");
        }
        //reset ban ghi truy thu truy linh
        insuranceRetractionsRepositoryJPA.resetRetraction(id);

        insuranceContributionsRepository.deActiveObject(InsuranceContributionsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<InsuranceContributionsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<InsuranceContributionsEntity> optional = insuranceContributionsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InsuranceContributionsEntity.class);
        }
        InsuranceContributionsResponse dto = new InsuranceContributionsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(InsuranceContributionsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/BM_Tinh_trich_nop_bh.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        if (dto.getPeriodDate() != null) {
            String periodName = I18n.getMessage("common.label.period") + ": " + Utils.formatDate(dto.getPeriodDate(), Constant.SHORT_FORMAT_DATE);
            dynamicExport.setText(periodName, 0);
        }

        List<Map<String, Object>> listDataExport = insuranceContributionsRepository.getListExport(dto);
        String fileName = "BM_Xuat_DS_trich_nop.xlsx";
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, fileName);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int calculate(List<String> empCodes, Date periodDate) throws Exception {

        //Lay thong tin co so de tinh bao hiem
        List<InsuranceContributionsDto> contributionsDtos = calculateContributions(empCodes, periodDate, false);
        //xoa ban ghi chua duoc phe duyet
        insuranceContributionsRepository.deleteOldData(periodDate, empCodes);
        insuranceContributionsRepositoryJPA.flush();
        insuranceContributionsRepository.saveAll(contributionsDtos, periodDate);
        insuranceContributionsRepositoryJPA.flush();

        //thuc hien tu dong truy thu BHXH
        insuranceContributionsRepository.autoRetroactive(periodDate, empCodes);

        insuranceContributionsRepositoryJPA.flush();
        //cap nhat du lieu ve don vi hach toan
//        insuranceContributionsRepository.updateDebitOrgId(periodDate);

        //xoa du lieu tinh truy thu truy linh cua ky duoc tinh di
        insuranceRetractionsRepository.deleteOldData(empCodes, periodDate, null);

        return contributionsDtos.size();

    }

    @Override
    public List<InsuranceContributionsDto> calculateContributions(List<String> empCodes, Date periodDate, boolean isRetro) throws Exception {
        Date startDate = Utils.getFirstDay(periodDate);
        Date endDate = Utils.getLastDay(periodDate);
        final ContributionParameterDto parameterDto = configParameterRepository.getConfig(ContributionParameterDto.class, periodDate);
        parameterDto.setIdCongThaiSans(insuranceContributionsRepository.getWorkdayTypeIds(parameterDto.getCongThaiSans()));
        parameterDto.setIdCongTrichNops(insuranceContributionsRepository.getWorkdayTypeIds(parameterDto.getCongTrichNops()));

        List<InsuranceContributionsDto> contributionsDtos = insuranceContributionsRepository.getListToCalculate(empCodes, periodDate, parameterDto, isRetro);
        List<ContributionRateEntity> contributionRateEntities = contributionRateRepositoryJPA.getConfigActive(periodDate);
        Map<String, ContributionRateEntity> mapContributionRates = contributionRateEntities.stream().collect(Collectors.toMap(ContributionRateEntity::getEmpTypeCode, contributionRateEntity -> contributionRateEntity));
        List<InsuranceContributionsDto> listKhongThu = new ArrayList<>();
        List<Long> empIdKoThu = new ArrayList<>();

        //Lay danh sach dieu chinh
        List<EmployeeChangesEntity> lstChanges = employeeChangesRepository.getListEntities(endDate);
        Map<Long, EmployeeChangesEntity> mapChangeEntities = lstChanges.stream().collect(Collectors.toMap(EmployeeChangesEntity::getEmployeeId, entity -> entity));

        contributionsDtos.stream().forEach(dto -> {
            //tinh thong tin trich nop
            if ("HD".equalsIgnoreCase(dto.getEmpTypeCode())) {
                dto.setContractSalary(Math.round(Utils.NVL(dto.getInsuranceFactor()) * parameterDto.getLuongToiThieuVung(dto.getInsuranceRegion())));
                dto.setReserveSalary(Math.round(Utils.NVL(dto.getReserveFactor()) * parameterDto.getLuongToiThieuVung(dto.getInsuranceRegion())));
                dto.setTotalSalary(dto.getContractSalary() + dto.getReserveSalary());
                dto.setInsuranceAgency(Constant.INSURANCE_AGENCY.BHXH_BA_DINH);
            } else {
                dto.setInsuranceAgency(Constant.INSURANCE_AGENCY.BHXH_BQP);
                dto.setContractSalary(Math.round(Utils.NVL(dto.getInsuranceFactor()) * parameterDto.getLuongCoSo()));
                dto.setReserveSalary(Math.round(Utils.NVL(dto.getReserveFactor()) * parameterDto.getLuongCoSo()));
                dto.setPosAllowanceSalary(Math.round(Utils.NVL(dto.getAllowanceFactor()) * parameterDto.getLuongCoSo()));
                dto.setSenioritySalary(Math.round(Utils.NVL(dto.getSeniorityPercent()) / 100 * (dto.getContractSalary() + dto.getReserveSalary())));
                dto.setPosSenioritySalary(Math.round(Utils.NVL(dto.getPosSeniorityPercent()) / 100 * (dto.getContractSalary()
                                                                                                      + dto.getReserveSalary()
                                                                                                      + dto.getPosAllowanceSalary()
                                                                                                      + dto.getSenioritySalary()
                )));
                dto.setTotalSalary(dto.getContractSalary()
                                   + dto.getReserveSalary()
                                   + dto.getPosAllowanceSalary()
                                   + dto.getSenioritySalary()
                                   + dto.getPosSenioritySalary()
                );
            }

            dto.setLeaveTimekeeping(Utils.NVL(parameterDto.getCongTieuChuan()) - Utils.NVL(dto.getInsuranceTimekeeping()));
            if (dto.getLeaveTimekeeping() >= Utils.NVL(parameterDto.getSoNgayNghiToiDa())) {
                if (dto.getLeaveTimekeeping() - Utils.NVL(dto.getMaternityTimekeeping()) >= Utils.NVL(parameterDto.getSoNgayNghiToiDa())) {
                    listKhongThu.add(dto);
                    empIdKoThu.add(dto.getEmployeeId());
                    dto.setType(InsuranceContributionsEntity.TYPES.KO_THU);
                } else {
                    //truong hop nghi thai san
                    dto.setReason("Nghỉ thai sản");
                    dto.setType(InsuranceContributionsEntity.TYPES.THAI_SAN);
                }
            } else {
                dto.setType(InsuranceContributionsEntity.TYPES.THU_BHXH);
                dto.setContributionAmount(mapContributionRates.get(dto.getEmpTypeCode()), parameterDto);
            }
            EmployeeChangesEntity changesEntity = mapChangeEntities.get(dto.getEmployeeId());
            if (changesEntity != null) {
                if (EmployeeChangesEntity.CONTRIBUTION_TYPES.TRICH_NOP.equalsIgnoreCase(changesEntity.getContributionType())
                    && !InsuranceContributionsEntity.TYPES.THU_BHXH.equalsIgnoreCase(dto.getType())) {
                    dto.setType(InsuranceContributionsEntity.TYPES.THU_BHXH);
                    dto.setContributionAmount(mapContributionRates.get(dto.getEmpTypeCode()), parameterDto);
                    dto.setReason("Điều chỉnh thu BHXH tại Đơn vị");
                } else {
                    if (EmployeeChangesEntity.CONTRIBUTION_TYPES.KO_TRICH_NOP.equalsIgnoreCase(changesEntity.getContributionType())
                        && InsuranceContributionsEntity.TYPES.THU_BHXH.equalsIgnoreCase(dto.getType())) {
                        dto.setType(InsuranceContributionsEntity.TYPES.KO_THU);
                        dto.resetContributionAmount();
                        dto.setReason("Điều chỉnh không thu BHXH tại Đơn vị");
                    }
                }
            }

        });
        if (!listKhongThu.isEmpty()) {
            //Lay danh thong tin cong
            Map<Long, String> mapCongNghi = insuranceContributionsRepository.getWorkdayLeaves(empIdKoThu, periodDate, parameterDto.getCongTieuChuan());
            for (InsuranceContributionsDto dto : listKhongThu) {//Lay thong tin nghi trong thang
                String congNghiTrongThang = mapCongNghi.get(dto.getEmployeeId());
                //truong hop khong du cong trich nop bao hiem
                StringBuilder reason = new StringBuilder("Không đủ công trích nộp bảo hiểm");
                if (dto.getStartDate().after(startDate)) {
                    reason.append(String.format(" (Thời gian đủ điều kiện trích nộp từ ngày %s, %s)", Utils.formatDate(dto.getStartDate()), Utils.NVL(congNghiTrongThang)));
                } else if (dto.getEndDate().before(endDate)) {
                    reason.append(String.format(" (Thời gian đủ điều kiện trích nộp đến ngày %s, %s)", Utils.formatDate(dto.getEndDate()), Utils.NVL(congNghiTrongThang)));
                } else if (!Utils.isNullOrEmpty(congNghiTrongThang)) {
                    reason.append(String.format(" (%s)", congNghiTrongThang));
                }
                dto.setReason(reason.toString());
            }
        }
        return contributionsDtos;
    }

    @Override
    @Transactional
    public int retroByIds(Date periodDate, List<Long> ids) throws BaseAppException {
        List<InsuranceRetractionsEntity> insuranceRetractions = insuranceContributionsRepository.findByListId(InsuranceRetractionsEntity.class, ids);
        for (InsuranceRetractionsEntity item : insuranceRetractions) {
            if (item.isDeleted()) {
                throw new RecordNotExistsException(item.getInsuranceRetractionId(), InsuranceRetractionsEntity.class);
            }
            if (!item.getPeriodDate().before(periodDate)) {
                throw new BaseAppException(String.format("Chỉ được thực hiện truy thu/truy lĩnh sau kỳ tháng %s", Utils.formatDate(item.getPeriodDate(), "MM/yyyy")));
            }
            if (item.getRetroPeriodDate() != null) {
                throw new BaseAppException(String.format("Dữ liệu đã được thực hiện truy tru/truy lĩnh trong kỳ tháng %s", Utils.formatDate(item.getRetroPeriodDate(), "MM/yyyy")));
            }
            //Lay ban ghi thu trong thang
            InsuranceContributionsEntity currentEntity = insuranceContributionsRepository.getInsuranceContribution(periodDate, item.getEmployeeId());


            InsuranceContributionsEntity contributionsEntity = new InsuranceContributionsEntity();
            Utils.copyProperties(item, contributionsEntity);
            contributionsEntity.setCreatedBy(Utils.getUserNameLogin());
            contributionsEntity.setCreatedTime(new Date());
            contributionsEntity.setModifiedBy(null);
            contributionsEntity.setModifiedTime(null);
            contributionsEntity.setPeriodDate(periodDate);
            contributionsEntity.setRetroForPeriodDate(item.getPeriodDate());
            if (item.getType().equals(InsuranceContributionsEntity.TYPES.TRUY_THU)) {
                contributionsEntity.setReason(String.format("Truy thu kỳ %s", Utils.formatDate(item.getPeriodDate(), "MM/yyyy")));
            } else {
                contributionsEntity.setReason(String.format("Truy lĩnh kỳ %s", Utils.formatDate(item.getPeriodDate(), "MM/yyyy")));
            }
            contributionsEntity.setStatus(InsuranceContributionsEntity.STATUS.DU_THAO);
            if (currentEntity != null) {
                contributionsEntity.setOrgId(currentEntity.getOrgId());
                contributionsEntity.setJobId(currentEntity.getJobId());
            }

            insuranceContributionsRepositoryJPA.save(contributionsEntity);


            item.setRetroPeriodDate(periodDate);
            item.setInsuranceContributionId(contributionsEntity.getInsuranceContributionId());

            insuranceRetractionsRepositoryJPA.save(item);

        }
        insuranceContributionsRepositoryJPA.flush();
//        insuranceContributionsRepository.updateDebitOrgId(periodDate);
        return insuranceRetractions.size();
    }

    @Override
    @Transactional
    public int switchType(String type, List<Long> ids, String reason) throws Exception {
        List<InsuranceContributionsEntity> listData = insuranceContributionsRepository.findByListId(InsuranceContributionsEntity.class, ids);
        if (listData.isEmpty()) {
            throw new BaseAppException("Danh sách ids không hợp lệ!");
        }

        final ContributionParameterDto parameterDto = configParameterRepository.getConfig(ContributionParameterDto.class, listData.get(0).getPeriodDate());
        parameterDto.setIdCongThaiSans(insuranceContributionsRepository.getWorkdayTypeIds(parameterDto.getCongThaiSans()));
        parameterDto.setIdCongTrichNops(insuranceContributionsRepository.getWorkdayTypeIds(parameterDto.getCongTrichNops()));
        List<ContributionRateEntity> contributionRateEntities = contributionRateRepositoryJPA.getConfigActive(listData.get(0).getPeriodDate());
        Map<String, ContributionRateEntity> mapContributionRates = contributionRateEntities.stream().collect(Collectors.toMap(ContributionRateEntity::getEmpTypeCode, contributionRateEntity -> contributionRateEntity));
        for (InsuranceContributionsEntity entity : listData) {
            if (entity.getType().equals(InsuranceContributionsEntity.TYPES.TRUY_THU)
                || entity.getType().equals(InsuranceContributionsEntity.TYPES.TRUY_LINH)) {
                throw new BaseAppException("Không được chuyển đổi loại danh sách của bản ghi truy thu/truy lĩnh");
            }
            if (type.equals(entity.getType())) {
                throw new BaseAppException("Loại danh sách chuyển đổi không hợp lệ");
            }
            if (InsuranceContributionsEntity.STATUS.PHE_DUYET.equals(entity.getStatus())) {
                throw new BaseAppException("Không được chuyển đổi loại danh sách của bản ghi đã phê duyệt");
            }
            entity.setType(type);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());

            if (type.equals(InsuranceContributionsEntity.TYPES.THAI_SAN)
                || type.equals(InsuranceContributionsEntity.TYPES.KO_THU)
            ) {
                entity.resetAmount();
                entity.setReason("Chuyển danh sách: " + Utils.NVL(reason));
            } else {
                //tinh so tien can thu bao hiem
                entity.setReason("Chuyển danh sách:" + Utils.NVL(reason));
                entity.setContributionAmount(mapContributionRates.get(entity.getEmpTypeCode()), parameterDto);
            }
            insuranceContributionsRepositoryJPA.save(entity);
        }
        return 0;
    }

    @Override
    @Transactional
    public void retroMedical(Date periodDate, InsuranceContributionsRequest.RetroMedicalForm retroMedicalForm) throws BaseAppException {
        List<String> empCodes = Arrays.asList(retroMedicalForm.getEmpCodes().replace(" ", "").split(","));
        //lay thong tin lan tinh bao hiem gan nhat cua nhan vien
        List<InsuranceContributionsEntity> contributionsEntities = insuranceContributionsRepository.getLastContributions(empCodes, Utils.getLastDay(retroMedicalForm.getFromPeriodDate()));
        if (contributionsEntities.size() < empCodes.size()) {
            throw new BaseAppException("Tồn tại mã nhân viên không hợp lệ!");
        }
        Date pDate = Utils.getLastDay(retroMedicalForm.getFromPeriodDate());
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        List<ContributionRateEntity> contributionRateEntities = contributionRateRepositoryJPA.getConfigActive(periodDate);
        Map<String, ContributionRateEntity> mapContributionRates = contributionRateEntities.stream().collect(Collectors.toMap(ContributionRateEntity::getEmpTypeCode, contributionRateEntity -> contributionRateEntity));
        List<InsuranceContributionsEntity> results = new ArrayList<>();
        while (!pDate.after(Utils.getLastDay(retroMedicalForm.getToPeriodDate()))) {
            for (InsuranceContributionsEntity item : contributionsEntities) {
                //lay du lieu truy thu bao hiem y te
                String medicalPayDate = insuranceContributionsRepository.isMedicalPayed(pDate, item.getEmployeeId());
                if (!Utils.isNullOrEmpty(medicalPayDate)) {
                    HrEmployeesEntity hrEmployeesEntity = insuranceContributionsRepository.get(HrEmployeesEntity.class, item.getEmployeeId());
                    throw new BaseAppException(String.format("Nhân viên %s đã thu BHYT của kỳ %s tại kỳ tháng %s",
                            hrEmployeesEntity.getEmployeeCode() + " - " + hrEmployeesEntity.getFullName(),
                            Utils.formatDate(pDate, "MM/yyyy"), medicalPayDate));
                }
                InsuranceContributionsEntity newEntity = new InsuranceContributionsEntity();
                Utils.copyProperties(item, newEntity);
                newEntity.setInsuranceContributionId(null);
                newEntity.setPeriodDate(pDate);
                newEntity.setCreatedTime(currentDate);
                newEntity.setCreatedBy(userName);
                newEntity.setModifiedBy(null);
                newEntity.setModifiedTime(null);
                newEntity.setType(InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT);
                newEntity.setStatus(InsuranceContributionsEntity.STATUS.DU_THAO);
                newEntity.resetAmount();
                newEntity.setMedicalAmount(mapContributionRates.get(newEntity.getEmpTypeCode()), "Y".equalsIgnoreCase(retroMedicalForm.getIsIndividuals()), "Y".equalsIgnoreCase(retroMedicalForm.getIsUnitPayed()), true);
                newEntity.setReason("Truy thu BHYT kỳ " + Utils.formatDate(pDate, "MM/yyyy"));
                newEntity.setNote("Truy thu BHYT kỳ " + Utils.formatDate(pDate, "MM/yyyy"));
                newEntity.setRetroForPeriodDate(pDate);
                results.add(newEntity);
            }

            pDate = Utils.getLastDay(DateUtils.addMonths(pDate, 1));
        }
        insuranceContributionsRepository.insertBatch(InsuranceContributionsEntity.class, results, userName);
        insuranceContributionsRepositoryJPA.flush();
//        insuranceContributionsRepository.updateDebitOrgId(periodDate);
    }

    @Override
    public ResponseEntity<Object> downloadTemplateRetroMedical(Date periodDate) throws Exception {
        String pathTemplate = "template/import/insurance/BM_Import_truy_thu_bhyt.xlsx";
        byte[] data = getClass().getClassLoader().getResourceAsStream(pathTemplate).readAllBytes();
        String fileName = Utils.removeSign(String.format("BM_Import_truy_thu_bhyt_%s.xlsx", Utils.formatDate(periodDate, "yyyyMM")));
        fileName = fileName.replace(" ", "-");
        return ResponseUtils.getResponseFileEntity(data, fileName);
    }

    @Override
    public void importRetroMedical(Date periodDate, MultipartFile fileImport) throws Exception {
        ImportExcel importExcel = new ImportExcel("template/import/insurance/BM_Import_truy_thu_bhyt.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            List<String> empCodes = new ArrayList<>();
            dataList.forEach(item -> empCodes.add((String) item[1]));
            List<EmployeeDto> dtos = employeeRepository.getEmployeeDtos(empCodes);
            Map<String, EmployeeDto> mapEmps = new HashMap<>();
            dtos.forEach(item -> {
                mapEmps.put(item.getEmployeeCode(), item);
            });
            int row = 0;
            List<ContributionRateEntity> contributionRateEntities = contributionRateRepositoryJPA.getConfigActive(periodDate);
            Map<String, ContributionRateEntity> mapContributionRates = contributionRateEntities.stream().collect(Collectors.toMap(ContributionRateEntity::getEmpTypeCode, contributionRateEntity -> contributionRateEntity));
            final String userName = Utils.getUserNameLogin();
            final Date currentDate = new Date();
            List<InsuranceContributionsEntity> results = new ArrayList<>();
            Map<Date, List<String>> mapPeriodDates = new HashMap<>();


            for (Object[] objs : dataList) {
                Date startDate = Utils.getLastDay(Utils.stringToDate((String) objs[3], "MM/yyyy"));
                Date endDate = Utils.getLastDay(Utils.stringToDate((String) objs[4], "MM/yyyy"));
                String empCode = (String) objs[1];
                if (startDate != null && endDate != null) {
                    Date pDate = startDate;
                    while (!pDate.after(Utils.getLastDay(endDate))) {
                        if (mapPeriodDates.get(pDate) == null) {
                            mapPeriodDates.put(pDate, new ArrayList<>());
                        }
                        mapPeriodDates.get(pDate).add(empCode);
                        pDate = Utils.getLastDay(DateUtils.addMonths(pDate, 1));
                    }
                }
            }
            List<InsuranceContributionsEntity> contributionsEntities = insuranceContributionsRepository.getLastContributions(empCodes, periodDate);
            Map<Long, InsuranceContributionsEntity> mapContributionsEntities = new HashMap<>();
            contributionsEntities.forEach(item -> {
                mapContributionsEntities.put(item.getEmployeeId(), item);
            });
            //Lay danh sach ky da duoc tinh bao hiem
            Map<String, InsuranceContributionsDto> mapPayeds = new HashMap<>();
            for (Date date : mapPeriodDates.keySet()) {
                List<InsuranceContributionsDto> exists = insuranceContributionsRepository.getListEmpMedicalPayed(date, mapPeriodDates.get(date));
                exists.forEach(dto -> {
                    mapPayeds.put(dto.getEmployeeId() + "" + date, dto);
                });
            }

            for (Object[] objs : dataList) {
                if (mapEmps.get(objs[1]) == null) {
                    importExcel.addError(row, 1, "Mã nhân viên không hợp lệ", (String) objs[1]);
                    row++;
                    continue;
                } else {
                    if (!mapEmps.get(objs[1]).getFullName().equalsIgnoreCase((String) objs[2])) {
                        importExcel.addError(row, 2, "Mã nhân viên không trùng với tên nhân viên trong hệ thống", (String) objs[4]);
                    }
                }

                Date startDate = Utils.getLastDay(Utils.stringToDate((String) objs[3], "MM/yyyy"));
                if (startDate == null) {
                    importExcel.addError(row, 3, "Trường từ kỳ phải có định dạng MM/yyyy", (String) objs[3]);
                }
                Date endDate = Utils.getLastDay(Utils.stringToDate((String) objs[4], "MM/yyyy"));
                if (endDate == null) {
                    importExcel.addError(row, 4, "Trường đến kỳ phải có định dạng MM/yyyy", (String) objs[4]);
                }

                if (startDate == null || endDate == null) {
                    break;
                }
                if (!"X".equalsIgnoreCase((String) objs[5]) && !"X".equalsIgnoreCase((String) objs[6])) {
                    importExcel.addError(row, 5, "Phải tích chọn lựa chọn ít nhất cá nhân hoặc đơn vị thực hiện đóng", (String) objs[2]);
                }

                Date pDate = startDate;

//                List<InsuranceContributionsEntity> contributionsEntities = insuranceContributionsRepository.getLastContributions(Arrays.asList(new String[]{(String) objs[1]}), startDate);
                if (mapEmps.get(objs[1]) != null && mapContributionsEntities.get(mapEmps.get(objs[1]).getEmployeeId()) == null) {
                    //Lay thong tin he so luong gan nhat cua nhan vien
                    InsuranceContributionsEntity contributionsEntity = null;
                    Date tempDate = periodDate;
                    int loopDepth = 0;
                    while (loopDepth < 12) {
                        try {
                            List<InsuranceContributionsDto> contributionsDtos = calculateContributions(Arrays.asList((String) objs[1]), tempDate, false);
                            if (!contributionsDtos.isEmpty()) {
                                contributionsEntity = Utils.copyProperties(contributionsDtos.get(0), new InsuranceContributionsEntity());
                                break;
                            }
                            loopDepth++;
                            tempDate = Utils.getLastDay(DateUtils.addMonths(tempDate, -1));
                        } catch (Exception e) {
                            log.error("", e);
                            break;
                        }

                    }

                    if (contributionsEntity == null) {
                        importExcel.addError(row, 2, "Không có thông tin thu BHYT của nhân viên tại kỳ gần nhất", (String) objs[1]);
                    } else {
                        mapContributionsEntities.put(mapEmps.get(objs[1]).getEmployeeId(), contributionsEntity);
                    }
                    continue;
                }

                while (!pDate.after(Utils.getLastDay(endDate))) {
                    InsuranceContributionsEntity item = mapContributionsEntities.get(mapEmps.get(objs[1]).getEmployeeId());
                    //lay du lieu truy thu bao hiem y te
//                        String medicalPayDate = insuranceContributionsRepository.isMedicalPayed(pDate, item.getEmployeeId());
                    if (mapPayeds.get(item.getEmployeeId() + "" + pDate) != null) {
                        HrEmployeesEntity hrEmployeesEntity = insuranceContributionsRepository.get(HrEmployeesEntity.class, item.getEmployeeId());
                        importExcel.addError(row, 2, String.format("Nhân viên %s đã thu BHYT của kỳ %s tại kỳ tháng %s",
                                hrEmployeesEntity.getEmployeeCode() + " - " + hrEmployeesEntity.getFullName(),
                                Utils.formatDate(pDate, "MM/yyyy"),
                                Utils.formatDate(mapPayeds.get(item.getEmployeeId() + "" + pDate).getPeriodDate()), "MM/yyyy"), (String) objs[1]);
                    }
                    InsuranceContributionsEntity newEntity = new InsuranceContributionsEntity();
                    Utils.copyProperties(item, newEntity);
                    newEntity.setInsuranceContributionId(null);
                    newEntity.setPeriodDate(pDate);
                    newEntity.setCreatedTime(currentDate);
                    newEntity.setCreatedBy(userName);
                    newEntity.setModifiedBy(null);
                    newEntity.setModifiedTime(null);
                    newEntity.setType(InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT);
                    newEntity.setStatus(InsuranceContributionsEntity.STATUS.DU_THAO);
                    newEntity.resetAmount();

                    boolean isPersonalPayed = "X".equalsIgnoreCase((String) objs[5]);
                    boolean isUnitPayed = "X".equalsIgnoreCase((String) objs[6]);
                    if ((isPersonalPayed && isUnitPayed) || (!isPersonalPayed))
                        newEntity.setMedicalAmount(mapContributionRates.get(newEntity.getEmpTypeCode()), isPersonalPayed, isUnitPayed, false);
                    newEntity.setReason("Truy thu BHYT kỳ " + Utils.formatDate(pDate, "MM/yyyy"));
                    newEntity.setNote((String) objs[7]);
                    newEntity.setRetroForPeriodDate(pDate);
                    results.add(newEntity);

                    pDate = Utils.getLastDay(DateUtils.addMonths(pDate, 1));
                }

                row++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                insuranceContributionsRepository.insertBatch(InsuranceContributionsEntity.class, results, userName);
                insuranceContributionsRepositoryJPA.flush();
//                insuranceContributionsRepository.updateDebitOrgId(periodDate);
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
    }

    @Override
    public void validateBeforeCalculate(Date periodDate) throws BaseAppException {
        Date prePeriodDate = insuranceContributionsRepository.getPrePeriodNotApproved(periodDate);
        if (prePeriodDate != null) {
            throw new BaseAppException(MessageFormat.format("Dữ liệu của kỳ {0} chưa được phê duyệt, bạn cần phê duyệt dữ liệu của các kỳ liền trước!", Utils.formatDate(prePeriodDate, "MM/yyyy")));
        }
    }

    @Override
    @Transactional
    public List<Long> updateStatusById(BaseApproveRequest dto, String status) throws BaseAppException {
        if (dto.getListId() == null || dto.getListId().isEmpty()) {
            throw new BaseAppException(I18n.getMessage("global.badRequest"));
        }

        List<InsuranceContributionsEntity> listData = insuranceContributionsRepository.findByListId(InsuranceContributionsEntity.class, dto.getListId());
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        for (InsuranceContributionsEntity entity : listData) {
            if (status.equals(entity.getStatus())) {
                throw new BaseAppException(I18n.getMessage("global.badRequest"));
            }
        }
        insuranceContributionsRepository.updateStatus(dto.getListId(), status);
        return dto.getListId();
    }

    @Override
    @Transactional
    public List<Long> updateStatus(InsuranceContributionsRequest.SearchForm dto, String status) throws BaseAppException {
        List<InsuranceContributionsEntity> listEntity = insuranceContributionsRepository.getListDataByForm(dto);
        if (listEntity == null || listEntity.isEmpty()) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        List<Long> ids = listEntity.stream()
                .map(InsuranceContributionsEntity::getInsuranceContributionId)
                .collect(Collectors.toList());
        insuranceContributionsRepository.updateStatus(ids, status);
        return ids;
    }

    private void processApprove(InsuranceContributionsEntity draftEntity) {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();

        draftEntity.setModifiedTime(currentDate);
        draftEntity.setModifiedBy(userName);
        draftEntity.setStatus(InsuranceContributionsEntity.STATUS.PHE_DUYET);
        draftEntity.setRejectReason(null);
        insuranceContributionsRepositoryJPA.save(draftEntity);
    }

}
