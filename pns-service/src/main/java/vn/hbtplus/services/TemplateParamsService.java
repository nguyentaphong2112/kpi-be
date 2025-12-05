/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import vn.hbtplus.models.dto.TemplateParamsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.repositories.entity.TemplateParamsEntity;

/**
 * Lop interface service ung voi bang PNS_TEMPLATE_PARAMS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TemplateParamsService {

    ListResponseEntity<TemplateParamsEntity> searchData(TemplateParamsDTO dto);

    BaseResponseEntity<Object> saveData(TemplateParamsDTO dto);

    BaseResponseEntity<Object> deleteData(Long id);

    BaseResponseEntity<Object> getDataById(Long id);

}
