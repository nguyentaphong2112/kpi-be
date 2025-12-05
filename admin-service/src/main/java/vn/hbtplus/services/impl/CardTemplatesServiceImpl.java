/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import com.aspose.words.Document;
import com.aspose.words.ImageSaveOptions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.models.request.CardTemplatesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.CardTemplatesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.AttachmentEntity;
import vn.hbtplus.repositories.entity.CardTemplatesEntity;
import vn.hbtplus.repositories.impl.AttachmentRepository;
import vn.hbtplus.repositories.impl.CardTemplatesRepository;
import vn.hbtplus.repositories.jpa.CardTemplatesRepositoryJPA;
import vn.hbtplus.services.CardTemplatesService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang sys_card_templates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CardTemplatesServiceImpl implements CardTemplatesService {

    private final CardTemplatesRepository cardTemplatesRepository;
    private final CardTemplatesRepositoryJPA cardTemplatesRepositoryJPA;
    private final FileService fileService;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageFeignClient storageFeignClient;
    private final HttpServletRequest request;
    private final MdcForkJoinPool forkJoinPool;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CardTemplatesResponse.SearchResult> searchData(CardTemplatesRequest.SearchForm dto) {
        return ResponseUtils.ok(cardTemplatesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CardTemplatesRequest.SubmitForm dto, List<MultipartFile> files, Long cardTemplateId) throws BaseAppException {
        CardTemplatesEntity entity;
        boolean isDuplicateTitle = cardTemplatesRepository.duplicate(CardTemplatesEntity.class, cardTemplateId, "templateType", dto.getTemplateType(), "title", dto.getTitle());
        if (isDuplicateTitle) {
            throw new BaseAppException("ERROR_DUPLICATE_CARD_TEMPLATE_TITLE", I18n.getMessage("error.cardTemplate.title.duplicate"));
        }
        if (cardTemplateId != null && cardTemplateId > 0L) {
            entity = cardTemplatesRepositoryJPA.getById(cardTemplateId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            dto.setCardTemplateId(cardTemplateId);
        } else {
            entity = new CardTemplatesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setParameters(Utils.toJson(dto.getParameters()));
        entity.setDefaultParameters(Utils.toJson(dto.getDefaultParameters()));
        cardTemplatesRepositoryJPA.save(entity);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.CARD_TEMPLATES, Constant.ATTACHMENT.FILE_TYPES.CARD_TEMPLATES);
        fileService.uploadFiles(files, entity.getCardTemplateId(), Constant.ATTACHMENT.TABLE_NAMES.CARD_TEMPLATES, Constant.ATTACHMENT.FILE_TYPES.CARD_TEMPLATES, Constant.ATTACHMENT.MODULE);

        return ResponseUtils.ok(entity.getCardTemplateId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CardTemplatesEntity> optional = cardTemplatesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CardTemplatesEntity.class);
        }
        cardTemplatesRepository.deActiveObject(CardTemplatesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CardTemplatesResponse.DetailBean> getDataById(Long id, String loginName)  throws RecordNotExistsException {
        CardTemplatesEntity entity = cardTemplatesRepository.get(CardTemplatesEntity.class, id);
        if (entity == null) {
            throw new RecordNotExistsException(id, CardTemplatesEntity.class);
        }
        CardTemplatesResponse.DetailBean dto = new CardTemplatesResponse.DetailBean();
        Utils.copyProperties(entity, dto);
        if (StringUtils.equalsIgnoreCase(dto.getIsApplyAll(), BaseConstants.COMMON.YES)) {
            loginName = null;
        }
        dto.setAttachFileList(attachmentRepository.getAttachments(Constant.ATTACHMENT.TABLE_NAMES.CARD_TEMPLATES, Constant.ATTACHMENT.FILE_TYPES.CARD_TEMPLATES, id, loginName));

        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(CardTemplatesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = cardTemplatesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public BaseResponseEntity<CardTemplatesResponse.DetailBean> getCardTemplateByType(String templateType, String loginName) {
        CardTemplatesResponse.DetailBean result = cardTemplatesRepository.getCardTemplateByType(templateType);
        if (result != null && StringUtils.equalsIgnoreCase(result.getIsApplyAll(), BaseConstants.COMMON.YES)) {
            loginName = null;
        }
        result.setAttachFileList(attachmentRepository.getAttachments(Constant.ATTACHMENT.TABLE_NAMES.CARD_TEMPLATES, Constant.ATTACHMENT.FILE_TYPES.CARD_TEMPLATES, result.getCardTemplateId(), loginName));
        return ResponseUtils.ok(result);
    }

    @Override
    public ResponseEntity getFileTemplateById(Long attachmentId) throws Exception {
        AttachmentEntity attachment = cardTemplatesRepository.get(AttachmentEntity.class, attachmentId);
        if (attachment == null) {
            throw new RecordNotExistsException(attachmentId, AttachmentEntity.class);
        }
        byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, attachment.getFileId());
        return ResponseUtils.ok(convertByteToBase64(fileContent));
    }

    @Override
    public List<CardTemplatesResponse.DetailBean> getListTitleCardTemplate(String templateType) {
        List<CardTemplatesEntity> listData = cardTemplatesRepository.findByProperties(CardTemplatesEntity.class, "templateType", templateType);
        return Utils.mapAll(listData, CardTemplatesResponse.DetailBean.class);
    }

    @Override
    public ResponseEntity deleteFileTemplate(Long attachmentId) {
        AttachmentEntity entity = cardTemplatesRepository.get(AttachmentEntity.class, attachmentId);
        if (entity == null) {
            throw new RecordNotExistsException(attachmentId, AttachmentEntity.class);
        }
        fileService.deActiveFileByAttachmentId(List.of(attachmentId), Constant.ATTACHMENT.TABLE_NAMES.CARD_TEMPLATES, Constant.ATTACHMENT.FILE_TYPES.CARD_TEMPLATES);
        return ResponseUtils.ok(attachmentId);
    }

    @Override
    public ResponseEntity getFileByListAttachment(List<Long> listAttachmentId) throws Exception {
        List<String> listData = new ArrayList<>();
        List<AttachmentEntity> listEntity = cardTemplatesRepository.findByListId(AttachmentEntity.class, listAttachmentId);

        if (!Utils.isNullOrEmpty(listEntity)) {
            HttpHeaders httpHeaders = Utils.getRequestHeader(request);
            List<CompletableFuture<String>> futures = listEntity.stream().map(item -> CompletableFuture.supplyAsync(() -> {
                byte[] fileContent = storageFeignClient.downloadFile(httpHeaders, Constant.ATTACHMENT.MODULE, item.getFileId());
                try {
                    return convertByteToBase64(fileContent);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, forkJoinPool)).toList();
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            listData = allOf.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList())).get();
        }
        return ResponseUtils.ok(listData);
    }


    private String convertByteToBase64(byte[] fileContent) throws Exception {
        boolean isTypeDoc = Utils.checkMagicHeaderFile(fileContent, BaseConstants.DOCX_MAGIC_NUMBER) || Utils.checkMagicHeaderFile(fileContent, BaseConstants.DOC_MAGIC_NUMBER);
        if (isTypeDoc) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);
            Document document = new Document(inputStream);
            ImageSaveOptions imageOptions = new ImageSaveOptions(com.aspose.words.SaveFormat.PNG);
            imageOptions.setPageIndex(0);
            imageOptions.setPageCount(1);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream, imageOptions);
            fileContent = outputStream.toByteArray();
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

}
