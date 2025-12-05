/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.insurance.models.response.EmployeesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.repositories.impl.EmployeeRepository;
import vn.hbtplus.insurance.services.EmployeesService;
import vn.hbtplus.utils.ResponseUtils;


/**
 * Lop impl service ung voi bang hr_employees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EmployeesServiceImpl implements EmployeesService {

    private final EmployeeRepository employeesRepository;

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<EmployeesResponse> getDataByEmpId(Long empId) {
        return ResponseUtils.ok(employeesRepository.getDataByEmpId(empId));
    }

}
