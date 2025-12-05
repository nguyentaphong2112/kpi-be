/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.MentoringTraineesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.MentoringTraineesResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang lms_mentoring_trainees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface MentoringTraineesService {

    TableResponseEntity<MentoringTraineesResponse.SearchResult> searchData(MentoringTraineesRequest.SearchForm dto);

    ResponseEntity saveData(MentoringTraineesRequest.SubmitForm dto, List<MultipartFile> files, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<MentoringTraineesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(MentoringTraineesRequest.SearchForm dto) throws Exception;

    String getTemplateIndicator() throws Exception;

    boolean importData(MultipartFile fileImport) throws Exception;
}
