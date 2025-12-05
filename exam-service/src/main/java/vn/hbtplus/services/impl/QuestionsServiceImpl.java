/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.QuestionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.QuestionOptionsResponse;
import vn.hbtplus.models.response.QuestionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.QuestionOptionsEntity;
import vn.hbtplus.repositories.entity.QuestionsEntity;
import vn.hbtplus.repositories.impl.QuestionsRepository;
import vn.hbtplus.repositories.jpa.QuestionsRepositoryJPA;
import vn.hbtplus.services.QuestionsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static vn.hbtplus.repositories.entity.QuestionsEntity.STATUS;

/**
 * Lop impl service ung voi bang exm_questions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class QuestionsServiceImpl implements QuestionsService {

    private final QuestionsRepository questionsRepository;
    private final QuestionsRepositoryJPA questionsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<QuestionsResponse> searchData(QuestionsRequest.SearchForm dto) {
        return ResponseUtils.ok(questionsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(QuestionsRequest.SubmitForm dto) throws BaseAppException {
        QuestionsEntity entity;
        if (dto.getQuestionId() != null && dto.getQuestionId() > 0L) {
            entity = questionsRepositoryJPA.getById(dto.getQuestionId());
            if (!STATUS.LIST_STATUS_UPDATE_VALID.contains(entity.getStatusCode())) {
                throw new BaseAppException("Trạng thái bản ghi không được phép thao tác!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new QuestionsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setStatusCode(STATUS.DRAFT);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        questionsRepositoryJPA.save(entity);

        questionsRepository.deActiveObject(QuestionOptionsEntity.class, "questionId", entity.getQuestionId());
        List<QuestionOptionsEntity> listSaveOption = Utils.mapAll(dto.getOptions(), QuestionOptionsEntity.class);
        for (QuestionOptionsEntity optionEntity : listSaveOption) {
            optionEntity.setQuestionId(entity.getQuestionId());
        }
        questionsRepository.insertBatch(QuestionOptionsEntity.class, listSaveOption);

        return ResponseUtils.ok(entity.getQuestionId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<QuestionsEntity> optional = questionsRepositoryJPA.findById(id);
        if (!optional.isPresent()
            || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())
            || !STATUS.LIST_STATUS_UPDATE_VALID.contains(optional.get().getStatusCode())
        ) {
            throw new RecordNotExistsException(id, QuestionsEntity.class);
        }
        questionsRepository.deActiveObject(QuestionsEntity.class, id);
        questionsRepository.deActiveObject(QuestionOptionsEntity.class, "questionId", id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<QuestionsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<QuestionsEntity> optional = questionsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, QuestionsEntity.class);
        }
        QuestionsResponse dto = new QuestionsResponse();
        Utils.copyProperties(optional.get(), dto);

        List<QuestionOptionsEntity> optionsEntities = questionsRepository.findByProperties(QuestionOptionsEntity.class, "questionId", id, "orderNumber");
        dto.setOptions(Utils.mapAll(optionsEntities, QuestionOptionsResponse.class));

        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(QuestionsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = questionsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
