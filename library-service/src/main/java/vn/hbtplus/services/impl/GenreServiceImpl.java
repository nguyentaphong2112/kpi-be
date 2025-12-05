/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.TreeDto;
import vn.hbtplus.models.request.GenresRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.GenresResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.GenreEntity;
import vn.hbtplus.repositories.impl.GenresRepository;
import vn.hbtplus.repositories.jpa.GenresRepositoryJPA;
import vn.hbtplus.services.GenresService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang lib_genres
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenresService {

    private final GenresRepository genresRepository;
    private final GenresRepositoryJPA genresRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<GenresResponse> searchData(GenresRequest.SearchForm dto) {
        return ResponseUtils.ok(genresRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(GenresRequest.SubmitForm dto) throws BaseAppException {
        GenreEntity entity;
        if (dto.getGenreId() != null && dto.getGenreId() > 0L) {
            entity = genresRepositoryJPA.getById(dto.getGenreId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new GenreEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        genresRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getGenreId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<GenreEntity> optional = genresRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, GenreEntity.class);
        }
        genresRepository.deActiveObject(GenreEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<GenresResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<GenreEntity> optional = genresRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, GenreEntity.class);
        }
        GenresResponse dto = new GenresResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(GenresRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = genresRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public List<TreeDto> initTree() {
        List<TreeDto> lstMenu = genresRepository.getAllGenres();
        List<TreeDto> results = new ArrayList<>();
        Map<String, TreeDto> mapGenres = new HashMap<>();
        lstMenu.stream().forEach(item -> {
            mapGenres.put(item.getNodeId(), item);
        });
        lstMenu.stream().forEach(item -> {
            if (item.getParentId() == null || mapGenres.get(item.getParentId()) == null) {
                results.add(item);
            } else {
                TreeDto parent = mapGenres.get(item.getParentId());
                parent.addChild(item);
            }
        });

        return results;
    }

}
