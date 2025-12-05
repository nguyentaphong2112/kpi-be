/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import vn.hbtplus.tax.personal.models.request.LockRegistrationsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.tax.personal.models.response.LockRegistrationsResponse;

/**
 * Lop interface service ung voi bang PTX_LOCK_REGISTRATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface LockRegistrationsService {

    ListResponseEntity<LockRegistrationsResponse> searchData(LockRegistrationsDTO dto);

    ResponseEntity<Object> saveData(LockRegistrationsDTO dto, List<MultipartFile> files);

    ResponseEntity<Object> deleteData(Long id);

    BaseResponseEntity<LockRegistrationsResponse> getDataById(Long id);

    boolean isValidPeriod(Date dateRemind);
}
