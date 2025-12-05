/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.BookTranslatorsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookTranslatorsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.BookTranslatorEntity;
import vn.hbtplus.repositories.impl.BookTranslatorsRepository;
import vn.hbtplus.repositories.jpa.BookTranslatorsRepositoryJPA;
import vn.hbtplus.services.BookTranslatorService;
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
 * Lop impl service ung voi bang lib_book_translators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class BookTranslatorServiceImpl implements BookTranslatorService {

    private final BookTranslatorsRepository bookTranslatorsRepository;
    private final BookTranslatorsRepositoryJPA bookTranslatorsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<BookTranslatorsResponse> searchData(BookTranslatorsRequest.SearchForm dto) {
        return ResponseUtils.ok(bookTranslatorsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(BookTranslatorsRequest.SubmitForm dto) throws BaseAppException {
        BookTranslatorEntity entity;
        if (dto.getBookTranslatorId() != null && dto.getBookTranslatorId() > 0L) {
            entity = bookTranslatorsRepositoryJPA.getById(dto.getBookTranslatorId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new BookTranslatorEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        bookTranslatorsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getBookTranslatorId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<BookTranslatorEntity> optional = bookTranslatorsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookTranslatorEntity.class);
        }
        bookTranslatorsRepository.deActiveObject(BookTranslatorEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<BookTranslatorsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<BookTranslatorEntity> optional = bookTranslatorsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookTranslatorEntity.class);
        }
        BookTranslatorsResponse dto = new BookTranslatorsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(BookTranslatorsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = bookTranslatorsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
