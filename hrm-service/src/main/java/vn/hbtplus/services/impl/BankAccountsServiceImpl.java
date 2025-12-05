/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.BankAccountsRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BankAccountsResponse;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.BankAccountsEntity;
import vn.hbtplus.repositories.entity.PersonalIdentitiesEntity;
import vn.hbtplus.repositories.impl.BankAccountsRepository;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.jpa.BankAccountsRepositoryJPA;
import vn.hbtplus.services.BankAccountsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang hr_bank_accounts
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class BankAccountsServiceImpl implements BankAccountsService {

    private final BankAccountsRepository bankAccountsRepository;
    private final BankAccountsRepositoryJPA bankAccountsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final EmployeesRepository employeesRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<BankAccountsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(bankAccountsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(BankAccountsRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException {
        boolean duplicateIdNo = bankAccountsRepository.checkDuplicateBankAccount(dto, id);
        if (duplicateIdNo) {
            throw new BaseAppException("ERROR_BANK_ACCOUNT_DUPLICATE_WITH_EMP", I18n.getMessage("error.bankAccount.accountNo.duplicateWithEmp"));
        }

        BankAccountsEntity entity;
        if (id != null && id > 0L) {
            entity = bankAccountsRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("bankAccountId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new BankAccountsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        bankAccountsRepositoryJPA.saveAndFlush(entity);

        if (BaseConstants.COMMON.YES.equalsIgnoreCase(entity.getIsMain())) {
            bankAccountsRepository.updateBankAccount(employeeId, entity.getBankAccountId());
        }
        objectAttributesService.saveObjectAttributes(entity.getBankAccountId(), dto.getListAttributes(), BankAccountsEntity.class, null);
        return ResponseUtils.ok(entity.getBankAccountId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<BankAccountsEntity> optional = bankAccountsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BankAccountsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("bankAccountId and employeeId not match!");
        }
        bankAccountsRepository.deActiveObject(BankAccountsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<BankAccountsResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<BankAccountsEntity> optional = bankAccountsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BankAccountsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("bankAccountId and employeeId not match!");
        }
        BankAccountsResponse.DetailBean dto = new BankAccountsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, bankAccountsRepository.getSQLTableName(BankAccountsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/thong-tin-tai-khoan.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = bankAccountsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-tai-khoan.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<BankAccountsResponse.SearchResult> getBankAccounts(Long id, BaseSearchRequest request) {
        return bankAccountsRepository.getBankAccounts(id, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate) throws Exception {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<Object[]> dataList = new ArrayList<>();
        String fileConfigName = "BM_Import_thong_tin_tai_khoan.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = obj[1] != null ? ((String) obj[1]).toUpperCase() : null;
                if (empCode != null && !empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }
            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);
            Map<String, String> bankMap = employeesRepository.getListCategories(Constant.CATEGORY_CODES.NGAN_HANG).stream().collect(Collectors.toMap(dto -> dto.getName().toLowerCase(), CategoryDto::getValue, (existing, replacement) -> replacement));
            Map<Long, List<BankAccountsEntity>> mapBankByEmpCode = bankAccountsRepository.getMapDataByCode(empCodeList);
            Map<String, List<BankAccountsEntity>> mapBankAll = bankAccountsRepository.getMapAllData();
            int row = 0;
            int col;
            List<BankAccountsEntity> listSave = new ArrayList<>();
            for (Object[] obj : dataList) {
                col = 1;
                String employeeCode = (String) obj[col++];
                String employeeName = obj[col] != null ? (String) obj[col] : null;
                col++;
                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, 1, "Mã nhân viên không hợp lệ", employeeCode);
                    break;
                }
                String accountNo = ((String) obj[col++]).trim();
                if (!Utils.isNullOrEmpty(mapBankAll.get(accountNo.toLowerCase()))
                        && !mapBankAll.get(accountNo.toLowerCase()).get(0).getEmployeeId().equals(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId())) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.bankAccount.accountNo.duplicateWithEmp"), accountNo.trim());
                    break;
                }
                String bankName = ((String) obj[col++]).trim();
                String bankId = bankMap.get(bankName.toLowerCase());
                if (Utils.isNullOrEmpty(bankId)) {
                    importExcel.addError(row, col, I18n.getMessage("error.category.invalid"), bankName);
                    break;
                }
                String bankBranch = obj[col] != null ? (String) obj[col] : null;
                col++;
                String isMainName = obj[col] != null ? (String) obj[col] : null;
                String isMain = null;
                if (!Utils.isNullOrEmpty(isMainName)) {
                    isMain = BankAccountsEntity.IS_MAIN_MAP.get(isMainName);
                }
                List<BankAccountsEntity> bankAccountsEntities = mapBankByEmpCode.get(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId());
                BankAccountsEntity bankEntity;
                boolean isValid = false;
                if (!Utils.isNullOrEmpty(bankAccountsEntities)) {
                    for (BankAccountsEntity bankAccountsEntity : bankAccountsEntities) {
                        if (accountNo.equals(bankAccountsEntity.getAccountNo())) {
                            bankEntity = bankAccountsEntity;
                            bankEntity.setBankId(bankId);
                            bankEntity.setIsMain(isMain);
                            bankEntity.setBankBranch(bankBranch);
                            bankEntity.setModifiedBy(userName);
                            bankEntity.setModifiedTime(currentDate);
                            listSave.add(bankEntity);
                            isValid = true;
                        } else if ("Y".equals(isMain) && "Y".equals(bankAccountsEntity.getIsMain())) {
                            bankEntity = bankAccountsEntity;
                            bankEntity.setIsMain("N");
                            bankEntity.setModifiedBy(userName);
                            bankEntity.setModifiedTime(currentDate);
                            listSave.add(bankEntity);
                        }
                    }
                }
                if (!isValid) {
                    bankEntity = new BankAccountsEntity();
                    bankEntity.setEmployeeId(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId());
                    bankEntity.setAccountNo(accountNo);
                    bankEntity.setBankId(bankId);
                    bankEntity.setBankBranch(bankBranch);
                    bankEntity.setIsMain(isMain);
                    bankEntity.setCreatedBy(userName);
                    bankEntity.setCreatedTime(currentDate);
                    listSave.add(bankEntity);
                }
            }


            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                bankAccountsRepositoryJPA.saveAll(listSave);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        String pathTemplate = "template/import/BM_Import_thong_tin_tai_khoan.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<CategoryDto> bankList = employeesRepository.getListCategories(Constant.CATEGORY_CODES.NGAN_HANG);
        dynamicExport.setActiveSheet(1);
        int row = 1;
        for (CategoryDto entry : bankList) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(entry.getName(), 1, row++);
        }

        dynamicExport.setActiveSheet(0);


        return ResponseUtils.ok(dynamicExport, "BM_Import_thong_tin_tai_khoan.xlsx", false);
    }
}
