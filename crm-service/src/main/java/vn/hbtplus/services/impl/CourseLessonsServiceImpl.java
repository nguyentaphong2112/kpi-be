/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.CourseLessonsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CourseLessonsEntity;
import vn.hbtplus.repositories.impl.CourseLessonsRepository;
import vn.hbtplus.repositories.jpa.CourseLessonsRepositoryJPA;
import vn.hbtplus.services.CourseLessonsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang crm_course_lessons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CourseLessonsServiceImpl implements CourseLessonsService {

    private final CourseLessonsRepository courseLessonsRepository;
    private final CourseLessonsRepositoryJPA courseLessonsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CourseLessonsResponse.SearchResult> searchData(CourseLessonsRequest.SearchForm dto) {
        return ResponseUtils.ok(courseLessonsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CourseLessonsRequest.SubmitForm dto, Long id) throws BaseAppException {
        CourseLessonsEntity entity;
        if (id != null && id > 0L) {
            entity = courseLessonsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new CourseLessonsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        courseLessonsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getCourseLessonId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CourseLessonsEntity> optional = courseLessonsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CourseLessonsEntity.class);
        }
        courseLessonsRepository.deActiveObject(CourseLessonsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CourseLessonsResponse.SearchResult> getDataById(Long id)  throws RecordNotExistsException {
        Optional<CourseLessonsEntity> optional = courseLessonsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CourseLessonsEntity.class);
        }
        CourseLessonsResponse.SearchResult dto = new CourseLessonsResponse.SearchResult();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ListResponseEntity<CourseLessonsEntity> getDataByCourseId(Long id) throws RecordNotExistsException {
        List<CourseLessonsEntity> entityList = courseLessonsRepository.findByProperties(CourseLessonsEntity.class, "courseId", id);
        return ResponseUtils.ok(entityList);
    }

    @Override
    public ListResponseEntity<CourseLessonsResponse.Selected> getDataByCourseListId(Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(courseLessonsRepository.getListData(id));
    }

    @Override
    public ResponseEntity<Object> exportData(CourseLessonsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = courseLessonsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
