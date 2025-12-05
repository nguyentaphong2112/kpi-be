/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.insurance.models.response.ContractProcessResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.repositories.impl.ContractProcessRepository;
import vn.hbtplus.insurance.services.ContractProcessService;
import vn.hbtplus.utils.ResponseUtils;


/**
 * Lop impl service ung voi bang hr_contract_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ContractProcessServiceImpl implements ContractProcessService {

    private final ContractProcessRepository contractProcessRepository;

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<ContractProcessResponse> getDataByEmpId(Long empId) {
        return ResponseUtils.ok(contractProcessRepository.getDataByEmpId(empId));
    }

}
