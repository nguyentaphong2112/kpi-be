/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.SalaryRanksRequest;
import vn.hbtplus.models.response.SalaryRanksResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ObjectRelationsEntity;
import vn.hbtplus.repositories.entity.SalaryRanksEntity;
import vn.hbtplus.utils.Utils;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_salary_ranks
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class SalaryRanksRepository extends BaseRepository {

    public BaseDataTableDto searchData(SalaryRanksRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.salary_rank_id,
                    a.code,
                    a.name,
                    a.salary_type,
                    a.order_number,
                    a.start_date,
                    a.end_date,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.salary_type AND sc.category_type = :categoryType) salaryTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("categoryType", Constant.CATEGORY_CODES.LOAI_NGACH_LUONG);
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, SalaryRanksResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(SalaryRanksRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.salary_rank_id,
                    a.code,
                    a.name,
                    a.salary_type,
                    a.order_number,
                    a.start_date,
                    a.end_date,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, SalaryRanksRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_salary_ranks a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().trim() + "%");
        }
        if (!Utils.isNullOrEmpty(dto.getSalaryType())) {
            sql.append(" AND (a.salary_type) like :salaryType");
            params.put("salaryType", "%" + dto.getSalaryType().trim() + "%");
        }
        sql.append(" ORDER BY a.order_number");
    }

    public void inactiveGradeNotIn(List<Long> salaryGradeIds, Long salaryRankId) {
        String sql = """
                update hr_salary_grades sg
                set sg.is_deleted = 'Y', sg.modified_time = now(),
                    sg.modified_by = :userName
                where sg.is_deleted = 'N'
                and sg.salary_grade_id not in (:salaryGradeIds)
                and sg.salary_rank_id = :salaryRankId 
                """;
        Map params = new HashMap();
        params.put("salaryRankId", salaryRankId);
        params.put("salaryGradeIds", salaryGradeIds.isEmpty() ? Utils.castToList(-1L) : salaryGradeIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public List<SalaryRanksResponse.SalaryGradeDto> getSalaryGrades(Long salaryRankId) {
        String sql = """
                select
                    sg.salary_grade_id,
                    sg.name gradeName,
                    concat(sg.name, ' (', FORMAT(sg.amount, 2), ')') name,
                    sg.salary_rank_id,
                    sg.duration,
                    sg.amount
                from hr_salary_grades sg
                where sg.salary_rank_id = :salaryRankId
                and sg.is_deleted = 'N'
                order by sg.amount
                """;
        Map params = new HashMap();
        params.put("salaryRankId", salaryRankId);
        return getListData(sql, params, SalaryRanksResponse.SalaryGradeDto.class);
    }

    public List<SalaryRanksResponse.SalaryJobDto> getSalaryJobs(Long salaryRankId) {
        String sql = """
                select jb.job_id,
                    jb.code as job_code,
                    jb.name as job_name 
                from hr_object_relations sg,
                    hr_jobs jb  
                where sg.object_id = :salaryRankId
                and jb.job_id = sg.refer_object_id
                and sg.table_name = :tableName
                and sg.refer_table_name = :referTableName
                and sg.function_code = :functionCode
                and sg.is_deleted = 'N'
                order by jb.order_number, jb.name
                """;
        Map params = new HashMap();
        params.put("salaryRankId", salaryRankId);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.SALARY_RANKS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.JOB);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.GAN_CHUC_DANH_HUONG_LUONG);
        return getListData(sql, params, SalaryRanksResponse.SalaryJobDto.class);
    }

    public List<SalaryRanksResponse> getSalaryRanks(String salaryType, Date startDate, Long empTypeId) {
        StringBuilder sql = new StringBuilder("""
                    select a.* from hr_salary_ranks a
                     where a.salary_type = :salaryType
                     and a.is_deleted = 'N'
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("salaryType", salaryType);
        if (startDate != null) {
            sql.append(" and a.start_date <= :startDate and (a.end_date is null or a.end_date >= :startDate)");
            params.put("startDate", startDate);
        }
        if (empTypeId != null && empTypeId > 0L) {
            sql.append("""
                        and exists (
                            select 1 from hr_object_attributes oa
                            where oa.is_deleted = 'N'
                            and oa.table_name = :tableName
                            and oa.attribute_code = :attributeCode
                            and oa.object_id = a.salary_rank_id
                            and concat(',', oa.attribute_value, ',') like :attributeValue
                        )
                    """);
            params.put("tableName", getSQLTableName(SalaryRanksEntity.class));
            params.put("attributeCode", Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DOI_TUONG);
            params.put("attributeValue", "%," + empTypeId + ",%");
        }
        sql.append(" order by a.order_number, a.name");
        return getListData(sql.toString(), params, SalaryRanksResponse.class);
    }

    public List<SalaryRanksResponse> getSalaryRanksByListType(List<String> listSalaryType) {
        String sql = """
                    select a.* from hr_salary_ranks a
                    where a.salary_type in (:listSalaryType)
                    and a.is_deleted = 'N'
                    order by a.name, a.order_number
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("listSalaryType", listSalaryType);
        return getListData(sql, params, SalaryRanksResponse.class);
    }

    public Map<String, SalaryRanksResponse> getMapSalaryRanks(String salaryType, Long empTypeId, List<String> salaryRankNames) {
        Map<String, SalaryRanksResponse> mapSalaryRank = new HashMap<>();
        if (Utils.isNullOrEmpty(salaryRankNames)) {
            return mapSalaryRank;
        }
        StringBuilder sql = new StringBuilder("""
                    select a.* from hr_salary_ranks a
                     where a.salary_type = :salaryType
                     and a.is_deleted = 'N' and name in (:salaryRankNames)
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("salaryType", salaryType);
        params.put("salaryRankNames", salaryRankNames);
        if (empTypeId != null && empTypeId > 0L) {
            sql.append("""
                        and exists (
                            select 1 from hr_object_attributes oa
                            where oa.is_deleted = 'N'
                            and oa.table_name = :tableName
                            and oa.object_id = a.salary_rank_id
                            and oa.attribute_code = :attributeCode
                            and oa.attribute_value = :attributeValue
                        )
                    """);
            params.put("tableName", getSQLTableName(SalaryRanksEntity.class));
            params.put("attributeCode", Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DOI_TUONG);
            params.put("attributeValue", empTypeId);
        }
        sql.append(" order by a.order_number, a.name");

        List<SalaryRanksResponse> salaryRanksResponseList = getListData(sql.toString(), params, SalaryRanksResponse.class);
        salaryRanksResponseList.forEach(item -> {
            mapSalaryRank.put(StringUtils.lowerCase(item.getName()), item);
        });

        return mapSalaryRank;
    }

    public Map<String, Map<String, SalaryRanksResponse.SalaryGradeDto>> getMapSalaryGradeByRanks(List<String> salaryRankList) {
        Map<String, Map<String, SalaryRanksResponse.SalaryGradeDto>> mapSalaryRankGrade = new HashMap<>();
        if (Utils.isNullOrEmpty(salaryRankList)) {
            return mapSalaryRankGrade;
        }
        String sql = """
                select sg.*, sr.name salary_rank_name
                from hr_salary_grades sg
                    inner join hr_salary_ranks sr on sg.salary_rank_id = sr.salary_rank_id
                where sr.name in (:salaryRankList) and ifnull(sg.is_deleted, :isDeleted) = :isDeleted
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("salaryRankList", salaryRankList);
        List<SalaryRanksResponse.SalaryGradeDto> salaryGradeList = getListData(sql, params, SalaryRanksResponse.SalaryGradeDto.class);

        salaryGradeList.forEach(item -> {
            Map<String, SalaryRanksResponse.SalaryGradeDto> mapSalaryGrade;
            String key = item.getSalaryRankName();
            key = StringUtils.trimToEmpty(key).toLowerCase();
            if (mapSalaryRankGrade.get(key) == null) {
                mapSalaryGrade = new HashMap<>();
            } else {
                mapSalaryGrade = mapSalaryRankGrade.get(key);
            }
            mapSalaryGrade.put(StringUtils.lowerCase(item.getName()), item);
            mapSalaryRankGrade.put(key, mapSalaryGrade);
        });

        return mapSalaryRankGrade;
    }

    public List<SalaryRanksResponse.SalaryGradeDto> getSalaryGradeByRankCode(List<String> salaryRankCodes, String salaryType) {
        String sql = """
                select sg.salary_rank_id, sg.salary_grade_id,
                    sg.name as gradeName,
                    sg.amount, sr.code as salaryRankCode, sr.name salaryRankName
                    from hr_salary_grades sg, hr_salary_ranks sr
                    where sg.salary_rank_id = sr.salary_rank_id
                    and sg.is_deleted = 'N'
                    and sr.is_deleted = 'N'
                    and UPPER(sr.code) in (:salaryRankCodes)
                    and sr.salary_type = :salaryType
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("salaryRankCodes", salaryRankCodes);
        params.put("salaryType", salaryType);
        return getListData(sql, params, SalaryRanksResponse.SalaryGradeDto.class);
    }
}
