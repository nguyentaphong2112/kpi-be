/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.BookLoansRequest;
import vn.hbtplus.models.response.BookLoansResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lib_book_loans
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class BookLoansRepository extends BaseRepository {

    public BaseDataTableDto<BookLoansResponse> searchData(BookLoansRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_loan_id,
                    a.book_id
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, BookLoansResponse.class);
    }

    public List<Map<String, Object>> getListExport(BookLoansRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_loan_id,
                    a.book_id
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, BookLoansRequest.SearchForm dto) {
        sql.append("""
            FROM lib_book_loans a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<String> getListLoaned(List<Long> bookEditionDetailIds) {
        String sql = "select ed.book_no " +
                "   from lib_book_loans ln, lib_book_edition_details ed" +
                " where ed.book_edition_detail_id = ln.book_edition_detail_id" +
                "   and ln.return_date is null" +
                "   and ln.is_deleted = 'N'" +
                "   and ed.is_deleted = 'N'" +
                "   and ed.book_edition_detail_id in (:bookEditionDetailIds)  ";
        Map<String, Object> params = new HashMap<>();
        params.put("bookEditionDetailIds", bookEditionDetailIds);
        return getListData(sql, params, String.class);
    }

    public void saveReturning(List<Long> bookEditionDetailIds) {
        String sql = "update lib_book_loans ed " +
                " set ed.return_date = now()," +
                "   ed.modified_time = now()," +
                "   ed.modified_by = :userName" +
                "   where ed.book_edition_detail_id in (:bookEditionDetailIds)";
        Map<String, Object> params = new HashMap<>();
        params.put("bookEditionDetailIds", bookEditionDetailIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }
}
