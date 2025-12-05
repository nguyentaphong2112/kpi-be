package vn.hbtplus.tax.income.repositories.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.models.EmployeeDto;
import vn.hbtplus.tax.income.models.request.TaxCommitmentRequest;
import vn.hbtplus.tax.income.models.response.TaxCommitmentResponse;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.constraints.NotNull;
import java.util.*;

@Repository
public class TaxCommitmentRepository extends BaseRepository {
    public BaseDataTableDto<TaxCommitmentResponse> searchData(TaxCommitmentRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                select tc.tax_commitment_id,
                       tc.employee_id,
                       tc.income_amount,
                       tc.start_date,
                       tc.end_date,
                       tc.created_by,
                       tc.created_time,
                       tc.modified_by,
                       tc.modified_time,
                       tc.last_update_time,
                       tc.description,
                       e.employee_code,
                       e.full_name,
                       org.full_name org_name
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, TaxCommitmentResponse.class);
    }

    public List<Map<String, Object>> getDataExportByCondition(TaxCommitmentRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                select tc.income_amount,
                       DATE_FORMAT(tc.start_date, '%m/%Y') start_date,
                       DATE_FORMAT(tc.end_date, '%m/%Y') end_date,
                       tc.created_by,
                       tc.created_time,
                       tc.modified_by,
                       tc.modified_time,
                       tc.last_update_time,
                       tc.description,
                       e.employee_code,
                       e.full_name,
                       org.full_name org_name
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        List<Map<String, Object>>  resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;
    }
    private void addCondition(StringBuilder sql, Map<String, Object> params, TaxCommitmentRequest.SearchForm dto) {
        sql.append("""
                    from pit_tax_commitments tc
                        inner join hr_employees e on tc.employee_id = e.employee_id
                        inner join hr_organizations org on e.organization_id = org.organization_id
                    where ifnull(tc.is_deleted, :isDeleted) = :isDeleted
                """);
        if (!Utils.isNullObject(dto.getOrgId())) {
            sql.append(" AND org.path_id LIKE :orgId ");
            params.put("orgId", "%/" + dto.getOrgId() + "/%");
        }
        if (StringUtils.isNotBlank(dto.getKeySearch())) {
            sql.append(" AND (lower(e.employee_code) like :keySearch  or lower(e.full_name) like :keySearch OR lower(e.email) like :keySearch)");
            params.put("keySearch", "%" + StringUtils.lowerCase(dto.getKeySearch()).trim() + "%");
        }
        QueryUtils.filterConflictDate(Utils.getFirstDay(dto.getStartDate()), Utils.getFirstDay(dto.getEndDate()), sql, params,
                "tc.start_date", "tc.end_date", "startDate", "endDate");

//        QueryUtils.filterGe(Utils.getFirstDay(dto.getStartDate()), sql, params, "tc.end_date", "startDate");
//        QueryUtils.filterLe(Utils.getLastDay(dto.getEndDate()), sql, params, "tc.start_date", "endDate");

        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        sql.append(" ORDER BY COALESCE(tc.modified_time, tc.created_time) DESC, org.path_order, e.employee_code");
    }

    public List<TaxCommitmentResponse> getEmployeeConflictDate(TaxCommitmentRequest.UpdateForm form) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                select * from pit_tax_commitments where 1 = 1 and ifnull(is_deleted, :isDeleted) = :isDeleted
                """);
        Map<String, Object> params = new HashMap<>();
        QueryUtils.filterConflictDate(Utils.getFirstDay(form.getStartDate()), Utils.getFirstDay(form.getEndDate()), sql, params,
                "start_date", "end_date", "startDate", "endDate");
        QueryUtils.filterNotEq(form.getTaxCommitmentId(), sql, params, "tax_commitment_id");
        QueryUtils.filter(form.getEmployeeId(), sql, params, "employee_id");
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, TaxCommitmentResponse.class);
    }

    public Map<String, List<TaxCommitmentResponse>> getTaxCommitmentByEmpCodes(List<String> empCodes) {
        Map<String, List<TaxCommitmentResponse>> mapResult = new HashMap<>();
        if (Utils.isNullOrEmpty(empCodes)) {
            return mapResult;
        }
        String sql = """
                SELECT
                    e.employee_code,
                    e.full_name,
                    tax.tax_commitment_id,
                    tax.start_date,
                    tax.end_date
                FROM
                    pit_tax_commitments tax
                    INNER JOIN hr_employees e ON tax.employee_id = e.employee_id
                WHERE
                    ifnull(tax.is_deleted, :isDeleted) = :isDeleted
                    AND e.employee_code IN (:empCodes) 
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodes", empCodes);

        List<TaxCommitmentResponse> responseList = getListData(sql, params, TaxCommitmentResponse.class);
        responseList.forEach(item -> {
            List<TaxCommitmentResponse> taxCommitmentList = mapResult.get(StringUtils.lowerCase(item.getEmployeeCode()));
            if (taxCommitmentList == null) {
                taxCommitmentList = new ArrayList<>();
            }

            taxCommitmentList.add(item);
            mapResult.put(StringUtils.lowerCase(item.getEmployeeCode()), taxCommitmentList);
        });
        return mapResult;
    }

    public List<EmployeeDto> getListSeniority(List<Long> empIds) {
        String sql = """
                select e.employee_id, f_get_seniority(e.employee_id, now()) seniority
                from hr_employees e
                where e.employee_id in (:empIds)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empIds", empIds);
        return getListData(sql, params, EmployeeDto.class);
    }
}
