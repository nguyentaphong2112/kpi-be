/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.MembersRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.MembersEntity;
import vn.hbtplus.repositories.impl.MembersRepository;
import vn.hbtplus.repositories.jpa.MembersRepositoryJPA;
import vn.hbtplus.services.MembersService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.I18n;
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
 * Lop impl service ung voi bang lib_members
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class MembersServiceImpl implements MembersService {

    private final MembersRepository membersRepository;
    private final MembersRepositoryJPA membersRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<MembersResponse.SearchResult> searchData(MembersRequest.SearchForm dto) {
        return ResponseUtils.ok(membersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(MembersRequest.SubmitForm dto,Long memberId) throws BaseAppException {
        boolean isDuplicate = membersRepository.duplicate(MembersEntity.class, memberId, "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CONTRACT_DUPLICATE", I18n.getMessage("error.member.code.duplicate"));
        }

        MembersEntity entity;
        if (memberId != null && memberId > 0L) {
            entity = membersRepositoryJPA.getById(memberId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new MembersEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        membersRepositoryJPA.save(entity);
        membersRepositoryJPA.flush();

        objectAttributesService.saveObjectAttributes(entity.getMemberId(), dto.getListAttributes(), MembersEntity.class, null);
        return ResponseUtils.ok(entity.getMemberId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<MembersEntity> optional = membersRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MembersEntity.class);
        }
        membersRepository.deActiveObject(MembersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<MembersResponse.DetailBean> getDataById(Long id)  throws RecordNotExistsException {
        Optional<MembersEntity> optional = membersRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MembersEntity.class);
        }
        MembersResponse.DetailBean dto = new MembersResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, membersRepository.getSQLTableName(MembersEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(MembersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = membersRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public BaseDataTableDto getPageable(BaseSearchRequest request) {
       return membersRepository.getPageable(request);
    }

}
