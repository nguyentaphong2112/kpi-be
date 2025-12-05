/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.BookLoansRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookLoansResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.BookEditionDetailEntity;
import vn.hbtplus.repositories.entity.BookLoanEntity;
import vn.hbtplus.repositories.impl.BookEditionDetailsRepository;
import vn.hbtplus.repositories.impl.BookLoansRepository;
import vn.hbtplus.repositories.jpa.BookLoansRepositoryJPA;
import vn.hbtplus.services.BookLoanService;
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
 * Lop impl service ung voi bang lib_book_loans
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanService {

    private final BookLoansRepository bookLoansRepository;
    private final BookEditionDetailsRepository bookEditionDetailsRepository;
    private final BookLoansRepositoryJPA bookLoansRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<BookLoansResponse> searchData(BookLoansRequest.SearchForm dto) {
        return ResponseUtils.ok(bookLoansRepository.searchData(dto));
    }

    @Override
    @Transactional
    public boolean saveBorrowing(List<Long> bookEditionDetailIds, Long memberId) throws BaseAppException {
        BookLoanEntity entity;
        //validate bookEditionDetailIds
        if(isBeingLoaned(bookEditionDetailIds)){
            throw new BaseAppException("BOOK_ALREADY_LOANED", "error.book.already.loaned");
        }
        String userName = Utils.getUserNameLogin();
        bookEditionDetailIds.forEach(id -> {
            BookLoanEntity bookLoanEntity = new BookLoanEntity();
            bookLoanEntity.setBookEditionDetailId(id);
            bookLoanEntity.setBorrowedDate(new Date());
            bookLoanEntity.setMemberId(memberId);
            bookLoanEntity.setCreatedBy(userName);
            bookLoanEntity.setCreatedTime(new Date());
            bookLoansRepositoryJPA.save(bookLoanEntity);
        });

        //update trạng thái sách sang đã mượn
        bookEditionDetailsRepository.updateStatus(bookEditionDetailIds, BookEditionDetailEntity.STATUS.DANG_MUON);

        return true;
    }

    @Override
    @Transactional
    public boolean saveReturning(List<Long> ids) {
        bookLoansRepository.saveReturning(ids);

        bookEditionDetailsRepository.updateStatus(ids, BookEditionDetailEntity.STATUS.HIEN_CO);
        return true;
    }

    private boolean isBeingLoaned(List<Long> bookEditionDetailIds) {
        List<String> books = bookLoansRepository.getListLoaned(bookEditionDetailIds);
        return !books.isEmpty();
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<BookLoanEntity> optional = bookLoansRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookLoanEntity.class);
        }
        bookLoansRepository.deActiveObject(BookLoanEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<BookLoansResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<BookLoanEntity> optional = bookLoansRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookLoanEntity.class);
        }
        BookLoansResponse dto = new BookLoansResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(BookLoansRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = bookLoansRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
