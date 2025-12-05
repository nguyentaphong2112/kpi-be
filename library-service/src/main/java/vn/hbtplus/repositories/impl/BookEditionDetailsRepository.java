/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.BookEditionDetailsRequest;
import vn.hbtplus.models.response.BookEditionDetailsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lib_book_edition_details
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class BookEditionDetailsRepository extends BaseRepository {

    public BaseDataTableDto searchData(BookEditionDetailsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_edition_detail_id,
                    a.book_edition_id,
                    a.book_no,
                    a.status,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, BookEditionDetailsResponse.class);
    }

    public List<Map<String, Object>> getListExport(BookEditionDetailsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_edition_detail_id,
                    a.book_edition_id,
                    a.book_no,
                    a.status,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, BookEditionDetailsRequest.SearchForm dto) {
        sql.append("""
                    FROM lib_book_edition_details a
                    
                    
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (dto.getEditionId() != null & dto.getEditionId() > 0L) {
            sql.append(" AND a.book_edition_id = :editionId");
            params.put("editionId", dto.getEditionId());
        }
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public BaseDataTableDto searchByEditionId(BookEditionDetailsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_no,
                    a.status,
                    a.note
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, BookEditionDetailsResponse.EditionDetail.class);
    }


    public BookEditionDetailsResponse.EditionDetail getDataByBookNo(String bookNo) {
        String sql = """
                select 
                    a.book_edition_detail_id,
                    a.status,
                    a.book_no,
                	b.title as bookTitle,
                	ln.borrowed_date borrowedDate,
                	mb.name borrowerName
                from
                	lib_book_editions ed ,
                	lib_books b ,
                	lib_book_edition_details a
                	left join lib_book_loans ln on ln.book_edition_detail_id = a.book_edition_detail_id
                		and ln.return_date is null and ln.is_deleted = 'N'
                	left join lib_members mb on mb.member_id = ln.member_id
                where a.book_edition_id = ed.book_edition_id
                	and ed.book_id = b.book_id
                	and a.is_deleted = 'N'
                	and ed.is_deleted = 'N'
                	and a.book_no like :bookNo
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("bookNo", bookNo);
        return getFirstData(sql, params, BookEditionDetailsResponse.EditionDetail.class);
    }

    public void updateStatus(List<Long> bookEditionDetailIds, String status) {
        String sql = "update lib_book_edition_details a" +
                " set a.status = :status," +
                "   a.modified_time = now()," +
                "   a.modified_by = :userName" +
                " where book_edition_detail_id in (:bookEditionDetailIds)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("bookEditionDetailIds", bookEditionDetailIds);
        params.put("status", status);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public List<String> getListExisted(List<String> bookNos) {
        String sql = "select book_no from lib_book_edition_details a" +
                " where a.is_deleted = 'N'" +
                "   and a.book_no in (:bookNos)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("bookNos", bookNos);
        return getListData(sql, params, String.class);
    }

    public String getMaxBookNo(String prefixBookNo) {
        String sql = "select max(book_no) from lib_book_edition_details a" +
                " where a.book_no like :prefixBookNo";
        HashMap<String, Object> params = new HashMap<>();
        params.put("prefixBookNo", prefixBookNo + "%");
        return getFirstData(sql, params, String.class);
    }
}
