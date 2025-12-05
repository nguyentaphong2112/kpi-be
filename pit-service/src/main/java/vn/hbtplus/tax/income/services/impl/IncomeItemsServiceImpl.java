/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.income.models.request.IncomeItemsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.income.models.response.CategoryResponse;
import vn.hbtplus.tax.income.models.response.IncomeItemsResponse;
import vn.hbtplus.tax.income.repositories.entity.IncomeItemsEntity;
import vn.hbtplus.tax.income.repositories.entity.IncomeTemplateColumnsEntity;
import vn.hbtplus.tax.income.repositories.entity.IncomeTemplatesEntity;
import vn.hbtplus.tax.income.repositories.impl.CategoryRepository;
import vn.hbtplus.tax.income.repositories.impl.IncomeItemsRepository;
import vn.hbtplus.tax.income.repositories.jpa.IncomeItemsRepositoryJPA;
import vn.hbtplus.tax.income.services.IncomeItemsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang pit_income_items
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class IncomeItemsServiceImpl implements IncomeItemsService {

    private final IncomeItemsRepository incomeItemsRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeItemsRepositoryJPA incomeItemsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<IncomeItemsResponse> searchData(IncomeItemsRequest.SearchForm dto) {
        return incomeItemsRepository.searchData(dto);
    }

    @Override
    @Transactional
    public Long saveData(IncomeItemsRequest.SubmitForm dto, Long id) throws RecordNotExistsException {
        IncomeItemsEntity entity;
        if (incomeItemsRepository.checkDuplicateName(dto.getName(), id, dto.getSalaryPeriodDate())) {
            throw new RecordNotExistsException(I18n.getMessage("pit.incomeItem.name.duplicate"));
        }
        if (id != null && id > 0L) {
            entity = incomeItemsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new IncomeItemsEntity();
            IncomeTemplatesEntity templatesEntity = incomeItemsRepository.get(IncomeTemplatesEntity.class, "incomeTemplateId", dto.getIncomeTemplateId());
            if (templatesEntity == null) {
                throw new RecordNotExistsException(I18n.getMessage("pit.incomeItem.template.not.exists"));
            }
            String salaryPeriod = Utils.formatDate(dto.getSalaryPeriodDate(), "yyyyMM");
            String code = incomeItemsRepository.generateCodeByTemplate(templatesEntity.getType() + salaryPeriod);
            entity.setCode(code);
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        incomeItemsRepositoryJPA.save(entity);
        return entity.getIncomeItemId();
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws BaseAppException {
        Optional<IncomeItemsEntity> optional = incomeItemsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomeItemsEntity.class);
        }

        if (incomeItemsRepository.checkUserIncomeItemById(id)) {
            throw new BaseAppException(I18n.getMessage("global.record.user.other.function"));
        }
        incomeItemsRepository.deActiveObject(IncomeItemsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<IncomeItemsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<IncomeItemsEntity> optional = incomeItemsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, IncomeItemsEntity.class);
        }
        IncomeItemsResponse dto = new IncomeItemsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(IncomeItemsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/tax/BM_danh_muc_khoan_thu_nhap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = incomeItemsRepository.getListExport(dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_danh_muc_khoan_thu_nhap.xlsx");
    }

    @Override
    public ResponseEntity<Object> downloadTemplate(Long id) throws Exception {
        Optional<IncomeItemsEntity> optionals = incomeItemsRepositoryJPA.findById(id);
        if (!optionals.isPresent() || optionals.get().isDeleted()) {
            throw new RecordNotExistsException(id, IncomeItemsEntity.class);
        }
        IncomeTemplatesEntity templatesEntity = incomeItemsRepository.findByProperties(IncomeTemplatesEntity.class, "incomeTemplateId", optionals.get().getIncomeTemplateId()).get(0);
        List<IncomeTemplateColumnsEntity> columns = incomeItemsRepository.findByProperties(IncomeTemplateColumnsEntity.class, "incomeTemplateId", optionals.get().getIncomeTemplateId(), "orderNumber");

        String pathTemplate = "template/export/tax/BM_Import_Thu_nhap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        int col = 1;
        if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.STAFF)) {
            dynamicExport.setText(I18n.getMessage("global.require.employeeCode"), col++);
            dynamicExport.setText(I18n.getMessage("global.require.fullName"), col++);
        } else {
            if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.ALL)) {
                dynamicExport.setText(I18n.getMessage("global.require.employeeCode"), col++);
            }
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.extraCode"), col++);
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.require.fullName"), col++);
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
        if (templatesEntity.getEmpType().equalsIgnoreCase(IncomeTemplatesEntity.EMP_TYPE.NON_REST)) {
            //Xu ly import doi tuong vang lai thi bo sung them cot don vi ke khai
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.require.declareOrg"), col++);
        }
        if (StringUtils.equalsAnyIgnoreCase(templatesEntity.getType(), IncomeTemplatesEntity.TYPES.TNK, IncomeTemplatesEntity.TYPES.VL)) {
            dynamicExport.setText(I18n.getMessage("pit.incomeItem.require.accountingIncomeName"), col++);
        }
        dynamicExport.setText(I18n.getMessage("pit.incomeItem.note"), col++);
        dynamicExport.setCellFormat(0, col - 1, ExportExcel.BORDER_FORMAT);

        //lay danh sach don vi ke khai thue
        List<CategoryResponse> orgBeans = categoryRepository.getCategories(Constant.CATEGORY_TYPE.THUE_DON_VI_KE_KHAI);
        dynamicExport.setActiveSheet(1);
        for (int i = 0; i < orgBeans.size(); i++) {
            CategoryResponse orgBean = orgBeans.get(i);
            dynamicExport.setText(String.valueOf(i+1), 0, i + 1);
            dynamicExport.setText(orgBean.getName(), 1, i + 1);
        }
        dynamicExport.setActiveSheet(0);
        String fileName = Utils.removeSign(templatesEntity.getName()) + ".xlsx";
        fileName = fileName.replace(" ", "-");
        return ResponseUtils.ok(dynamicExport, fileName, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeItemsResponse> getDataBySalaryPeriod(String salaryPeriodDate, String isImport) {
        return incomeItemsRepository.getDataBySalaryPeriod(salaryPeriodDate, isImport);
    }

    @Override
    @Transactional
    public void autoCreateForPeriod(Date periodDate) {
        List<IncomeTemplatesEntity> lstTemplatesEntities = incomeItemsRepository.getTemplateNotExisItems(periodDate);
        for (IncomeTemplatesEntity templatesEntity : lstTemplatesEntities) {
            List<String> itemNames = getIncomeItemNames(periodDate, templatesEntity.getType());
            itemNames.stream().forEach(itemName -> {
                IncomeItemsEntity incomeItemsEntity = new IncomeItemsEntity();
                incomeItemsEntity.setCode(String.format("%s%s01", templatesEntity.getType(), Utils.formatDate(periodDate, "yyyyMM")));
                incomeItemsEntity.setName(itemName);
                incomeItemsEntity.setIncomeTemplateId(templatesEntity.getIncomeTemplateId());
                incomeItemsEntity.setSalaryPeriodDate(periodDate);
                incomeItemsEntity.setCreatedTime(new Date());
                incomeItemsEntity.setCreatedBy("System");
                incomeItemsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                incomeItemsRepositoryJPA.save(incomeItemsEntity);
            });
        }
    }

    private List<String> getIncomeItemNames(Date periodDate, String type) {
        List<String> itemNames = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(periodDate);
        int month = calendar.get(Calendar.MONTH) + 1;
        if (IncomeTemplatesEntity.TYPES.LT.equalsIgnoreCase(type)) {
            itemNames.add(String.format("Lương/Thù lao quyết toán Tháng %s - KCQ & TT", Utils.formatDate(periodDate, "MM/yyyy")));
            itemNames.add(String.format("Lương/Thù lao quyết toán Tháng %s - 63 CNCT", Utils.formatDate(periodDate, "MM/yyyy")));
            itemNames.add(String.format("Lương/Thù lao quyết toán Tháng %s - Nhóm HTTT", Utils.formatDate(periodDate, "MM/yyyy")));
        } else if (IncomeTemplatesEntity.TYPES.MN.equalsIgnoreCase(type)) {
            itemNames.add(String.format("Thù lao CTV máy nổ Tháng %s", Utils.formatDate(periodDate, "MM/yyyy")));
        } else if (IncomeTemplatesEntity.TYPES.TNK.equalsIgnoreCase(type)) {
            itemNames.add(String.format("Thu nhập khác GP&DVKT Tháng %s", Utils.formatDate(periodDate, "MM/yyyy")));
            itemNames.add(String.format("Thu nhập khác TT B2C Tháng %s", Utils.formatDate(periodDate, "MM/yyyy")));
            itemNames.add(String.format("Thu nhập khác KCQ & TT Tháng %s", Utils.formatDate(periodDate, "MM/yyyy")));
            itemNames.add(String.format("Thu nhập khác 63 CNCT Tháng %s", Utils.formatDate(periodDate, "MM/yyyy")));
        } else if (IncomeTemplatesEntity.TYPES.SXKDQ.equalsIgnoreCase(type) && month % 3 == 0) {
            itemNames.add(String.format("Lương/Thù lao Quý %d/%s - KCQ & TT", month / 3, Utils.formatDate(periodDate, "yyyy")));
            itemNames.add(String.format("Lương/Thù lao Quý %d/%s - 63 CNCT", month / 3, Utils.formatDate(periodDate, "yyyy")));
            itemNames.add(String.format("Lương/Thù lao Quý %d/%s - Nhóm HTTT", month / 3, Utils.formatDate(periodDate, "yyyy")));
        } else if (IncomeTemplatesEntity.TYPES.SXKDN.equalsIgnoreCase(type) && month == 12) {
            itemNames.add(String.format("Lương/Thù lao năm %s - KCQ & TT", Utils.formatDate(periodDate, "yyyy")));
            itemNames.add(String.format("Lương/Thù lao năm %s - 63 CNCT", Utils.formatDate(periodDate, "yyyy")));
            itemNames.add(String.format("Lương/Thù lao năm %s - Nhóm HTTT", Utils.formatDate(periodDate, "yyyy")));
        }
        return itemNames;
    }
}
