/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.models.response.WorkProcessResponse;


/**
 * Lop interface service ung voi bang hr_work_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface WorkProcessService {

    ListResponseEntity<WorkProcessResponse> getDataByEmpId(Long empId);

}
