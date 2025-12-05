/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.BooksRequest;
import vn.hbtplus.models.response.BooksResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lib_books
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class BooksRepository extends BaseRepository {

    public BaseDataTableDto searchData(BooksRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_id,
                    a.title,
                    (select sc.name from sys_categories sc where sc.category_type = :authorType and sc.value = a.author_id) author_name                    
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("authorType", Constant.CATEGORY_TYPE.LIB_TAC_GIA);
        addCondition(sql, params, dto);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" ORDER BY relevance desc, a.title, a.created_time desc");
        } else {
            sql.append(" ORDER BY a.title, a.created_time desc");
        }
        return getListPagination(sql.toString(), params, dto, BooksResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(BooksRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.book_id,
                    a.title,
                    a.original_title,
                    a.subtitle,
                    a.genre_id,
                    a.author_id,
                    a.summary,
                    a.language_id,
                    a.type,
                    a.table_of_contents,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params,
                              BooksRequest.SearchForm dto) {
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" , MATCH(title, original_title, subtitle, summary, table_of_contents) AGAINST (:keySearch) relevance");
        }

        sql.append("""
                    FROM lib_books a
                    left join lib_genres lg on lg.genre_id = a.genre_id
                    left join sys_categories sc on sc.category_type = :authorType and a.author_id = sc.value
                    WHERE a.is_deleted = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("authorType", Constant.CATEGORY_TYPE.LIB_TAC_GIA);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            //xử lý tìm kiếm fulltext search
            sql.append(" AND MATCH(title, original_title, subtitle, summary, table_of_contents) AGAINST (:keySearch)");
            params.put("keySearch", dto.getKeySearch());
        }
        if (!Utils.isNullOrEmpty(dto.getAuthorIds())) {
            sql.append(" and a.author_id in (:authorIds)");
            params.put("authorIds", dto.getAuthorIds());
        }
        if (!Utils.isNullOrEmpty(dto.getTypes())) {
            sql.append(" and a.type in (:types)");
            params.put("types", dto.getTypes());
        }
        if (!Utils.isNullOrEmpty(dto.getTranslatorIds())) {
            sql.append(" and exists (" +
                    "   select 1 from lib_book_translators lt" +
                    "   where lt.book_id = a.book_id" +
                    "   and lt.translator_id in (:translatorIds)" +
                    ")");
            params.put("translatorIds", dto.getTranslatorIds());
        }
        if (!Utils.isNullOrEmpty(dto.getGenreIds())) {
            sql.append(" and exists (" +
                    "   select 1 from lib_genres lg1 " +
                    "   where lg1.genre_id in (:genreIds)" +
                    "   and lg.path_id like concat(lg1.path_id,'%')" +
                    ")");
            params.put("genreIds", dto.getGenreIds());
        }
        if(dto.isFavourite()){
            sql.append(" and exists (" +
                    "   select 1 from lib_favourite_books fb " +
                    "   where fb.is_deleted = 'N'" +
                    "   and fb.book_id = a.book_id" +
                    "   and fb.user_name = :userName" +
                    ")");
            params.put("userName", Utils.getUserNameLogin());
        }
    }

    public void inactiveEditionNotIn(Long bookId, List<Long> editionIds) {
        StringBuilder sql = new StringBuilder("""
                update lib_book_editions e
                set e.is_deleted = 'Y', e.modified_by = :userName, e.modified_time = now()
                where e.is_deleted = 'N'
                and e.book_id = :bookId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("bookId", bookId);
        params.put("userName", Utils.getUserNameLogin());
        if (!Utils.isNullOrEmpty(editionIds)) {
            sql.append(" and e.book_edition_id not in (:editionIds)");
            params.put("editionIds", editionIds);
        }
        executeSqlDatabase(sql.toString(), params);
    }

    public BaseDataTableDto searchAuthor(BooksRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ifnull(a.author_id,0) author_id,
                    ifnull((select sc.name from sys_categories sc where sc.category_type = :authorType and sc.value = a.author_id),'Chưa xác định') author_name,
                    count(*) as total                   
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("authorType", Constant.CATEGORY_TYPE.LIB_TAC_GIA);
        addCondition(sql, params, dto);
        sql.append(" group by a.author_id" +
                "   order by author_name");
        return getListPagination(sql.toString(), params, dto, BooksResponse.AuthorResult.class);
    }

    public BooksResponse.DetailResult findByBookId(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                	b.title,
                	b.original_title,
                	b.subtitle,
                	b.summary,
                	b.type,
                	b.tags,
                	b.author_id,
                	b.genre_id,
                	b.language_id,
                	b.table_of_contents,
                	sc.name as author_name,
                	sc1.name as language,
                	g.name as genre_name,
                	(CASE
                 		WHEN EXISTS (
                 		SELECT
                 			1
                 		FROM
                 			lib_favourite_books fb
                 		WHERE
                 			fb.book_id = b.book_id
                 			AND fb.is_deleted = 'N'
                 			AND fb.user_name = :userName
                         )
                        THEN 'Y'
                 		ELSE 'N'
                 	END) as isFavourite
                FROM
                	lib_books b
                LEFT JOIN sys_categories sc ON
                	(b.author_id = sc.value
                		AND sc.category_type = :authorType)
                LEFT JOIN sys_categories sc1 ON
                	(b.language_id = sc1.value
                		AND sc1.category_type = :languageType)
                LEFT JOIN lib_genres g ON
                	(b.genre_id = g.genre_id)
                WHERE
                	b.book_id = :id
                	AND b.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("authorType", Constant.CATEGORY_TYPE.LIB_TAC_GIA);
        params.put("languageType", Constant.CATEGORY_TYPE.LIB_NGON_NGU);
        params.put("userName", Utils.getUserNameLogin());
        return getFirstData(sql.toString(), params, BooksResponse.DetailResult.class);
    }

    public List<BooksResponse.Edition> getEditionByBookId(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                	lbe.book_edition_id,
                	lbe.published_year,
                	lbe.total_pages,
                	lbe.store_id,
                	lbe.publisher_id,
                	lbe.book_format_id,
                	sc.name as store_name,
                	sc1.name as publisher_name,
                	sc2.name as book_format_name,
                	(select count(1) from lib_book_edition_details a where a.book_edition_id = lbe.book_edition_id and a.status = 'HIEN_CO') available_number,
                    (select count(1) from lib_book_edition_details a where a.book_edition_id = lbe.book_edition_id and a.status = 'DANG_MUON') borrow_number
                FROM
                	lib_book_editions lbe
                LEFT JOIN sys_categories sc ON
                	(lbe.store_id = sc.value
                		AND sc.category_type = :storeType)
                LEFT JOIN sys_categories sc1 ON
                	(lbe.publisher_id = sc1.value
                		AND sc1.category_type = :publisherType)
                LEFT JOIN sys_categories sc2 ON
                	(lbe.book_format_id = sc2.value
                		AND sc2.category_type = :formatType)
                WHERE
                	lbe.book_id = :id
                	AND lbe.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("storeType", Constant.CATEGORY_TYPE.LIB_KHO_SACH);
        params.put("publisherType", Constant.CATEGORY_TYPE.LIB_NHA_XUAT_BAN);
        params.put("formatType", Constant.CATEGORY_TYPE.LIB_KICH_THUOC);
        return getListData(sql.toString(), params, BooksResponse.Edition.class);
    }

    public List<BooksResponse.TranslatorResult> getListTranslatorByBookId(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                	lbt.translator_id,
                	sc.name as translator_name
                FROM lib_book_translators lbt
                JOIN sys_categories sc ON
                	(lbt.translator_id = sc.value
                		AND sc.category_type = :translatorType)
                WHERE
                	lbt.book_id = :id
                	AND lbt.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("translatorType", Constant.CATEGORY_TYPE.LIB_DICH_GIA);
        return getListData(sql.toString(), params, BooksResponse.TranslatorResult.class);
    }


    public BaseDataTableDto searchGenre(BooksRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ifnull(a.genre_id,0) genre_id,
                    ifnull(lg.name,'Chưa xác định') as genre_name,
                    count(*) as total                   
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        sql.append(" group by a.genre_id" +
                "   order by genre_name");
        return getListPagination(sql.toString(), params, dto, BooksResponse.GenreResult.class);
    }

    public BaseDataTableDto searchTranslator(BooksRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                select 
                    lt.translator_id,
                    ifnull(sc.`name`,'Chưa xác định') as translator_name,
                    count(*) as total
                 from lib_books a
                    left join lib_book_translators lt on lt.book_id = a.book_id and lt.is_deleted = 'N'
                    left join sys_categories sc on sc.category_type = :translatorType and sc.`value` = lt.translator_id               
                    left join lib_genres lg on lg.genre_id = a.genre_id               
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("translatorType", Constant.CATEGORY_TYPE.LIB_DICH_GIA);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" , MATCH(title, original_title, subtitle, summary, table_of_contents) AGAINST (:keySearch) relevance");
        }
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            //xử lý tìm kiếm fulltext search
            sql.append(" AND MATCH(title, original_title, subtitle, summary, table_of_contents) AGAINST (:keySearch)");
            params.put("keySearch", dto.getKeySearch());
        }
        if (!Utils.isNullOrEmpty(dto.getAuthorIds())) {
            sql.append(" and a.author_id in (:authorIds)");
            params.put("authorIds", dto.getAuthorIds());
        }
        if (!Utils.isNullOrEmpty(dto.getTypes())) {
            sql.append(" and a.type in (:types)");
            params.put("types", dto.getTypes());
        }
        if (!Utils.isNullOrEmpty(dto.getTranslatorIds())) {
            sql.append(" and lt.translator_id in (:translatorIds)");
            params.put("translatorIds", dto.getTranslatorIds());
        }
        if (!Utils.isNullOrEmpty(dto.getGenreIds())) {
            sql.append(" and exists (" +
                    "   select 1 from lib_genres lg1 " +
                    "   where lg1.genre_id in (:genreIds)" +
                    "   and lg.path_id like concat(lg1.path_id,'%')" +
                    ")");
            params.put("genreIds", dto.getGenreIds());
        }
        if(dto.isFavourite()){
            sql.append(" and exists (" +
                    "   select 1 from lib_favourite_books fb " +
                    "   where fb.is_deleted = 'N'" +
                    "   and fb.book_id = a.book_id" +
                    "   and fb.user_name = :userName" +
                    ")");
            params.put("userName", Utils.getUserNameLogin());
        }
        sql.append(" group by lt.translator_id" +
                "   order by translator_name");
        return getListPagination(sql.toString(), params, dto, BooksResponse.TranslatorResult.class);
    }
}
