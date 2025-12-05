/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.DocumentTypesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ContractTypesEntity;
import vn.hbtplus.repositories.entity.DocumentTypesEntity;
import vn.hbtplus.repositories.impl.DocumentTypesRepository;
import vn.hbtplus.repositories.jpa.DocumentTypesRepositoryJPA;
import vn.hbtplus.services.DocumentTypesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.ObjectAttributesService;
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
 * Lop impl service ung voi bang hr_document_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class DocumentTypesServiceImpl implements DocumentTypesService {

    private final DocumentTypesRepository documentTypesRepository;
    private final DocumentTypesRepositoryJPA documentTypesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DocumentTypesResponse.SearchResult> searchData(DocumentTypesRequest.SearchForm dto) {
        return ResponseUtils.ok(documentTypesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(DocumentTypesRequest.SubmitForm dto, Long documentTypeId) throws BaseAppException {

        boolean isDuplicate = documentTypesRepository.duplicate(DocumentTypesEntity.class, documentTypeId, "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_DOCUMENT_DUPLICATE", I18n.getMessage("error.documentTypes.code.duplicate"));
        }

        isDuplicate = documentTypesRepository.duplicate(DocumentTypesEntity.class, documentTypeId, "name", dto.getName());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_DOCUMENT_DUPLICATE", I18n.getMessage("error.documentTypes.name.duplicate"));
        }


        DocumentTypesEntity entity;
        if (documentTypeId != null && documentTypeId > 0L) {
            entity = documentTypesRepositoryJPA.getById(documentTypeId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new DocumentTypesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        documentTypesRepositoryJPA.save(entity);
        documentTypesRepositoryJPA.flush();

        objectAttributesService.saveObjectAttributes(entity.getDocumentTypeId(), dto.getListAttributes(), DocumentTypesEntity.class, null);
        return ResponseUtils.ok(entity.getDocumentTypeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<DocumentTypesEntity> optional = documentTypesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DocumentTypesEntity.class);
        }
        documentTypesRepository.deActiveObject(DocumentTypesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DocumentTypesResponse.DetailBean> getDataById(Long id)  throws RecordNotExistsException {
        Optional<DocumentTypesEntity> optional = documentTypesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DocumentTypesEntity.class);
        }
        DocumentTypesResponse.DetailBean dto = new DocumentTypesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, documentTypesRepository.getSQLTableName(DocumentTypesEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(DocumentTypesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = documentTypesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ListResponseEntity<DocumentTypesResponse.DetailBean> getList() {
        List<DocumentTypesEntity> listData = documentTypesRepository.findAll(DocumentTypesEntity.class);
        return ResponseUtils.ok(Utils.mapAll(listData, DocumentTypesResponse.DetailBean.class));
    }

}
