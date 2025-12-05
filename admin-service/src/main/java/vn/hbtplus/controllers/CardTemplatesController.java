/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.CardTemplatesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.CardTemplatesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.CardTemplatesService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constant.RESOURCES.SYS_CARD_TEMPLATES)
public class CardTemplatesController {
    private final CardTemplatesService cardTemplatesService;

    @GetMapping(value = "/v1/card-templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CardTemplatesResponse.SearchResult> searchData(CardTemplatesRequest.SearchForm dto) {
        return cardTemplatesService.searchData(dto);
    }

    @PostMapping(value = "/v1/card-templates", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestPart(value = "data") CardTemplatesRequest.SubmitForm data,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return cardTemplatesService.saveData(data, files, null);
    }

    @PutMapping(value = "/v1/card-templates/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestPart(value = "data") CardTemplatesRequest.SubmitForm data,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                     @PathVariable Long id) throws BaseAppException {
        return cardTemplatesService.saveData(data, files, id);
    }

    @DeleteMapping(value = "/v1/card-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return cardTemplatesService.deleteData(id);
    }

    @GetMapping(value = "/v1/card-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CardTemplatesResponse.DetailBean> getDataById(@PathVariable Long id, @RequestParam(required = false) String loginName)  throws RecordNotExistsException {
        return cardTemplatesService.getDataById(id, loginName);
    }

    @GetMapping(value = "/v1/card-templates/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CardTemplatesRequest.SearchForm dto) throws Exception {
        return cardTemplatesService.exportData(dto);
    }

    @GetMapping(value = "/v1/card-templates/by-template-type/{templateType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CardTemplatesResponse.DetailBean> getListCardTemplate(@PathVariable String templateType, @RequestParam(required = false) String loginName) {
        return cardTemplatesService.getCardTemplateByType(templateType, loginName);
    }

    @GetMapping(value = "/v1/card-templates/file/{attachmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getFileTemplateById(@PathVariable Long attachmentId) throws Exception {
        return cardTemplatesService.getFileTemplateById(attachmentId);
    }

    @GetMapping(value = "/v1/card-templates/file/by-list-attachment", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getFileByListAttachment(@RequestParam List<Long> listAttachmentId) throws Exception {
        return cardTemplatesService.getFileByListAttachment(listAttachmentId);
    }

    @GetMapping(value = "/v1/card-templates/title/{templateType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CardTemplatesResponse.DetailBean> getListTitleCardTemplate(@PathVariable String templateType) {
        return ResponseUtils.ok(cardTemplatesService.getListTitleCardTemplate(templateType));
    }

    @DeleteMapping(value = "/v1/card-templates/delete-file/{attachmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteFileTemplate(@PathVariable Long attachmentId) {
        return cardTemplatesService.deleteFileTemplate(attachmentId);
    }

}
