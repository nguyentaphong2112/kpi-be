/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.tax.income.models.EmpTaxInfoDto;
import vn.hbtplus.tax.income.models.TaxParameterDto;
import vn.hbtplus.tax.income.models.request.TaxSettlementMastersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.income.models.response.TaxSettlementDetailsResponse;
import vn.hbtplus.tax.income.models.response.TaxSettlementMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.*;
import vn.hbtplus.tax.income.repositories.impl.ConfigParameterRepository;
import vn.hbtplus.tax.income.repositories.impl.TaxDeclareMastersRepository;
import vn.hbtplus.tax.income.repositories.impl.TaxSettlementMastersRepository;
import vn.hbtplus.tax.income.repositories.jpa.TaxSettlementMastersRepositoryJPA;
import vn.hbtplus.tax.income.services.TaxSettlementMastersService;
import vn.hbtplus.utils.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang pit_tax_settlement_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class TaxSettlementMastersServiceImpl implements TaxSettlementMastersService {

    private final TaxSettlementMastersRepository taxSettlementMastersRepository;
    private final TaxSettlementMastersRepositoryJPA taxSettlementMastersRepositoryJPA;
    private final TaxDeclareMastersRepository taxDeclareMastersRepository;
    private final ConfigParameterRepository configParameterRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TaxSettlementMastersResponse> searchData(TaxSettlementMastersRequest.SearchForm dto) {
        return ResponseUtils.ok(taxSettlementMastersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(TaxSettlementMastersRequest.SubmitForm dto) throws BaseAppException {
        TaxSettlementMastersEntity entity;
        if (dto.getTaxSettlementMasterId() != null && dto.getTaxSettlementMasterId() > 0L) {
            entity = taxSettlementMastersRepositoryJPA.getById(dto.getTaxSettlementMasterId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new TaxSettlementMastersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        taxSettlementMastersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getTaxSettlementMasterId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws BaseAppException {
        Optional<TaxSettlementMastersEntity> optional = taxSettlementMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || optional.get().isDeleted()) {
            throw new RecordNotExistsException(id, TaxSettlementMastersEntity.class);
        }

        if (!optional.get().getStatus().equals(TaxDeclareMastersEntity.STATUS.DU_THAO)) {
            throw new BaseAppException(I18n.getMessage("global.validate.delete"));
        }

        taxSettlementMastersRepository.deleteOldData(optional.get());
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TaxSettlementMastersResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<TaxSettlementMastersEntity> optional = taxSettlementMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TaxSettlementMastersEntity.class);
        }
        TaxSettlementMastersResponse dto = new TaxSettlementMastersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(TaxSettlementMastersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/tax/quyet_toan_thue/danh-sach-quyet-toan-thue.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = taxSettlementMastersRepository.getListExport(dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-quyet-toan-thue.xlsx");
    }

    @Override
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        String pathTemplate = "template/import/tax/BM_Import_quyet_toan_thue.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        return ResponseUtils.ok(dynamicExport, "BM_Import_quyet_toan_thue.xlsx", false);
    }

    @Override
    @Transactional
    public Long importSettlement(MultipartFile fileImport, int year) throws IOException, ErrorImportException, ExecutionException, InterruptedException {
        ImportExcel importExcel = new ImportExcel("template/import/tax/BM_Import_quyet_toan_thue.xml");
        List<Object[]> dataList = new ArrayList<>();
        String createBy = Utils.getUserNameLogin();

        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            Map<String, EmpTaxInfoDto> mapEmps = new HashMap<>();
            List<String> empCodes = new ArrayList<>();
            dataList.stream().forEach(item -> {
                if (item[1] != null && !empCodes.contains(item[1])) {
                    empCodes.add((String) item[1]);
                }
            });
            Date endDate = Utils.stringToDate("31/12/" + year);
            //Lay danh sach nhan vien theo ma nhan vien
            List<EmpTaxInfoDto> listEmps = taxDeclareMastersRepository.getEmpTaxInfos(empCodes, endDate);
            listEmps.stream().forEach(item -> {
                mapEmps.put(item.getEmpCode(), item);
            });

            List<TaxSettlementDetailsEntity> listDetailsEntity = new ArrayList<>();
            int row = 0;
            for (Object[] obj : dataList) {
                TaxSettlementDetailsEntity detailsEntity = new TaxSettlementDetailsEntity();
                if (obj[1] != null && mapEmps.get(obj[1]) == null) {
                    importExcel.addError(row, 1, I18n.getMessage("global.validate.employeeCode"), (String) obj[1]);
                } else if (obj[1] != null) {
                    String fullName = (String) obj[2];
                    if (!fullName.equalsIgnoreCase(mapEmps.get(obj[1]).getFullName())) {
                        importExcel.addError(row, 1, I18n.getMessage("global.validate.fullName"), fullName);
                    }
                    detailsEntity.setEmpTypeCode(mapEmps.get(obj[1]).getEmpTypeCode()); //đối tượng vãng lai
                    detailsEntity.setWorkingStatus(mapEmps.get(obj[1]).getStatus()); //trang thai cua nhan vien
                }
                int col = 1;
                detailsEntity.setEmpCode((String) obj[col++]);
                detailsEntity.setFullName((String) obj[col++]);
                detailsEntity.setTaxNo((String) obj[col++]);
                //validate ma so thue
                detailsEntity.setPersonalIdNo((String) obj[col]);
                if (Utils.isNullOrEmpty(detailsEntity.getEmpCode())) {
                    detailsEntity.setEmpTypeCode("VL"); //đối tượng vãng lai
                }

                for (int i = 1; i <= 12; i++) {
                    TaxSettlementMonthsEntity monthsEntity = new TaxSettlementMonthsEntity();
                    monthsEntity.setMonth(i);
                    monthsEntity.setYear(year);
                    monthsEntity.setIncomeTaxable((Long) obj[col + i]);
                    monthsEntity.setTaxCollected((Long) obj[col + i + 12]);
                    monthsEntity.setInsuranceDeduction((Long) obj[col + i + 24]);
                    detailsEntity.getMonthValues().add(monthsEntity);
                }
                col = col + 37;
                Long numOfDependents = (Long) obj[col++];
                if (numOfDependents != null) {
                    detailsEntity.setNumOfDependents(numOfDependents.intValue());
                }
                detailsEntity.setSelfDeduction((Long) obj[col++]);
                detailsEntity.setDependentDeduction((Long) obj[col++]);
                detailsEntity.setTotalIncomeTaxable((Long) obj[col++]);
                detailsEntity.setTotalInsuranceDeduction((Long) obj[col++]);
                detailsEntity.setTotalTaxCollected((Long) obj[col++]);
                detailsEntity.setTotalIncomeTax((Long) obj[col++]);
                detailsEntity.setTotalTaxPayed((Long) obj[col]);

                listDetailsEntity.add(detailsEntity);
                row++;
            }


            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                //xoa du lieu import cu
                TaxSettlementMastersEntity oldMastersEntity = taxSettlementMastersRepository.getTaxSettlementMaster(year, TaxDeclareMastersEntity.INPUT_TYPES.IMPORT);

                taxSettlementMastersRepository.deleteOldData(oldMastersEntity);

                String userName = Utils.getUserNameLogin();
                Date currentDate = new Date();
                TaxSettlementMastersEntity mastersEntity = new TaxSettlementMastersEntity();
                mastersEntity.setStatus(TaxDeclareMastersEntity.STATUS.DU_THAO);
                mastersEntity.setYear(year);
                mastersEntity.setCreatedBy(userName);
                mastersEntity.setCreatedTime(currentDate);
                mastersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                mastersEntity.setInputType(TaxDeclareMastersEntity.INPUT_TYPES.IMPORT);
                taxSettlementMastersRepositoryJPA.save(mastersEntity);

                List<TaxSettlementMonthsEntity> listMonthsEntity = new ArrayList<>();
                Long startDetailId = taxSettlementMastersRepository.getNextId(TaxDeclareDetailsEntity.class, listDetailsEntity.size());
                for (int i = 0; i < listDetailsEntity.size(); i++) {
                    TaxSettlementDetailsEntity detailsEntity = listDetailsEntity.get(i);
                    detailsEntity.setTaxSettlementDetailId(startDetailId + i);
                    detailsEntity.setTaxSettlementMasterId(mastersEntity.getTaxSettlementMasterId());
                    detailsEntity.setYear(mastersEntity.getYear());
                    detailsEntity.setCreatedBy(userName);
                    detailsEntity.setCreatedTime(currentDate);

                    for (TaxSettlementMonthsEntity entry : detailsEntity.getMonthValues()) {
                        entry.setTaxSettlementDetailId(detailsEntity.getTaxSettlementDetailId());
                        entry.setTaxSettlementMasterId(detailsEntity.getTaxSettlementMasterId());
                        listMonthsEntity.add(entry);
                    }
                }
                taxSettlementMastersRepository.insertBatch(TaxSettlementDetailsEntity.class, listDetailsEntity, createBy);
                taxSettlementMastersRepository.insertBatch(TaxSettlementMonthsEntity.class, listMonthsEntity, createBy);
                taxSettlementMastersRepositoryJPA.flush();
                taxSettlementMastersRepository.updateDataMasterById(mastersEntity.getTaxSettlementMasterId(), year);
                return mastersEntity.getTaxSettlementMasterId();
            }

        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
    }

    @Override
    public ResponseEntity<Object> exportDataById(Long id) throws Exception {
        String pathTemplate = "template/export/tax/BM_Bao_cao_chi_tiet_quyet_toan.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);
        Map<String, Object> mapParam = new HashMap<>();
        TaxSettlementMastersEntity mastersEntity = taxSettlementMastersRepository.get(TaxSettlementMastersEntity.class, id);
        if (mastersEntity == null) {
            throw new RecordNotExistsException(id, TaxSettlementMastersEntity.class);
        }

        mapParam.put("ngay_xuat_bc", Utils.formatDate(new Date()));
        mapParam.put("ky_quyet_toan", mastersEntity.getYear());
        dynamicExport.replaceKeys(mapParam);

        List<Map<String, Object>> listExps = taxSettlementMastersRepository.getListDetailsById(mastersEntity);
        List<TaxSettlementMonthsEntity> monthsEntities = taxSettlementMastersRepository.findAllByProperties(TaxSettlementMonthsEntity.class, "year", mastersEntity.getYear(), "taxSettlementMasterId", mastersEntity.getTaxSettlementMasterId());
        Map<Long, Map<String, Long>> mapValues = new HashMap<>();
        monthsEntities.stream().forEach(item -> {
            if (mapValues.get(item.getTaxSettlementDetailId()) == null) {
                mapValues.put(item.getTaxSettlementDetailId(), new HashMap<>());
            }
            mapValues.get(item.getTaxSettlementDetailId()).put("chiu_thue_thang_" + item.getMonth(), item.getIncomeTaxable());
            mapValues.get(item.getTaxSettlementDetailId()).put("thue_tncn_thang_" + item.getMonth(), item.getTaxCollected());
            mapValues.get(item.getTaxSettlementDetailId()).put("bao_hiem_thang_" + item.getMonth(), item.getInsuranceDeduction());
        });
        listExps.stream().forEach(map -> {
            Long detailId = Long.valueOf(map.get("TAX_SETTLEMENT_DETAIL_ID").toString());
            if (mapValues.get(detailId) != null) {
                map.putAll(mapValues.get(detailId));
            }
        });
        dynamicExport.replaceKeys(listExps);
        dynamicExport.setCellFormat(6, 0, dynamicExport.getLastRow() + listExps.size(), 70, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM_QT_05AB.xlsx");
    }

    @Override
    @Transactional
    public TaxSettlementMastersEntity calculate(Integer year, TaxSettlementMastersRequest.CalculateForm calculateForm) throws ExecutionException, InterruptedException, BaseAppException, InstantiationException, IllegalAccessException {
        //Lay danh sach ca nhan nop thue
        List<TaxSettlementDetailsEntity> listEntities = new ArrayList<>();
        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        String createBy = Utils.getUserNameLogin();

        calculateForm.getMonths().stream().forEach(partition -> {
            if (!Utils.isNullOrEmpty(partition.getInputType())) {
                Date taxPeriodDate = Utils.getLastDay(Utils.stringToDate("01/" + partition.getMonth() + "/" + year));
                Supplier<Object> getDatas = () -> taxSettlementMastersRepository.getTaxDeclareOfMonth(taxPeriodDate, partition.getInputType());
                completableFutures.add(CompletableFuture.supplyAsync(getDatas));
            }
        });
        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<Object> objs = allFutures.get();

        objs.stream().forEach(item -> {
            listEntities.addAll((Collection<? extends TaxSettlementDetailsEntity>) item);
        });
        if (listEntities.isEmpty()) {
            throw new BaseAppException("Không tồn tại dữ liệu kê khai thuế!");
        }
        //tong hop du lieu danh sach ca nhan nop thue
        List<String> empCodes = new ArrayList<>();
        List<Long> taxDeclareMasterIds = new ArrayList<>();
        Map<String, TaxSettlementDetailsEntity> mapTaxDeclares = new HashMap<>();
        Map<String, List<TaxSettlementDetailsEntity>> mapEntities = extractedToMapValues(listEntities, empCodes, mapTaxDeclares, taxDeclareMasterIds);

        Date endDate = Utils.stringToDate("31/12/" + year);
        List<EmpTaxInfoDto> listEmps = taxDeclareMastersRepository.getEmpTaxInfos(empCodes, endDate);
        Map<String, EmpTaxInfoDto> mapEmps = new HashMap<>();
        List<String> empNotInVCC = new ArrayList<>();
        listEmps.stream().forEach(item -> {
            mapEmps.put(item.getEmpCode(), item);
            if (item.getOrgId() == null) { //nhan vien khong thuoc VCC
                empNotInVCC.add(item.getEmpCode());
            }
        });


        TaxParameterDto taxParameterDto = configParameterRepository.getConfig(TaxParameterDto.class, endDate);
//        List<TaxRateDetailEntity> ratioDetailEntities = taxRateDetailRepository.getTaxRatios(endDate);

        for (TaxSettlementDetailsEntity entity : mapEntities.get("0")) { //Lay doi tuong co ma nhan vien
            EmpTaxInfoDto taxInfoDto = mapEmps.get(entity.getEmpCode());
            //neu khong tim thay thong tin nhan vien
            //to do
            if (taxInfoDto != null) {
                entity.setTaxNo(taxInfoDto.getTaxNo());
                entity.setPersonalIdNo(taxInfoDto.getPersonalIdNo());
                entity.setEmpTypeCode(taxInfoDto.getEmpTypeCode());
                entity.setWorkingStatus(taxInfoDto.getStatus());
            } else {

            }

            if (mapTaxDeclares.get("1#" + entity.getTaxNo()) != null) { //gop doi tuong trung ma so thue
                entity.add(mapTaxDeclares.get("1#" + entity.getTaxNo()));
                mapTaxDeclares.remove("1#" + entity.getTaxNo());
            }
            if (mapTaxDeclares.get("2#" + entity.getPersonalIdNo()) != null) { //gop doi tuong trung so cccd
                entity.add(mapTaxDeclares.get("2#" + entity.getPersonalIdNo()));
                mapTaxDeclares.remove("2#" + entity.getPersonalIdNo());
            }
        }
        for (TaxSettlementDetailsEntity entity : mapEntities.get("1")) { //Lay doi tuong co ma so thue
            if (mapTaxDeclares.get("1#" + entity.getTaxNo()) != null) {
                if (mapTaxDeclares.get("2#" + entity.getPersonalIdNo()) != null) { //gop doi tuong trung so cccd
                    entity.add(mapTaxDeclares.get("2#" + entity.getPersonalIdNo()));
                    mapTaxDeclares.remove("2#" + entity.getPersonalIdNo());
                }
            }
        }
        TaxSettlementMastersEntity oldMastersEntity = taxSettlementMastersRepository.getTaxSettlementMaster(year, TaxDeclareMastersEntity.INPUT_TYPES.CALCULATE);

        //xoa du lieu cu
        taxSettlementMastersRepository.deleteOldData(oldMastersEntity);

        //Luu thong tin bang master
        final String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        TaxSettlementMastersEntity mastersEntity = new TaxSettlementMastersEntity();
        mastersEntity.setStatus(TaxDeclareMastersEntity.STATUS.DU_THAO);
        mastersEntity.setYear(year);
        mastersEntity.setCreatedBy(userName);
        mastersEntity.setCreatedTime(currentDate);
        mastersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        mastersEntity.setInputType(TaxDeclareMastersEntity.INPUT_TYPES.CALCULATE);
        mastersEntity.setTaxDeclareMasterIds(taxDeclareMasterIds.toString().substring(1, taxDeclareMasterIds.toString().length() - 1));
        taxSettlementMastersRepositoryJPA.save(mastersEntity);

        Long startDetailId = taxSettlementMastersRepository.getNextId(TaxSettlementDetailsEntity.class, mapTaxDeclares.values().size());
        int idx = 0;
        List<TaxSettlementMonthsEntity> monthsEntities = new ArrayList<>();
        for (TaxSettlementDetailsEntity detailsEntity : mapTaxDeclares.values()) {
            //xac dinh bieu mau bao cao
            if (!Utils.NVL(detailsEntity.getInsuranceDeduction()).equals(0L)
                    || taxParameterDto.getDoiTuongLuyTiens().contains(detailsEntity.getEmpTypeCode())) {
                detailsEntity.setReportForm(TaxSettlementDetailsEntity.REPORT_FORM.FORM_05_1);
                detailsEntity.setSelfDeduction(taxParameterDto.getSelfDeduct() * objs.size());
                if (mapEmps.get(detailsEntity.getEmpCode()) != null) {
                    detailsEntity.setNumOfDependents(mapEmps.get(detailsEntity.getEmpCode()).getNumOfDependents());
                }

                Integer numOfDependents = detailsEntity.getNumOfDependents() == null ? 0 : detailsEntity.getNumOfDependents();
                detailsEntity.setDependentDeduction(numOfDependents * objs.size() * taxParameterDto.getDependentDeduct());
            } else {
                detailsEntity.setReportForm(TaxSettlementDetailsEntity.REPORT_FORM.FORM_05_2);
            }
            //


            detailsEntity.setTaxSettlementMasterId(mastersEntity.getTaxSettlementMasterId());
            detailsEntity.setTaxSettlementDetailId(startDetailId + idx);
            detailsEntity.setYear(year);
            detailsEntity.setCreatedBy(userName);
            detailsEntity.setCreatedTime(currentDate);
            if (Utils.isNullOrEmpty(detailsEntity.getEmpTypeCode())) {
                detailsEntity.setEmpTypeCode("VL");
            }
            if (taxParameterDto.getDoiTuongLuyTiens().contains(detailsEntity.getEmpTypeCode())) {

            }

            for (TaxSettlementMonthsEntity monthsEntity : detailsEntity.getMonthValues()) {
                monthsEntity.setTaxSettlementDetailId(detailsEntity.getTaxSettlementDetailId());
                monthsEntity.setYear(year);
                monthsEntity.setTaxSettlementMasterId(detailsEntity.getTaxSettlementMasterId());
                monthsEntities.add(monthsEntity);
            }
            idx++;
        }
        taxSettlementMastersRepository.insertBatch(TaxSettlementDetailsEntity.class, new ArrayList(mapTaxDeclares.values()), createBy);
        taxSettlementMastersRepository.insertBatch(TaxSettlementMonthsEntity.class, monthsEntities, createBy);
        taxSettlementMastersRepositoryJPA.flush();
        taxSettlementMastersRepository.updateStatusTaxDeclare(taxDeclareMasterIds, TaxDeclareMastersEntity.STATUS.DA_QUYET_TOAN);
        taxSettlementMastersRepository.updateDataMasterById(mastersEntity.getTaxSettlementMasterId(), year);
        return mastersEntity;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) throws BaseAppException {
        Optional<TaxSettlementMastersEntity> optional = taxSettlementMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || optional.get().isDeleted()) {
            throw new RecordNotExistsException(id, TaxSettlementMastersEntity.class);
        }
        TaxSettlementMastersEntity mastersEntity = optional.get();
        if (!mastersEntity.getStatus().equals(status)) {
            mastersEntity.setStatus(status);
            mastersEntity.setModifiedBy(Utils.getUserNameLogin());
            mastersEntity.setModifiedTime(new Date());
            taxSettlementMastersRepositoryJPA.save(mastersEntity);
        }
    }

    private static Map<String, List<TaxSettlementDetailsEntity>> extractedToMapValues(List<TaxSettlementDetailsEntity> incomeItemDetailsEntities, List<String> empCodes, Map<String, TaxSettlementDetailsEntity> mapTaxDeclares, List<Long> taxDeclareMasterIds) {
        Map<String, List<TaxSettlementDetailsEntity>> mapResults = new HashMap<>();
        mapResults.put("0", new ArrayList<>());
        mapResults.put("1", new ArrayList<>());
        mapResults.put("2", new ArrayList<>());
        mapResults.put("3", new ArrayList<>());
        incomeItemDetailsEntities.stream().forEach(item -> {
            if (!taxDeclareMasterIds.contains(item.getTaxDeclareMasterId())) {
                taxDeclareMasterIds.add(item.getTaxDeclareMasterId());
            }
            if (!Utils.isNullOrEmpty(item.getEmpCode())) {
                if (empCodes.contains(item.getEmpCode())) {
                    mapTaxDeclares.get(item.getEmpCode()).add(item);
                } else {
                    empCodes.add(item.getEmpCode());
                    mapTaxDeclares.put(item.getEmpCode(), item);
                    item.initMonthValues();
                    mapResults.get("0").add(item);
                }
            } else {
                if (!Utils.isNullOrEmpty(item.getTaxNo())) {
                    //gop theo ma so thue
                    if (mapTaxDeclares.get("1#" + item.getTaxNo()) == null) {
                        mapResults.get("1").add(item);
                        mapTaxDeclares.put("1#" + item.getTaxNo(), item);
                        item.initMonthValues();
                    } else {
                        mapTaxDeclares.get("1#" + item.getTaxNo()).add(item);
                    }
                } else if (!Utils.isNullOrEmpty(item.getPersonalIdNo())) {
                    //gop theo ma so thue
                    if (mapTaxDeclares.get("2#" + item.getPersonalIdNo()) == null) {
                        mapResults.get("2").add(item);
                        mapTaxDeclares.put("2#" + item.getPersonalIdNo(), item);
                        item.initMonthValues();
                    } else {
                        mapTaxDeclares.get("2#" + item.getPersonalIdNo()).add(item);
                    }
                } else {
                    if (mapTaxDeclares.get("3#" + item.getFullName().toLowerCase()) == null) {
                        mapResults.get("3").add(item);
                        mapTaxDeclares.put("3#" + item.getFullName().toLowerCase(), item);
                        item.initMonthValues();
                    } else {
                        mapTaxDeclares.get("3#" + item.getFullName().toLowerCase()).add(item);
                    }
                }
            }
        });
        return mapResults;
    }

    /**
     * Báo cáo tổng hợp theo đầu mối
     *
     * @param id id kỳ
     * @return byte
     * @throws Exception throw loi
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportOrgGroupDetailByMasterId(Long id) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/BM_BC_tong_hop_theo_dau_moi.xlsx";
        int startDataRow = 5;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        // xu ly lay data
        TaxSettlementMastersEntity mastersEntity = taxSettlementMastersRepository.get(TaxSettlementMastersEntity.class, id);
        Date endDate = Utils.stringToDate("31/12/" + mastersEntity.getYear());
        TaxParameterDto taxParameterDto = configParameterRepository.getConfig(TaxParameterDto.class, endDate);
        List<TaxSettlementDetailsResponse> listDataExport = taxSettlementMastersRepository.getListExportByMasterId(id, taxParameterDto.getDoiTuongLuyTiens());


        int index = 1;
        int col = 0;
        for (TaxSettlementDetailsResponse item : listDataExport) {
            col = 0;
            dynamicExport.setEntry(index++, col++);
            dynamicExport.setText(item.getOrgName(), col++);
            dynamicExport.setNumber(item.getCountEmpIn(), col++);
            dynamicExport.setNumber(item.getCountEmpOut(), col++);
            dynamicExport.setNumber(item.getSumIncomeTaxable(), col++);
            dynamicExport.setNumber(item.getSumTaxCollected(), col++);
            dynamicExport.setNumber(item.getSumInsuranceDeduction(), col++);
            col = col + 5;
            dynamicExport.setNumber(item.getSumTotalIncomeTaxableIn(), col++);
            dynamicExport.setNumber(item.getSumTotalIncomeTaxableOut(), col++);
            dynamicExport.setNumber(item.getCountNumOfDependents(), col++);
            dynamicExport.setNumber(item.getSumDeduction(), col++);
            dynamicExport.setNumber(item.getSumTotalInsuranceDeduction(), col++);
            col = col + 2;
            dynamicExport.setNumber(item.getSumTotalTaxCollected(), col++);
            dynamicExport.setNumber(item.getSumTotalIncomeTax(), col++);
            dynamicExport.setNumber(item.getSumTotalTaxPayedSubmitted(), col++);
            dynamicExport.setNumber(item.getSumTotalTaxPayedNotSubmit(), col);

            dynamicExport.increaseRow();
        }
        dynamicExport.setCellFormat(startDataRow - 1, 0, dynamicExport.getLastRow() - 1, col, ExportExcel.BORDER_FORMAT);

        return ResponseUtils.ok(dynamicExport, "BC_tong_hop_theo_dau_moi.xlsx");
    }

    @Override
    public ResponseEntity exportMonth(Long id) throws Exception {
        TaxSettlementMastersEntity taxSettlementMastersEntity = taxSettlementMastersRepositoryJPA.findById(id).get();
        List<Long> taxDeclareMasterIds = Utils.stringToListLong(taxSettlementMastersEntity.getTaxDeclareMasterIds(), ",");
        String pathTemplate = "template/export/tax/tinh_thue/BM_BC_Tong_hop_theo_thang.xlsx";
        int startDataRow = 5;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        List<Map<String, Object>> mapExps = taxSettlementMastersRepository.getTaxDeclareMasters(taxDeclareMasterIds);
        dynamicExport.replaceKeys(mapExps);

        return ResponseUtils.ok(dynamicExport, "BC_Tong_hop_theo_thang.xlsx");
    }
}
