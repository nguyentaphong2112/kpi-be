/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.models.response.WorkProcessResponse;
import vn.hbtplus.insurance.repositories.impl.WorkProcessRepository;
import vn.hbtplus.insurance.services.WorkProcessService;
import vn.hbtplus.utils.ResponseUtils;


/**
 * Lop impl service ung voi bang hr_work_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class WorkProcessServiceImpl implements WorkProcessService {

    private final WorkProcessRepository workProcessRepository;

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<WorkProcessResponse> getDataByEmpId(Long empId) {
        return ResponseUtils.ok(workProcessRepository.getDataByEmpId(empId));
    }

}
