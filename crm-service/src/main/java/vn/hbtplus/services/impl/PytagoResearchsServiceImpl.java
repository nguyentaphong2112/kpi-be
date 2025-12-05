/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.AttributeConfigDto;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.request.CustomersRequest;
import vn.hbtplus.models.request.PytagoResearchsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CustomersEntity;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.PytagoResearchsEntity;
import vn.hbtplus.repositories.impl.PytagoResearchsRepository;
import vn.hbtplus.repositories.jpa.CustomersRepositoryJPA;
import vn.hbtplus.repositories.jpa.EmployeesRepositoryJPA;
import vn.hbtplus.repositories.jpa.PytagoResearchsRepositoryJPA;
import vn.hbtplus.services.CustomersService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.PytagoResearchsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang crm_pytago_researchs
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PytagoResearchsServiceImpl implements PytagoResearchsService {

    private final PytagoResearchsRepository pytagoResearchsRepository;
    private final PytagoResearchsRepositoryJPA pytagoResearchsRepositoryJPA;
    private final EmployeesRepositoryJPA employeesRepositoryJPA;
    private final CustomersRepositoryJPA customersRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final CustomersService customersService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PytagoResearchsResponse> searchData(PytagoResearchsRequest.SearchForm dto) {
        return ResponseUtils.ok(pytagoResearchsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(PytagoResearchsRequest.SubmitForm dto) throws BaseAppException {
        PytagoResearchsEntity entity = new PytagoResearchsEntity();
        entity.setCreatedTime(new Date());
        entity.setCreatedBy(Utils.getUserNameLogin());
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        pytagoResearchsRepositoryJPA.save(entity);

        if (!Utils.getUserNameLogin().equals("unknown user")) {
            //update so lan da tra cuu
            increaseSearchCount(Utils.getUserNameLogin());
        }

        return ResponseUtils.ok(entity.getPytagoResearchId());
    }

    private void increaseSearchCount(String userNameLogin) {
        EmployeesEntity employeesEntity = employeesRepositoryJPA.getEmployeeByLoginName(userNameLogin);
        if (employeesEntity != null) {
            int count = pytagoResearchsRepository.updateSearchCount(employeesEntity.getEmployeeId(), "crm_employees");
            if (count == 0) {
                objectAttributesService.saveObjectAttribute(employeesEntity.getEmployeeId(), "crm_employees", "SO_LAN_TRA_CUU", "1", "long");
            }
        } else {
            CustomersEntity customersEntity = customersRepositoryJPA.getCustomerByLoginName(userNameLogin);
            int count = pytagoResearchsRepository.updateSearchCount(customersEntity.getCustomerId(), "crm_customers");
            if (count == 0) {
                objectAttributesService.saveObjectAttribute(customersEntity.getCustomerId(), "crm_customers", "SO_LAN_TRA_CUU", "1", "long");
            }
        }
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<PytagoResearchsEntity> optional = pytagoResearchsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PytagoResearchsEntity.class);
        }
        pytagoResearchsRepository.deActiveObject(PytagoResearchsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PytagoResearchsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<PytagoResearchsEntity> optional = pytagoResearchsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PytagoResearchsEntity.class);
        }
        PytagoResearchsResponse dto = new PytagoResearchsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(PytagoResearchsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_du_lieu_SHUD.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = pytagoResearchsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_du_lieu_SHUD.xlsx");
    }

    @Override
    public PytagoResearchsResponse.SearchCount getSearchCount() {
        String userNameLogin = Utils.getUserNameLogin();
        EmployeesEntity employeesEntity = employeesRepositoryJPA.getEmployeeByLoginName(userNameLogin);
        if (employeesEntity != null) {
            return pytagoResearchsRepository.getSearchCount(employeesEntity.getEmployeeId(), "crm_employees");
        } else {
            CustomersEntity customersEntity = customersRepositoryJPA.getCustomerByLoginName(userNameLogin);
            if (customersEntity != null) {
                return pytagoResearchsRepository.getSearchCount(customersEntity.getCustomerId(), "crm_customers");
            }
        }
        return null;
    }

    @Override
    public ResponseEntity createCustomer(Long id) {
        Optional<PytagoResearchsEntity> optional = pytagoResearchsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PytagoResearchsEntity.class);
        }
        CustomersEntity customersEntity = customersRepositoryJPA.getCustomerByLoginName(Utils.getUserNameLogin());
        Long customerId = customersEntity == null ? null : customersEntity.getCustomerId();
        PytagoResearchsEntity pytagoResearchsEntity = optional.get();
        CustomersRequest.SubmitForm submitForm = new CustomersRequest.SubmitForm();
        submitForm.setFullName(pytagoResearchsEntity.getFullName());
        submitForm.setEmail(pytagoResearchsEntity.getEmail());
        submitForm.setDateOfBirth(pytagoResearchsEntity.getDateOfBirth());
        submitForm.setMobileNumber(pytagoResearchsEntity.getMobileNumber());
        submitForm.setUserTakeCareId(customerId);
        submitForm.setIntroducerId(customerId);
        submitForm.setReceiverId(customerId);
        submitForm.setLoginName(pytagoResearchsEntity.getMobileNumber());
        submitForm.setPassword("123456");
        AttributeRequestDto attributeRequestDto = new AttributeRequestDto();
        attributeRequestDto.setAttributeValue(pytagoResearchsEntity.getCurrentAddress());
        attributeRequestDto.setDataType("string");
        attributeRequestDto.setAttributeName("Địa chỉ hiện tại");
        attributeRequestDto.setAttributeCode("DIA_CHI_HIEN_TAI");
        submitForm.setListAttributes(Utils.castToList(attributeRequestDto));
        return customersService.saveData(submitForm, null);
    }

}
