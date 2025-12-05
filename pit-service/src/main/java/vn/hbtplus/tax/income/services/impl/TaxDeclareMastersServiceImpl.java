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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.ValidateResponseDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.EmpTaxInfoDto;
import vn.hbtplus.tax.income.models.IncomeItemDetailsDto;
import vn.hbtplus.tax.income.models.TaxDeclareColumnDto;
import vn.hbtplus.tax.income.models.TaxParameterDto;
import vn.hbtplus.tax.income.models.dto.ContractProcessDTO;
import vn.hbtplus.tax.income.models.request.TaxDeclareMastersRequest;
import vn.hbtplus.tax.income.models.response.CategoryResponse;
import vn.hbtplus.tax.income.models.response.TaxDeclareDetailsResponse;
import vn.hbtplus.tax.income.models.response.TaxDeclareMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.*;
import vn.hbtplus.tax.income.repositories.impl.*;
import vn.hbtplus.tax.income.repositories.jpa.TaxDeclareMastersRepositoryJPA;
import vn.hbtplus.tax.income.services.TaxDeclareMastersService;
import vn.hbtplus.utils.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Lop impl service ung voi bang pit_tax_declare_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class TaxDeclareMastersServiceImpl implements TaxDeclareMastersService {

    private final TaxDeclareMastersRepository taxDeclareMastersRepository;
    private final TaxDeclareMastersRepositoryJPA taxDeclareMastersRepositoryJPA;
    private final IncomeItemMastersRepository incomeItemMastersRepository;
    private final ConfigParameterRepository configParameterRepository;
    private final CategoryRepository categoryRepository;
    private final TaxRateDetailRepository taxRateDetailRepository;
//    private final String[] doiTuongLuyTien = new String[]{"SQ", "QNCN", "CNVQP", "HĐLĐ"};

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TaxDeclareMastersResponse> searchData(TaxDeclareMastersRequest.SearchForm dto) {
        return ResponseUtils.ok(taxDeclareMastersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(TaxDeclareMastersRequest.SubmitForm dto) throws BaseAppException {
        TaxDeclareMastersEntity entity;
        if (dto.getTaxDeclareMasterId() != null && dto.getTaxDeclareMasterId() > 0L) {
            entity = taxDeclareMastersRepositoryJPA.getById(dto.getTaxDeclareMasterId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new TaxDeclareMastersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        taxDeclareMastersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getTaxDeclareMasterId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws BaseAppException {
        Optional<TaxDeclareMastersEntity> optional = taxDeclareMastersRepositoryJPA.findById(id);
        if (!optional.isPresent()
                || optional.get().isDeleted()
        ) {
            throw new RecordNotExistsException(id, TaxDeclareMastersEntity.class);
        }
        if (!optional.get().getStatus().equals(TaxDeclareMastersEntity.STATUS.DU_THAO)) {
            throw new BaseAppException(I18n.getMessage("global.validate.delete"));
        }

        taxDeclareMastersRepository.deleteOldData(optional.get());

        if (optional.get().getIncomeItemMasterIds() != null) {
            taxDeclareMastersRepository.updateIncomeItemMaster(Utils.stringToListLong(optional.get().getIncomeItemMasterIds(), ","), IncomeItemMastersEntity.STATUS.DA_CHOT);
        }

        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TaxDeclareMastersResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<TaxDeclareMastersEntity> optional = taxDeclareMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TaxDeclareMastersEntity.class);
        }
        TaxDeclareMastersResponse dto = new TaxDeclareMastersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportData(TaxDeclareMastersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/tax/ke_khai_thue/BM_BC_Ke_khai_thue.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = taxDeclareMastersRepository.getListExport(dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_BC_Ke_khai_thue.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportDetailKK02(Long id, List<Long> orgIds) throws Exception {
        String pathTemplate = "template/export/tax/ke_khai_thue/BM_KK02.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        Map<String, Object> mapParam = new HashMap<>();
        TaxDeclareMastersEntity mastersEntity = taxDeclareMastersRepository.get(TaxDeclareMastersEntity.class, id);
        mapParam.put("ngay_xuat_bc", Utils.formatDate(new Date()));
        mapParam.put("ky_ke_khai", Utils.formatDate(mastersEntity.getTaxPeriodDate(), Constant.SHORT_FORMAT_DATE));
        dynamicExport.replaceKeys(mapParam);

        // Xử lý sinh cột động
        int col = 11;
        int startDataRow = 6;
        dynamicExport.setLastRow(startDataRow);
        List<TaxDeclareColumnDto> listColumns = getColumns(mastersEntity.getTaxPeriodDate());
        dynamicExport.mergeCell(startDataRow - 1, col, startDataRow - 1, col + listColumns.size());
        col++;
        for (TaxDeclareColumnDto dto : listColumns) {
            dynamicExport.setText(dto.getColumnName(), col++);
        }

        int startColumnCopy = 28;
        dynamicExport.copyRange(startDataRow - 1, col, startDataRow, col + 12, 0, startColumnCopy, 1, startColumnCopy + 12);
        dynamicExport.deleteRange(0, startColumnCopy, 1, startColumnCopy + 12);
        dynamicExport.increaseRow();

        // xu ly lay data
        List<TaxDeclareDetailsResponse> listDataExport = taxDeclareMastersRepository.getListExportKK02(id, orgIds);
        List<TaxDeclareColumnEntity> listDataColumn = taxDeclareMastersRepository.findByProperties(TaxDeclareColumnEntity.class, "taxDeclareMasterId", id);
        Map<String, Long> mapDataColumn = new HashMap<>();
        for (TaxDeclareColumnEntity columnEntity : listDataColumn) {
            mapDataColumn.put(columnEntity.getTaxDeclareDetailId() + columnEntity.getColumnCode(), columnEntity.getColumnValue());
        }

        int index = 1;
        for (TaxDeclareDetailsResponse item : listDataExport) {
            col = 0;
            dynamicExport.setEntry(index++, col++);
            dynamicExport.setText(item.getEmpCode(), col++);
            dynamicExport.setText(item.getFullName(), col++);
            dynamicExport.setText(item.getTaxNo(), col++);
            dynamicExport.setText(item.getPersonalIdNo(), col++);
            dynamicExport.setText(item.getEmpTypeCode(), col++);
            dynamicExport.setText(item.getWorkOrgName(), col++);
            dynamicExport.setText(item.getOrgName(), col++);
            dynamicExport.setText(item.getPosName(), col++);
            dynamicExport.setText(item.getTaxMethod(), col++);
            Long sumDeduction = Utils.NVL(item.getDependentDeduction()) + Utils.NVL(item.getInsuranceDeduction()) + Utils.NVL(item.getOtherDeduction());
            dynamicExport.setText(Utils.NVL(item.getIncomeTax()) + Utils.NVL(item.getMonthRetroTax()) != 0 ? "x" : "", col++);
            //Tổng thu nhập chịu thuế trong năm trên tiền lương, tiền công
            Long sum = 0L;
            col++;
            for (TaxDeclareColumnDto dto : listColumns) {
                Long columnValue = Utils.NVL(mapDataColumn.get(item.getTaxDeclareDetailId() + dto.getColumnCode()));
                sum = sum + columnValue;
                dynamicExport.setNumber(columnValue, col++);
            }
            dynamicExport.setNumber(sum, col - listColumns.size() - 1);
            //Giảm trừ thu nhập chịu thuế
            dynamicExport.setNumber(sumDeduction, col++);
            dynamicExport.setEntry(item.getNumOfDependents(), col++);
            dynamicExport.setNumber(item.getDependentDeduction(), col++);
            dynamicExport.setNumber(item.getInsuranceDeduction(), col++);
            dynamicExport.setNumber(item.getOtherDeduction(), col++);
            //Thuế thu nhập kê khai tháng này
            Long taxIncome = sum - sumDeduction;
            dynamicExport.setNumber(taxIncome > 0 ? taxIncome : 0, col++);
            dynamicExport.setNumber(item.getIncomeTax(), col++);
            dynamicExport.setNumber(item.getTaxCollected(), col++);
            dynamicExport.setNumber(Utils.NVL(item.getIncomeTax()) + Utils.NVL(item.getMonthRetroTax()), col++);
            dynamicExport.setNumber(Utils.NVL(item.getIncomeTax()) - Utils.NVL(item.getTaxCollected()), col++);
            dynamicExport.setNumber(item.getMonthRetroTax(), col++);
            dynamicExport.setText(item.getNote(), col);

            dynamicExport.increaseRow();
        }
        dynamicExport.setCellFormat(startDataRow - 1, 0, dynamicExport.getLastRow() - 1, col, ExportExcel.BORDER_FORMAT);

        return ResponseUtils.ok(dynamicExport, "BM_KK02.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportTaxAllocation(Long id, List<Long> orgIds) throws Exception {
        int startDataRow = 4;
        String pathTemplate = "template/export/tax/ke_khai_thue/BM_BC_phan_bo_thue.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        List<Map<String, Object>> lstExp = taxDeclareMastersRepository.getListExportTaxAllocation(id, orgIds);
        dynamicExport.replaceKeys(lstExp);
        return ResponseUtils.ok(dynamicExport, "BC_phan_bo_thue.xlsx");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void lockPeriodById(Long id) throws BaseAppException {
        Optional<TaxDeclareMastersEntity> optional = taxDeclareMastersRepositoryJPA.findById(id);
        if (!optional.isPresent()
                || optional.get().isDeleted()
                || !TaxDeclareMastersEntity.STATUS.DU_THAO.equals(optional.get().getStatus())
        ) {
            throw new RecordNotExistsException(id, TaxDeclareMastersEntity.class);
        }
        if (!TaxDeclareMastersEntity.STATUS.DU_THAO.equals(optional.get().getStatus())) {
            throw new BaseAppException("Chỉ được khóa bản ghi ở trạng thái dự thảo!");
        }
        TaxDeclareMastersEntity entity = optional.get();
        entity.setStatus(TaxDeclareMastersEntity.STATUS.DA_CHOT);
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setModifiedTime(new Date());
        taxDeclareMastersRepositoryJPA.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void unLockPeriodById(Long id) throws BaseAppException {
        Optional<TaxDeclareMastersEntity> optional = taxDeclareMastersRepositoryJPA.findById(id);
        if (!optional.isPresent()
                || optional.get().isDeleted()
        ) {
            throw new RecordNotExistsException(id, TaxDeclareMastersEntity.class);
        }
        if (!TaxDeclareMastersEntity.STATUS.DA_CHOT.equals(optional.get().getStatus())) {
            throw new BaseAppException("Chỉ được mở khóa bản ghi ở trạng thái đã chốt!");
        }
        TaxDeclareMastersEntity entity = optional.get();
        entity.setStatus(TaxDeclareMastersEntity.STATUS.DU_THAO);
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setModifiedTime(new Date());
        taxDeclareMastersRepositoryJPA.save(entity);
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity downloadTemplate(Date taxPeriodDate) throws Exception {
        String pathTemplate = "template/export/tax/BM_Import_KekhaiThang.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 3, true);
        dynamicExport.setActiveSheet(1);

        List<CategoryResponse> listEmpTypes = categoryRepository.getCategories(Constant.CATEGORY_TYPE.DIEN_DOI_TUONG_THUE_TNCN);
        int index = 1;
        int row = 1;
        for (CategoryResponse cat : listEmpTypes) {
            dynamicExport.setEntry(String.valueOf(index++), 0, row);
            dynamicExport.setText(cat.getCode(), 1, row);
            dynamicExport.setText(cat.getName(), 2, row);
            row++;
        }
        dynamicExport.setCellFormat(0, 0, listEmpTypes.size(), 2, ExportExcel.BORDER_FORMAT);

        dynamicExport.setActiveSheet(2);
        List<CategoryResponse> taxMethods = categoryRepository.getCategories(Constant.CATEGORY_TYPE.CACH_TINH_THUE);
        index = 1;
        row = 1;
        for (CategoryResponse cat : taxMethods) {
            dynamicExport.setEntry(String.valueOf(index++), 0, row);
            dynamicExport.setText(cat.getCode(), 1, row);
            dynamicExport.setText(cat.getName(), 2, row);
            row++;
        }
        dynamicExport.setCellFormat(0, 0, taxMethods.size(), 2, ExportExcel.BORDER_FORMAT);

        dynamicExport.setActiveSheet(3);
        List<CategoryResponse> orgBeans = categoryRepository.getCategories(Constant.CATEGORY_TYPE.THUE_DON_VI_KE_KHAI);
        index = 1;
        row = 1;
        for (CategoryResponse cat : orgBeans) {
            dynamicExport.setEntry(String.valueOf(index++), 0, row);
            dynamicExport.setText(cat.getName(), 1, row);
            row++;
        }
        dynamicExport.setCellFormat(0, 0, orgBeans.size(), 1, ExportExcel.BORDER_FORMAT);

        dynamicExport.setActiveSheet(0);
        int col = 7;
        List<TaxDeclareColumnDto> listColumns = getColumns(taxPeriodDate);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.incomeTaxable"), col, dynamicExport.getLastRow() - 1);
        dynamicExport.mergeCell(dynamicExport.getLastRow() - 1, col, dynamicExport.getLastRow() - 1, col + listColumns.size() - 1);
        for (TaxDeclareColumnDto dto : listColumns) {
            dynamicExport.setText(dto.getColumnName(), col++);
        }
        dynamicExport.mergeCell(dynamicExport.getLastRow() - 1, col, dynamicExport.getLastRow() - 1, col);
//        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.incomeFreeTax"), col++, dynamicExport.getLastRow() - 1);

        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.deduction"), col, dynamicExport.getLastRow() - 1);
        dynamicExport.mergeCell(dynamicExport.getLastRow() - 1, col, dynamicExport.getLastRow() - 1, col + 3);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.numOfDependents"), col++);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.dependentDeduction"), col++);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.insuranceDeduction"), col++);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.otherDeduction"), col++);

        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.taxes"), col, dynamicExport.getLastRow() - 1);
        dynamicExport.mergeCell(dynamicExport.getLastRow() - 1, col, dynamicExport.getLastRow() - 1, col + 2);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.incomeTax"), col++);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.taxCollected"), col++);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.monthRetroRax"), col++);
        dynamicExport.mergeCell(dynamicExport.getLastRow() - 1, col, dynamicExport.getLastRow(), col);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.declareOrg"), col++, dynamicExport.getLastRow() - 1);
        dynamicExport.setText(I18n.getMessage("pit.taxDeclare.note"), col, dynamicExport.getLastRow() - 1);
        dynamicExport.mergeCell(dynamicExport.getLastRow() - 1, col, dynamicExport.getLastRow(), col);
        dynamicExport.setCellFormat(dynamicExport.getLastRow() - 1, 0, dynamicExport.getLastRow(), col, ExportExcel.BORDER_FORMAT);
        String fileName = Utils.removeSign(String.format("BM_Import_KekhaiThang_%s.xlsx", Utils.formatDate(taxPeriodDate, "yyyyMM")));
        fileName = fileName.replace(" ", "-");
        return ResponseUtils.ok(dynamicExport, fileName, false);
    }

    @Override
    @Transactional
    public Long importTaxDeclare(MultipartFile fileImport, Date taxPeriodDate) throws ErrorImportException, IOException, ExecutionException, InterruptedException {
        List<TaxDeclareColumnDto> columns = getColumns(taxPeriodDate);
        ImportExcel importExcel = new ImportExcel(initImportConfigBean(taxPeriodDate), 100000, 3);
        List<Object[]> dataList = new ArrayList<>();
        String createBy = Utils.getUserNameLogin();
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            Map<String, String> mapFullNames = new HashMap<>();
            List<String> empCodes = new ArrayList<>();
            dataList.stream().forEach(item -> {
                if (item[1] != null && !empCodes.contains(item[1])) {
                    empCodes.add((String) item[1]);
                }
            });
            //Lay danh sach nhan vien theo ma nhan vien
            List<EmpTaxInfoDto> listEmps = taxDeclareMastersRepository.getEmpTaxInfos(empCodes, taxPeriodDate);
            listEmps.stream().forEach(item -> {
                mapFullNames.put(item.getEmpCode(), item.getFullName().toLowerCase());
            });

            List<CategoryResponse> listEmpTypes = categoryRepository.getCategories(Constant.CATEGORY_TYPE.DIEN_DOI_TUONG_THUE_TNCN);
            Map<String, CategoryResponse> mapEmpTypes = new HashMap<>();
            listEmpTypes.stream().forEach(item -> {
                mapEmpTypes.put(item.getCode().toLowerCase(), item);
                mapEmpTypes.put(item.getName().toLowerCase(), item);
            });
            List<CategoryResponse> taxMethods = categoryRepository.getCategories(Constant.CATEGORY_TYPE.CACH_TINH_THUE);
            Map<String, CategoryResponse> mapTaxMethods = new HashMap<>();
            taxMethods.stream().forEach(item -> {
                mapTaxMethods.put(item.getCode().toLowerCase(), item);
                mapTaxMethods.put(item.getName().toLowerCase(), item);
            });
            List<CategoryResponse> orgBeans = categoryRepository.getCategories(Constant.CATEGORY_TYPE.THUE_DON_VI_KE_KHAI);
            Map<String, Long> mapDonVis = new HashMap();
            orgBeans.forEach(item -> {
                mapDonVis.put(item.getName().toLowerCase(), Long.valueOf(item.getValue()));
            });


            int row = 0;
            List<TaxDeclareDetailsEntity> listDetailsEntity = new ArrayList<>();
            for (Object[] obj : dataList) {
                TaxDeclareDetailsEntity detailsEntity = new TaxDeclareDetailsEntity();

                if (obj[1] != null && mapFullNames.get(obj[1]) == null) {
                    importExcel.addError(row, 1, I18n.getMessage("global.validate.employeeCode"), (String) obj[1]);
                } else if (obj[1] != null) {
                    String fullName = (String) obj[2];
                    if (!fullName.equalsIgnoreCase(mapFullNames.get(obj[1]))) {
                        importExcel.addError(row, 1, I18n.getMessage("global.validate.fullName"), fullName);
                    }
                }
                int col = 1;
                detailsEntity.setEmpCode((String) obj[col++]);
                detailsEntity.setFullName((String) obj[col++]);
                detailsEntity.setTaxNo((String) obj[col++]);
                //validate ma so thue
                detailsEntity.setPersonalIdNo((String) obj[col++]);
                String empType = (String) obj[col];
                if (mapEmpTypes.get(empType.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Diện đối tượng không hợp lệ", empType);
                } else {
                    detailsEntity.setEmpTypeCode(mapEmpTypes.get(empType.toLowerCase()).getCode());
                }
                col++;
                String taxMethod = (String) obj[col];
                if (mapTaxMethods.get(taxMethod.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Cách tính thuế không hợp lệ", taxMethod);
                } else {
                    detailsEntity.setTaxMethod(mapTaxMethods.get(taxMethod.toLowerCase()).getCode());
                }
                col++;
                for (TaxDeclareColumnDto column : columns) {
                    detailsEntity.getMapValues().put(column.getColumnCode(), (Long) obj[col++]);
                }
//                detailsEntity.setIncomeFreeTax((Long) obj[col++]);
                detailsEntity.setNumOfDependents(obj[col] == null ? 0 : ((Long) obj[col]).intValue());
                col++;
                detailsEntity.setDependentDeduction((Long) obj[col++]);
                detailsEntity.setInsuranceDeduction((Long) obj[col++]);
                detailsEntity.setOtherDeduction((Long) obj[col++]);
                detailsEntity.setIncomeTax((Long) obj[col++]);
                detailsEntity.setTaxCollected((Long) obj[col++]);
                detailsEntity.setTaxPayable(Utils.NVL(detailsEntity.getIncomeTax()) - Utils.NVL(detailsEntity.getTaxCollected()));
                detailsEntity.setMonthRetroTax((Long) obj[col++]);
                String donvi = (String) obj[col];
                if (Utils.isNullOrEmpty(donvi)) {
                    if (Utils.isNullOrEmpty(detailsEntity.getEmpCode())) {
                        importExcel.addError(row, col, "Đơn vị phân bổ bắt buộc nhập với đối tượng vãng lai", donvi);
                    }
                } else {
                    if (mapDonVis.get(donvi.toLowerCase()) == null) {
                        importExcel.addError(row, col, "Đơn vị cần nhập đúng theo danh mục tại sheet 'Danh sach don vi'", donvi);
                    } else {
                        detailsEntity.setDeclareOrgId(mapDonVis.get(donvi.toLowerCase()));
                        detailsEntity.setWorkOrgId(mapDonVis.get(donvi.toLowerCase()));
                    }
                }
                col++;
                detailsEntity.setNote((String) obj[col]);
                listDetailsEntity.add(detailsEntity);
                row++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                String userName = Utils.getUserNameLogin();
                Date currentDate = new Date();
                TaxDeclareMastersEntity mastersEntity = new TaxDeclareMastersEntity();
                mastersEntity.setStatus(TaxDeclareMastersEntity.STATUS.DU_THAO);
                mastersEntity.setTaxPeriodDate(taxPeriodDate);
                mastersEntity.setCreatedBy(userName);
                mastersEntity.setCreatedTime(currentDate);
                mastersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                mastersEntity.setInputType(TaxDeclareMastersEntity.INPUT_TYPES.IMPORT);
                taxDeclareMastersRepositoryJPA.save(mastersEntity);

                List<TaxDeclareColumnEntity> listColumnEntity = new ArrayList<>();
                Long taxDeclareDetailId = taxDeclareMastersRepository.getNextId(TaxDeclareDetailsEntity.class, listDetailsEntity.size());
                for (int i = 0; i < listDetailsEntity.size(); i++) {
                    TaxDeclareDetailsEntity detailsEntity = listDetailsEntity.get(i);
                    detailsEntity.setTaxDeclareDetailId(taxDeclareDetailId + i);
                    detailsEntity.setTaxDeclareMasterId(mastersEntity.getTaxDeclareMasterId());
                    detailsEntity.setTaxPeriodDate(mastersEntity.getTaxPeriodDate());
                    detailsEntity.setCreatedBy(userName);
                    detailsEntity.setCreatedTime(currentDate);

                    for (Map.Entry<String, Long> entry : detailsEntity.getMapValues().entrySet()) {
                        listColumnEntity.add(new TaxDeclareColumnEntity(detailsEntity, entry));
                        //add thu nhap tong cong
                        detailsEntity.setIncomeTaxable(Utils.NVL(detailsEntity.getIncomeTaxable()) + Utils.NVL(entry.getValue()));
                    }
                }
                taxDeclareMastersRepository.insertBatch(TaxDeclareDetailsEntity.class, listDetailsEntity, createBy);
                taxDeclareMastersRepository.insertBatch(TaxDeclareColumnEntity.class, listColumnEntity, createBy);
                taxDeclareMastersRepositoryJPA.flush();
                taxDeclareMastersRepository.updateDataMasterById(mastersEntity.getTaxDeclareMasterId());
                taxDeclareMastersRepository.updateTaxDeclareOrgId(mastersEntity.getTaxDeclareMasterId(), taxPeriodDate);
                return mastersEntity.getTaxDeclareMasterId();
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Long calculate(Date taxPeriodDate) throws BaseAppException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException, ParseException {
        TaxDeclareMastersEntity mastersEntity = taxDeclareMastersRepository.getTaxDeclareMaster(taxPeriodDate, TaxDeclareMastersEntity.INPUT_TYPES.CALCULATE);
        String createBy = Utils.getUserNameLogin();
        if (mastersEntity != null) {
            if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DA_QUYET_TOAN)) {
                throw new BaseAppException("Dữ liệu kê khai đã được quyết toán thuế!");
            } else {
                if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DU_THAO)) {
                    //neu la du thao thi xoa ban ghi di
                    taxDeclareMastersRepository.forceDeleteOldData(mastersEntity);
                } else if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DA_CHOT)) {
                    //neu du lieu da chot thi thuc hien cap nhat inactive
                    taxDeclareMastersRepository.inactiveOldData(mastersEntity);
                } else {
                    throw new BaseAppException();
                }
            }
        }


        //lay du lieu cac khoan da duoc tinh thue
        List<TaxDeclareDetailsEntity> incomeItemDetailsEntities = incomeItemMastersRepository.getEntities(taxPeriodDate);
        List<String> empCodes = new ArrayList<>();
        Map<String, TaxDeclareDetailsEntity> mapTaxDeclares = new HashMap<>();

        extractedToMapValues(incomeItemDetailsEntities, empCodes, mapTaxDeclares);

        //Lay thong tin co ban cua nhan vien
        List<EmpTaxInfoDto> listEmps = taxDeclareMastersRepository.getEmpTaxInfos(empCodes, taxPeriodDate);
        Map<String, EmpTaxInfoDto> mapEmps = new HashMap<>();
        listEmps.stream().forEach(item -> {
            mapEmps.put(item.getEmpCode(), item);
        });
        TaxParameterDto taxParameterDto = configParameterRepository.getConfig(TaxParameterDto.class, taxPeriodDate);
        List<TaxRateDetailEntity> ratioDetailEntities = taxRateDetailRepository.getTaxRatios(taxPeriodDate);
        Long startDetailId = taxDeclareMastersRepository.getNextId(TaxDeclareDetailsEntity.class, mapTaxDeclares.values().size());
        int idx = 0;

        //luu master
        //thuc hien insert du lieu
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        List<Long> incomeItemMasterIds = taxDeclareMastersRepository.getIncomeItemMasterIds(taxPeriodDate);
        if (Utils.isNullOrEmpty(incomeItemMasterIds)) {
            throw new BaseAppException("Không tồn tại thuế thu nhập cá nhân trong tháng " + Utils.formatDate(taxPeriodDate, "MM/YYYY"));
        }

        mastersEntity = new TaxDeclareMastersEntity();
        mastersEntity.setInputType(TaxDeclareMastersEntity.INPUT_TYPES.CALCULATE);
        mastersEntity.setTaxPeriodDate(taxPeriodDate);
        mastersEntity.setIncomeItemMasterIds(incomeItemMasterIds.toString().substring(1, incomeItemMasterIds.toString().length() - 1));
        mastersEntity.setCreatedBy(userName);
        taxDeclareMastersRepositoryJPA.save(mastersEntity);

        List<TaxDeclareColumnEntity> columnEntities = new ArrayList<>();
        final List<TaxDeclareColumnDto> listColumns = getColumns(taxPeriodDate);

        for (TaxDeclareDetailsEntity item : mapTaxDeclares.values()) {
            //Xac dinh cach tinh thue
            String taxMethod = getTaxMethod(item, mapEmps.get(Utils.NVL(item.getEmpCode())), taxParameterDto);
            if (Utils.NVL(item.getIncomeTaxable()) > taxParameterDto.getMinIncome()) {
                if (taxMethod.equals(Constant.TAXES_METHOD.LUY_TIEN)) {
                    long incomeTax = 0L;
                    item.setNumOfDependents(Utils.NVL(mapEmps.get(item.getEmpCode()).getNumOfDependents()));
                    item.setDependentDeduction(taxParameterDto.getDependentDeduct() * item.getNumOfDependents()
                            + taxParameterDto.getSelfDeduct());
                    long thuNhapTinhThue = (Utils.NVL(item.getIncomeTaxable())
                            - Utils.NVL(item.getInsuranceDeduction())
                            - Utils.NVL(item.getDependentDeduction()));
                    if (thuNhapTinhThue > 0) {
                        for (TaxRateDetailEntity ratio : ratioDetailEntities) {
                            if (thuNhapTinhThue > ratio.getAmount()) {
                                incomeTax += Math.round(((thuNhapTinhThue - ratio.getAmount()) * ratio.getPercent() / 100));
                                thuNhapTinhThue = ratio.getAmount();
                            }
                        }
                        item.setIncomeTax(incomeTax);
                    }

                } else if (taxMethod.equals(Constant.TAXES_METHOD.KO_CAM_KET)) {
                    Long incomeTax = Math.round((Utils.NVL(item.getIncomeTaxable())
                            - Utils.NVL(item.getInsuranceDeduction())) * 0.1);
                    if (incomeTax > 0) {
                        item.setIncomeTax(incomeTax);
                    }
                }
                item.setOtherDeduction(0L);
            } else {
                item.setOtherDeduction(item.getIncomeTaxable());
                item.setIncomeTax(0L);
            }
            item.setTaxDeclareDetailId(startDetailId + idx);
            item.setTaxMethod(taxMethod);
            if (item.getEmpCode() != null && mapEmps.get(item.getEmpCode()) != null) {
                EmpTaxInfoDto empDto = mapEmps.get(item.getEmpCode());
                item.setTaxNo(empDto.getTaxNo());
                item.setPersonalIdNo(empDto.getPersonalIdNo());
                item.setEmpTypeCode(empDto.getEmpTypeCode());
                item.setFullName(empDto.getFullName());
                item.setPosName(empDto.getPositionName());
                if (empDto.getStatus().equals("OUT")) {
                    item.setNote(Utils.join(", ", item.getNote(), "Đã nghỉ việc"));
                } else if (empDto.getStatus().equals("ORG_CHANGED")) {
                    item.setNote(Utils.join(", ", item.getNote(), "Đã chuyển công tác"));
                }
                item.setDeclareOrgId(null);
            } else {
                item.setEmpTypeCode("VL");
                item.setPosName("Vãng lai");
            }
            item.setTaxDeclareMasterId(mastersEntity.getTaxDeclareMasterId());
            item.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            item.setTaxPeriodDate(taxPeriodDate);
            item.setTaxPayable(Utils.NVL(item.getIncomeTax()) - Utils.NVL(item.getTaxCollected()));
            item.setCreatedBy(userName);
            item.setCreatedTime(currentDate);
            columnEntities.addAll(initColumnEntity(item, listColumns));
//            for (Map.Entry<String, Long> entry : item.getMapValues().entrySet()) {
//                columnEntities.add(new TaxDeclareColumnEntity(item, entry));
//            }
            idx++;
        }

        taxDeclareMastersRepository.insertBatch(TaxDeclareDetailsEntity.class, Arrays.asList(mapTaxDeclares.values().toArray()), createBy);
        taxDeclareMastersRepository.insertBatch(TaxDeclareColumnEntity.class, columnEntities, createBy);
        taxDeclareMastersRepositoryJPA.flush();
        taxDeclareMastersRepository.updateDataMasterById(mastersEntity.getTaxDeclareMasterId());

        taxDeclareMastersRepository.updateTaxDeclareOrgId(mastersEntity.getTaxDeclareMasterId(), taxPeriodDate);
        //update trang thai
        taxDeclareMastersRepository.updateIncomeItemMaster(incomeItemMasterIds, IncomeItemMastersEntity.STATUS.DA_KE_KHAI);
        incomeItemMasterIds.stream().forEach(id -> {
            incomeItemMastersRepository.updateTaxDeclareOrgId(id, taxPeriodDate);
        });
//        taxDeclareMastersRepository.updateIncomeItemDetails(incomeItemMasterIds);
        return mastersEntity.getTaxDeclareMasterId();
    }

    private List<TaxDeclareColumnEntity> initColumnEntity(TaxDeclareDetailsEntity item, List<TaxDeclareColumnDto> listColumns) {
        Map<String, TaxDeclareColumnEntity> results = new HashMap<>();
        listColumns.stream().forEach(columnDto -> {
            item.getMapValues().forEach((incomeType, value) -> {
                if (columnDto.getIncomeTypes().contains(incomeType)) {
                    if (results.get(columnDto.getColumnCode()) == null) {
                        results.put(columnDto.getColumnCode(), new TaxDeclareColumnEntity(item, columnDto.getColumnCode(), value));
                    } else {
                        results.get(columnDto.getColumnCode()).setColumnValue(results.get(columnDto.getColumnCode()).getColumnValue() + value);
                    }
                }
            });
        });
        return new ArrayList<>(results.values());
    }

    private String getTaxMethod(TaxDeclareDetailsEntity item, EmpTaxInfoDto empTaxInfoDto, TaxParameterDto taxParameterDto) throws ParseException {
        if (Utils.isNullOrEmpty(item.getEmpCode()) || empTaxInfoDto == null) {
            return Constant.TAXES_METHOD.KO_CAM_KET;
        }

        ContractProcessDTO contractProcessDTO = incomeItemMastersRepository.getEmployeeByCode(item.getEmpCode());
        if (contractProcessDTO.getEndDate() == null) {
            contractProcessDTO.setEndDate(new Date());
        }

        int totalMonth = Utils.NVL(Utils.calculateMonthsBetween(contractProcessDTO.getStartDate(), contractProcessDTO.getEndDate()));

        if (totalMonth < 12) {
            return Constant.TAXES_METHOD.KO_CAM_KET;
        }

        String status = empTaxInfoDto.getStatus();
        if ("OUT".equals(status) || "ORG_CHANGED".equals(status)) {
            return Utils.NVL(empTaxInfoDto.getIncomeCommitment()) > 0 ? Constant.TAXES_METHOD.CAM_KET : Constant.TAXES_METHOD.KO_CAM_KET;
        }

        if (taxParameterDto.getDoiTuongLuyTiens().contains(empTaxInfoDto.getEmpTypeCode())) {
            return Constant.TAXES_METHOD.LUY_TIEN;
        }
        return Utils.NVL(empTaxInfoDto.getIncomeCommitment()) > 0 ? Constant.TAXES_METHOD.CAM_KET : Constant.TAXES_METHOD.KO_CAM_KET;
    }

    private static void extractedToMapValues(List<TaxDeclareDetailsEntity> incomeItemDetailsEntities, List<String> empCodes, Map<String, TaxDeclareDetailsEntity> mapTaxDeclares) {
        Map<String, String> mapMaSoThue = new HashMap<>();
        Map<String, String> mapCMT = new HashMap<>();
        incomeItemDetailsEntities.stream().forEach(item -> {
            String key;
            String value = "" + item.getTaxNo()
                    + "-" + item.getPersonalIdNo()
                    + "-" + item.getFullName().toLowerCase();
            if (!Utils.isNullOrEmpty(item.getEmpCode())) {
                if (!empCodes.contains(item.getEmpCode())) {
                    empCodes.add(item.getEmpCode());
                }
            }

            if (!Utils.isNullOrEmpty(item.getTaxNo())) {
                key = "1#" + item.getTaxNo() + "-" + item.getPersonalIdNo() + item.getFullName().toLowerCase();
                if (mapMaSoThue.get(item.getTaxNo()) != null && !value.equalsIgnoreCase(mapMaSoThue.get(item.getTaxNo()))) {
                    //error ma so thue bi duplicate
                    item.setNote("Mã số thuế bị trùng");
                }
                mapMaSoThue.put(item.getTaxNo(), value);
            } else if (!Utils.isNullOrEmpty(item.getPersonalIdNo())) {
                key = "2#" + item.getPersonalIdNo() + item.getFullName().toLowerCase();
                if (mapCMT.get(item.getPersonalIdNo()) != null && !value.equalsIgnoreCase(mapCMT.get(item.getPersonalIdNo()))) {
                    //error ma so thue bi duplicate
                    item.setNote("Số CMT/CCCD (hộ chiếu) bị trùng");
                }
                mapCMT.put(item.getPersonalIdNo(), value);
            } else if (!Utils.isNullOrEmpty(item.getEmpCode())) {
                key = "0#" + item.getEmpCode();
            } else {
                key = "3#" + item.getFullName().toLowerCase();
            }

            if (mapTaxDeclares.get(key) == null) {
                mapTaxDeclares.put(key, item);
                item.initMapValues();
            } else {
                mapTaxDeclares.get(key).add(item);
            }
        });
    }

    @Override
    public ValidateResponseDto validateCalculate(Date taxPeriodDate, String inputType) throws BaseAppException {
        //ham validate tinh thue
        TaxDeclareMastersEntity mastersEntity = taxDeclareMastersRepository.getTaxDeclareMaster(taxPeriodDate, inputType);
        if (mastersEntity != null && mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DA_QUYET_TOAN)) {
            throw new BaseAppException("Dữ liệu đã được tính quyết toán, bạn cần thực hiện hủy dữ liệu quyết toán!");
        }

        if (inputType.equals(TaxDeclareMastersEntity.INPUT_TYPES.IMPORT)) {
            if (mastersEntity == null) {
                return new ValidateResponseDto();
            } else if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DA_CHOT)) {
                return new ValidateResponseDto("WARNING", "WARN_001", "Dữ liệu kê khai đã được chốt");
            } else if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DU_THAO)) {
                return new ValidateResponseDto("WARNING", "WARN_002", "Dữ liệu kê khai đã tồn tại");
            }
        } else {
            //Lay danh sach khoan thu nhap chua duoc chot
            if (incomeItemMastersRepository.isExistsItemInStatus(taxPeriodDate, IncomeItemMastersEntity.STATUS.DU_THAO)) {
                throw new BaseAppException("Tồn tại khoản thu nhập chưa được tính thuế!");
            } else if (incomeItemMastersRepository.isExistsItemInStatus(taxPeriodDate, IncomeItemMastersEntity.STATUS.DA_TINH_THUE)) {
                if (mastersEntity == null) {
                    return new ValidateResponseDto("WARNING", "WARN_003", "Tồn tại khoản thu nhập chưa được chốt!");
                } else if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DA_CHOT)) {
                    return new ValidateResponseDto("WARNING", "WARN_001_003", "Dữ liệu kê khai đã được chốt, đồng thời tồn tại khoản thu nhập chưa được chốt!");
                } else if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DU_THAO)) {
                    return new ValidateResponseDto("WARNING", "WARN_002_003", "Dữ liệu kê khai đã tồn tại, đồng thời tồn tại khoản thu nhập chưa được chốt!");
                }
            } else {
                if (mastersEntity == null) {
                    return new ValidateResponseDto();
                } else if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DA_CHOT)) {
                    return new ValidateResponseDto("WARNING", "WARN_001", "Dữ liệu kê khai đã được chốt!");
                } else if (mastersEntity.getStatus().equals(TaxDeclareMastersEntity.STATUS.DU_THAO)) {
                    return new ValidateResponseDto("WARNING", "WARN_002", "Dữ liệu kê khai đã tồn tại!");
                }
            }

        }

        return new ValidateResponseDto();
    }

    @Override
    public ResponseEntity<Object> exportXml(Long id, List<Long> orgIds) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        TaxDeclareMastersEntity mastersEntity = taxDeclareMastersRepository.get(TaxDeclareMastersEntity.class, id);
        String templateFile = "template/export/tax/tinh_thue/xml/BM_Tokhai_KekhaiThue.xml";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateFile);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(inputStream);

        Element kyKKhaiThue = (Element) doc.getElementsByTagName("KyKKhaiThue").item(0);
        List<Map<String, Object>> lstExp = taxDeclareMastersRepository.getListExportTaxAllocation(id, orgIds);

        // Thay đổi giá trị của các phần tử bên trong <KyKKhaiThue>
//        kyKKhaiThue.getElementsByTagName("kieuKy").item(0).setTextContent("M");
        kyKKhaiThue.getElementsByTagName("kyKKhai").item(0).setTextContent(Utils.formatDate(mastersEntity.getTaxPeriodDate(), "MM/yyyy"));
        kyKKhaiThue.getElementsByTagName("kyKKhaiTuNgay").item(0).setTextContent(Utils.formatDate(Utils.getFirstDay(mastersEntity.getTaxPeriodDate())));
        kyKKhaiThue.getElementsByTagName("kyKKhaiDenNgay").item(0).setTextContent(Utils.formatDate(mastersEntity.getTaxPeriodDate()));
        doc.getElementsByTagName("ngayLapTKhai").item(0).setTextContent(Utils.formatDate(new Date(), "yyyy-MM-dd"));
        doc.getElementsByTagName("ngayKy").item(0).setTextContent(Utils.formatDate(new Date(), "yyyy-MM-dd"));

        //add element
        Element phanBoTuTienLuong = (Element) doc.getElementsByTagName("PhanBoTuTienLuong").item(0);
        Double thuNhapChiuThue = 0d;
        Double thuNhapChiuThueCuTru = 0d;
        Double thuNhapChiuThueCoKhautru = 0d;
        Double thuNhapChiuThueCoKhautruCuTru = 0d;
        Long soNguoi = 0L;
        Long soNguoiHDLD = 0L;
        Long soNguoiCoKhauTru = 0L;
        Long soNguoiCoKhauTruCutru = 0L;
        Double thueKeKhai = 0d;
        Double thueKeKhaiCuTru = 0d;

        for (int i = 0; i < lstExp.size(); i++) {
            Map map = lstExp.get(i);
            Element element = doc.createElement("NDungPhanBoTuTienLuong");
            element.setAttribute("id", "ID_" + (i + 1));
            addChildElement(element, "stt", String.valueOf(i + 1));
            addChildElement(element, "loai", (String) map.get("LOAI"));
            addChildElement(element, "ct07", (String) map.get("DON_VI"));
            addChildElement(element, "ct08", (String) map.get("MA_SO_THUE"));
            addChildElement(element, "ct08a_huyen_ma", (String) map.get("MA_HUYEN"));
            addChildElement(element, "ct08a_huyen_ten", (String) map.get("TEN_HUYEN"));
            addChildElement(element, "ct08b_tinh_ma", (String) map.get("MA_TINH"));
            addChildElement(element, "ct08b_tinh_ten", (String) map.get("TEN_TINH"));
            addChildElement(element, "ct09_cqt_ma", (String) map.get("MA_CQ_THUE"));
            addChildElement(element, "ct09_cqt_ten", (String) map.get("TEN_CQ_THUE"));
            addChildElement(element, "ct10", Utils.formatNumber(map.get("THU_NHAP_CHIU_THUE"), "######"));
            addChildElement(element, "ct11", Utils.formatNumber(map.get("TNCT_CO_KHAU_TRU"), "######"));
            addChildElement(element, "ct12", Utils.formatNumber(map.get("SO_NGUOI"), "######"));
            addChildElement(element, "ct13", Utils.formatNumber(map.get("SO_NGUOI_KHAU_TRU"), "######"));
            addChildElement(element, "ct14", Utils.formatNumber(map.get("THUE_KE_KHAI"), "######"));

            thuNhapChiuThue += Utils.NVL((BigDecimal) map.get("THU_NHAP_CHIU_THUE")).doubleValue();
            thuNhapChiuThueCuTru += Utils.NVL((BigDecimal) map.get("THU_NHAP_CHIU_THUE_CU_TRU")).doubleValue();
            thuNhapChiuThueCoKhautru += Utils.NVL((BigDecimal) map.get("TNCT_CO_KHAU_TRU")).doubleValue();
            thuNhapChiuThueCoKhautruCuTru += Utils.NVL((BigDecimal) map.get("TNCT_CO_KHAU_TRU_CU_TRU")).doubleValue();
            soNguoi += Utils.NVL((BigDecimal) map.get("SO_NGUOI")).longValue();
            soNguoiHDLD += Utils.NVL((BigDecimal) map.get("SO_NGUOI_HDLD")).longValue();
            soNguoiCoKhauTru += Utils.NVL((BigDecimal) map.get("SO_NGUOI_KHAU_TRU")).longValue();
            soNguoiCoKhauTruCutru += Utils.NVL((BigDecimal) map.get("SO_NGUOI_KHAU_TRU_CU_TRU")).longValue();
            thueKeKhai += Utils.NVL((BigDecimal) map.get("THUE_KE_KHAI")).doubleValue();
            thueKeKhaiCuTru += Utils.NVL((BigDecimal) map.get("THUE_KE_KHAI_CU_TRU")).doubleValue();
            phanBoTuTienLuong.appendChild(element);
        }
        phanBoTuTienLuong.getElementsByTagName("ct15").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThue, "######"));
        phanBoTuTienLuong.getElementsByTagName("ct16").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThueCoKhautru, "######"));
        phanBoTuTienLuong.getElementsByTagName("ct17").item(0).setTextContent(Utils.formatNumber(soNguoi, "######"));
        phanBoTuTienLuong.getElementsByTagName("ct18").item(0).setTextContent(Utils.formatNumber(soNguoiCoKhauTru, "######"));
        phanBoTuTienLuong.getElementsByTagName("ct19").item(0).setTextContent(Utils.formatNumber(thueKeKhai, "######"));

        Element cTieuTKhaiChinh = (Element) doc.getElementsByTagName("CTieuTKhaiChinh").item(0);
        cTieuTKhaiChinh.getElementsByTagName("ct16").item(0).setTextContent(Utils.formatNumber(soNguoi, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct17").item(0).setTextContent(Utils.formatNumber(soNguoiHDLD, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct18").item(0).setTextContent(Utils.formatNumber(soNguoiCoKhauTru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct19").item(0).setTextContent(Utils.formatNumber(soNguoiCoKhauTruCutru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct20").item(0).setTextContent(Utils.formatNumber(soNguoiCoKhauTru - soNguoiCoKhauTruCutru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct21").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThue, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct22").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThueCuTru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct23").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThue - thuNhapChiuThueCuTru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct26").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThueCoKhautru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct27").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThueCoKhautruCuTru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct28").item(0).setTextContent(Utils.formatNumber(thuNhapChiuThueCoKhautru - thuNhapChiuThueCoKhautruCuTru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct29").item(0).setTextContent(Utils.formatNumber(thueKeKhai, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct30").item(0).setTextContent(Utils.formatNumber(thueKeKhaiCuTru, "######"));
        cTieuTKhaiChinh.getElementsByTagName("ct31").item(0).setTextContent(Utils.formatNumber(thueKeKhai - thueKeKhaiCuTru, "######"));

        return ResponseUtils.ok(doc, "Tokhai_KekhaiThue.xml");
    }

    @Override
    public TaxDeclareMastersEntity getTaxDeclareMaster(Date taxPeriodDate, String inputType) {

        return taxDeclareMastersRepository.getTaxDeclareMaster(taxPeriodDate, inputType);
    }

    private void addChildElement(Element parentElement, String elementName, String textContent) {
        Element newElement = parentElement.getOwnerDocument().createElement(elementName);

        newElement.setTextContent(Utils.NVL(textContent));
        parentElement.appendChild(newElement);
    }

    private ImportExcel.ImportConfigBean[] initImportConfigBean(Date taxPeriodDate) {
        List<ImportExcel.ImportConfigBean> columnExcels = new ArrayList<>();
        columnExcels.add(new ImportExcel.ImportConfigBean("stt", ImportExcel.STRING, true, 10, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.empCode"), ImportExcel.STRING, true, 10, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.fullName"), ImportExcel.STRING, false, 300, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.taxNumber"), ImportExcel.STRING, true, 10, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.personalIdNo"), ImportExcel.STRING, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.empTypeCode"), ImportExcel.STRING, false, 50, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.taxMethod"), ImportExcel.STRING, false, 50, false));
        List<TaxDeclareColumnDto> columns = getColumns(taxPeriodDate);
        columns.stream().forEach(column -> {
            columnExcels.add(new ImportExcel.ImportConfigBean(column.getColumnName(), ImportExcel.LONG, true, 20, false));
        });

//        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.incomeFreeTax"), ImportExcel.LONG, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.numOfDependents"), ImportExcel.LONG, true, 5, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.dependentDeduction"), ImportExcel.LONG, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.insuranceDeduction"), ImportExcel.LONG, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.otherDeduction"), ImportExcel.LONG, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.incomeTax"), ImportExcel.LONG, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.taxCollected"), ImportExcel.LONG, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.monthRetroRax"), ImportExcel.LONG, true, 15, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.declareOrg"), ImportExcel.STRING, true, 255, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.taxDeclare.note"), ImportExcel.STRING, true, 500, false));

        return columnExcels.toArray(new ImportExcel.ImportConfigBean[columnExcels.size()]);
    }

    public List<TaxDeclareColumnDto> getColumns(Date taxPeriodDate) {
        List<TaxDeclareColumnDto> columns = new ArrayList<>();
        columns.add(new TaxDeclareColumnDto("LK", "Lương tháng", "LT"));
        columns.add(new TaxDeclareColumnDto("SXKD", "Lương năm/bổ sung, thu nhập khác", "SXKDQ", "SXKDN", "TNK"));
        columns.add(new TaxDeclareColumnDto("LT", "Lương làm thêm", "OT"));
        return columns;
    }

}
