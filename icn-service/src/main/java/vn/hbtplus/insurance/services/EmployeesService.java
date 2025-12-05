/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import vn.hbtplus.insurance.models.response.EmployeesResponse;
import vn.hbtplus.models.response.ListResponseEntity;


/**
 * Lop interface service ung voi bang hr_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EmployeesService {

    ListResponseEntity<EmployeesResponse> getDataByEmpId(Long empId);

}
