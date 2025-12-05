/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.request.FavouriteBooksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.FavouriteBooksResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.FavouriteBookEntity;
import vn.hbtplus.repositories.impl.FavouriteBooksRepository;
import vn.hbtplus.repositories.jpa.FavouriteBooksRepositoryJPA;
import vn.hbtplus.services.FavouriteBookService;
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
 * Lop impl service ung voi bang lib_favourite_books
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class FavouriteBookServiceImpl implements FavouriteBookService {

    private final FavouriteBooksRepository favouriteBooksRepository;
    private final FavouriteBooksRepositoryJPA favouriteBooksRepositoryJPA;


    @Override
    public boolean likeBook(Long bookId) {
        FavouriteBookEntity entity = favouriteBooksRepositoryJPA.findByBookIdAndUserName(bookId, Utils.getUserNameLogin());
        if (entity == null) {
            entity = new FavouriteBookEntity();
            entity.setBookId(bookId);
            entity.setUserName(Utils.getUserNameLogin());
        }
        entity.setCreatedBy(Utils.getUserNameLogin());
        entity.setCreatedTime(new Date());
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        favouriteBooksRepositoryJPA.save(entity);
        return true;
    }

    @Override
    public boolean unlikeBook(Long bookId) {
        FavouriteBookEntity entity = favouriteBooksRepositoryJPA.findByBookIdAndUserName(bookId, Utils.getUserNameLogin());
        if (entity == null) {
            return false;
        }
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setModifiedTime(new Date());
        entity.setIsDeleted(BaseConstants.STATUS.DELETED);
        favouriteBooksRepositoryJPA.save(entity);
        return true;
    }
}
