/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.CourseTraineesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CourseTraineesEntity;
import vn.hbtplus.repositories.impl.CourseTraineesRepository;
import vn.hbtplus.repositories.jpa.CourseTraineesRepositoryJPA;
import vn.hbtplus.services.CourseTraineesService;
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
 * Lop impl service ung voi bang crm_course_trainees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CourseTraineesServiceImpl implements CourseTraineesService {

    private final CourseTraineesRepository courseTraineesRepository;
    private final CourseTraineesRepositoryJPA courseTraineesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CourseTraineesResponse> searchData(CourseTraineesRequest.SearchForm dto) {
        return ResponseUtils.ok(courseTraineesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CourseTraineesRequest.SubmitForm dto, Long id) throws BaseAppException {
        CourseTraineesEntity entity;
        if (id != null && id > 0L) {
            entity = courseTraineesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new CourseTraineesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        courseTraineesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getCourseTraineeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CourseTraineesEntity> optional = courseTraineesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CourseTraineesEntity.class);
        }
        courseTraineesRepository.deActiveObject(CourseTraineesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CourseTraineesResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<CourseTraineesEntity> optional = courseTraineesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CourseTraineesEntity.class);
        }
        CourseTraineesResponse dto = new CourseTraineesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(CourseTraineesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = courseTraineesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
