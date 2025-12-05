package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.kpi.models.dto.EmpStatisticBean;
import vn.kpi.models.dto.OrganizationDto;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class REmployeeStatisticRepository extends BaseRepository {
    public List<OrganizationDto> getListOrgExport(EmployeesRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                select a.* from hr_organizations a
                where a.is_deleted = 'N'                
                """);

        Map map = new HashMap<>();
        if(dto.getOrganizationId() != null && dto.getOrganizationId() > 0){
            sql.append(" and a.path_id like :orgPath");
            map.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }
        sql.append(" order by a.path_order");
        return getListData(sql.toString(), map, OrganizationDto.class);
    }

    public Map<String, Integer> getStatisticByGender(List<Long> orgIds, EmployeesRequest.ReportForm dto) {
        String sql = """
                select 
                	a.organization_id,
                	e.gender_id as type,
                	count(*) as total
                from hr_employees e, hr_work_process wp, hr_organizations org, hr_organizations a
                where wp.employee_id = e.employee_id
                and wp.organization_id = org.organization_id
                and a.organization_id in (:orgIds)
                and org.path_id like CONCAT(a.path_id,'%')
                and wp.is_deleted = 'N'
                and e.is_deleted = 'N'
                and :endDate BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.document_type_id in (
                	select document_type_id from hr_document_types dt
                	where dt.type <> 'OUT'
                )
                group by a.organization_id, e.gender_id
                """;
        return getStatisticData(orgIds, dto, sql);
    }
    public Map<String, Integer> getStatisticByPartyMember(List<Long> orgIds, EmployeesRequest.ReportForm dto) {
        String sql = """
                select 
                	a.organization_id,
                	case when e.party_date is not null then 'X' end as type,
                	count(*) as total
                from hr_employees e, hr_work_process wp, hr_organizations org, hr_organizations a
                where wp.employee_id = e.employee_id
                and wp.organization_id = org.organization_id
                and a.organization_id in (:orgIds)
                and org.path_id like CONCAT(a.path_id,'%')
                and wp.is_deleted = 'N'
                and e.is_deleted = 'N'
                and :endDate BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.document_type_id in (
                	select document_type_id from hr_document_types dt
                	where dt.type <> 'OUT'
                )
                group by a.organization_id, type
                """;
        return getStatisticData(orgIds, dto, sql);
    }

    public Map<String, Integer> getStatisticByEmpType(List<Long> orgIds, EmployeesRequest.ReportForm dto) {
        String sql = """
                select 
                	a.organization_id,
                	cp.emp_type_id as type,
                	count(*) as total
                from hr_employees e, hr_work_process wp, hr_contract_process cp, hr_organizations org, hr_organizations a
                where wp.employee_id = e.employee_id
                and cp.employee_id = e.employee_id
                and cp.classify_code = 'HOP_DONG'
                and wp.organization_id = org.organization_id
                and a.organization_id in (:orgIds)
                and org.path_id like CONCAT(a.path_id,'%')
                and wp.is_deleted = 'N'
                and e.is_deleted = 'N'
                and cp.is_deleted = 'N'
                and :endDate BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.document_type_id in (
                	select document_type_id from hr_document_types dt
                	where dt.type <> 'OUT'
                )
                and cp.start_date <= :endDate
                and not exists (
                	select 1 from hr_contract_process cp1
                	where cp1.is_deleted = 'N'
                	and cp1.employee_id = e.employee_id
                	and cp1.classify_code = 'HOP_DONG'
                	and (cp1.start_date > cp.start_date
                		or (cp1.start_date = cp.start_date and  cp1.contract_process_id > cp1.contract_process_id)
                	)		
                )
                group by a.organization_id, cp.emp_type_id
                """;
        return getStatisticData(orgIds, dto, sql);
    }
    public Map<String, Integer> getStatisticByMajorLevel(List<Long> orgIds, EmployeesRequest.ReportForm dto) {
        String sql = """
                select 
                	a.organization_id,
                	cp.major_level_id as type,
                	count(*) as total
                from hr_employees e, hr_work_process wp, hr_education_degrees cp, hr_organizations org, hr_organizations a
                where wp.employee_id = e.employee_id
                and cp.employee_id = e.employee_id
                and cp.is_highest = 'Y'
                and wp.organization_id = org.organization_id
                and a.organization_id in (:orgIds)
                and org.path_id like CONCAT(a.path_id,'%')
                and wp.is_deleted = 'N'
                and e.is_deleted = 'N'
                and cp.is_deleted = 'N'
                and :endDate BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.document_type_id in (
                	select document_type_id from hr_document_types dt
                	where dt.type <> 'OUT'
                )
                group by a.organization_id, cp.major_level_id
                """;
        return getStatisticData(orgIds, dto, sql);
    }
    public Map<String, Integer> getStatisticByEduPromotion(List<Long> orgIds, EmployeesRequest.ReportForm dto) {
        String sql = """
                select 
                	a.organization_id,
                	ep.promotion_rank_id as type,
                	count(*) as total
                from hr_employees e, hr_work_process wp, hr_education_promotions ep, hr_organizations org, hr_organizations a
                ,sys_categories scp 
                where wp.employee_id = e.employee_id
                and ep.employee_id = e.employee_id
                and wp.organization_id = org.organization_id
                and ep.promotion_rank_id = scp.value 
                and scp.category_type = 'HR_HOC_HAM'
                and a.organization_id in (:orgIds)
                and org.path_id like CONCAT(a.path_id,'%')
                and wp.is_deleted = 'N'
                and e.is_deleted = 'N'
                and ep.is_deleted = 'N'
                and :endDate BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.document_type_id in (
                	select document_type_id from hr_document_types dt
                	where dt.type <> 'OUT'
                )
                and not exists (
                	select 1 from hr_education_promotions ep1, sys_categories scp1
                	where ep1.employee_id = wp.employee_id
                	and ep1.is_deleted = 'N'
                	and ep1.promotion_rank_id = scp1.value
                	and scp1.category_type = 'HR_HOC_HAM'
                	and (scp1.order_number < scp.order_number
                		or (scp1.order_number = scp.order_number and ep1.education_promotion_id > ep.education_promotion_id)
                	)
                )
                group by a.organization_id, ep.promotion_rank_id
                """;
        return getStatisticData(orgIds, dto, sql);
    }

    public Map<String, Integer> getStatisticByPosition(List<Long> orgIds, EmployeesRequest.ReportForm dto) {
        String sql = """
                select 
                	a.organization_id,
                	(
                	select at.attribute_value from hr_object_attributes at
                        where at.is_deleted = 'N'
                        and at.object_id = wp.job_id
                        and at.table_name = 'hr_jobs'
                        and at.attribute_code = 'PHAN_NHOM_VI_TRI'
                	) as type,
                	count(*) as total
                from hr_employees e, hr_work_process wp, hr_organizations org, hr_organizations a
                where wp.employee_id = e.employee_id
                and wp.organization_id = org.organization_id
                and a.organization_id in (:orgIds)
                and org.path_id like CONCAT(a.path_id,'%')
                and wp.is_deleted = 'N'
                and e.is_deleted = 'N'
                and :endDate BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.document_type_id in (
                	select document_type_id from hr_document_types dt
                	where dt.type <> 'OUT'
                )
                group by a.organization_id, type
                """;
        return getStatisticData(orgIds, dto, sql);
    }
    public Map<String, Integer> getStatisticByPositionRank(List<Long> orgIds, EmployeesRequest.ReportForm dto) {
        String sql = """
                select 
                	a.organization_id,
                	(
                	select at.attribute_value from hr_object_attributes at
                        where at.is_deleted = 'N'
                        and at.object_id = wp.job_id
                        and at.attribute_code = 'HANG_CHUC_DANH'
                	) as type,
                	count(*) as total
                from hr_employees e, hr_work_process wp, hr_organizations org, hr_organizations a
                where wp.employee_id = e.employee_id
                and wp.organization_id = org.organization_id
                and a.organization_id in (:orgIds)
                and org.path_id like CONCAT(a.path_id,'%')
                and wp.is_deleted = 'N'
                and e.is_deleted = 'N'
                and :endDate BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.document_type_id in (
                	select document_type_id from hr_document_types dt
                	where dt.type <> 'OUT'
                )
                group by a.organization_id, type
                """;
        return getStatisticData(orgIds, dto, sql);
    }

    private Map<String, Integer> getStatisticData(List<Long> orgIds, EmployeesRequest.ReportForm dto, String sql) {
        Map<String, Object> params = getMapReportParameter(dto);
        params.put("orgIds", orgIds);
        List<EmpStatisticBean> empList = getListData(sql, params, EmpStatisticBean.class);
        Map<String, Integer> mapResults = new HashMap<>();
        empList.forEach(item -> {
            mapResults.put(item.getOrganizationId() + "-" + item.getType(), item.getTotal());
        });
        return mapResults;
    }


    private Map<String, Object> getMapReportParameter(EmployeesRequest.ReportForm dto) {
        Map<String, Object> params = new HashMap<>();
        if (EmployeesRequest.PERIOD_TYPES.MONTH.equalsIgnoreCase(dto.getTypeReportPeriod())) {
            params.put("startDate", Utils.getFirstDay(Utils.stringToDate(dto.getMonth(), "MM/yyyy")));
            params.put("endDate", Utils.getLastDay(Utils.stringToDate(dto.getMonth(), "MM/yyyy")));
        } else {
            params.put("startDate", dto.getStartDate());
            params.put("endDate", dto.getEndDate());
        }
        params.put("organizationId", dto.getOrganizationId());
        params.put("empTypeIds", dto.getEmpTypeIds());
        return params;
    }



}
