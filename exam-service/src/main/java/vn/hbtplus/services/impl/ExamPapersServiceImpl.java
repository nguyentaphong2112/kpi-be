/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.ExamPapersRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.ExamPapersEntity;
import vn.hbtplus.repositories.impl.ExamPapersRepository;
import vn.hbtplus.repositories.jpa.ExamPapersRepositoryJPA;
import vn.hbtplus.services.ExamPapersService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang exm_exam_papers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ExamPapersServiceImpl implements ExamPapersService {

    private final ExamPapersRepository examPapersRepository;
    private final ExamPapersRepositoryJPA examPapersRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ExamPapersResponse> searchData(ExamPapersRequest.SearchForm dto) {
        return ResponseUtils.ok(examPapersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ExamPapersRequest.SubmitForm dto) throws BaseAppException {
        ExamPapersEntity entity;
        if (dto.getExamPaperId() != null && dto.getExamPaperId() > 0L) {
            entity = examPapersRepositoryJPA.getById(dto.getExamPaperId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ExamPapersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        examPapersRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getExamPaperId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ExamPapersEntity> optional = examPapersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ExamPapersEntity.class);
        }
        examPapersRepository.deActiveObject(ExamPapersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ExamPapersResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<ExamPapersEntity> optional = examPapersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ExamPapersEntity.class);
        }
        ExamPapersResponse dto = new ExamPapersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ExamPapersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = examPapersRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
