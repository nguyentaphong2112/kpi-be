/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.SalaryRanksRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.DocumentTypesEntity;
import vn.hbtplus.repositories.entity.ObjectRelationsEntity;
import vn.hbtplus.repositories.entity.SalaryGradesEntity;
import vn.hbtplus.repositories.entity.SalaryRanksEntity;
import vn.hbtplus.repositories.impl.SalaryRanksRepository;
import vn.hbtplus.repositories.jpa.SalaryGradesRepositoryJPA;
import vn.hbtplus.repositories.jpa.SalaryRanksRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.ObjectRelationsService;
import vn.hbtplus.services.SalaryRanksService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang hr_salary_ranks
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class SalaryRanksServiceImpl implements SalaryRanksService {

    private final SalaryRanksRepository salaryRanksRepository;
    private final SalaryRanksRepositoryJPA salaryRanksRepositoryJPA;
    private final SalaryGradesRepositoryJPA salaryGradesRepositoryJPA;
    private final ObjectRelationsService objectRelationsService;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<SalaryRanksResponse.SearchResult> searchData(SalaryRanksRequest.SearchForm dto) {
        return ResponseUtils.ok(salaryRanksRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(SalaryRanksRequest.SubmitForm dto, Long salaryRankId) throws BaseAppException {
        if (salaryRanksRepository.duplicate(SalaryRanksEntity.class, salaryRankId, "code", dto.getCode(), "name", dto.getName(), "startDate", dto.getStartDate(), "salaryType", dto.getSalaryType())) {
            throw new BaseAppException("ERROR_SALARY_DUPLICATE", I18n.getMessage("error.salaryRanks.code.duplicate"));
        }

        SalaryRanksEntity entity;
        if (salaryRankId != null && salaryRankId > 0L) {
            entity = salaryRanksRepositoryJPA.getById(salaryRankId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new SalaryRanksEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        salaryRanksRepositoryJPA.save(entity);

        //luu du lieu salary grades
        saveSalaryGrades(dto.getGrades(), entity.getSalaryRankId());

        //luu du lieu chuc danh huong luong
        saveJobSalary(dto.getJobIds(), entity.getSalaryRankId());

        objectAttributesService.saveObjectAttributes(entity.getSalaryRankId(), dto.getListAttributes(), SalaryRanksEntity.class, null);

        return ResponseUtils.ok(entity.getSalaryRankId());
    }

    private void saveJobSalary(List<Long> jobIds, Long salaryRankId) {
        objectRelationsService.inactiveReferIdNotIn(salaryRankId, jobIds, ObjectRelationsEntity.TABLE_NAMES.SALARY_RANKS, ObjectRelationsEntity.TABLE_NAMES.JOB, ObjectRelationsEntity.FUNCTION_CODES.GAN_CHUC_DANH_HUONG_LUONG);
        objectRelationsService.saveObjectRelations(salaryRankId, jobIds, ObjectRelationsEntity.TABLE_NAMES.SALARY_RANKS, ObjectRelationsEntity.TABLE_NAMES.JOB, ObjectRelationsEntity.FUNCTION_CODES.GAN_CHUC_DANH_HUONG_LUONG);
    }

    private void saveSalaryGrades(List<SalaryRanksRequest.SalaryGradeDto> grades, Long salaryRankId) throws BaseAppException {
        List<Long> salaryGradeIds = new ArrayList<>();
        grades.forEach(item -> {
            if (item.getSalaryGradeId() != null) {
                salaryGradeIds.add(item.getSalaryGradeId());
            }
        });

        salaryRanksRepository.inactiveGradeNotIn(salaryGradeIds, salaryRankId);

        //insert du lieu
        grades.forEach(item -> {
            if (!Utils.isNullOrEmpty(item.getName())) {
                SalaryGradesEntity gradesEntity;
                if (item.getSalaryGradeId() == null) {
                    gradesEntity = new SalaryGradesEntity();
                    gradesEntity.setCreatedTime(new Date());
                    gradesEntity.setCreatedBy(Utils.getUserNameLogin());
                    gradesEntity.setSalaryRankId(salaryRankId);
                } else {
                    gradesEntity = salaryGradesRepositoryJPA.getById(item.getSalaryGradeId());
                    if (!gradesEntity.getSalaryRankId().equals(salaryRankId)) {
                        throw new RuntimeException("salaryRankId is invalid");
                    }
                    gradesEntity.setModifiedTime(new Date());
                    gradesEntity.setModifiedBy(Utils.getUserNameLogin());
                }
                gradesEntity.setAmount(item.getAmount());
                gradesEntity.setName(item.getName());
                gradesEntity.setNote(item.getNote());
                gradesEntity.setDuration(item.getDuration());
                salaryGradesRepositoryJPA.save(gradesEntity);
            }
        });
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<SalaryRanksEntity> optional = salaryRanksRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, SalaryRanksEntity.class);
        }
        salaryRanksRepository.deActiveObject(SalaryRanksEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<SalaryRanksResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<SalaryRanksEntity> optional = salaryRanksRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, SalaryRanksEntity.class);
        }
        SalaryRanksResponse.DetailBean dto = new SalaryRanksResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setGrades(salaryRanksRepository.getSalaryGrades(id));
        if(dto.getGrades().isEmpty()){
            dto.getGrades().add(new SalaryRanksResponse.SalaryGradeDto());
        }
        dto.setSalaryJobs(salaryRanksRepository.getSalaryJobs(id));
        dto.setListAttributes(objectAttributesService.getAttributes(id, "hr_salary_ranks"));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(SalaryRanksRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = salaryRanksRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<SalaryRanksResponse> getSalaryRanks(String salaryType, java.sql.Date startDate, Long empTypeId, boolean isGetAttributes) {
        List<SalaryRanksResponse> results = salaryRanksRepository.getSalaryRanks(salaryType, startDate, empTypeId);
        if (!isGetAttributes || results.isEmpty()) {
            return results;
        }

        List<Long> ids = new ArrayList<>();
        results.stream().forEach(item -> {
            ids.add(item.getSalaryRankId());
        });
        Map<Long, List<ObjectAttributesResponse>> maps = objectAttributesService.getListMapAttributes(ids, "hr_salary_ranks");
        results.forEach(item -> {
            item.setListAttributes(maps.get(item.getSalaryRankId()));
        });
        return results;
    }

    @Override
    public List<SalaryRanksResponse.SalaryGradeDto> getSalaryGrades(Long id) {
        return salaryRanksRepository.getSalaryGrades(id);
    }

    @Override
    public List<SalaryRanksResponse> getSalaryRanksByListType(List<String> listSalaryType, boolean isGetAttributes) {
        List<SalaryRanksResponse> results = salaryRanksRepository.getSalaryRanksByListType(listSalaryType);
        if (!isGetAttributes || results.isEmpty()) {
            return results;
        }

        List<Long> ids = new ArrayList<>();
        results.forEach(item -> ids.add(item.getSalaryRankId()));
        Map<Long, List<ObjectAttributesResponse>> maps = objectAttributesService.getListMapAttributes(ids, "hr_salary_ranks");
        results.forEach(item -> item.setListAttributes(maps.get(item.getSalaryRankId())));
        return results;
    }

}
