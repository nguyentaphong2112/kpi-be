/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.insurance.models.response.DependentPersonsResponse;
import vn.hbtplus.insurance.repositories.impl.DependentPersonsRepository;
import vn.hbtplus.insurance.services.DependentPersonsService;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.utils.ResponseUtils;


/**
 * Lop impl service ung voi bang hr_dependent_persons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class DependentPersonsServiceImpl implements DependentPersonsService {

    private final DependentPersonsRepository dependentPersonsRepository;

    @Override
    @Transactional(readOnly = true)
    public ListResponseEntity<DependentPersonsResponse> getDataByEmpId(Long empId) {
        return ResponseUtils.ok(dependentPersonsRepository.getDataByEmpId(empId));
    }

}
