/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import com.jxcell.CellException;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.BookEditionDetailsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookEditionDetailsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.utils.ExportExcel;

import java.util.List;

/**
 * Lop interface service ung voi bang lib_book_edition_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface BookEditionDetailService {

    TableResponseEntity<BookEditionDetailsResponse> searchData(BookEditionDetailsRequest.SearchForm dto);

    boolean saveData(Long bookEditionId, List<String> bookNos) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<BookEditionDetailsResponse> getDataById(Long id) throws RecordNotExistsException;

    TableResponseEntity<BookEditionDetailsResponse.EditionDetail> getDataByEditionId(BookEditionDetailsRequest.SearchForm dto) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(BookEditionDetailsRequest.SearchForm dto) throws Exception;

    BookEditionDetailsResponse.EditionDetail getDataByBookNo(String bookNo) throws BaseAppException;

    ExportExcel incrementBookNo(Integer total) throws BaseAppException, InstantiationException, IllegalAccessException, CellException;
}
