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
import vn.hbtplus.models.request.JobsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.JobsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.JobsEntity;
import vn.hbtplus.repositories.entity.PositionsEntity;
import vn.hbtplus.repositories.impl.JobsRepository;
import vn.hbtplus.repositories.jpa.JobsRepositoryJPA;
import vn.hbtplus.services.JobsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_jobs
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class JobsServiceImpl implements JobsService {

    private final JobsRepository jobsRepository;
    private final ObjectAttributesService objectAttributesService;
    private final JobsRepositoryJPA jobsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<JobsResponse.SearchResult> searchData(JobsRequest.SearchForm dto) {
        return ResponseUtils.ok(jobsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(JobsRequest.SubmitForm dto, Long jobId) throws BaseAppException {
        // check duplicate code
        boolean isDuplicate = jobsRepository.duplicate(JobsEntity.class, jobId, "code", dto.getCode(), "jobType", dto.getJobType());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_JOB_DUPLICATE", I18n.getMessage("error.jobs.code.duplicate"));
        }

        // check duplicate name
        isDuplicate = jobsRepository.duplicate(JobsEntity.class, jobId, "name", dto.getName(), "jobType", dto.getJobType());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_JOB_DUPLICATE", I18n.getMessage("error.jobs.name.duplicate"));
        }

        JobsEntity entity;
        String oldName = null;
        if (jobId != null && jobId > 0L) {
            entity = jobsRepositoryJPA.getById(jobId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
            oldName = entity.getName();
        } else {
            entity = new JobsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        jobsRepositoryJPA.save(entity);
        jobsRepositoryJPA.flush();

        objectAttributesService.saveObjectAttributes(entity.getJobId(), dto.getListAttributes(), JobsEntity.class, null);

//        if (oldName != null && !oldName.equals(dto.getName())) {
//            jobsRepository.updatePositionName(entity.getJobId(), entity.getName());
//        }
        return ResponseUtils.ok(entity.getJobId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long id) throws BaseAppException {
        Optional<JobsEntity> optional = jobsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, JobsEntity.class);
        }

        List<PositionsEntity> listPositions = jobsRepository.findByProperties(PositionsEntity.class, "jobId", id);
        if (!Utils.isNullOrEmpty(listPositions)) {
            throw new BaseAppException("ERROR_JOB_USED", I18n.getMessage("error.jobs.name.used"));
        }

        jobsRepository.deActiveObject(JobsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<JobsResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<JobsEntity> optional = jobsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, JobsEntity.class);
        }
        JobsResponse.DetailBean dto = new JobsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
//        dto.setListAttributes(objectAttributesService.getAttributes(id, jobsRepository.getSQLTableName(JobsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(JobsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/danh-muc-chuc-danh.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = jobsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-muc-chuc-danh.xlsx");
    }

    @Override
    public List<JobsResponse.DetailBean> getListJobs(List<String> jobType, Long organizationId) {
        return jobsRepository.getListJobs(jobType, organizationId);
    }

}
