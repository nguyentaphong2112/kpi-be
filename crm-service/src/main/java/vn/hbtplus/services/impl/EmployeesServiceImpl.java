/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.AdminFeignClient;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.FamilyRelationshipsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.EmployeeProfilesEntity;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.FamilyRelationshipsEntity;
import vn.hbtplus.repositories.impl.EmployeeProfilesRepository;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.impl.FamilyRelationshipsRepository;
import vn.hbtplus.repositories.jpa.EmployeeProfilesRepositoryJPA;
import vn.hbtplus.repositories.jpa.EmployeesRepositoryJPA;
import vn.hbtplus.repositories.jpa.FamilyRelationshipsRepositoryJPA;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang crm_employees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EmployeesServiceImpl implements EmployeesService {

    private final EmployeesRepository employeesRepository;
    private final EmployeesRepositoryJPA employeesRepositoryJPA;
    private final FamilyRelationshipsRepositoryJPA familyRelationshipsRepositoryJPA;
    private final EmployeeProfilesRepositoryJPA employeeProfilesRepositoryJPA;
    private final EmployeeProfilesRepository employeeProfilesRepository;
    private final FamilyRelationshipsRepository familyRelationshipsRepository;
    private final AdminFeignClient adminFeignClient;
    private final ObjectAttributesService objectAttributesService;
    private final FileService fileService;
    private final AttachmentServiceImpl attachmentServiceImpl;
    private final HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmployeesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(employeesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EmployeesRequest.SubmitForm dto, Long id) throws BaseAppException {
        EmployeesEntity entity;

        boolean isDuplicate = employeesRepository.duplicate(EmployeesEntity.class, id, "mobile_number", dto.getMobileNumber());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_EMPLOYEES_DUPLICATE", I18n.getMessage("error.employee.mobileNumber.duplicate"));
        }

        if (id != null && id > 0L) {
            entity = employeesRepositoryJPA.getById(dto.getEmployeeId());
            dto.setLoginName(entity.getLoginName());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EmployeesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        employeesRepositoryJPA.save(entity);

        //Luu du lieu than nhan
        saveFamilyRelationship(entity.getEmployeeId(), dto.getFamilyRelationships());
        //xoa du lieu than nhan bi xoa
        if (!Utils.isNullOrEmpty(dto.getFamilyRelationshipIdDelete())) {
            familyRelationshipsRepository.deleteByFamilyRelationshipIds(dto.getFamilyRelationshipIdDelete(), FamilyRelationshipsEntity.OBJECT_TYPES.NHAN_VIEN, id);
        }
        //Luu du lieu ho so dinh kem
        saveProfileAttachments(entity.getEmployeeId(), dto.getProfileAttachments());

        //Luu thuoc tinh bo sung
        objectAttributesService.saveObjectAttributes(entity.getEmployeeId(), dto.getListAttributes(), EmployeesEntity.class, Constant.RESOURCES.EMPLOYEE);

        //Tạo mơi tài khoản
        if (id == null || id <= 0) {
            createUser(entity, dto.getPassword(), Utils.castToList(dto.getJobRankId()));
        }

        return ResponseUtils.ok(entity.getEmployeeId());
    }

    private void createUser(EmployeesEntity entity, String password, List<String> defaultRoles) {
        EmployeesRequest.CreateUser createUserRequest = new EmployeesRequest.CreateUser();
        createUserRequest.setEmail(entity.getEmail());
        createUserRequest.setLoginName(entity.getLoginName());
        createUserRequest.setPassword(password);
        createUserRequest.setFullName(entity.getFullName());
        createUserRequest.setMobileNumber(entity.getMobileNumber());
        createUserRequest.setDefaultRoles(defaultRoles);

        //xu ly them moi user
        try {
            adminFeignClient.createUser(Utils.getRequestHeader(request), createUserRequest);
        } catch (Exception e) {
            throw e;
        }
    }

    private void saveProfileAttachments(Long employeeId, List<EmployeesRequest.ProfileAttachment> profileAttachments) {
        if (!Utils.isNullOrEmpty(profileAttachments)) {
            profileAttachments.forEach(item -> {
                EmployeeProfilesEntity employeeProfileEntity = employeeProfilesRepositoryJPA.getByEmployeeIdAndAttachmentType(employeeId, item.getAttachmentType());
                if (employeeProfileEntity == null) {
                    employeeProfileEntity = new EmployeeProfilesEntity();
                    employeeProfileEntity.setEmployeeId(employeeId);
                    employeeProfileEntity.setAttachmentType(item.getAttachmentType());
                    employeeProfileEntity.setCreatedTime(new Date());
                    employeeProfileEntity.setCreatedBy(Utils.getUserNameLogin());
                }
                employeeProfileEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                employeeProfilesRepositoryJPA.save(employeeProfileEntity);

                //Luu file dinh kem
                fileService.deActiveFileByAttachmentId(item.getIdsDelete(), Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE_PROFILE, Constant.RESOURCES.EMPLOYEE);
                fileService.uploadFiles(item.getFileAttachments(), employeeProfileEntity.getEmployeeProfileId(), Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE_PROFILE, Constant.RESOURCES.EMPLOYEE, Constant.ATTACHMENT.MODULE);
            });
        }
    }

    private void saveFamilyRelationship(Long employeeId, List<FamilyRelationshipsRequest.SubmitForm> familyRelationships) {
        if (!Utils.isNullOrEmpty(familyRelationships)) {
            familyRelationships.forEach(item -> {
                FamilyRelationshipsEntity entity;
                if (item.getFamilyRelationshipId() != null && item.getFamilyRelationshipId() > 0L) {
                    entity = familyRelationshipsRepositoryJPA.getById(item.getFamilyRelationshipId());
                    entity.setModifiedTime(new Date());
                    entity.setModifiedBy(Utils.getUserNameLogin());
                } else {
                    entity = new FamilyRelationshipsEntity();
                    entity.setCreatedTime(new Date());
                    entity.setCreatedBy(Utils.getUserNameLogin());
                }
                Utils.copyProperties(item, entity);
                entity.setObjectId(employeeId);
                entity.setObjectType(FamilyRelationshipsEntity.OBJECT_TYPES.NHAN_VIEN);
                entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                familyRelationshipsRepositoryJPA.save(entity);
            });
        }
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<EmployeesEntity> optional = employeesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeesEntity.class);
        }
        employeesRepository.deActiveObject(EmployeesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EmployeesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<EmployeesEntity> optional = employeesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EmployeesEntity.class);
        }
        EmployeesResponse.DetailBean dto = new EmployeesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, "crm_employees"));
        List<EmployeesResponse.ProfileAttachment> profileAttachments = employeeProfilesRepository.getListProfileAttachments(id);
        profileAttachments.forEach(e -> {
            e.setAttachFileList(attachmentServiceImpl.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.EMPLOYEE_PROFILE, Constant.RESOURCES.EMPLOYEE, e.getEmployeeProfileId()));
        });
        dto.setProfileAttachments(profileAttachments);
        dto.setFamilyRelationships(familyRelationshipsRepository.getListFamilyRelationship(FamilyRelationshipsEntity.OBJECT_TYPES.NHAN_VIEN, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh_sach_nhan_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = employeesRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh_sach_nhan_vien.xlsx");
    }

    @Override
    public List<EmployeesResponse.SearchResult> getListEmployee(String keySearch) {
        return employeesRepository.getListEmployee(keySearch);
    }

}
