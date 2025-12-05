package vn.hbtplus.tax.income.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.EmployeeDto;
import vn.hbtplus.tax.income.models.response.EmployeesResponse;
import vn.hbtplus.repositories.BaseRepository;
//import vt.vcc.constant.BaseConstants;
//import vt.vcc.insurance.constant.Constants;
//import vt.vcc.insurance.dto.EmployeeDto;
//import vt.vcc.insurance.dto.response.EmployeesResponse;
//import vt.vcc.insurance.repository.entity.*;
//import vt.vcc.repository.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class EmployeeRepository extends BaseRepository {
    @Autowired
    ConfigParameterRepository configParameterRepository;

    public List<EmployeeDto> getEmployeeDtos(List<String> empCodes) throws ExecutionException, InterruptedException {
        String sql = "select e.employee_code, e.full_name, e.employee_id, f_get_seniority(e.employee_id, now()) seniority from hr_employees e" +
                " where e.is_deleted = 'N'" +
                "   and e.employee_code in (:empCodes)";

        return getListData(sql, new HashMap<>(), EmployeeDto.class, "empCodes", empCodes);
    }

    public List<EmployeesResponse> getDataByEmpId(Long empId) {
        String sql = """
            SELECT
                a.employee_code,
                a.full_name,
                a.date_of_birth,
                a.email,
                a.mobile_number,
                a.personal_id_no,
                a.personal_id_place,
                a.personal_id_date,
                a.current_address,
                a.permanent_address,
                a.status,
                o.full_name orgName,
                (select j.job_name from hr_jobs j where j.job_id = a.job_id) jobName,
                (select s.name from sys_categories s where s.value = a.gender and s.category_type = :categoryType) genderName,
                a.tax_number
            FROM hr_employees a
            JOIN hr_organizations o ON o.org_id = a.org_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
            AND a.employee_id = :empId
        """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("categoryType", Constant.CATEGORY_TYPE.GIOI_TINH);
        params.put("empId", empId);
        return getListData(sql, params, EmployeesResponse.class);
    }

//    public Map<Long, HrEmployeesEntity> getEmpByListId(List<Long> listEmpId){
//        List<HrEmployeesEntity> listEmp = findByListId(HrEmployeesEntity.class, listEmpId);
//        Map<Long, HrEmployeesEntity> mapResult = new HashMap<>();
//        for (HrEmployeesEntity entity : listEmp){
//            mapResult.put(entity.getEmployeeId(), entity);
//        }
//        return mapResult;
//    }
//
//    public Map<Long, HrWorkProcessEntity> getWpByListId(List<Long> listEmpId){
//        List<HrWorkProcessEntity> listEmp = findByListId(HrWorkProcessEntity.class, listEmpId);
//        Map<Long, HrWorkProcessEntity> mapResult = new HashMap<>();
//        for (HrWorkProcessEntity entity : listEmp){
//            mapResult.put(entity.getWorkProcessId(), entity);
//        }
//        return mapResult;
//    }
//
//    public Map<Long, HrInsuranceSalaryProcessEntity> getInsuranceSalaryByListId(List<Long> ids){
//        List<HrInsuranceSalaryProcessEntity> listData = findByListId(HrInsuranceSalaryProcessEntity.class, ids);
//        Map<Long, HrInsuranceSalaryProcessEntity> mapResult = new HashMap<>();
//        for (HrInsuranceSalaryProcessEntity entity : listData){
//            mapResult.put(entity.getInsuranceSalaryProcessId(), entity);
//        }
//        return mapResult;
//    }
//
//    public Map<Long, HrEmpTypeProcessEntity> getEmpTypeByListId(List<Long> ids){
//        List<HrEmpTypeProcessEntity> listData = findByListId(HrEmpTypeProcessEntity.class, ids);
//        Map<Long, HrEmpTypeProcessEntity> mapResult = new HashMap<>();
//        for (HrEmpTypeProcessEntity entity : listData){
//            mapResult.put(entity.getEmpTypeProcessId(), entity);
//        }
//        return mapResult;
//    }
//
//    public Map<Long, HrAllowanceProcessEntity> getAllowanceByListId(List<Long> ids){
//        List<HrAllowanceProcessEntity> listData = findByListId(HrAllowanceProcessEntity.class, ids);
//        Map<Long, HrAllowanceProcessEntity> mapResult = new HashMap<>();
//        for (HrAllowanceProcessEntity entity : listData){
//            mapResult.put(entity.getAllowanceProcessId(), entity);
//        }
//        return mapResult;
//    }
//
//    public Map<Long, AbsTimekeepingsEntity> getTimekeepingsByListId(List<Long> ids){
//        List<AbsTimekeepingsEntity> listData = findByListId(AbsTimekeepingsEntity.class, ids);
//        Map<Long, AbsTimekeepingsEntity> mapResult = new HashMap<>();
//        for (AbsTimekeepingsEntity entity : listData){
//            mapResult.put(entity.getTimekeepingId(), entity);
//        }
//        return mapResult;
//    }

//    public Stream<EmployeeDto> getAllEmployee() {
//        String sql = """
//                 select employee_id, employee_code, full_name
//                 , full_name as full_name1
//                 , full_name as full_name2
//                 , full_name as full_name3
//                 , full_name as full_name4
//                 , full_name as full_name5
//                 , full_name as full_name6
//                 , full_name as full_name7
//                 , full_name as full_name8
//                 , full_name as full_name9
//                 , full_name as full_name10
//                 , full_name as full_name11
//                 , full_name as full_name12
//                 , full_name as full_name13
//                 , full_name as full_name14
//                 , full_name as full_name15
//                 , full_name as full_name16
//                 , full_name as full_name17
//                 , full_name as full_name18
//                 , full_name as full_name19
//                 from hr_employees
//                 order by employee_code
//                """;
//        return getDataToStream(sql, new HashMap<>(), EmployeeDto.class);
//    }
//
//    public List<Map<String, Object>> exportAllEmp() {
//        String sql = """
//                 select employee_id, employee_code, full_name
//                 , full_name as full_name1
//                 , full_name as full_name2
//                 , full_name as full_name3
//                 , full_name as full_name4
//                 , full_name as full_name5
//                 , full_name as full_name6
//                 , full_name as full_name7
//                 , full_name as full_name8
//                 , full_name as full_name9
//                 , full_name as full_name10
//                 , full_name as full_name11
//                 , full_name as full_name12
//                 , full_name as full_name13
//                 , full_name as full_name14
//                 , full_name as full_name15
//                 , full_name as full_name16
//                 , full_name as full_name17
//                 , full_name as full_name18
//                 , full_name as full_name19
//                 from hr_employees
//                 order by employee_code
//                """;
//        return getListData(sql, new HashMap<>());
//    }
}
