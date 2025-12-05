/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import vn.hbtplus.insurance.models.response.DependentPersonsResponse;
import vn.hbtplus.models.response.ListResponseEntity;


/**
 * Lop interface service ung voi bang hr_dependent_persons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface DependentPersonsService {

    ListResponseEntity<DependentPersonsResponse> getDataByEmpId(Long empId);
}
