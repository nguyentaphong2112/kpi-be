package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RequestsApproversRepository extends BaseRepository {

    public Long getEmployeeIdByEmpCode(String employeeCode) {
        String sql = "SELECT he.employee_id FROM hr_employees he" +
                " WHERE he.is_deleted = :isDeleted " +
                "AND employee_code = :employeeCode";
        Map map = new HashMap<>();
        map.put("isDeleted" , BaseConstants.STATUS.NOT_DELETED);
        map.put("employeeCode" , employeeCode);
        return queryForObject(sql, map, Long.class);
    }
}
