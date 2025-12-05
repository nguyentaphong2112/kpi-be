/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.CourseLessonResultsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CourseLessonResultsEntity;
import vn.hbtplus.repositories.impl.CourseLessonResultsRepository;
import vn.hbtplus.repositories.jpa.CourseLessonResultsRepositoryJPA;
import vn.hbtplus.services.CourseLessonResultsService;
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
 * Lop impl service ung voi bang crm_course_lesson_results
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CourseLessonResultsServiceImpl implements CourseLessonResultsService {

    private final CourseLessonResultsRepository courseLessonResultsRepository;
    private final CourseLessonResultsRepositoryJPA courseLessonResultsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CourseLessonResultsResponse> searchData(CourseLessonResultsRequest.SearchForm dto) {
        return ResponseUtils.ok(courseLessonResultsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CourseLessonResultsRequest.SubmitForm dto, Long id) throws BaseAppException {
        CourseLessonResultsEntity entity;
        if (id != null && id > 0L) {
            entity = courseLessonResultsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new CourseLessonResultsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        courseLessonResultsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getCourseLessonResultId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CourseLessonResultsEntity> optional = courseLessonResultsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CourseLessonResultsEntity.class);
        }
        courseLessonResultsRepository.deActiveObject(CourseLessonResultsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CourseLessonResultsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<CourseLessonResultsEntity> optional = courseLessonResultsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CourseLessonResultsEntity.class);
        }
        CourseLessonResultsResponse dto = new CourseLessonResultsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(CourseLessonResultsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = courseLessonResultsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
