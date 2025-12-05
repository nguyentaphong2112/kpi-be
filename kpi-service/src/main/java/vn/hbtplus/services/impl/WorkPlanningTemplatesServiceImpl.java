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
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.WorkPlanningTemplatesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.WorkPlanningTemplatesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.OrganizationEntity;
import vn.hbtplus.repositories.entity.OrganizationWorkPlanningsEntity;
import vn.hbtplus.repositories.entity.WorkPlanningTemplatesEntity;
import vn.hbtplus.repositories.impl.EmployeeEvaluationsRepository;
import vn.hbtplus.repositories.impl.WorkPlanningTemplatesRepository;
import vn.hbtplus.repositories.jpa.WorkPlanningTemplatesRepositoryJPA;
import vn.hbtplus.services.OrganizationWorkPlanningsService;
import vn.hbtplus.services.WorkPlanningTemplatesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang kpi_WorkPlanningTemplates
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class WorkPlanningTemplatesServiceImpl implements WorkPlanningTemplatesService {

    private final WorkPlanningTemplatesRepository workPlanningTemplatesRepository;
    private final WorkPlanningTemplatesRepositoryJPA workPlanningTemplatesRepositoryJPA;
    private final EmployeeEvaluationsRepository employeeEvaluationsRepository;
    private final OrganizationWorkPlanningsService organizationWorkPlanningsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WorkPlanningTemplatesResponse.SearchResult> searchData(WorkPlanningTemplatesRequest.SearchForm dto) {
        return ResponseUtils.ok(workPlanningTemplatesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(WorkPlanningTemplatesRequest.SubmitForm dto, Long id) throws BaseAppException {
        boolean isDuplicate = workPlanningTemplatesRepository.duplicate(WorkPlanningTemplatesEntity.class, id, "name", dto.getName());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_WORK_PLANNING_DUPLICATE", I18n.getMessage("error.workPlanning.name.duplicate"));
        }
        WorkPlanningTemplatesEntity entity;
        if (id != null && id > 0L) {
            entity = workPlanningTemplatesRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WorkPlanningTemplatesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setContent(dto.getContent());
        workPlanningTemplatesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getWorkPlanningTemplateId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WorkPlanningTemplatesEntity> optional = workPlanningTemplatesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkPlanningTemplatesEntity.class);
        }
        workPlanningTemplatesRepository.deActiveObject(WorkPlanningTemplatesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WorkPlanningTemplatesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<WorkPlanningTemplatesEntity> optional = workPlanningTemplatesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WorkPlanningTemplatesEntity.class);
        }
        WorkPlanningTemplatesResponse.DetailBean dto = new WorkPlanningTemplatesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(Long id, Long periodId, List<Long> organizationIds) throws Exception {
        String pathTemplate = "template/export/kpi/BM_Xuat_ke_hoach_cong_tac.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 9, true);
        WorkPlanningTemplatesResponse.DetailBean workPlanningDto = workPlanningTemplatesRepository.getListExportById(id);
        Map<Long, String> mapUnit = workPlanningTemplatesRepository.getMapData("name", "value", CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.DON_VI_TINH);
        if (workPlanningDto == null) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceText("${name}", workPlanningDto.getName().toString());
        String content = workPlanningDto.getContent();
        List<WorkPlanningTemplatesResponse.Content> contentList = Utils.fromJsonList(content, WorkPlanningTemplatesResponse.Content.class);
        int lastRow = 6;
        int col = 3;
        int totalColAdd = (organizationIds.size() - 1) * 3;
        if (!Utils.isNullOrEmpty(organizationIds)) {
            for (Long orgId : organizationIds) {
                OrganizationEntity organizationEntity = workPlanningTemplatesRepository.get(OrganizationEntity.class, orgId);
                dynamicExport.setText(organizationEntity.getName(), col, lastRow);
                dynamicExport.mergeCell(lastRow, col, lastRow, col + 2);
                dynamicExport.setText("HK I", col++, lastRow + 1);
                dynamicExport.setText("HK II", col++, lastRow + 1);
                dynamicExport.setText("Cả năm", col++, lastRow + 1);
            }
            dynamicExport.setText("Ghi chú", col, lastRow);
            dynamicExport.setColumnWidth(col, 25);
            dynamicExport.mergeCell(lastRow, 6 + totalColAdd, lastRow + 1, 6 + totalColAdd);
            dynamicExport.setColumnWidth(6, 5 + totalColAdd, 12);
            dynamicExport.setCellFormat(lastRow, 6, lastRow + 1, 6 + totalColAdd, ExportExcel.BOLD_FORMAT);
            dynamicExport.setCellFormat(lastRow, 6, lastRow + 1, 6 + totalColAdd, ExportExcel.CENTER_FORMAT);

        } else {
            dynamicExport.mergeCell(lastRow, 6, lastRow + 1, 6);
        }

        //neu co thong tin don vi thi lay ket qua cua don vi
        Map<String, WorkPlanningTemplatesResponse.Content> mapValues = new HashMap<>();
        if (periodId != null) {
            List<OrganizationWorkPlanningsEntity> listOrgWorkPlaining = organizationWorkPlanningsService.getOrgPlanning(periodId, organizationIds);
            listOrgWorkPlaining.forEach(item -> {
                List<WorkPlanningTemplatesResponse.Content> plannings = Utils.fromJsonList(item.getContent(), WorkPlanningTemplatesResponse.Content.class);
                setPathParam(plannings);
                plannings.forEach(p -> {
                    if (p != null) {
                        mapValues.put(item.getOrganizationId() + "#" + p.getPathParam(), p);
                    }
                });
            });
        }
        setPathParam(contentList);
        for (int i = 0; i < contentList.size(); i++) {
            WorkPlanningTemplatesResponse.Content contentDTO = contentList.get(i);
            boolean checkDescendant = checkDescendant(contentList, contentDTO);
            int columnIndex = 0;
            if ("3".equals(contentDTO.getLevel())) {
                dynamicExport.setEntry("", columnIndex++);
                dynamicExport.setText(getKeyByIndex(contentDTO.getKey()) + " " + contentDTO.getParam(), columnIndex++);
            } else if ("4".equals(contentDTO.getLevel())) {
                dynamicExport.setEntry("", columnIndex++);
                dynamicExport.setText("+ " + contentDTO.getParam(), columnIndex++);
            } else if ("5".equals(contentDTO.getLevel())) {
                dynamicExport.setEntry("", columnIndex++);
                dynamicExport.setText("- " + contentDTO.getParam(), columnIndex++);
            } else {
                dynamicExport.setEntry(contentDTO.getKey(), columnIndex++);
                dynamicExport.setText(contentDTO.getParam(), columnIndex++);
            }
            dynamicExport.setText(mapUnit.get(Utils.isNullOrEmpty(contentDTO.getUnit()) ? 0 : Long.parseLong(contentDTO.getUnit())), columnIndex++);
            if (!Utils.isNullOrEmpty(organizationIds)) {
                for (Long orgId : organizationIds) {
                    WorkPlanningTemplatesResponse.Content planning = mapValues.get(orgId + "#" + contentDTO.getPathParam());
                    dynamicExport.setText(planning == null ? null : planning.getStepOne(), columnIndex++);
                    dynamicExport.setText(planning == null ? null : planning.getStepTwo(), columnIndex++);
                    dynamicExport.setText(planning == null ? null : planning.getFullYear(), columnIndex++);
                }
            }
            dynamicExport.setText(contentDTO.getNote(), columnIndex);
            if (Utils.isNullOrEmpty(contentDTO.getParentKey())) {
                dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
            }
            if ("2".equals(contentDTO.getLevel()) && checkDescendant) {
                dynamicExport.setCellFormat(0, 1, ExportExcel.NORMAL_ITALIC);
                dynamicExport.setCellFormat(0, 1, ExportExcel.BOLD_FORMAT);
            }
            if ("3".equals(contentDTO.getLevel()) && checkDescendant) {
                dynamicExport.setCellFormat(1, 1, ExportExcel.NORMAL_ITALIC);
                dynamicExport.setCellFormat(1, 1, ExportExcel.BOLD_FORMAT);
            }
            dynamicExport.increaseRow();
        }
        if (Utils.isNullOrEmpty(organizationIds)) {
            dynamicExport.setCellFormat(dynamicExport.getLastRow() - contentList.size(), 0, dynamicExport.getLastRow() - 1, 6, ExportExcel.BORDER_FORMAT);
        } else {
            dynamicExport.setCellFormat(dynamicExport.getLastRow() - contentList.size() - 2, 0, dynamicExport.getLastRow() - 1, 6 + totalColAdd, ExportExcel.BORDER_FORMAT);
        }
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_ke_hoach_cong_tac.xlsx", false);
    }

    private static void setPathParam(List<WorkPlanningTemplatesResponse.Content> plannings) {
        Map<String, WorkPlanningTemplatesResponse.Content> mapKeys = new HashMap<>();
        plannings.forEach(p -> {
            if (p != null) {
                mapKeys.put(p.getKey(), p);
                if (Utils.isNullOrEmpty(p.getParentKey())) {
                    p.setPathParam(p.getParam());
                } else {
                    p.setPathParam(p.getParam() + "#" + p.getParentKey());
                }
            }
        });
    }

    @Override
    public ResponseEntity getList(WorkPlanningTemplatesRequest.SearchForm dto) {
        return ResponseUtils.ok(workPlanningTemplatesRepository.getListData(dto));
    }

    private boolean checkDescendant(List<WorkPlanningTemplatesResponse.Content> contentList, WorkPlanningTemplatesResponse.Content contentDTO) {
        for (int i = 0; i < contentList.size(); i++) {
            if (contentDTO.getKey().equals(contentList.get(i).getParentKey())) {
                return true;
            }
        }
        return false;
    }

    private String getKeyByIndex(String key) {
        String characters = "abcdefghijklmnopqrstuvwxyz";
        String[] arrIndex = key.split("\\.");
        int index = Integer.parseInt(arrIndex[arrIndex.length - 1]);
        return characters.substring(index - 1, index) + ")";
    }

}
