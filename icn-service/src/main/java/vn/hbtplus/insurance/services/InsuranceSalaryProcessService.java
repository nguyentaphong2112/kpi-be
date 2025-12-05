/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import vn.hbtplus.insurance.models.response.InsuranceSalaryProcessResponse;
import vn.hbtplus.models.response.ListResponseEntity;


/**
 * Lop interface service ung voi bang hr_insurance_salary_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface InsuranceSalaryProcessService {

    ListResponseEntity<InsuranceSalaryProcessResponse> getDataByEmpId(Long empId);

}
