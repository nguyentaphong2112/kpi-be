/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CardTemplatesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_card_templates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CardTemplatesService {

    TableResponseEntity<CardTemplatesResponse.SearchResult> searchData(CardTemplatesRequest.SearchForm dto);

    ResponseEntity saveData(CardTemplatesRequest.SubmitForm dto, List<MultipartFile> files, Long cardTemplateId) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<CardTemplatesResponse.DetailBean> getDataById(Long id, String loginName) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(CardTemplatesRequest.SearchForm dto) throws Exception;

    BaseResponseEntity<CardTemplatesResponse.DetailBean> getCardTemplateByType(String templateType, String loginName);

    ResponseEntity getFileTemplateById(Long attachmentId) throws Exception;

    List<CardTemplatesResponse.DetailBean> getListTitleCardTemplate(String templateType);

    ResponseEntity deleteFileTemplate(Long attachmentId);

    ResponseEntity getFileByListAttachment(List<Long> listAttachmentId) throws Exception;
}
