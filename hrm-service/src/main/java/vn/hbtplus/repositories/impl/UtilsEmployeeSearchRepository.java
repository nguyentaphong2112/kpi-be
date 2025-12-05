package vn.hbtplus.repositories.impl;

import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UtilsEmployeeSearchRepository {
    public static void setParamEmployeeSearch(EmployeesRequest.SearchForm dto, StringBuilder sql, Map<String, Object> params) {
        QueryUtils.filter(dto.getListEmpTypeId(), sql, params, "e.emp_type_id");
        QueryUtils.filter(dto.getListStatus(), sql, params, "e.status");
        QueryUtils.filter(dto.getEmployeeId(), sql, params, "e.employee_id");
        QueryUtils.filter(dto.getListReligion(), sql, params, "e.religion_id");
        QueryUtils.filter(dto.getListEthnic(), sql, params, "e.ethnic_id");
        QueryUtils.filter(dto.getMobileNumber(), sql, params, "e.mobile_number");
        QueryUtils.filter(dto.getTaxNo(), sql, params, "e.tax_no");
        QueryUtils.filter(dto.getInsuranceNo(), sql, params, "e.insurance_no");
        QueryUtils.filter(dto.getInsuranceNo(), sql, params, "e.insurance_no");
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" and (e.employee_code like :keySearch or upper(e.full_name) like :keySearch or upper(e.email) like :keySearch");
            sql.append("""
                        or exists (
                            select 1 from hr_personal_identities pi
                            where pi.employee_id = e.employee_id 
                            and identity_no like :keySearch 
                            and is_deleted = :activeStatus)
                        )
                    """);
            params.put("keySearch", "%" + dto.getKeySearch().trim().toUpperCase() + "%");
        }

        if (!Utils.isNullOrEmpty(dto.getAges())) {
            List<String> ageConditions = dto.getAges().stream()
                    .map(age -> Arrays.stream(age.split("và"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(ageStr -> "TIMESTAMPDIFF(YEAR, e.date_of_birth, CURDATE()) " + ageStr)
                            .collect(Collectors.joining(" AND ", "(", ")"))
                    )
                    .filter(condition -> !condition.equals("()"))
                    .toList();

            if (!ageConditions.isEmpty()) {
                sql.append(" AND (");
                sql.append(String.join(" OR ", ageConditions));
                sql.append(") ");
            }
        }

        if (!Utils.isNullOrEmpty(dto.getMajorLevelList()) || !Utils.isNullOrEmpty(dto.getMajorName())) {
            sql.append("""
                    
                    and EXISTS(
                        SELECT 1 from hr_education_degrees ed
                        where ed.employee_id = e.employee_id
                        and ed.is_deleted = 'N'
                    
                    """);
            QueryUtils.filter(dto.getMajorLevelList(), sql, params, "ed.major_level_id");
            QueryUtils.filter(dto.getMajorName(), sql, params, "ed.major_name");
            sql.append(" ) ");
        }

        if (!Utils.isNullOrEmpty(dto.getListCertificateType())) {
            sql.append("""
                    
                    and EXISTS(
                        SELECT 1 from hr_education_certificates ee
                        where ee.employee_id = e.employee_id
                        and ee.is_deleted = 'N'
                    
                    """);
            QueryUtils.filter(dto.getListCertificateType(), sql, params, "ee.certificate_type_id");
            sql.append(" ) ");
        }


        if (!Utils.isNullOrEmpty(dto.getListDateOfBirth())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListDateOfBirth().get(0)), sql, params, "e.date_of_birth", "fromDateOfBirth");
            QueryUtils.filterLe(dto.getListDateOfBirth().size() > 1 ? Utils.stringToDate(dto.getListDateOfBirth().get(1)) : null, sql, params, "e.date_of_birth", "toDateOfBirth");
        }

        if (!Utils.isNullObject(dto.getOrganizationId())) {
            sql.append(" AND o.path_id LIKE :orgId ");
            params.put("orgId", "%/" + dto.getOrganizationId() + "/%");
        }
        if (!Utils.isNullOrEmpty(dto.getListPositionId())) {
            sql.append("""
                        AND (
                            e.job_id IN (:listJobId)
                            OR EXISTS (
                                select 1 from hr_concurrent_process cp
                                where cp.employee_id = e.employee_id
                                and cp.start_date <= :sysDate
                                and (cp.end_date >= :sysDate OR cp.end_date IS NULL)
                                and nvl(cp.is_deleted, :activeStatus) = :activeStatus
                                and cp.job_id IN (:listJobId)
                            )
                        )
                    """);
            params.put("sysDate", Utils.truncDate(new Date()));
            params.put("listJobId", dto.getListPositionId());
        }
    }
}
