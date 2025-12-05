/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.BookReservationsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookReservationsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.BookReservationEntity;
import vn.hbtplus.repositories.impl.BookReservationsRepository;
import vn.hbtplus.repositories.jpa.BookReservationsRepositoryJPA;
import vn.hbtplus.services.BookReservationService;
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
 * Lop impl service ung voi bang lib_book_reservations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class BookReservationServiceImpl implements BookReservationService {

    private final BookReservationsRepository bookReservationsRepository;
    private final BookReservationsRepositoryJPA bookReservationsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<BookReservationsResponse> searchData(BookReservationsRequest.SearchForm dto) {
        return ResponseUtils.ok(bookReservationsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(BookReservationsRequest.SubmitForm dto) throws BaseAppException {
        BookReservationEntity entity;
        if (dto.getLibBookReservationId() != null && dto.getLibBookReservationId() > 0L) {
            entity = bookReservationsRepositoryJPA.getById(dto.getLibBookReservationId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new BookReservationEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        bookReservationsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getLibBookReservationId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<BookReservationEntity> optional = bookReservationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookReservationEntity.class);
        }
        bookReservationsRepository.deActiveObject(BookReservationEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<BookReservationsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<BookReservationEntity> optional = bookReservationsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookReservationEntity.class);
        }
        BookReservationsResponse dto = new BookReservationsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(BookReservationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = bookReservationsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
