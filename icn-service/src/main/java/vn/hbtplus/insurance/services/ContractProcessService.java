/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import vn.hbtplus.insurance.models.response.ContractProcessResponse;
import vn.hbtplus.models.response.ListResponseEntity;


/**
 * Lop interface service ung voi bang hr_contract_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractProcessService {

    ListResponseEntity<ContractProcessResponse> getDataByEmpId(Long empId);

}
