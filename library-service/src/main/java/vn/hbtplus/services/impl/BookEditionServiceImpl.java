/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.BookEditionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookEditionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.BookEditionEntity;
import vn.hbtplus.repositories.impl.BookEditionsRepository;
import vn.hbtplus.repositories.jpa.BookEditionsRepositoryJPA;
import vn.hbtplus.services.BookEditionService;
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
 * Lop impl service ung voi bang lib_book_editions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class BookEditionServiceImpl implements BookEditionService {

    private final BookEditionsRepository bookEditionsRepository;
    private final BookEditionsRepositoryJPA bookEditionsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<BookEditionsResponse> searchData(BookEditionsRequest.SearchForm dto) {
        return ResponseUtils.ok(bookEditionsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(BookEditionsRequest.SubmitForm dto) throws BaseAppException {
        BookEditionEntity entity;
        if (dto.getBookEditionId() != null && dto.getBookEditionId() > 0L) {
            entity = bookEditionsRepositoryJPA.getById(dto.getBookEditionId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new BookEditionEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        bookEditionsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getBookEditionId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<BookEditionEntity> optional = bookEditionsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookEditionEntity.class);
        }
        bookEditionsRepository.deActiveObject(BookEditionEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<BookEditionsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<BookEditionEntity> optional = bookEditionsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookEditionEntity.class);
        }
        BookEditionsResponse dto = new BookEditionsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(BookEditionsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = bookEditionsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<BookEditionsResponse.ChooseBookEdition> getListEditions(Long bookId) {
        return bookEditionsRepository.getListEditions(bookId);
    }

}
