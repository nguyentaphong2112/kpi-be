/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.BookEditionsRequest;
import vn.hbtplus.models.response.BookEditionsResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lib_book_editions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class BookEditionsRepository extends BaseRepository {

    public BaseDataTableDto searchData(BookEditionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_edition_id,
                    a.book_id,
                    a.published_year,
                    a.store_id,
                    a.total_pages,
                    a.book_format_id,
                    a.publisher_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, BookEditionsResponse.class);
    }

    public List<Map<String, Object>> getListExport(BookEditionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_edition_id,
                    a.book_id,
                    a.published_year,
                    a.store_id,
                    a.total_pages,
                    a.book_format_id,
                    a.publisher_id,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, BookEditionsRequest.SearchForm dto) {
        sql.append("""
            FROM lib_book_editions a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<BookEditionsResponse.ChooseBookEdition> getListEditions(Long bookId) {
        String sql = """
                select 
                	e.book_edition_id,
                	CONCAT(ifnull(sc.`name`, 'NXB - '), ' (Năm ', ifnull(e.published_year,'') , ')', ' - ', ifnull(st.name,'')
                	, ' - ', ifnull(sf.name,'')) as name
                from lib_book_editions e
                	left join sys_categories sc on sc.category_type = 'LIB_NHA_XUAT_BAN' and sc.`value` = e.publisher_id
                	left join sys_categories st on st.category_type = 'LIB_KHO_SACH' and st.`value` = e.store_id
                	left join sys_categories sf on sf.category_type = 'LIB_KICH_THUOC' and sf.`value` = e.book_format_id
                where e.is_deleted = 'N'
                and e.book_id = :bookId
                order by name
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("bookId", bookId);
        return getListData(sql, params, BookEditionsResponse.ChooseBookEdition.class);
    }
}
