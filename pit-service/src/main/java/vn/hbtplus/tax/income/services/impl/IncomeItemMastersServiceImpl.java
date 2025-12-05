/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.*;
import vn.hbtplus.tax.income.models.dto.ContractProcessDTO;
import vn.hbtplus.tax.income.models.request.IncomeItemMastersRequest;
import vn.hbtplus.tax.income.models.response.CategoryResponse;
import vn.hbtplus.tax.income.models.response.IncomeItemMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.*;
import vn.hbtplus.tax.income.repositories.impl.*;
import vn.hbtplus.tax.income.repositories.jpa.IncomeItemMastersRepositoryJPA;
import vn.hbtplus.tax.income.services.IncomeItemMastersService;
import vn.hbtplus.utils.*;

import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang pit_income_item_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeItemMastersServiceImpl implements IncomeItemMastersService {

    private final IncomeItemMastersRepository incomeItemMastersRepository;
    private final IncomeItemMastersRepositoryJPA incomeItemMastersRepositoryJPA;
    private final TaxDeclareMastersRepository taxDeclareMastersRepository;
    private final EmployeeRepository employeeRepository;
    private final ConfigParameterRepository configParameterRepository;
    private final TaxRateDetailRepository taxRateDetailRepository;

    private final IncomeItemsRepository incomeItemsRepository;
    private final CategoryRepository categoryRepository;

    private final IncomeItemDetailRepository incomeItemDetailRepository;
//    private final String[] doiTuongLuyTien = new String[]{"SQ", "QNCN", "CNVQP", "HĐLĐ"};

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<IncomeItemMastersResponse> searchData(IncomeItemMastersRequest.SearchForm dto) {
        return incomeItemMastersRepository.searchData(dto);
    }

    @Override
    @Transactional
    public ResponseEntity saveData(IncomeItemMastersRequest.SubmitForm dto) throws BaseAppException {
        IncomeItemMastersEntity entity;
        if (dto.getIncomeItemMasterId() != null && dto.getIncomeItemMasterId() > 0L) {
            entity = incomeItemMastersRepositoryJPA.getById(dto.getIncomeItemMasterId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new IncomeItemMastersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        incomeItemMastersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getIncomeItemMasterId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws BaseAppException {
        Optional<IncomeItemMastersEntity> optional = incomeItemMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomeItemMastersEntity.class);
        }

        IncomeItemMastersEntity mastersEntity = optional.get();

        if (!mastersEntity.getStatus().equals(IncomeItemMastersEntity.STATUS.DU_THAO)
                && !mastersEntity.getStatus().equals(IncomeItemMastersEntity.STATUS.DA_TINH_THUE)) {
            throw new BaseAppException(I18n.getMessage("pit.tax.validate.delete"));
        }

        incomeItemMastersRepository.deleteByProperties(IncomeItemColumnsEntity.class, "incomeItemMasterId", id, "taxPeriodDate", mastersEntity.getTaxPeriodDate());
        incomeItemMastersRepository.deleteByProperties(IncomeItemDetailsEntity.class, "incomeItemMasterId", id, "taxPeriodDate", mastersEntity.getTaxPeriodDate());
        incomeItemMastersRepository.deleteById(IncomeItemMastersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IncomeItemMastersResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<IncomeItemMastersEntity> optional = incomeItemMastersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomeItemMastersEntity.class);
        }
        IncomeItemMastersResponse dto = new IncomeItemMastersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportData(IncomeItemMastersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/tax/BM_danh_sach_khoan_thu_nhap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = incomeItemMastersRepository.getListExport(dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_danh_sach_khoan_thu_nhap.xlsx");

    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Long importIncome(MultipartFile fileImport, Long incomeItemId, Date taxPeriodDate, String isCalculated) throws Exception {
        IncomeItemsEntity incomeItemsEntity = incomeItemMastersRepository.get(IncomeItemsEntity.class, incomeItemId);
        if (incomeItemsEntity == null) {
            throw new RecordNotExistsException(incomeItemId, IncomeItemsEntity.class);
        }
        //Lay ban ghi da import neu co
        IncomeItemMastersEntity mastersEntity = incomeItemMastersRepository.getIncomeItemMaster(incomeItemId, taxPeriodDate);
        if (mastersEntity != null) {
            if (mastersEntity.getStatus().equals(IncomeItemMastersEntity.STATUS.DA_CHOT)) {
                throw new BaseAppException("Tồn tại dữ liệu của khoản thu nhập đã được chốt!");
            } else if (mastersEntity.getStatus().equals(IncomeItemMastersEntity.STATUS.DA_KE_KHAI)) {
                throw new BaseAppException("Tồn tại dữ liệu của khoản thu nhập đã được kê khai!");
            } else {
                throw new BaseAppException("Đã tồn tại dữ liệu của khoản thu nhập!");
            }
        }

        IncomeTemplatesEntity templatesEntity = incomeItemMastersRepository.get(IncomeTemplatesEntity.class, incomeItemsEntity.getIncomeTemplateId());
        List<IncomeTemplateColumnsEntity> columns = incomeItemMastersRepository.findByProperties(IncomeTemplateColumnsEntity.class, "incomeTemplateId", incomeItemsEntity.getIncomeTemplateId(), "orderNumber");
        List<CategoryResponse> orgBeans = categoryRepository.getCategories(Constant.CATEGORY_TYPE.THUE_DON_VI_KE_KHAI);
        Map<String, Long> mapDonVis = new HashMap();
        orgBeans.forEach(item -> {
            mapDonVis.put(item.getName().toLowerCase(), Long.valueOf(item.getValue()));
        });

        ImportExcel importExcel = new ImportExcel(initImportConfigBean(templatesEntity, columns), 100000, 2);
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            Map<String, String> mapFullNames = new HashMap<>();
            List<String> empCodes = getEmpCodes(templatesEntity, dataList);
            //Lay danh sach nhan vien theo ma nhan vien
            List<EmployeeDto> listEmps = employeeRepository.getEmployeeDtos(empCodes);
            listEmps.stream().forEach(item -> {
                mapFullNames.put(item.getEmployeeCode(), item.getFullName());
            });

            int row = 0;
            List<IncomeItemDetailsEntity> detailsEntities = new ArrayList<>();
            Map<String, String> mapExtraCodes = new HashMap<>();
            Map<String, String> mapTaxCodes = new HashMap<>();
            Map<String, String> mapPersonalIdCodes = new HashMap<>();

            for (Object[] obj : dataList) {
                IncomeItemDetailsEntity itemDetailsEntity = new IncomeItemDetailsEntity(taxPeriodDate);
                //luu so thu tu phu vu order khi xuat bao cao
                itemDetailsEntity.setOrderNumber((long) row);
                if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.STAFF)
                        || templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.ALL)) {
                    if (obj[1] != null && mapFullNames.get((String) obj[1]) == null) {
                        importExcel.addError(row, 1, "Mã nhân viên không hợp lệ", (String) obj[1]);
                    } else if (obj[1] != null) {
                        String fullName = templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.STAFF) ? (String) obj[2] : (String) obj[3];
                        fullName = Utils.normalizeFullName(fullName);
                        if (!fullName.equalsIgnoreCase(mapFullNames.get((String) obj[1]))) {
                            importExcel.addError(row, 1, String.format("Họ và tên không trùng với tên của nhân viên theo mã %s (%s) trong hệ thống", obj[1], mapFullNames.get((String) obj[1])), fullName);
                        }
                    }
                }
                int col = 1;
                if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.STAFF)) {
                    itemDetailsEntity.setEmpCode((String) obj[col++]);
                    itemDetailsEntity.setFullName(Utils.normalizeFullName((String) obj[col++]));
                } else {
                    if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.ALL)) {
                        itemDetailsEntity.setEmpCode((String) obj[col++]);
                    }
                    itemDetailsEntity.setExtraCode((String) obj[col++]);
                    itemDetailsEntity.setFullName(Utils.normalizeFullName((String) obj[col++]));
                    //validate ma so thue
                    if (!Utils.isValidTaxNo((String) obj[col])) {
                        itemDetailsEntity.setNote(Utils.join(", ", itemDetailsEntity.getNote(), "Mã số thuế không hợp lệ"));
                    }
                    itemDetailsEntity.setTaxNo((String) obj[col++]);

                    itemDetailsEntity.setPersonalIdNo((String) obj[col++]);
                    itemDetailsEntity.setPersonalIdDate((Date) obj[col++]);
                    itemDetailsEntity.setPersonalIdPlace((String) obj[col++]);

                    String value = "" + itemDetailsEntity.getExtraCode() + "-" + itemDetailsEntity.getTaxNo()
                            + "-" + itemDetailsEntity.getPersonalIdNo()
                            + "-" + itemDetailsEntity.getPersonalIdDate()
                            + "-" + itemDetailsEntity.getPersonalIdPlace();
                    if (!Utils.isNullOrEmpty(itemDetailsEntity.getExtraCode())) {
                        if (mapExtraCodes.get(itemDetailsEntity.getExtraCode()) != null && !value.equalsIgnoreCase(mapExtraCodes.get(itemDetailsEntity.getExtraCode()))) {
                            itemDetailsEntity.setNote(Utils.join(", ", itemDetailsEntity.getNote(), "Trùng mã phụ nhưng khác thông tin họ tên, mã số thuế hoặc cccd!"));
                        } else {
                            mapExtraCodes.put(itemDetailsEntity.getExtraCode(), value);
                        }
                    }
                    if (!Utils.isNullOrEmpty(itemDetailsEntity.getTaxNo())) {
                        if (mapTaxCodes.get(itemDetailsEntity.getTaxNo()) != null && !value.equalsIgnoreCase(mapTaxCodes.get(itemDetailsEntity.getTaxNo()))) {
                            itemDetailsEntity.setNote(Utils.join(", ", itemDetailsEntity.getNote(), "Trùng mã số thuế nhưng khác thông tin mã phụ,họ tên hoặc cccd!"));
                        } else {
                            mapTaxCodes.put(itemDetailsEntity.getTaxNo(), value);
                        }
                    }
                    if (!Utils.isNullOrEmpty(itemDetailsEntity.getPersonalIdNo())) {
                        if (mapPersonalIdCodes.get(itemDetailsEntity.getPersonalIdNo()) != null && !value.equalsIgnoreCase(mapPersonalIdCodes.get(itemDetailsEntity.getPersonalIdNo()))) {
                            itemDetailsEntity.setNote(Utils.join(", ", itemDetailsEntity.getNote(), "Trùng CCCD/CMT hoặc hộ chiếu nhưng khác thông tin mã phụ,họ tên, mã số thuế hoặc nơi cấp, ngày cấp!"));
                        } else {
                            mapPersonalIdCodes.put(itemDetailsEntity.getPersonalIdNo(), value);
                        }
                    }

                }
                String message = "";
                for (IncomeTemplateColumnsEntity column : columns) {
                    Long value = (Long) obj[col++];
                    if (value != null && value < 0L) {
                        message = "Dữ liệu import có chứa giá trị âm";
                    }
                    itemDetailsEntity.getMapValues().put(column.getColumnCode(), value);
                }
                if (StringUtils.isNotBlank(message)) {
                    itemDetailsEntity.setNote(Utils.join(", ", itemDetailsEntity.getNote(), message));
                }
                itemDetailsEntity.setIncomeTax((Long) obj[col++]);
                if (!"Y".equalsIgnoreCase(isCalculated) && !Utils.NVL(itemDetailsEntity.getIncomeTax()).equals(0L)) {
                    //bao loi khi chon import chua tinh thue nhung co nhap thong tin thue
                    importExcel.addError(row, col, "Không được nhập thông tin thuế khi chọn import chưa tính thuế!", Utils.formatNumber(itemDetailsEntity.getIncomeTax()));
                }
                if("Y".equalsIgnoreCase(isCalculated) && !Utils.isNullObject(itemDetailsEntity.getIncomeTax())) {
                    //bao loi khi chon import da tinh thue nhung khong nhap thong tin thue
                    importExcel.addError(row, col, "Không được bỏ trống thông tin thuế khi chọn import đã tính thuế!","");
                }

                itemDetailsEntity.setMonthRetroTax((Long) obj[col++]);
                itemDetailsEntity.setYearRetroTax((Long) obj[col++]);
                if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.NON_REST)) {
                    String donvi = (String) obj[col];
                    if (mapDonVis.get(donvi.toLowerCase()) == null) {
                        importExcel.addError(row, col, "Đơn vị cần nhập đúng theo danh mục tại sheet 'Danh sach don vi'", donvi);
                    } else {
                        itemDetailsEntity.setDeclareOrgId(mapDonVis.get(donvi.toLowerCase()));
                        itemDetailsEntity.setWorkOrgId(mapDonVis.get(donvi.toLowerCase()));
                    }
                    col++;
                }

                String accountingIncomeName = "";
                if (StringUtils.equalsAnyIgnoreCase(templatesEntity.getType(), IncomeTemplatesEntity.TYPES.VL, IncomeTemplatesEntity.TYPES.TNK)) {
                    accountingIncomeName = (String) obj[col++];
                    accountingIncomeName = StringUtils.trim(accountingIncomeName);
                } else {
                    accountingIncomeName = incomeItemsEntity.getName();
                }
                itemDetailsEntity.setAccountingIncomeName(accountingIncomeName);

                String note = (String) obj[col];
                note = StringUtils.trim(note);
                if (StringUtils.isNotBlank(note)) {
                    itemDetailsEntity.setNote(Utils.join(", ", itemDetailsEntity.getNote(), note));
                }

                detailsEntities.add(itemDetailsEntity);
                row++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                if (mastersEntity != null) {
                    //xoa du lieu da ton tai
                    incomeItemMastersRepository.deleteOldData(mastersEntity);
                }
                IncomeItemMastersEntity incomeItemMastersEntity = new IncomeItemMastersEntity();
                incomeItemMastersEntity.setIncomeItemId(incomeItemId);
                incomeItemMastersEntity.setInputTimes(1);
                incomeItemMastersEntity.setTaxPeriodDate(taxPeriodDate);
                incomeItemMastersEntity.setIsTaxCalculated(isCalculated.equals("Y") ? 1 : 0);
                if (incomeItemMastersEntity.getIsTaxCalculated() == 1) {
                    incomeItemMastersEntity.setStatus(IncomeItemMastersEntity.STATUS.DA_TINH_THUE);
                    incomeItemMastersEntity.setTaxCalBy(Utils.getUserNameLogin());
                    incomeItemMastersEntity.setTaxDate(new Date());
                } else {
                    incomeItemMastersEntity.setStatus(IncomeItemMastersEntity.STATUS.DU_THAO);
                }
                incomeItemMastersEntity.setCreatedBy(Utils.getUserNameLogin());
                incomeItemMastersEntity.setCreatedTime(new Date());
                incomeItemMastersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                incomeItemMastersRepositoryJPA.save(incomeItemMastersEntity);

                incomeItemMastersRepository.saveData(detailsEntities, incomeItemMastersEntity.getIncomeItemMasterId());
                incomeItemMastersRepositoryJPA.flush();
                incomeItemMastersRepository.updateTaxDeclareOrgId(incomeItemMastersEntity.getIncomeItemMasterId(), taxPeriodDate);

                return incomeItemMastersEntity.getIncomeItemMasterId();
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
    }

    private List<String> getEmpCodes(IncomeTemplatesEntity templatesEntity, List<Object[]> dataList) {
        List<String> empCodes = new ArrayList<>();
        for (Object[] obj : dataList) {
            if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.STAFF)
                    || templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.ALL)) {
                String empCode = (String) obj[1];
                if (!Utils.isNullOrEmpty(empCode)) {
                    if (!empCodes.contains(empCode.toUpperCase())) {
                        empCodes.add(empCode.toUpperCase());
                    }
                }
            }
        }
        return empCodes;
    }

    private ImportExcel.ImportConfigBean[] initImportConfigBean(IncomeTemplatesEntity templatesEntity, List<IncomeTemplateColumnsEntity> columns) {

        List<ImportExcel.ImportConfigBean> columnExcels = new ArrayList<>();
        columnExcels.add(new ImportExcel.ImportConfigBean("stt", ImportExcel.STRING, true, 10, false));
        if (templatesEntity.getEmpType().equalsIgnoreCase("STAFF")) {
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("global.employeeCode"), ImportExcel.STRING, false, 10, false));
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("global.fullName"), ImportExcel.STRING, false, 300, false));
        } else {
            if (templatesEntity.getEmpType().equalsIgnoreCase("ALL")) {
                columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("global.employeeCode"), ImportExcel.STRING, true, 10, false));
            }
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.extraCode"), ImportExcel.STRING, true, 50, false));
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.fullName"), ImportExcel.STRING, false, 300, false));
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.taxNumber"), ImportExcel.STRING, true, 10, false));
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.personalIdNo"), ImportExcel.STRING, true, 12, false));
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.personalIdDate"), ImportExcel.DATE, true, 10, false));
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.personalIdPlace"), ImportExcel.STRING, true, 300, false));
        }
        for (IncomeTemplateColumnsEntity column : columns) {
            columnExcels.add(new ImportExcel.ImportConfigBean(column.getColumnName(), ImportExcel.LONG, true, 20, false));
        }
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.incomeTax"), ImportExcel.LONG, true, 20, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.monthRetroTax"), ImportExcel.LONG, true, 20, false));
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.yearRetroTax"), ImportExcel.LONG, true, 20, false));
        if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.NON_REST)) {
            //doi tuong vang lai thi bo sung them cot don vi
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.declareOrg"), ImportExcel.STRING, false, 300, false));
        }
        if (StringUtils.equalsAnyIgnoreCase(templatesEntity.getType(), IncomeTemplatesEntity.TYPES.TNK, IncomeTemplatesEntity.TYPES.VL)) {
            columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.accountingIncomeName"), ImportExcel.STRING, false, 500, false));
        }
        columnExcels.add(new ImportExcel.ImportConfigBean(I18n.getMessage("pit.incomeItem.note"), ImportExcel.STRING, true, 500, false));
        return columnExcels.toArray(new ImportExcel.ImportConfigBean[columnExcels.size()]);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateTax(Long id) throws Exception {

        //Lay du lieu de tinh thue
        IncomeItemMastersEntity mastersEntity = incomeItemMastersRepository.get(IncomeItemMastersEntity.class, id);
        if (mastersEntity == null || mastersEntity.isDeleted()) {
            throw new RecordNotExistsException(id, IncomeItemMastersEntity.class);
        }
        //chi duoc tinh thue ban ghi o trang thai chua tinh thue hoac da tinh thue
        if (!IncomeItemMastersEntity.STATUS.DU_THAO.equalsIgnoreCase(mastersEntity.getStatus())
                && !IncomeItemMastersEntity.STATUS.DA_TINH_THUE.equalsIgnoreCase(mastersEntity.getStatus())) {
            throw new BaseAppException("Chỉ được tính thuế khoản thu nhập đang ở trạng thái dự thảo hoặc đã tính thuế!");
        } else if (mastersEntity.getIsTaxCalculated() == 1) {
            throw new BaseAppException("Khoản thu nhập đã được tính thuế bên ngoài!");
        }
        TaxParameterDto taxParameterDto = configParameterRepository.getConfig(TaxParameterDto.class, mastersEntity.getTaxPeriodDate());
        List<TaxRateDetailEntity> ratioDetailEntities = taxRateDetailRepository.getTaxRatios(mastersEntity.getTaxPeriodDate());

        //Lay danh sach doi tuong can tinh thue
        List<IncomeItemDetailsDto> detailsEntities = incomeItemMastersRepository.getIncomeItemDetails(id, mastersEntity.getTaxPeriodDate());
        //thuc hien gom cac ban ghi cua cung 1 doi tuong tinh thue vao
        List<String> empCodes = detailsEntities.stream()
                .filter(item -> !Utils.isNullOrEmpty(item.getEmpCode()))
                .map(IncomeItemDetailsDto::getEmpCode)
                .distinct()
                .collect(Collectors.toList());
        //Lay thong tin doi tuong cua nhan vien tai ngay cuoi ky
        List<EmpTaxInfoDto> listEmps = taxDeclareMastersRepository.getEmpTaxInfos(empCodes, mastersEntity.getTaxPeriodDate());
        Map<String, EmpTaxInfoDto> mapEmps = listEmps.stream()
                .collect(Collectors.toMap(EmpTaxInfoDto::getEmpCode, Function.identity()));

        //Lay tong cac khoan da chi cua nhan vien trong thang de tinh bieu thue luy tien
        List<IncomeItemDetailsDto> listPreIncomes = incomeItemMastersRepository.getPreIncomeItems(empCodes, mastersEntity.getTaxPeriodDate(), id);
        Map<String, IncomeItemDetailsDto> mapPreIncomes = new HashMap<>();
        listPreIncomes.stream().forEach(item -> {
            if (mapPreIncomes.get(item.getEmpCode()) == null) {
                mapPreIncomes.put(item.getEmpCode(), item);
            } else {
                mapPreIncomes.get(item.getEmpCode()).add(item);
            }
        });
        detailsEntities.stream().forEach(item -> {
            if (!Utils.isNullOrEmpty(item.getEmpCode()) && mapEmps.get(item.getEmpCode()) != null) {
                if ("446681".equalsIgnoreCase(item.getEmpCode())) {
                    log.debug("catch you");
                }
                //xac dinh cach tinh thue cua nhan vien
                String taxMethod = null;
                try {
                    taxMethod = getTaxMethod(item, mapEmps.get(Utils.NVL(item.getEmpCode())), taxParameterDto);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                //xem tong cac khoan thu nhap trong thang nho hon muc toi thieu thi set thue = 0
                Long totalIncomeTaxable = Utils.NVL(item.getIncomeTaxable())
                        + (mapPreIncomes.get(item.getEmpCode()) == null ? 0L : Utils.NVL(mapPreIncomes.get(item.getEmpCode()).getIncomeTaxable()));

                if (totalIncomeTaxable > taxParameterDto.getMinIncome()) {
                    long incomeTax = 0L;
                    if (taxMethod.equalsIgnoreCase(Constant.TAXES_METHOD.KO_CAM_KET)) {
                        //số thuế cần thu = số thuế phải thu - số thuế đã thu
                        long thuNhapTinhThue = item.getIncomeHasTaxes()
                                + (mapPreIncomes.get(item.getEmpCode()) == null ? 0L : Utils.NVL(mapPreIncomes.get(item.getEmpCode()).getIncomeHasTaxes()));
                        incomeTax = Math.round(thuNhapTinhThue * 0.1
                                - (mapPreIncomes.get(item.getEmpCode()) == null ? 0L : Utils.NVL(mapPreIncomes.get(item.getEmpCode()).getIncomeTax())));
                    } else if (taxMethod.equalsIgnoreCase(Constant.TAXES_METHOD.CAM_KET)) {
                        //to do
                    } else {
                        long thuNhapTinhThue = item.getIncomeHasTaxes()
                                + (mapPreIncomes.get(item.getEmpCode()) == null ? 0L : Utils.NVL(mapPreIncomes.get(item.getEmpCode()).getIncomeHasTaxes()))
                                - taxParameterDto.getSelfDeduct()
                                - taxParameterDto.getDependentDeduct() * Utils.NVL(mapEmps.get(item.getEmpCode()).getNumOfDependents());
                        if (mapEmps.get(item.getEmpCode()) != null) {
                            item.setNumOfDependents(mapEmps.get(item.getEmpCode()).getNumOfDependents());
                        }
                        long thuePhaiThu = 0L;
                        for (TaxRateDetailEntity ratio : ratioDetailEntities) {
                            if (thuNhapTinhThue > ratio.getAmount()) {
                                thuePhaiThu += Math.round((thuNhapTinhThue - ratio.getAmount()) * ratio.getPercent() / 100);
                                thuNhapTinhThue = ratio.getAmount();
                            }
                        }
                        incomeTax = thuePhaiThu -
                                (mapPreIncomes.get(item.getEmpCode()) == null ? 0L : Utils.NVL(mapPreIncomes.get(item.getEmpCode()).getIncomeTax()));
                    }
//                    //nêu số thuế cần thu < 0 thì set = 0
//                    if (incomeTax < 0) {
//                        item.setIncomeTax(0l);
//                    } else
                    if (item.getIncomeHasTaxes() <= 0) {
                        item.setIncomeTax(0l);
                    } else {
                        if (incomeTax > item.getIncomeHasTaxes()) {
                            item.setIncomeTax(item.getIncomeHasTaxes());
                        } else {
                            item.setIncomeTax(incomeTax);
                        }
                    }
                    item.setTaxMethod(taxMethod);
                } else {
                    item.setIncomeTax(0L);
                    item.setTaxMethod(taxMethod);
                }

            } else if (!Utils.isNullOrEmpty(item.getEmpCode())) {
                //truong hop co ma nhan vien nhung khong lay duoc thong tin cua nhan vien
                //dua ra thong bao loi
            } else {
                //truong hop khong co ma nhan vien
                //xac dinh cach tinh thue la khong cam ket
                String key = String.join(Utils.NVL(item.getTaxNo()), Utils.NVL(item.getPersonalIdNo()), Utils.NVL(item.getFullName()));
                long tongThuNhap = Utils.NVL(item.getIncomeTaxable()) + (mapPreIncomes.get(key) == null ? 0L : Utils.NVL(mapPreIncomes.get(key).getIncomeTaxable()));
                if (tongThuNhap > taxParameterDto.getMinIncome()) {
                    long tongThuePhaiThu = Math.round(0.1 * (Utils.NVL(item.getIncomeTax10()) + (mapPreIncomes.get(key) == null ? 0L : Utils.NVL(mapPreIncomes.get(key).getIncomeTax10())))
                            + 0.2 * (Utils.NVL(item.getIncomeTax20()) + (mapPreIncomes.get(key) == null ? 0L : Utils.NVL(mapPreIncomes.get(key).getIncomeTax20()))));

                    long incomeTax = tongThuePhaiThu - (mapPreIncomes.get(key) == null ? 0L : mapPreIncomes.get(key).getIncomeTax());
                    item.setIncomeTax(incomeTax);
                } else {
                    item.setIncomeTax(0L);
                }
                item.setTaxMethod(Constant.TAXES_METHOD.KO_CAM_KET);
            }
            //add vao thu nhap tong
            String key = item.getEmpCode();
            if (Utils.isNullOrEmpty(item.getEmpCode())) {
                key = String.join(Utils.NVL(item.getTaxNo()), Utils.NVL(item.getPersonalIdNo()), Utils.NVL(item.getFullName()));
            }
            if (mapPreIncomes.get(key) == null) {
                try {
                    mapPreIncomes.put(key, item.clone());
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                mapPreIncomes.get(key).add(item);
            }
        });

        incomeItemMastersRepository.updateTax(detailsEntities);
        mastersEntity.setModifiedTime(new Date());
        mastersEntity.setModifiedBy(Utils.getUserNameLogin());
        mastersEntity.setStatus(IncomeItemMastersEntity.STATUS.DA_TINH_THUE);
        mastersEntity.setTaxCalBy(Utils.getUserNameLogin());
        mastersEntity.setTaxDate(new Date());
        incomeItemMastersRepositoryJPA.save(mastersEntity);
        incomeItemMastersRepositoryJPA.flush();

        //sau khi tinh thue thi update lai bang master
        incomeItemMastersRepository.updateDataMasterById(id);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void lockPeriodById(Long id) throws BaseAppException {
        Optional<IncomeItemMastersEntity> optional = incomeItemMastersRepositoryJPA.findById(id);
        if (!optional.isPresent()
                || optional.get().isDeleted()
        ) {
            throw new RecordNotExistsException(id, IncomeItemMastersEntity.class);
        }
        IncomeItemMastersEntity mastersEntity = optional.get();
        //chi duoc khoa ban ghi o trang thai da tinh thue
        if (IncomeItemMastersEntity.STATUS.DU_THAO.equals(mastersEntity.getStatus())) {
            throw new BaseAppException("Không thể thực hiện chốt khi bản ghi chưa được tính thuế!");
        } else if (!IncomeItemMastersEntity.STATUS.DA_TINH_THUE.equals(mastersEntity.getStatus())) {
            throw new BaseAppException("Chỉ được thực hiện chốt với bản ghi ở trạng thái đã tính thuế!");
        }
        mastersEntity.setModifiedBy(Utils.getUserNameLogin());
        mastersEntity.setModifiedTime(new Date());
        mastersEntity.setStatus(IncomeItemMastersEntity.STATUS.DA_CHOT);
        incomeItemMastersRepositoryJPA.save(mastersEntity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void unLockPeriodById(Long id) throws BaseAppException {
        Optional<IncomeItemMastersEntity> optional = incomeItemMastersRepositoryJPA.findById(id);
        if (!optional.isPresent()
                || optional.get().isDeleted()
        ) {
            throw new RecordNotExistsException(id, IncomeItemMastersEntity.class);
        }
        IncomeItemMastersEntity mastersEntity = optional.get();
        //chi duoc khoa ban ghi o trang thai da tinh thue
        if (!IncomeItemMastersEntity.STATUS.DA_CHOT.equals(mastersEntity.getStatus())) {
            throw new BaseAppException("Chỉ được thực hiện mở chốt với bản ghi ở trạng thái đã chốt!");
        }
        mastersEntity.setModifiedBy(Utils.getUserNameLogin());
        mastersEntity.setModifiedTime(new Date());
        mastersEntity.setStatus(IncomeItemMastersEntity.STATUS.DA_TINH_THUE);
        incomeItemMastersRepositoryJPA.save(mastersEntity);
    }

    private String getTaxMethod(IncomeItemDetailsDto item, EmpTaxInfoDto empTaxInfoDto, TaxParameterDto taxParameterDto) throws ParseException {
        if (Utils.isNullOrEmpty(item.getEmpCode()) || empTaxInfoDto == null) {
            return Constant.TAXES_METHOD.KO_CAM_KET;
        }

        ContractProcessDTO contractProcessDTO = incomeItemMastersRepository.getEmployeeByCode(item.getEmpCode());
        if (contractProcessDTO.getEndDate() == null) {
            contractProcessDTO.setEndDate(new Date());
        }

        int totalMonth = (contractProcessDTO != null) ? Utils.NVL(Utils.calculateMonthsBetween(contractProcessDTO.getStartDate(), contractProcessDTO.getEndDate())) : 0;

        if (contractProcessDTO == null || totalMonth < 12) {
            return Constant.TAXES_METHOD.KO_CAM_KET;
        }

        String status = empTaxInfoDto.getStatus();
        if ("OUT".equals(status) || "ORG_CHANGED".equals(status)) {
            return Utils.NVL(empTaxInfoDto.getIncomeCommitment()) > 0 ? Constant.TAXES_METHOD.CAM_KET : Constant.TAXES_METHOD.KO_CAM_KET;
        }

        if (taxParameterDto.getDoiTuongLuyTiens().contains(empTaxInfoDto.getEmpTypeCode())) {
            return totalMonth >= 12  ? Constant.TAXES_METHOD.LUY_TIEN : Constant.TAXES_METHOD.KO_CAM_KET;
        }
        return Utils.NVL(empTaxInfoDto.getIncomeCommitment()) > 0 ? Constant.TAXES_METHOD.CAM_KET : Constant.TAXES_METHOD.KO_CAM_KET;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportDetailIncomeById(Long incomeItemMasterId, Integer isPreview) throws Exception {
        Optional<IncomeItemMastersEntity> optional = incomeItemMastersRepositoryJPA.findById(incomeItemMasterId);
        if (!optional.isPresent() || optional.get().isDeleted()) {
            throw new RecordNotExistsException(incomeItemMasterId, IncomeItemMastersEntity.class);
        }

        List<IncomeItemDetailsDto> listDataExport = incomeItemDetailRepository.getDataDetailByMaster(incomeItemMasterId);
        if (Utils.isNullOrEmpty(listDataExport)) {
            return ResponseUtils.getResponseDataNotFound();
        } else {
            String pathTemplate = "template/export/tax/chi-tiet-khoan-thu-nhap.xlsx";
            ExportExcel dynamicExport = new ExportExcel(pathTemplate, 3, true);
            IncomeItemMastersEntity entity = optional.get();
            IncomeItemsEntity itemsEntity = incomeItemsRepository.get(IncomeItemsEntity.class, "incomeItemId", entity.getIncomeItemId());
            if (itemsEntity == null) {
                return ResponseUtils.getResponseDataNotFound();
            }
            IncomeTemplatesEntity templatesEntity = incomeItemsRepository.get(IncomeTemplatesEntity.class, "incomeTemplateId", itemsEntity.getIncomeTemplateId());
            List<IncomeTemplateColumnsEntity> columns = incomeItemsRepository.findByProperties(IncomeTemplateColumnsEntity.class, "incomeTemplateId", itemsEntity.getIncomeTemplateId(), "orderNumber");
            Map<String, List<IncomeItemColumnDto>> mapIncomeColumn = incomeItemDetailRepository.getIncomeItemColumnByMasterId(incomeItemMasterId);
            int col = 1;
            if (templatesEntity.getEmpType().equalsIgnoreCase("STAFF")) {
                dynamicExport.setText(I18n.getMessage("global.employeeCode"), col++);
                dynamicExport.setText(I18n.getMessage("global.fullName"), col++);
            } else {
                if (templatesEntity.getEmpType().equalsIgnoreCase("ALL")) {
                    dynamicExport.setText(I18n.getMessage("global.employeeCode"), col++);
                }
                dynamicExport.setText(I18n.getMessage("pit.incomeItem.extraCode"), col++);
                dynamicExport.setText(I18n.getMessage("pit.incomeItem.fullName"), col++);
                dynamicExport.setText(I18n.getMessage("pit.incomeItem.taxNumber"), col++);
                dynamicExport.setText(I18n.getMessage("pit.incomeItem.personalIdNo"), col++);
                dynamicExport.setText(I18n.getMessage("pit.incomeItem.personalIdDate"), col++);
                dynamicExport.setText(I18n.getMessage("pit.incomeItem.personalIdPlace"), col++);
            }
            for (IncomeTemplateColumnsEntity column : columns) {
                dynamicExport.setText(column.getColumnName(), col++);
            }
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.incomeTax"), col++);
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.monthRetroTax"), col++);
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.yearRetroTax"), col++);
            if (!templatesEntity.getEmpType().equalsIgnoreCase("STAFF")) {
                dynamicExport.setText(I18n.getMessage("pit.incomeItem.declareOrg"), col++);
            }
            //in cach tinh thue + so nguoi giam tru gia canh
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.taxMethod"), col++);
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.numOfDependents"), col++);
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.note"), col++);
            dynamicExport.setCellFormat(0, col - 1, ExportExcel.BORDER_FORMAT);

            int index = 1;
            for (IncomeItemDetailsDto dto : listDataExport) {
                dynamicExport.increaseRow();
                col = 0;
                dynamicExport.setEntry(String.valueOf(index++), col++);
                if (templatesEntity.getEmpType().equalsIgnoreCase("STAFF")) {
                    dynamicExport.setText(dto.getEmpCode(), col++);
                    dynamicExport.setText(dto.getFullName(), col++);
                } else {
                    if (templatesEntity.getEmpType().equalsIgnoreCase("ALL")) {
                        dynamicExport.setText(dto.getEmpCode(), col++);
                    }
                    dynamicExport.setText(dto.getExtraCode(), col++);
                    dynamicExport.setText(dto.getFullName(), col++);
                    dynamicExport.setText(dto.getTaxNo(), col++);
                    dynamicExport.setText(dto.getPersonalIdNo(), col++);
                    dynamicExport.setText(Utils.formatDate(dto.getPersonalIdDate()), col++);
                    dynamicExport.setText(dto.getPersonalIdPlace(), col++);
                }

                String key = String.format(Constant.KEY_MASTER_DETAIL, dto.getIncomeItemMasterId(), dto.getIncomeItemDetailId());
                List<IncomeItemColumnDto> columnList = mapIncomeColumn.get(key);
                if (!Utils.isNullOrEmpty(columnList)){
                    for (IncomeItemColumnDto item : columnList) {
                        dynamicExport.setNumber(item.getColumnValue(), col++);
                    }
                }

                dynamicExport.setNumber(dto.getIncomeTax(), col++);
                dynamicExport.setNumber(dto.getMonthRetroTax(), col++);
                dynamicExport.setNumber(dto.getYearRetroTax(), col++);
                if (!templatesEntity.getEmpType().equalsIgnoreCase("STAFF")) {
                    dynamicExport.setText(dto.getDeclareOrgName(), col++);
                }
                dynamicExport.setText(getTaxMethodName(dto.getTaxMethod()), col++);
                dynamicExport.setNumber(dto.getNumOfDependents(), col++);
                dynamicExport.setText(dto.getNote(), col);
            }
            dynamicExport.setCellFormat(3, 0, dynamicExport.getLastRow(), col, ExportExcel.BORDER_FORMAT);
            if (isPreview == null || isPreview > 0) {
                return ResponseUtils.ok(dynamicExport, "chi-tiet-khoan-thu-nhap.xlsx");
            } else {
                return ResponseUtils.ok(dynamicExport, "chi-tiet-khoan-thu-nhap.xlsx", false);
            }
        }
    }

    private String getTaxMethodName(String taxMethod) {
        if (Constant.TAXES_METHOD.LUY_TIEN.equals(taxMethod)) {
            return "Lũy tiến";
        }
        if (Constant.TAXES_METHOD.KO_CAM_KET.equals(taxMethod)) {
            return "Không cam kết";
        }
        if (Constant.TAXES_METHOD.CAM_KET.equals(taxMethod)) {
            return "Cam kết";
        }
        return "N/A";
    }

    @Override
    public void undoCalculateTax(Long incomeItemMasterId) throws BaseAppException {
        Optional<IncomeItemMastersEntity> optional = incomeItemMastersRepositoryJPA.findById(incomeItemMasterId);
        if (!optional.isPresent() || optional.get().isDeleted()) {
            throw new RecordNotExistsException(incomeItemMasterId, IncomeItemMastersEntity.class);
        }
        IncomeItemMastersEntity mastersEntity = optional.get();
        if (!IncomeItemMastersEntity.STATUS.DA_TINH_THUE.equalsIgnoreCase(mastersEntity.getStatus())) {
            throw new BaseAppException("Chỉ được hủy tính thuế bản ghi ở trạng thái Đã tính thuế!");
        }
        if (mastersEntity.getIsTaxCalculated() == 1) {
            throw new BaseAppException("Không được hủy tính thế bản ghi import đã tính thuế!");
        }
        mastersEntity.setStatus(IncomeItemMastersEntity.STATUS.DU_THAO);
        mastersEntity.setModifiedBy(Utils.getUserNameLogin());
        mastersEntity.setModifiedTime(new Date());
        mastersEntity.setTaxCalBy(null);
        mastersEntity.setTaxDate(null);
        incomeItemMastersRepositoryJPA.save(mastersEntity);
    }
}
