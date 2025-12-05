/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.dto.AttachmentDto;
import vn.hbtplus.models.request.BooksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BooksResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.AttachmentEntity;
import vn.hbtplus.repositories.entity.BookEditionEntity;
import vn.hbtplus.repositories.entity.BookEntity;
import vn.hbtplus.repositories.entity.BookTranslatorEntity;
import vn.hbtplus.repositories.impl.AttachmentRepository;
import vn.hbtplus.repositories.impl.BooksRepository;
import vn.hbtplus.repositories.jpa.BookEditionsRepositoryJPA;
import vn.hbtplus.repositories.jpa.BookTranslatorsRepositoryJPA;
import vn.hbtplus.repositories.jpa.BooksRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.BookService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.PdfUtils;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang lib_books
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BooksRepository booksRepository;
    private final BooksRepositoryJPA booksRepositoryJPA;
    private final AttachmentService attachmentService;
    private final BookEditionsRepositoryJPA bookEditionsRepositoryJPA;
    private final BookTranslatorsRepositoryJPA bookTranslatorsRepositoryJPA;
    private final FileStorageFeignClient storageFeignClient;
    private final HttpServletRequest request;
    private final AttachmentRepository attachmentRepository;

    private final MdcForkJoinPool mdcForkJoinPool;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<BooksResponse.SearchResult> searchData(BooksRequest.SearchForm dto) throws Exception {
        BaseDataTableDto<BooksResponse.SearchResult> dataList = booksRepository.searchData(dto);

        Map<Long, BooksResponse.SearchResult> mapData = new HashMap<>();
        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        HttpHeaders headers = Utils.getRequestHeader(request);
        dataList.getListData().forEach(item -> {
            Supplier<Object> datas = () -> getAvatarBook(item.getBookId(), headers);
            completableFutures.add(CompletableFuture.supplyAsync(datas, mdcForkJoinPool));

            mapData.put(item.getBookId(), item);
        });
        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<Object> objs = allFutures.get();
        objs.forEach(item -> {
            Pair<Long, String> pair = (Pair<Long, String>) item;
            BooksResponse.SearchResult data = mapData.get(pair.getLeft());
            if (data != null) {
                data.setAvatar(pair.getRight());
            }
        });

        return ResponseUtils.ok(dataList);
    }

    private Pair<Long, String> getAvatarBook(Long bookId, HttpHeaders headers) {
        Attachment attachmentEntity = attachmentService.getAttachmentEntity(Constant.ATTACHMENT.TABLE_NAMES.BOOK, Constant.ATTACHMENT.FILE_TYPES.BOOK_AVATAR, bookId);
        String base64Image = null;
        if (attachmentEntity != null) {
            //Lay du lieu file
            byte[] fileContent = storageFeignClient.downloadFile(headers, Constant.ATTACHMENT.MODULE, ((AttachmentDto) attachmentEntity).getFileId());
            if (fileContent != null) {
                base64Image = Base64.getEncoder().encodeToString(fileContent);
            }
        }

        return new MutablePair<>(bookId, base64Image);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity saveData(BooksRequest.SubmitForm dto,
                                   MultipartFile fileAvatar,
                                   MultipartFile fileContent,
                                   Long bookId) throws Exception {
        BookEntity entity;
        if (bookId != null && bookId > 0L) {
            entity = booksRepositoryJPA.getById(bookId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new BookEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        booksRepositoryJPA.save(entity);
        //luu thong tin phien ban sach
        List<Long> editionIds = new ArrayList<>();
        if (!Utils.isNullOrEmpty(dto.getListEditions())) {
            dto.getListEditions().forEach(item -> {
                if (item != null && item.getBookEditionId() != null) {
                    editionIds.add(item.getBookEditionId());
                }
            });
        }
        if (bookId != null && bookId > 0L) {
            booksRepository.inactiveEditionNotIn(bookId, editionIds);
        }

        if (!Utils.isNullOrEmpty(dto.getListEditions())) {
            dto.getListEditions().forEach(item -> {
                if (item != null) {
                    BookEditionEntity bookEdition;
                    if (item.getBookEditionId() != null && item.getBookEditionId() > 0) {
                        bookEdition = bookEditionsRepositoryJPA.getById(item.getBookEditionId());
                    } else {
                        bookEdition = new BookEditionEntity();
                        bookEdition.setCreatedBy(Utils.getUserNameLogin());
                        bookEdition.setCreatedTime(new Date());
                    }
                    Utils.copyProperties(item, bookEdition);
                    bookEdition.setBookId(entity.getBookId());
                    bookEdition.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                    bookEditionsRepositoryJPA.save(bookEdition);
                }
            });
        }
        //luu thong tin translator
        bookTranslatorsRepositoryJPA.deleteByBookId(bookId);
        if (!Utils.isNullOrEmpty(dto.getTranslatorIds())) {
            dto.getTranslatorIds().forEach(item -> {
                BookTranslatorEntity translatorEntity = new BookTranslatorEntity();
                translatorEntity.setBookId(entity.getBookId());
                translatorEntity.setTranslatorId(item);
                translatorEntity.setCreatedBy(Utils.getUserNameLogin());
                translatorEntity.setCreatedTime(new Date());
                bookTranslatorsRepositoryJPA.save(translatorEntity);
            });
        }
        if (fileAvatar != null) {
            uploadAndSaveEntity(fileAvatar, Constant.ATTACHMENT.FILE_TYPES.BOOK_AVATAR, entity);
        }
        if (fileContent != null) {
            uploadAndSaveEntity(fileContent, Constant.ATTACHMENT.FILE_TYPES.BOOK_CONTENT, entity);
            if (fileAvatar == null && bookId == null) {
                if (fileContent.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                    MultipartFile fistPage = PdfUtils.convertBytesToMultipartFile(
                            PdfUtils.convertToImage(fileContent.getInputStream().readAllBytes()),
                            "file-anh-dai-dien.png", "image/png"
                    );
                    uploadAndSaveEntity(fistPage, Constant.ATTACHMENT.FILE_TYPES.BOOK_AVATAR, entity);
                }
            }
        }

        return ResponseUtils.ok(entity.getBookId());
    }

    private void uploadAndSaveEntity(MultipartFile fileAvatar,
                                     String fileType,
                                     BookEntity entity) {
        BaseResponse<AttachmentFileDto> response = storageFeignClient.uploadFile(Utils.getRequestHeader(request), fileAvatar, Constant.ATTACHMENT.MODULE, fileType, entity);
        AttachmentFileDto fileResponse = response.getData();
        fileResponse.getFileId();

        attachmentService.inactiveAttachment(
                Constant.ATTACHMENT.TABLE_NAMES.BOOK,
                fileType,
                entity.getBookId());

        attachmentService.saveAttachment(Constant.ATTACHMENT.TABLE_NAMES.BOOK,
                fileType,
                entity.getBookId(),
                fileResponse
        );
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<BookEntity> optional = booksRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookEntity.class);
        }
        booksRepository.deActiveObject(BookEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<BooksResponse.DetailResult> getDataById(Long id) throws RecordNotExistsException {
        BooksResponse.DetailResult dto = booksRepository.findByBookId(id);
        List<BooksResponse.Edition> child = booksRepository.getEditionByBookId(id);
        List<BooksResponse.TranslatorResult> translatorList = booksRepository.getListTranslatorByBookId(id);
        dto.setListEditions(child);
        dto.setTranslatorIds(translatorList.stream().map(BooksResponse.TranslatorResult::getTranslatorId).toList());
        dto.setTranslatorName(translatorList.stream()
                .map(BooksResponse.TranslatorResult::getTranslatorName)
                .collect(Collectors.joining(", ")));
        dto.setFileAvatar(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.BOOK, Constant.ATTACHMENT.FILE_TYPES.BOOK_AVATAR, id));
        dto.setFileContent(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.BOOK, Constant.ATTACHMENT.FILE_TYPES.BOOK_CONTENT, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(BooksRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = booksRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public BaseDataTableDto searchAuthor(BooksRequest.SearchForm dto) {
        return booksRepository.searchAuthor(dto);
    }

    @Override
    public BaseDataTableDto searchTranslator(BooksRequest.SearchForm dto) {
        return booksRepository.searchTranslator(dto);
    }

    @Override
    public BaseDataTableDto searchGenre(BooksRequest.SearchForm dto) {
        return booksRepository.searchGenre(dto);
    }

    @Override
    public ResponseEntity getAvatar(Long bookId) {
        Attachment attachmentEntity = attachmentService.getAttachmentEntity(Constant.ATTACHMENT.TABLE_NAMES.BOOK, Constant.ATTACHMENT.FILE_TYPES.BOOK_AVATAR, bookId);
        if (attachmentEntity != null) {
            //Lay du lieu file
            byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, ((AttachmentDto) attachmentEntity).getFileId());
            String base64Image = null;
            if (fileContent != null) {
                base64Image = Base64.getEncoder().encodeToString(fileContent);
            }
            return ResponseUtils.ok(base64Image);
        }
        return null;
    }

    @Override
    public ResponseEntity getBookFile(Long bookId) throws IOException {
        Attachment attachmentEntity = attachmentService.getAttachmentEntity(Constant.ATTACHMENT.TABLE_NAMES.BOOK, Constant.ATTACHMENT.FILE_TYPES.BOOK_CONTENT, bookId);
        if (attachmentEntity != null) {
            byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, ((AttachmentDto) attachmentEntity).getFileId());
            return ResponseUtils.getResponseFileEntity(fileContent, attachmentEntity.getFileName());
        }
        return null;
    }

    @Override
    public ResponseEntity getByFileId(Long fileId) throws IOException {
        List<AttachmentEntity> attachmentEntity = attachmentRepository.findByProperties(AttachmentEntity.class, "fileId", fileId);
        if (attachmentEntity.size() > 0) {
            byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, attachmentEntity.get(0).getFileId());
            return ResponseUtils.getResponseFileEntity(fileContent, attachmentEntity.get(0).getFileName());
        }
        return null;
    }


}
