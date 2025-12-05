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
import vn.hbtplus.exceptions.RecordConflictException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.ResearchProjectsRequest;
import vn.hbtplus.models.response.ResearchProjectsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ResearchProjectLifecyclesEntity;
import vn.hbtplus.repositories.entity.ResearchProjectMembersEntity;
import vn.hbtplus.repositories.entity.ResearchProjectsEntity;
import vn.hbtplus.repositories.impl.ResearchProjectsRepository;
import vn.hbtplus.repositories.jpa.ResearchProjectLifecyclesRepositoryJPA;
import vn.hbtplus.repositories.jpa.ResearchProjectMembersRepositoryJPA;
import vn.hbtplus.repositories.jpa.ResearchProjectsRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.ResearchProjectsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang lms_research_projects
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class ResearchProjectsServiceImpl implements ResearchProjectsService {

    private final ResearchProjectsRepository researchProjectsRepository;
    private final ResearchProjectsRepositoryJPA researchProjectsRepositoryJPA;
    private final ResearchProjectMembersRepositoryJPA membersRepositoryJPA;
    private final ResearchProjectLifecyclesRepositoryJPA lifecyclesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final AttachmentService attachmentService;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ResearchProjectsResponse.SearchResult> searchData(ResearchProjectsRequest.SearchForm dto) {
        return ResponseUtils.ok(researchProjectsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ResearchProjectsRequest.SubmitForm dto, Long id) throws BaseAppException {
        //check xem de tai nghien cuu da ton tai chua
        if (researchProjectsRepository.duplicate(ResearchProjectsEntity.class, id, "title", dto.getTitle())) {
            throw new RecordConflictException("researchProject.error.duplicateTitle");
        }

        ResearchProjectsEntity entity;
        if (id != null && id > 0L) {
            entity = researchProjectsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ResearchProjectsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);


        researchProjectsRepositoryJPA.save(entity);
        //Luu thong tin objectAttributes
        objectAttributesService.saveObjectAttributes(entity.getResearchProjectId(), dto.getListAttributes(), ResearchProjectsEntity.class, null);

        //luu thong tin nguoi tham gia
        saveMembers(entity.getResearchProjectId(), dto.getListMembers(), ResearchProjectMembersEntity.TYPES.THAM_GIA_DE_TAI);
        //Luu lifecycle
        saveLifeCycles(entity.getResearchProjectId(), dto.getListLifecycles());

        //Luu file dinh kem
        //save file dinh kem
        fileService.uploadFiles(dto.getListFileAttachments(), entity.getResearchProjectId(), Constant.ATTACHMENT.TABLE_NAMES.RESEARCH_PROJECT, Constant.ATTACHMENT.FILE_TYPES.RESEARCH_PROJECT, Constant.ATTACHMENT.MODULE);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.RESEARCH_PROJECT, Constant.ATTACHMENT.FILE_TYPES.RESEARCH_PROJECT);

        return ResponseUtils.ok(entity.getResearchProjectId());
    }

    private void saveLifeCycles(Long researchProjectId, List<ResearchProjectsRequest.Lifecycle> listLifecycles) {
        List<ResearchProjectLifecyclesEntity> lifecyclesEntities = lifecyclesRepositoryJPA.findByResearchProjectId(researchProjectId);
        Map<String, ResearchProjectLifecyclesEntity> map = new HashMap<>();
        lifecyclesEntities.forEach(item -> {
            map.put(item.getType(), item);
        });
        if (!Utils.isNullOrEmpty(listLifecycles)) {
            listLifecycles.forEach(lifecycleRequest -> {
                ResearchProjectLifecyclesEntity lifecyclesEntity = map.get(lifecycleRequest.getType());
                if (lifecyclesEntity == null) {
                    lifecyclesEntity = new ResearchProjectLifecyclesEntity();
                    lifecyclesEntity.setCreatedTime(new Date());
                    lifecyclesEntity.setCreatedBy(Utils.getUserNameLogin());
                    lifecyclesEntity.setResearchProjectId(researchProjectId);
                    lifecyclesEntity.setType(lifecycleRequest.getType());
                } else {
                    lifecyclesEntity.setModifiedTime(new Date());
                    lifecyclesEntity.setModifiedBy(Utils.getUserNameLogin());
                }
                Utils.copyProperties(lifecycleRequest, lifecyclesEntity);
                lifecyclesEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                lifecyclesRepositoryJPA.save(lifecyclesEntity);


                //save attributes
                objectAttributesService.saveObjectAttributes(researchProjectId, lifecycleRequest.getListAttributes(), ResearchProjectsEntity.class, lifecycleRequest.getType());

                //save members
                saveMembers(researchProjectId, lifecycleRequest.getListMembers(), lifecycleRequest.getType());

                //save file dinh kem
                fileService.uploadFiles(lifecycleRequest.getListFileAttachments(), researchProjectId, Constant.ATTACHMENT.TABLE_NAMES.RESEARCH_PROJECT, Constant.ATTACHMENT.FILE_TYPES.RESEARCH_PROJECT + "-" + lifecycleRequest.getType(), Constant.ATTACHMENT.MODULE);
                fileService.deActiveFileByAttachmentId(lifecycleRequest.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.RESEARCH_PROJECT, Constant.ATTACHMENT.FILE_TYPES.RESEARCH_PROJECT + "-" + lifecycleRequest.getType());
            });
        }
        map.values().forEach(lifecyclesEntity -> {
            lifecyclesEntity.setModifiedTime(new Date());
            lifecyclesEntity.setModifiedBy(Utils.getUserNameLogin());
            lifecyclesEntity.setIsDeleted(BaseConstants.STATUS.DELETED);
            lifecyclesRepositoryJPA.save(lifecyclesEntity);
        });
    }


    private void saveMembers(Long researchProjectId, List<ResearchProjectsRequest.Member> listMembers, String type) {
        List<ResearchProjectMembersEntity> membersEntities = membersRepositoryJPA.findByResearchProjectIdAndType(researchProjectId, type);
        Map<Long, ResearchProjectMembersEntity> map = new HashMap<>();
        membersEntities.forEach(item -> {
            map.put(item.getEmployeeId(), item);
        });
        if (!Utils.isNullOrEmpty(listMembers)) {
            for (int i = 0; i < listMembers.size(); i++) {
                ResearchProjectsRequest.Member memberDto = listMembers.get(i);
                ResearchProjectMembersEntity membersEntity = map.get(memberDto.getEmployeeId());
                if (membersEntity == null) {
                    membersEntity = new ResearchProjectMembersEntity();
                    membersEntity.setCreatedTime(new Date());
                    membersEntity.setCreatedBy(Utils.getUserNameLogin());
                    membersEntity.setResearchProjectId(researchProjectId);
                    membersEntity.setType(type);
                } else {
                    membersEntity.setModifiedTime(new Date());
                    membersEntity.setModifiedBy(Utils.getUserNameLogin());
                }
                Utils.copyProperties(memberDto, membersEntity);
                membersEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                membersEntity.setOrderNumber(i + 1);
                membersRepositoryJPA.save(membersEntity);

                map.remove(memberDto.getEmployeeId());
            }
        }
        map.values().forEach(membersEntity -> {
            membersEntity.setModifiedTime(new Date());
            membersEntity.setModifiedBy(Utils.getUserNameLogin());
            membersEntity.setIsDeleted(BaseConstants.STATUS.DELETED);
            membersRepositoryJPA.save(membersEntity);
        });
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ResearchProjectsEntity> optional = researchProjectsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ResearchProjectsEntity.class);
        }
        researchProjectsRepository.deActiveObject(ResearchProjectsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ResearchProjectsResponse.DetailBean getDataById(Long id) throws RecordNotExistsException {
        Optional<ResearchProjectsEntity> optional = researchProjectsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ResearchProjectsEntity.class);
        }
        ResearchProjectsResponse.DetailBean dto = new ResearchProjectsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, "lms_research_projects"));
        dto.setListFileAttachments(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.RESEARCH_PROJECT, Constant.ATTACHMENT.FILE_TYPES.RESEARCH_PROJECT, id));
        dto.setListMembers(researchProjectsRepository.getListMembers(id, ResearchProjectMembersEntity.TYPES.THAM_GIA_DE_TAI));
        List<ResearchProjectLifecyclesEntity> lifecyclesEntities = lifecyclesRepositoryJPA.findByResearchProjectId(id);
        Map lifecycles = new HashMap();
        lifecyclesEntities.forEach(item -> {
            ResearchProjectsResponse.Lifecycle lifecycle = new ResearchProjectsResponse.Lifecycle();
            Utils.copyProperties(item, lifecycle);

            lifecycle.setListAttributes(dto.getListAttributes());
            lifecycle.setListMembers(researchProjectsRepository.getListMembers(id, item.getType()));
            lifecycle.setListFileAttachments(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.RESEARCH_PROJECT, Constant.ATTACHMENT.FILE_TYPES.RESEARCH_PROJECT + "-" + item.getType(), id));
            lifecycles.put(item.getType(), lifecycle);
        });
        dto.setLifecycles(lifecycles);
        return dto;
    }

    @Override
    public ResponseEntity<Object> exportData(ResearchProjectsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = researchProjectsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

}
