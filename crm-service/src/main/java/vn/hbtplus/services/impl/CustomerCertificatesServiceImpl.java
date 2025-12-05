/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.CustomerCertificatesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CustomerCertificatesEntity;
import vn.hbtplus.repositories.impl.CustomerCertificatesRepository;
import vn.hbtplus.repositories.jpa.CustomerCertificatesRepositoryJPA;
import vn.hbtplus.services.CustomerCertificatesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.I18n;
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
 * Lop impl service ung voi bang crm_customer_certificates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CustomerCertificatesServiceImpl implements CustomerCertificatesService {

    private final CustomerCertificatesRepository customerCertificatesRepository;
    private final CustomerCertificatesRepositoryJPA customerCertificatesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CustomerCertificatesResponse.SearchResult> searchData(CustomerCertificatesRequest.SearchForm dto) {
        return ResponseUtils.ok(customerCertificatesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CustomerCertificatesRequest.SubmitForm dto, Long id) throws BaseAppException {
        CustomerCertificatesEntity entity;
        boolean isDuplicate = customerCertificatesRepository.duplicate(CustomerCertificatesEntity.class, id, "customerId", dto.getCustomerId(), "certificateId", dto.getCertificateId());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CERTIFICATE_DUPLICATE", I18n.getMessage("error.certificate.duplicate"));
        }
        if (id != null && id > 0L) {
            entity = customerCertificatesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new CustomerCertificatesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setStatusId(CustomerCertificatesEntity.STATUS.CHO_PHE_DUYET);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        customerCertificatesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getCustomerCertificateId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CustomerCertificatesEntity> optional = customerCertificatesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomerCertificatesEntity.class);
        }
        customerCertificatesRepository.deActiveObject(CustomerCertificatesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CustomerCertificatesResponse.DetailBean> getDataById(Long id)  throws RecordNotExistsException {
        Optional<CustomerCertificatesEntity> optional = customerCertificatesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomerCertificatesEntity.class);
        }
        CustomerCertificatesResponse.DetailBean dto = new CustomerCertificatesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(CustomerCertificatesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_ds_quan_ly_chung_chi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = customerCertificatesRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        for (Map<String, Object> mapData : listDataExport) {
            Object productDetail = mapData.get("productDetail");
            if (productDetail != null) {
                mapData.put("productName", productDetail.toString().split("#")[0]);
            } else {
                mapData.put("productName", "");
            }
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_ds_quan_ly_chung_chi.xlsx");
    }

    @Override
    public ResponseEntity updateStatusById(CustomerCertificatesRequest.SubmitForm dto, Long id) throws RecordNotExistsException {
        Optional<CustomerCertificatesEntity> optional = customerCertificatesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CustomerCertificatesEntity.class);
        }
        CustomerCertificatesEntity entity = optional.get();
        String userName = Utils.getUserNameLogin();
        if (CustomerCertificatesEntity.STATUS.PHE_DUYET.equalsIgnoreCase(dto.getStatusId())) {
            if (CustomerCertificatesEntity.STATUS.CHO_PHE_DUYET.equalsIgnoreCase(entity.getStatusId())
            ) {
                entity.setStatusId(dto.getStatusId());
            } else if (CustomerCertificatesEntity.STATUS.DE_NGHI_XOA.equalsIgnoreCase(entity.getStatusId())) {
                entity.setIsDeleted(BaseConstants.STATUS.DELETED);
            }
            entity.setApprovedDate(new Date());
            entity.setApprovedBy(userName);
            entity.setApprovedNote(dto.getApprovedNote());
            customerCertificatesRepositoryJPA.save(entity);
        } else {
            if (CustomerCertificatesEntity.STATUS.DE_NGHI_XOA.equalsIgnoreCase(entity.getStatusId())) {
                entity.setStatusId(CustomerCertificatesEntity.STATUS.CHO_PHE_DUYET);
            }
            else {
                entity.setStatusId(dto.getStatusId());
            }
            entity.setApprovedNote(dto.getApprovedNote());
            entity.setApprovedDate(new Date());
            entity.setApprovedBy(userName);
            customerCertificatesRepositoryJPA.save(entity);
        }
        return ResponseUtils.ok(id);
    }

}
