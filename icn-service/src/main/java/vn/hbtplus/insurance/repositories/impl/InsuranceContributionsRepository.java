/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.*;
import vn.hbtplus.insurance.models.ContributionParameterDto;
import vn.hbtplus.insurance.models.InsuranceContributionsDto;
import vn.hbtplus.insurance.models.TimekeepingDto;
import vn.hbtplus.insurance.models.WorkProcessDto;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.jpa.ContributionRateRepositoryJPA;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Lop repository Impl ung voi bang icn_insurance_contributions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class InsuranceContributionsRepository extends BaseRepository {
    private final ContributionRateRepositoryJPA contributionRateRepositoryJPA;
    private final ConfigParameterRepository configParameterRepository;
    private final MdcForkJoinPool mdcForkJoinPool;

    public BaseDataTableDto searchData(InsuranceContributionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.insurance_contribution_id,
                    a.period_date,
                    e.employee_id,
                    e.employee_code,
                    e.full_name,
                    mj.name jobName,
                    o.full_name org_name,
                    a.contract_salary,
                    a.reserve_salary,
                    a.pos_allowance_salary,
                    a.seniority_salary,
                    a.pos_seniority_salary,
                    a.total_salary,
                    a.per_social_amount,
                    a.unit_social_amount,
                    a.per_medical_amount,
                    a.unit_medical_amount,
                    a.per_unemp_amount,
                    a.unit_unemp_amount,
                    a.unit_union_amount,
                    a.base_union_amount,
                    a.superior_union_amount,
                    a.mod_union_amount,
                    a.total_amount,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.status,
                    a.type,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) empTypeName,
                	(select s.name from sys_categories s where s.value = a.status and s.category_type = :statusType) statusName,
                	(select s.name from sys_categories s where s.value = a.type and s.category_type = :typeCateType) typeName,
                	(select s.name from sys_categories s where s.value = a.insurance_agency and s.category_type = :insuranceAgencyCode) insuranceAgency,
                	(select s.name from sys_categories s where s.value = a.labour_type and s.category_type = :labourTypeCode) labour_type,
                    a.reason,
                    a.note
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("statusType", Constant.CATEGORY_TYPE.TRANG_THAI_THU_BHXH);
        params.put("typeCateType", Constant.CATEGORY_TYPE.LOAI_DS_TRICH_NOP);
        params.put("insuranceAgencyCode", Constant.CATEGORY_TYPE.NOI_TGIA_BHXH);
        params.put("labourTypeCode", Constant.CATEGORY_TYPE.PHAN_LOAI_LAO_DONG);
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, InsuranceContributionsResponse.class);
    }

    public List<Map<String, Object>> getListExport(InsuranceContributionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                	e.employee_code,
                	e.full_name,
                	mj.name as job_name,
                	o.org_name_level_1 don_vi,
                	o.org_name_level_2 phong_ban,
                	o.org_name_level_3 to_nhom,
                	a.reason ly_do,
                	a.insurance_factor as he_so_luong,
                	a.allowance_factor as he_so_pccv,
                	a.reserve_factor as he_so_clbl,
                	a.seniority_percent as tham_nien_vuot_khung,
                	a.pos_seniority_percent as tham_nien_nghe,
                	ifnull(a.insurance_timekeeping,0) + ifnull(a.leave_timekeeping,0) as cong_che_do,
                	a.insurance_timekeeping as cong_trich_nop,
                	a.maternity_timekeeping as cong_thai_san,
                	a.contract_salary,
                	a.reserve_salary,
                	a.pos_allowance_salary,
                	a.seniority_salary,
                	a.pos_seniority_salary,
                	a.total_salary,
                	a.per_social_amount,
                	a.unit_social_amount,
                	a.per_medical_amount,
                	a.unit_medical_amount,
                	a.per_unemp_amount,
                	a.unit_unemp_amount,
                	a.unit_union_amount,
                	a.base_union_amount,
                	a.superior_union_amount,
                	a.mod_union_amount,
                	a.total_amount,
                	(select et.name from hr_emp_types et where et.code = a.emp_type_code) emp_type_code,
                	(select s.name from sys_categories s where s.value = a.labour_type and s.category_type = :labourTypeCode) loai_lao_dong,
                	(select s.name from sys_categories s where s.value = a.status and s.category_type = :statusType) status,
                	(select s.name from sys_categories s where s.value = a.type and s.category_type = :typeCateType) type,
                	a.reason,
                	a.note
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("statusType", Constant.CATEGORY_TYPE.TRANG_THAI_THU_BHXH);
        params.put("typeCateType", Constant.CATEGORY_TYPE.LOAI_DS_TRICH_NOP);
        params.put("labourTypeCode", Constant.CATEGORY_TYPE.PHAN_LOAI_LAO_DONG);
        addCondition(sql, params, dto);
        List result = getListData(sql.toString(), params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return result;
    }

    public List<InsuranceContributionsEntity> getListDataByForm(InsuranceContributionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("SELECT a.*");
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params, InsuranceContributionsEntity.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, InsuranceContributionsRequest.SearchForm dto) {
        sql.append("""
                    FROM icn_insurance_contributions a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.org_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        QueryUtils.filterOrg(dto.getOrgId(), sql, params, "o.path_id");
        if (dto.getPeriodDate() != null) {
            sql.append(" and a.period_date = :periodDate");
            params.put("periodDate", Utils.getLastDay(dto.getPeriodDate()));
        }
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name", "e.email");
        QueryUtils.filterEq(dto.getEmpTypeCode(), sql, params, "a.emp_type_code");
        QueryUtils.filter(dto.getType(), sql, params, "a.type");
        if (!Utils.isNullOrEmpty(dto.getListType())) {
            sql.append(" and a.type in (:listType)");
            params.put("listType", dto.getListType());
        }
        if (!Utils.isNullOrEmpty(dto.getListEmpTypeCode())) {
            sql.append(" and a.emp_type_code in (:empTypeCodes)");
            params.put("empTypeCodes", dto.getListEmpTypeCode());
        }
        QueryUtils.filter(dto.getStatus(), sql, params, "a.status");
        QueryUtils.filter(dto.getListStatus(), sql, params, "a.status");

        params.put("activeStatus", Constant.STATUS.NOT_DELETED);
        sql.append(" ORDER BY o.path_order, e.employee_code, a.retro_for_period_date");
    }

    public List<InsuranceContributionsDto> getListToCalculate(List<String> empCodes, Date periodDate, ContributionParameterDto parameterDto, boolean isRetro) throws ExecutionException, InterruptedException {
        String sql = """
                select T.employee_id, max(T.endDate) as endDate, min(T.startDate) as startDate
                from
                	(select wp.employee_id,
                		DATE(LEAST(IFNULL(wp.end_date,:endDate),IFNULL(etp.end_date,:endDate),:endDate)) endDate,
                		DATE(greatest(wp.start_date,etp.start_date,:startDate)) as startDate
                		from hr_work_process wp, hr_contract_process etp, hr_organizations org, hr_employees e, hr_emp_types ets
                		where wp.employee_id = etp.employee_id
                		and wp.start_date <= IFNULL(etp.end_date,:endDate)
                		and etp.start_date <= IFNULL(wp.end_date,:endDate)
                		and wp.start_date <= :endDate
                		and IFNULL(wp.end_date,:endDate) >= :startDate
                		and etp.start_date <= :endDate
                		and IFNULL(etp.end_date,:endDate) >= :startDate
                		and ets.emp_type_id = etp.emp_type_id
                        {conditionEmpType}
                		and wp.document_type_id in (
                			select document_type_id from hr_document_types dt
                			where dt.type <> 'OUT'
                		)
                		and wp.employee_id = e.employee_id
                		and org.path_id like :orgVcc
                		and wp.organization_id = org.organization_id
                		{conditionEmp}
                	) T
                """;
        if (!isRetro) {
            sql += "    where T.employee_id not in (" +
                    "                    select employee_id from icn_insurance_contributions a" +
                    "                    where a.period_date = :endDate" +
                    "                    and a.status in (:statusApproved)" +
                    "                )";
        }
        sql += "group by T.employee_id";

        //lay danh sach dien doi tuong co cau hinh duoc tinh trich nop bao hiem
        List<String> empTypeCodes = contributionRateRepositoryJPA.getListEmTypeCodes(periodDate);
        Map mapParams = new HashMap();
        if (Utils.isNullOrEmpty(empTypeCodes)) {
            sql = sql.replace("{conditionEmpType}", "");
        } else {
            sql = sql.replace("{conditionEmpType}", " and ets.code in (:empTypeCodes) ");
            mapParams.put("empTypeCodes", empTypeCodes);
        }
        if (empCodes == null || empCodes.isEmpty()) {
            sql = sql.replace("{conditionEmp}", "");
        } else {
            sql = sql.replace("{conditionEmp}", " and e.employee_code in (:empCodes)");
            mapParams.put("empCodes", empCodes);
        }
        mapParams.put("empTypeCodes", empTypeCodes);
        mapParams.put("statusApproved", Arrays.asList(new String[]{InsuranceContributionsEntity.STATUS.PHE_DUYET}));
        mapParams.put("orgVcc", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");
        mapParams.put("startDate", Utils.getFirstDay(periodDate));
        mapParams.put("endDate", Utils.getLastDay(periodDate));
        List<WorkProcessDto> listEmps = getListData(sql, mapParams, WorkProcessDto.class);
        Map<Date, List<Long>> mapEndDates = new HashMap<>();
        List<Long> empIds = new ArrayList<>();
        Map<Long, WorkProcessDto> mapWorkProcess = new HashMap<>();
        listEmps.stream().forEach(item -> {
            if (mapEndDates.get(item.getEndDate()) == null) {
                mapEndDates.put(item.getEndDate(), new ArrayList<>());
            }
            mapEndDates.get(item.getEndDate()).add(item.getEmployeeId());
            empIds.add(item.getEmployeeId());
            mapWorkProcess.put(item.getEmployeeId(), item);
        });

        List<InsuranceContributionsDto> listInsuranceEmps = new ArrayList<>();
        mapEndDates.entrySet().stream().forEach(entry -> {
            try {
                listInsuranceEmps.addAll(getInsuranceEmps(entry.getKey(), entry.getValue(), parameterDto));
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        //Lay thong tin cham cong
        List<TimekeepingDto> listTimekeepings = getTimekeepings(empIds, periodDate, empTypeCodes, parameterDto);
        Map<Long, List<TimekeepingDto>> mapTimekeepings = new HashMap<>();
        listTimekeepings.forEach(item ->
                mapTimekeepings.computeIfAbsent(item.getEmployeeId(), k -> new ArrayList<>())
                        .add(item)
        );


        listInsuranceEmps.stream().forEach(item -> {
            item.setTimekeeping(mapTimekeepings.get(item.getEmployeeId()), parameterDto);
            item.setStartDate(mapWorkProcess.get(item.getEmployeeId()).getStartDate());
            item.setEndDate(mapWorkProcess.get(item.getEmployeeId()).getEndDate());
        });

        return listInsuranceEmps;
    }

    private List<TimekeepingDto> getTimekeepings(List<Long> empIds,
                                                 Date periodDate,
                                                 List<String> empTypeCodes,
                                                 ContributionParameterDto parameterDto) throws ExecutionException, InterruptedException {
        String sql = """
                select et.employee_id, et.workday_type_id, sum(total_hours)/8 as numOfDays
                from abs_timekeepings et, (
                	select etp.employee_id, 
                	    greatest(etp.start_date, wp.start_date) as start_date, 
                	    least(IFNULL(etp.end_date,:endDate),IFNULL(wp.end_date,:endDate))  end_date
                	from hr_contract_process etp, hr_work_process wp, hr_organizations org, hr_emp_types ets
                	where etp.employee_id in (:empIds)
                	and etp.start_date <= :endDate
                	and IFNULL(etp.end_date,:endDate) >= :startDate
                	and wp.start_date <= :endDate
                	and IFNULL(wp.end_date,:endDate) >= :startDate
                	and ets.emp_type_id = etp.emp_type_id
                    and ets.code in (:empTypeCodes)
                	and etp.is_deleted = 'N'
                	and wp.employee_id = etp.employee_id
                	and wp.start_date <= IFNULL(etp.end_date,:endDate)
                	and etp.start_date <= IFNULL(wp.end_date,:endDate)
                	and wp.organization_id = org.organization_id
                	and org.path_id like :orgVCC
                ) T
                where et.date_timekeeping between :startDate and :endDate
                and et.employee_id in (:empIds)
                and et.employee_id = T.employee_id
                and et.is_deleted = 'N'
                and et.workday_type_id in (:workdayTypeIds)
                and et.date_timekeeping BETWEEN T.start_date and T.end_date
                group by et.employee_id,et.workday_type_id
                """;
        List<List<Long>> partitions = Utils.partition(empIds, Constant.BATCH_SIZE);
        List<Long> workdayTypeIds = new ArrayList<>();
        workdayTypeIds.addAll(parameterDto.getIdCongThaiSans());
        workdayTypeIds.addAll(parameterDto.getIdCongTrichNops());
        List<TimekeepingDto> result = new ArrayList<>();

        List<CompletableFuture<List<Map<String, Object>>>> completableFutures = new ArrayList<>();
        for(List<Long> partition : partitions){
            Map mapParams = new HashMap();
            mapParams.put("startDate", Utils.getFirstDay(periodDate));
            mapParams.put("endDate", Utils.getLastDay(periodDate));
            mapParams.put("empIds", partition);
            mapParams.put("empTypeCodes", empTypeCodes);
            mapParams.put("workdayTypeIds", workdayTypeIds);
            mapParams.put("orgVCC", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");
            //Lay danh sach thieu thong tin qua trinh dien doi tuong
            Supplier<List<Map<String, Object>>> getDatas = () -> getListData(sql, mapParams, TimekeepingDto.class);
            completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        }

        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<Object> objs = allFutures.get();
        for (Object items : objs) {
            result.addAll((List) items);
        }
        return result;
    }

    private List<InsuranceContributionsDto> getInsuranceEmps(Date date, List<Long> empIds, ContributionParameterDto parameterDto) throws ExecutionException, InterruptedException {
        String sql = """
                    select 
                    		wp.employee_id,
                    		'1' as insuranceRegion,
                    		wp.job_id,
                    		wp.organization_id org_id,
                    		ets.code emp_type_code,
                    		jb.name as job_name,
                    		isp.insurance_factor,
                    		isp.insurance_base_salary,
                    		isp.reserve_factor,
                    		isp.seniority_percent,
                    		(
                    		    select max(factor) from hr_allowance_process ap
                    		    where ap.is_deleted = 'N'
                    		    and ap.employee_id = wp.employee_id
                    		    and ap.start_date <= :endDate
                    		    and ifnull(ap.end_date, :endDate) >= :startDate
                    		    and ap.allowance_type_id = :pccv
                    		) as allowance_factor
                      from hr_work_process wp, hr_jobs jb, hr_organizations org, 
                        hr_contract_process etp,
                    	hr_insurance_salary_process isp, hr_emp_types ets
                    where :date BETWEEN wp.start_date and ifnull(wp.end_date, :date)
                    and :date BETWEEN etp.start_date and ifnull(etp.end_date, :date)
                    and :date BETWEEN isp.start_date and ifnull(isp.end_date, :date)
                    and wp.employee_id = etp.employee_id
                    and wp.employee_id = isp.employee_id
                    and wp.organization_id = org.organization_id
                    and wp.job_id = jb.job_id
                    and ets.emp_type_id = etp.emp_type_id
                    and wp.is_deleted = 'N'
                    and etp.is_deleted = 'N'
                    and isp.is_deleted = 'N'
                    and not exists (
                        select 1 from hr_work_process wp1
                        where wp1.employee_id = wp.employee_id
                        and wp1.is_deleted = 'N'
                        and :date BETWEEN wp1.start_date and ifnull(wp1.end_date, :date)
                        and (wp1.start_date > wp.start_date 
                            or (wp1.start_date = wp.start_date and wp1.work_process_id > wp.work_process_id)
                        )
                    )
                    and not exists (
                        select 1 from hr_contract_process wp1
                        where wp1.employee_id = etp.employee_id
                        and wp1.is_deleted = 'N'
                        and :date BETWEEN wp1.start_date and ifnull(wp1.end_date, :date)
                        and (wp1.start_date > etp.start_date 
                            or (wp1.start_date = etp.start_date and wp1.contract_process_id > etp.contract_process_id)
                        )
                    )
                    and not exists (
                        select 1 from hr_insurance_salary_process wp1
                        where wp1.employee_id = isp.employee_id
                        and wp1.is_deleted = 'N'
                        and :date BETWEEN wp1.start_date and ifnull(wp1.end_date, :date)
                        and (wp1.start_date > isp.start_date 
                            or (wp1.start_date = isp.start_date and wp1.insurance_salary_process_id > isp.insurance_salary_process_id)
                        )
                    )
                    and wp.employee_id in (:empIds)
                """;


        List<CompletableFuture<List<Map<String, Object>>>> completableFutures = new ArrayList<>();
        List<List<Long>> partitions = Utils.partition(empIds, Constant.BATCH_SIZE);
        List result = new ArrayList();
        for(List<Long> partition : partitions){
            Map mapParams = new HashMap();
            mapParams.put("date", date);
            mapParams.put("startDate", Utils.getFirstDay(date));
            mapParams.put("endDate", Utils.getLastDay(date));
            mapParams.put("pccv", parameterDto.getAllowanceTypeCode());
            mapParams.put("empIds", partition);
            //Lay danh sach thieu thong tin qua trinh dien doi tuong
            Supplier<List<Map<String, Object>>> getDatas = () -> getListData(sql, mapParams, InsuranceContributionsDto.class);
            completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        }

        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<Object> objs = allFutures.get();
        for (Object items : objs) {
            result.addAll((List) items);
        }
        return result;
    }

    public void saveAll(List<InsuranceContributionsDto> contributionsDtos, Date periodDate) throws BaseAppException {
        List<InsuranceContributionsEntity> insuranceContributionsEntities = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        Set<String> positionErrors = new HashSet<>();
        contributionsDtos.stream().forEach(item -> {
            InsuranceContributionsEntity entity = new InsuranceContributionsEntity();
            Utils.copyProperties(item, entity);
            entity.setStatus(InsuranceContributionsEntity.STATUS.DU_THAO);
            entity.setCreatedTime(new Date());
            entity.setPeriodDate(periodDate);
            entity.setCreatedBy(userName);
            insuranceContributionsEntities.add(entity);
            if (Utils.isNullOrEmpty(item.getLabourType())) {
                positionErrors.add(item.getJobName());
            }
        });
//        if (!positionErrors.isEmpty()) {
//            throw new BaseAppException(String.format("Tồn tại chức danh chưa có cấu hình phân loại đối tượng lao động : %s", positionErrors.toString()));
//        }
        insertBatch(InsuranceContributionsEntity.class, insuranceContributionsEntities, userName);
    }

    public void deleteOldData(Date periodDate, List<String> empCodes) {
        //xoa cac ban ghi chua phe duyet
        String sql = """
                 DELETE a from icn_insurance_contributions a
                     where a.period_date = :periodDate
                     and a.status not in (:statusApproved)
                """;
        Map mapParams = new HashMap();
        mapParams.put("periodDate", periodDate);
        mapParams.put("statusApproved", Arrays.asList(new String[]{InsuranceContributionsEntity.STATUS.PHE_DUYET}));

        if (empCodes != null && !empCodes.isEmpty()) {
            sql += " and a.employee_id in (" +
                    "   select employee_id from hr_employees e " +
                    "   where e.employee_code in (:empCodes)" +
                    ") ";
            mapParams.put("empCodes", empCodes);
        }
        executeSqlDatabase(sql, mapParams);

        //reset thong tin bang truy thu/truy linh
        String sqlUpdate = """
                 UPDATE icn_insurance_retractions a
                     set a.retro_period_date  = null,
                         a.insurance_contribution_id = null
                     where a.retro_period_date = :periodDate
                """;
        if (empCodes != null && !empCodes.isEmpty()) {
            sqlUpdate += " and a.employee_id in (" +
                    "   select employee_id from hr_employees e " +
                    "   where e.employee_code in (:empCodes)" +
                    ") ";
            mapParams.put("empCodes", empCodes);
        }
        executeSqlDatabase(sqlUpdate, mapParams);
    }

    public InsuranceContributionsEntity getInsuranceContribution(Date periodDate, Long employeeId) {
        String sql = "select a.* from icn_insurance_contributions a" +
                " where a.period_date = :periodDate" +
                " and a.employee_id = :employeeId" +
                " and a.type in (:type)" +
                " and a.is_deleted = 'N'";
        Map mapParams = new HashMap();
        mapParams.put("periodDate", periodDate);
        mapParams.put("employeeId", employeeId);
        mapParams.put("type", Arrays.asList(new String[]{InsuranceContributionsEntity.TYPES.THU_BHXH, InsuranceContributionsEntity.TYPES.THAI_SAN, InsuranceContributionsEntity.TYPES.KO_THU}));
        return getFirstData(sql, mapParams, InsuranceContributionsEntity.class);
    }

    public void updateStatus(List<Long> ids, String status) {
        if (Utils.isNullOrEmpty(ids)) {
            return;
        }
        String sql = "update icn_insurance_contributions a" +
                " set a.status = :status," +
                "   a.modified_by = :userName," +
                "   a.modified_time = :currentDate" +
                " where a.insurance_contribution_id in (:ids)";
        final String userName = Utils.getUserNameLogin();
        final Date currentDate = new Date();
        Map map = new HashMap();
        map.put("userName", userName);
        map.put("status", status);
        map.put("currentDate", currentDate);
        map.put("ids", ids);
        executeSqlDatabase(sql, map);
    }

    public List<InsuranceContributionsEntity> getLastContributions(List<String> empCodes, Date periodDate) {
        String sql = """
                select * from icn_insurance_contributions ic
                where ic.period_date <= :endDate
                and ic.is_deleted = 'N'
                and ic.employee_id in (
                	select employee_id from hr_employees where employee_code in (:empCodes)
                )
                and ic.type in (:types)
                and not exists (
                	select 1 from icn_insurance_contributions ic1
                	where ic1.period_date <= :endDate
                	and ic1.is_deleted = 'N'
                	and ic1.employee_id = ic.employee_id
                	and ic1.type in (:types)
                	and ic1.period_date > ic.period_date
                )
                """;
        Map map = new HashMap();
        map.put("empCodes", empCodes);
        map.put("endDate", periodDate);
        map.put("status", InsuranceContributionsEntity.STATUS.PHE_DUYET);
        map.put("types", Arrays.asList(InsuranceContributionsEntity.TYPES.THU_BHXH,
                InsuranceContributionsEntity.TYPES.KO_THU,
                InsuranceContributionsEntity.TYPES.THAI_SAN));
        return getListData(sql, map, InsuranceContributionsEntity.class);
    }

    public String isMedicalPayed(Date periodDate, Long employeeId) {
        String sql = """
                select DATE_FORMAT(a.period_date, '%m/%Y') as period_date 
                from icn_insurance_contributions a
                where a.employee_id = :employeeId
                and IFNULL(a.per_medical_amount,0) + IFNULL(a.unit_medical_amount,0) > 0
                and a.type in (:types)
                and (a.period_date = :periodDate or a.retro_for_period_date = :periodDate)
                and a.is_deleted = 'N'
                """;
        Map map = new HashMap();
        map.put("employeeId", employeeId);
        map.put("periodDate", periodDate);
        map.put("types", Arrays.asList(InsuranceContributionsEntity.TYPES.THU_BHXH,
                InsuranceContributionsEntity.TYPES.THAI_SAN,
                InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        return getFirstData(sql, map, String.class);
    }

    public List<InsuranceContributionsDto> getListEmpMedicalPayed(Date periodDate, List<String> employeeCodes) {
        String sql = """
                select a.*  
                from icn_insurance_contributions a, hr_employees e 
                where e.employee_code IN (:employeeCodes)
                and IFNULL(a.per_medical_amount,0) + IFNULL(a.unit_medical_amount,0) > 0
                and (
                    (a.period_date = :periodDate and a.type in (:typeTrongThang)) 
                    or (a.retro_for_period_date = :periodDate and a.type in (:truyThu))
                )
                and a.is_deleted = 'N'
                and e.employee_id = a.employee_id
                """;
        Map map = new HashMap();
        map.put("employeeCodes", employeeCodes);
        map.put("periodDate", periodDate);
        map.put("typeTrongThang", Arrays.asList(InsuranceContributionsEntity.TYPES.THU_BHXH,
                InsuranceContributionsEntity.TYPES.THAI_SAN));
        map.put("truyThu", Arrays.asList(InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        return getListData(sql, map, InsuranceContributionsDto.class);
    }

    public List<Long> getWorkdayTypeIds(List<String> codes) {
        String sql = """
                select workday_type_id 
                from abs_workday_types a 
                where a.code in (:codes)
                and a.is_deleted = 'N'  
                """;
        Map map = new HashMap();
        map.put("codes", codes);
        return getListData(sql, map, Long.class);
    }

//    public void updateDebitOrgId(Date periodDate) {
//        String sql = """
//                update icn_insurance_contributions icn
//                set icn.debit_org_id = f_get_org_before_support_foreign(icn.employee_id, :periodDate)
//                where icn.period_date = :periodDate
//                and icn.org_id in (
//                	select org_id from hr_organizations org
//                	where (org.path_id  like '%/9004673/%'
//                		or org.path_id  like '%/9004674/%'
//                		or org.path_id  like '%/9004559/%'
//                	)
//                )
//                """;
//        Map map = new HashMap();
//        map.put("periodDate", periodDate);
//        executeSqlDatabase(sql, map);
//    }

    public String getWorkdayLeaves(Long employeeId, Date periodDate, Double congCheDo) {
        String sql = "select wt.name as workdayTypeName, sum(et.total_hours/8) as numOfDays" +
                "   from abs_timekeepings et,abs_workday_types wt  " +
                " where et.date_timekeeping between :startDate and :endDate " +
                "   and et.workday_type_id = wt.workday_type_id" +
                "   and et.employee_id = :employeeId" +
                "   and wt.type = 'LEAVE'" +
                "   group by wt.name";
        Map map = new HashMap();
        map.put("startDate", Utils.getFirstDay(periodDate));
        map.put("endDate", Utils.getLastDay(periodDate));
        map.put("employeeId", employeeId);
        List<TimekeepingDto> mapValues = getListData(sql, map, TimekeepingDto.class);
        String result = "";
        if (mapValues.isEmpty()) {
            return null;
        } else {
            for (TimekeepingDto item : mapValues) {
                result = String.join(", ", result,
                        String.format("%s - %s/%s ngày", item.getWorkdayTypeName(),
                                Utils.formatNumber(item.getNumOfDays()), Utils.formatNumber(congCheDo)));

            }
            return result.substring(2);
        }
    }

    public Map<Long, String> getWorkdayLeaves(List<Long> employeeIds, Date periodDate, Double congCheDo) {
        String sql = "select et.employee_id, wt.name as workdayTypeName, sum(et.total_hours/8) as numOfDays" +
                "   from abs_timekeepings et,abs_workday_types wt  " +
                " where et.date_timekeeping between :startDate and :endDate " +
                "   and et.workday_type_id = wt.workday_type_id" +
                "   and et.employee_id in (:employeeIds)" +
                "   and wt.type = 'LEAVE'" +
                "   group by wt.name, et.employee_id";
        Map map = new HashMap();
        map.put("startDate", Utils.getFirstDay(periodDate));
        map.put("endDate", Utils.getLastDay(periodDate));
        map.put("employeeIds", employeeIds);
        List<TimekeepingDto> mapValues = getListData(sql, map, TimekeepingDto.class);
        Map mapResults = new HashMap();

        for (TimekeepingDto item : mapValues) {
            if (mapResults.get(item.getEmployeeId()) == null) {
                mapResults.put(item.getEmployeeId(), String.format("%s - %s/%s ngày", item.getWorkdayTypeName(),
                        Utils.formatNumber(item.getNumOfDays()), Utils.formatNumber(congCheDo)));
            } else {
                mapResults.put(item.getEmployeeId(), mapResults.get(item.getEmployeeId()) + ", " + String.format("%s - %s/%s ngày", item.getWorkdayTypeName(),
                        Utils.formatNumber(item.getNumOfDays()), Utils.formatNumber(congCheDo)));
            }
        }
        return mapResults;
    }

    public void autoRetroactive(Date periodDate, List<String> empCodes) {
        StringBuilder sql = new StringBuilder("""
                insert into icn_insurance_contributions(
                	insurance_contribution_id,period_date, employee_id, emp_type_code, labour_type, job_id, org_id,
                	contract_salary, reserve_salary, pos_allowance_salary, seniority_salary, pos_seniority_salary, 
                	total_salary, per_social_amount, unit_social_amount, per_medical_amount, unit_medical_amount,
                	per_unemp_amount, unit_unemp_amount, unit_union_amount, base_union_amount, superior_union_amount,
                	mod_union_amount, total_amount, retirement_social_amount, sickness_social_amount, accident_social_amount,
                	is_deleted, insurance_factor, insurance_base_salary, reserve_factor, allowance_factor, seniority_percent,
                	pos_seniority_percent, insurance_timekeeping, leave_timekeeping, maternity_timekeeping, insurance_agency, 
                	note, type, retro_for_period_date, insurance_retraction_id, reason, status, created_by, created_time, debit_org_id
                )
                select null,
                :periodDate period_date, a.employee_id, a.emp_type_code, a.labour_type, 
                IFNULL(current.job_id, a.job_id) job_id,
                IFNULL(current.org_id, a.org_id) org_id, a.contract_salary, a.reserve_salary, a.pos_allowance_salary, 
                a.seniority_salary, a.pos_seniority_salary, a.total_salary, a.per_social_amount, a.unit_social_amount, 
                a.per_medical_amount, a.unit_medical_amount, a.per_unemp_amount, a.unit_unemp_amount, a.unit_union_amount, 
                a.base_union_amount, a.superior_union_amount, a.mod_union_amount, a.total_amount, a.retirement_social_amount,
                a.sickness_social_amount, a.accident_social_amount, a.is_deleted, a.insurance_factor, 
                a.insurance_base_salary, a.reserve_factor, a.allowance_factor, a.seniority_percent, a.pos_seniority_percent, a.insurance_timekeeping, 
                a.leave_timekeeping, a.maternity_timekeeping, a.insurance_agency, a.note, a.type ,
                a.period_date retro_for_period_date, a.insurance_retraction_id,
                case 
                	when a.type = 'TRUY_THU' then CONCAT('Truy thu kỳ ', DATE_FORMAT(a.period_date,'%m/%Y'))
                	else CONCAT('Truy lĩnh kỳ ', DATE_FORMAT(a.period_date,'%m/%Y'))
                end reason,
                'DU_THAO' status,
                :userName,
                now() as created_time, a.org_id
                from icn_insurance_retractions a 
                LEFT JOIN icn_insurance_contributions current 
                    on current.period_date = :periodDate 
                	and current.employee_id = a.employee_id 
                	and current.type in (:typeTrongThang) 
                	and current.is_deleted = 'N'
                where a.period_date < :periodDate
                and a.is_deleted = 'N'
                and a.retro_period_date is null
                and a.table_type = 'CHENH_LECH'
                """);
        Map mapParams = new HashMap();
        mapParams.put("periodDate", periodDate);
        mapParams.put("typeTrongThang", Arrays.asList(InsuranceContributionsEntity.TYPES.THU_BHXH, InsuranceContributionsEntity.TYPES.KO_THU, InsuranceContributionsEntity.TYPES.THAI_SAN));
        mapParams.put("userName", Utils.getUserNameLogin());
        if (empCodes != null && !empCodes.isEmpty()) {
            sql.append(" and a.employee_id in (" +
                    "  select employee_id from hr_employees where employee_code in (:empCodes)" +
                    ")");
            mapParams.put("empCodes", empCodes);
        }
        executeSqlDatabase(sql.toString(), mapParams);


        //update viec da thuc hien truy thu/truy linh
        StringBuilder sqlUpdate = new StringBuilder("""
                update icn_insurance_retractions a, icn_insurance_contributions b  
                set a.insurance_contribution_id = b.insurance_contribution_id,
                    a.retro_period_date = :periodDate              
                where a.period_date < :periodDate
                and b.period_date = :periodDate
                and a.is_deleted = 'N'
                and a.insurance_retraction_id = b.insurance_retraction_id
                and a.type = b.type
                and a.retro_period_date is null
                """);
        if (empCodes != null && !empCodes.isEmpty()) {
            sqlUpdate.append(" and a.employee_id in (" +
                    "  select employee_id from hr_employees where employee_code in (:empCodes)" +
                    ")");
            mapParams.put("empCodes", empCodes);
        }
        executeSqlDatabase(sqlUpdate.toString(), mapParams);
    }

    public Date getPrePeriodNotApproved(Date periodDate) {
        String sql = """
                select ic.period_date from icn_insurance_contributions ic
                where ic.is_deleted = 'N'
                and ic.period_date < :periodDate
                and ic.status not in (:approvedStatus)
                order by ic.period_date desc limit 1
                """;
        Map map = new HashMap();
        map.put("periodDate", periodDate);
        map.put("approvedStatus", InsuranceContributionsEntity.STATUS.PHE_DUYET);
        InsuranceContributionsDto result = getFirstData(sql, map, InsuranceContributionsDto.class);
        if (result == null) {
            return null;
        }
        return result.getPeriodDate();
    }
}
