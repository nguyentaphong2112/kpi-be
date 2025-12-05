/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.FavouriteBooksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.FavouriteBooksResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang lib_favourite_books
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface FavouriteBookService {


    boolean likeBook(Long bookId);

    boolean unlikeBook(Long bookId);
}
