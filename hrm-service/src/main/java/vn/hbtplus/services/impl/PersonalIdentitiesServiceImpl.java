/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.PersonalIdentitiesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.PersonalIdentitiesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.PersonalIdentitiesEntity;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.impl.PersonalIdentitiesRepository;
import vn.hbtplus.repositories.jpa.PersonalIdentitiesRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.PersonalIdentitiesService;
import vn.hbtplus.utils.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang hr_personal_identities
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PersonalIdentitiesServiceImpl implements PersonalIdentitiesService {

    private final PersonalIdentitiesRepository personalIdentitiesRepository;
    private final PersonalIdentitiesRepositoryJPA personalIdentitiesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final EmployeesRepository employeesRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<PersonalIdentitiesResponse.SearchResult> getPersonalIdentities(Long id, BaseSearchRequest request) {
        return personalIdentitiesRepository.getPersonalIdentities(id, request);
    }

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PersonalIdentitiesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(personalIdentitiesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(PersonalIdentitiesRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException {
        validateData(dto, employeeId, id);
        PersonalIdentitiesEntity entity;
        boolean autoUpdateIsMain = false;
        if (id != null && id > 0L) {
            entity = personalIdentitiesRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("personalIdentityId and employeeId not matching!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            //truong hop reset gia tri ve 'N'
            if (StringUtils.equalsIgnoreCase(entity.getIsMain(), BaseConstants.COMMON.YES)
                    && StringUtils.equalsIgnoreCase(dto.getIsMain(), BaseConstants.COMMON.NO)
            ) {
                //truong hop can set 1 ban ghi ve isHighest = Y
                autoUpdateIsMain = true;
            }
        } else {
            entity = new PersonalIdentitiesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
            //neu chua co giay to nao thi set is_main = Y
            if (personalIdentitiesRepository.findByProperties(PersonalIdentitiesEntity.class, "employeeId", employeeId, "isDeleted", BaseConstants.STATUS.NOT_DELETED, "isMain", BaseConstants.COMMON.YES).isEmpty()) {
                dto.setIsMain(BaseConstants.COMMON.YES);
            }
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        personalIdentitiesRepositoryJPA.saveAndFlush(entity);

        if (autoUpdateIsMain) {
            personalIdentitiesRepository.autoUpdateIsMain(employeeId);
        }
        if (StringUtils.equalsIgnoreCase(entity.getIsMain(), BaseConstants.COMMON.YES)) {
            personalIdentitiesRepository.updatePersonalIdentity(employeeId, entity.getPersonalIdentityId());
        }
        objectAttributesService.saveObjectAttributes(entity.getPersonalIdentityId(), dto.getListAttributes(), PersonalIdentitiesEntity.class, null);
        return ResponseUtils.ok(entity.getPersonalIdentityId());
    }

    private void validateData(PersonalIdentitiesRequest.SubmitForm dto, Long employeeId, Long id) {
        boolean duplicateIdNo = personalIdentitiesRepository.checkDuplicateIdentityNo(dto, employeeId);
        if (duplicateIdNo) {
            throw new BaseAppException("ERROR_IDENTITY_DUPLICATE_WITH_EMP", I18n.getMessage("error.personalIdentity.identityNo.duplicateWithEmp"));
        }

        List<PersonalIdentitiesEntity> entityList = personalIdentitiesRepository.getPersonalIdentityList(id, dto.getIdentityTypeId(), employeeId);
        if (entityList.isEmpty()) {
            return;
        }

        entityList.forEach(item -> {
            if (!StringUtils.equalsIgnoreCase(item.getIdentityNo(), dto.getIdentityNo())) {
                throw new BaseAppException("ERROR_IDENTITY_DUPLICATE_WITH_EMP", I18n.getMessage("Chỉ được phép tồn tại 1 số định danh duy nhất"));
            }
        });
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<PersonalIdentitiesEntity> optional = personalIdentitiesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PersonalIdentitiesEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("personalIdentityId and employeeId not matching!");
        }
        personalIdentitiesRepository.deActiveObject(PersonalIdentitiesEntity.class, id);
        if (StringUtils.equalsIgnoreCase(optional.get().getIsMain(), BaseConstants.COMMON.YES)) {
            personalIdentitiesRepository.autoUpdateIsMain(employeeId);
        }
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PersonalIdentitiesResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<PersonalIdentitiesEntity> optional = personalIdentitiesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PersonalIdentitiesEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("personalIdentityId and employeeId not matching!");
        }
        PersonalIdentitiesResponse.DetailBean dto = new PersonalIdentitiesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, personalIdentitiesRepository.getSQLTableName(PersonalIdentitiesEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/danh-sach-thong-tin-dinh-danh.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = personalIdentitiesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-thong-tin-dinh-danh.xlsx");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate, String identityTypeId) throws Exception {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<Object[]> dataList = new ArrayList<>();
        String fileConfigName = "BM_Import_thong_tin_dinh_danh.xml";
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
            Map<Long, List<PersonalIdentitiesEntity>> mapIdentity = personalIdentitiesRepository.getMapDataByCode(empCodeList);
            Map<String, List<PersonalIdentitiesEntity>> mapAllData = personalIdentitiesRepository.getMapAllData();
            Map<String, String> placeMap = new HashMap<>();
            if (!"3".equals(identityTypeId)) {
                String type = "1".equals(identityTypeId) ? Constant.CATEGORY_CODES.NOI_CAP_CCCD : Constant.CATEGORY_CODES.NOI_CAP_CMT;
                placeMap = employeesRepository.getListCategories(type).stream().collect(Collectors.toMap(dto -> dto.getName().toLowerCase(), CategoryDto::getName, (existing, replacement) -> replacement));
            }

            int row = 0;
            int col;
            List<PersonalIdentitiesEntity> listSave = new ArrayList<>();
            for (Object[] obj : dataList) {
                col = 1;
                String employeeCode = (String) obj[col++];
                String employeeName = obj[col] != null ? (String) obj[col] : null;
                col++;
                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, 1, "Mã nhân viên không hợp lệ", employeeCode);
                    break;
                }
                String identityNo = (String) obj[col++];
                if ("1".equals(identityTypeId) && (identityNo.trim().length() != 12 || !identityNo.matches("\\d+"))) {
                    importExcel.addError(row, col - 1, I18n.getMessage("Số CCCD phải có độ dài là 12 và phải là số"), identityNo.trim());
                    break;
                } else if ("2".equals(identityTypeId) && (identityNo.trim().length() != 9 || !identityNo.matches("\\d+"))) {
                    importExcel.addError(row, col - 1, I18n.getMessage("Số CMT phải có độ dài là 9 và phải là số"), identityNo.trim());
                    break;
                } else if (!Utils.isNullOrEmpty(mapAllData.get(identityNo.toLowerCase()))
                        && !mapAllData.get(identityNo.toLowerCase()).get(0).getEmployeeId().equals(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId())) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.personalIdentity.identityNo.duplicateWithEmp"), identityNo.trim());
                    break;
                } else if (!Utils.isNullOrEmpty(mapAllData.get(identityNo.toLowerCase())) && !mapAllData.get(identityNo.toLowerCase()).get(0).getIdentityTypeId().equals(identityTypeId)) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.personalIdentity.identityNo.duplicateWithEmp"), identityNo.trim());
                    break;
                }

                String identityIssuePlaceName = ((String) obj[col++]).trim();
                String identityIssuePlace = null;
                if (!"3".equals(identityTypeId)) {
                    identityIssuePlace = placeMap.get(identityIssuePlaceName.toLowerCase());
                    if (identityIssuePlace == null) {
                        importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), identityIssuePlaceName);
                        break;
                    }
                }
                Date identityIssueDate = (Date) obj[col++];
                Date expiredDate = obj[col] != null ? (Date) obj[col] : null;
                List<PersonalIdentitiesEntity> personalIdentitiesEntities = mapIdentity.get(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId());
                PersonalIdentitiesEntity personalIdentitiesEntity;
                boolean isValid = false;
                if (!Utils.isNullOrEmpty(personalIdentitiesEntities)) {
                    for (PersonalIdentitiesEntity identitiesEntity : personalIdentitiesEntities) {
                        if (identityTypeId.equals(identitiesEntity.getIdentityTypeId()) && isForceUpdate) {
                            personalIdentitiesEntity = identitiesEntity;
                            personalIdentitiesEntity.setIdentityNo(identityNo);
                            personalIdentitiesEntity.setIdentityIssueDate(identityIssueDate);
                            personalIdentitiesEntity.setIdentityIssuePlace(!"3".equals(identityTypeId) ? identityIssuePlace : identityIssuePlaceName);
                            personalIdentitiesEntity.setModifiedBy(userName);
                            personalIdentitiesEntity.setModifiedTime(currentDate);
                            personalIdentitiesEntity.setExpiredDate(expiredDate);
                            listSave.add(personalIdentitiesEntity);
                            isValid = true;
                            break;
                        } else if (identitiesEntity.getIdentityNo().equals(identityNo) && identityTypeId.equals(identitiesEntity.getIdentityTypeId())) {
                            personalIdentitiesEntity = identitiesEntity;
                            personalIdentitiesEntity.setIdentityIssueDate(identityIssueDate);
                            personalIdentitiesEntity.setIdentityIssuePlace(!"3".equals(identityTypeId) ? identityIssuePlace : identityIssuePlaceName);
                            personalIdentitiesEntity.setModifiedBy(userName);
                            personalIdentitiesEntity.setModifiedTime(currentDate);
                            personalIdentitiesEntity.setExpiredDate(expiredDate);
                            listSave.add(personalIdentitiesEntity);
                            isValid = true;
                            break;
                        } else if (identityTypeId.equals(identitiesEntity.getIdentityTypeId())) {
                            isValid = true;
                            importExcel.addError(row, 3, I18n.getMessage("Chỉ được phép tồn tại 1 số định danh duy nhất"), identityNo.trim());
                            break;
                        }
                    }
                }
                if (!isValid) {
                    personalIdentitiesEntity = new PersonalIdentitiesEntity();
                    personalIdentitiesEntity.setEmployeeId(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId());
                    personalIdentitiesEntity.setIdentityTypeId(identityTypeId);
                    personalIdentitiesEntity.setIdentityNo(identityNo);
                    personalIdentitiesEntity.setIdentityIssueDate(identityIssueDate);
                    personalIdentitiesEntity.setIdentityIssuePlace(!"3".equals(identityTypeId) ? identityIssuePlace : identityIssuePlaceName);
                    personalIdentitiesEntity.setExpiredDate(expiredDate);
                    personalIdentitiesEntity.setCreatedBy(userName);
                    personalIdentitiesEntity.setCreatedTime(currentDate);
                    listSave.add(personalIdentitiesEntity);
                }
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                personalIdentitiesRepositoryJPA.saveAll(listSave);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> downloadImportTemplate(String identityTypeId) throws Exception {
        String pathTemplate = "template/import/BM_Import_thong_tin_dinh_danh.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        if (!"3".equals(identityTypeId)) {
            String type = "1".equals(identityTypeId) ? Constant.CATEGORY_CODES.NOI_CAP_CCCD : Constant.CATEGORY_CODES.NOI_CAP_CMT;
            List<CategoryDto> placeList = employeesRepository.getListCategories(type);
            dynamicExport.setActiveSheet(1);
            int row = 1;
            for (CategoryDto entry : placeList) {
                dynamicExport.setText(String.valueOf(row), 0, row);
                dynamicExport.setText(entry.getName(), 1, row++);
            }
            dynamicExport.setActiveSheet(0);
        }

        return ResponseUtils.ok(dynamicExport, "BM_Import_thong_tin_dinh_danh.xlsx", false);
    }

}
