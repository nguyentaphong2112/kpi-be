/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.insurance.models.response.InsuranceSalaryProcessResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.repositories.impl.InsuranceSalaryProcessRepository;
import vn.hbtplus.insurance.services.InsuranceSalaryProcessService;
import vn.hbtplus.utils.ResponseUtils;


/**
 * Lop impl service ung voi bang hr_insurance_salary_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class InsuranceSalaryProcessServiceImpl implements InsuranceSalaryProcessService {

    private final InsuranceSalaryProcessRepository insuranceSalaryProcessRepository;

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<InsuranceSalaryProcessResponse> getDataByEmpId(Long empId) {
        return ResponseUtils.ok(insuranceSalaryProcessRepository.getDataByEmpId(empId));
    }

}
