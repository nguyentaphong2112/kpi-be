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
import vn.hbtplus.models.request.UserBookmarksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.UserBookmarksResponse;
import vn.hbtplus.repositories.entity.UserBookmarksEntity;
import vn.hbtplus.repositories.impl.UserBookmarksRepository;
import vn.hbtplus.repositories.jpa.UserBookmarksRepositoryJPA;
import vn.hbtplus.services.UserBookmarksService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang sys_user_bookmarks
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class UserBookmarksServiceImpl implements UserBookmarksService {

    private final UserBookmarksRepository userBookmarksRepository;
    private final UserBookmarksRepositoryJPA userBookmarksRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<UserBookmarksResponse> searchData(UserBookmarksRequest.SearchForm dto) {
        return ResponseUtils.ok(userBookmarksRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(UserBookmarksRequest.SubmitForm dto, Long id) throws BaseAppException {
        String userName = Utils.getUserNameLogin();
        boolean isDuplicate = userBookmarksRepository.duplicate(UserBookmarksEntity.class, id, "loginName", userName, "bookmarkType", dto.getBookmarkType(), "name", dto.getName());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_USER_BOOKMARK_DUPLICATE", I18n.getMessage("error.validate.userBookMark.duplicate"));
        }
        UserBookmarksEntity entity;
        if (id != null && id > 0L) {
            entity = userBookmarksRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(userName);
        } else {
            entity = new UserBookmarksEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(userName);
        }
        Utils.copyProperties(dto, entity);
        entity.setOptions(Utils.toJson(dto.getListOptions()));
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setLoginName(userName);
        userBookmarksRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getUserBookmarkId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<UserBookmarksEntity> optional = userBookmarksRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, UserBookmarksEntity.class);
        }
        String userName = Utils.getUserNameLogin();
        if (!userName.equalsIgnoreCase(optional.get().getLoginName())) {
            throw new BaseAppException("loginName not matching!");
        }
        userBookmarksRepository.deActiveObject(UserBookmarksEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<UserBookmarksResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<UserBookmarksEntity> optional = userBookmarksRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, UserBookmarksEntity.class);
        }
        UserBookmarksResponse dto = new UserBookmarksResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(UserBookmarksRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = userBookmarksRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<UserBookmarksResponse.DetailBean> getDataByUser(String userName, String bookmarkType) {
        List<UserBookmarksEntity> listData = userBookmarksRepository.findByProperties(UserBookmarksEntity.class, "loginName", userName, "bookmarkType", bookmarkType);
        return Utils.mapAll(listData, UserBookmarksResponse.DetailBean.class);
    }

}
